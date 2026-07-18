'use strict';

/**
 * digi-VAMANA banking server — a small, self-contained demo backend for the
 * VAMANA-Verify TEE-backed transaction flow AND Silent Network Authentication
 * (SNA), the third leg of which is telecom-server.js (its own process, its
 * own terminal, its own port — see that file's doc comment for the protocol).
 *
 * Every confirm request runs SNA against the telecom server: a real OAuth2
 * client-credentials token exchange (RS256 JWT), a real HMAC-SHA256 signed
 * verify request, and real RSA-SHA256 verification of the telecom's signed
 * response — this process genuinely rejects a tampered or forged response,
 * it doesn't just trust whatever comes back.
 *
 * The TEE signature check (device registered via /register, ECDSA signature
 * over the transaction payload) only runs when the phone presents one —
 * that only happens when VAMANA-Verify is toggled on. SNA runs regardless;
 * it's a different, independent signal (network/SIM legitimacy vs. a human
 * fingerprint being present).
 *
 * No database, no dependencies beyond Node's standard library. State is
 * in-memory only and resets whenever either server restarts.
 *
 * Run:  node server.js            (in one terminal)
 *       node telecom-server.js    (in a second terminal)
 * Then: adb reverse tcp:8787 tcp:8787   (so the phone can reach 127.0.0.1:8787)
 */

const http = require('http');
const crypto = require('crypto');

const PORT = 8787;
const TELECOM_URL = 'http://127.0.0.1:8788';
const CLIENT_ID = 'digi-VAMANA-BANK';
const CLIENT_SECRET = 'demo-client-secret-change-me'; // must match telecom-server.js
const HMAC_SECRET = 'demo-hmac-session-key-change-me'; // must match telecom-server.js
const MAX_TIMESTAMP_SKEW_MS = 60_000;

// ── Account ledger — same 5 contacts as DummyContacts.kt on the client ──────
const accounts = new Map([
  ['1', { name: 'Aarav Mehta', balance: 25000 }],
  ['2', { name: 'Priya Sharma', balance: 50000 }],
  ['3', { name: 'Rohan Verma', balance: 12500 }],
  ['4', { name: 'Ananya Iyer', balance: 75000 }],
  ['5', { name: 'Karan Malhotra', balance: 8000 }],
]);

// deviceId -> public key (SPKI DER, base64). Registered once per device on
// first successful TEE confirmation; trusted on first use for this demo —
// there is no Android Key Attestation certificate-chain verification here,
// so this proves "signed by whoever registered this key", not "signed by
// real hardware". The hardware guarantee lives entirely on the phone side.
const registeredDevices = new Map();

// Replay protection: every transactionId can be used exactly once.
const seenTransactionIds = new Set();

// Cached across requests exactly like a real bank integration would cache
// its telecom OAuth2 token until it's close to expiry.
let cachedTelecomToken = null; // { token, exp }
let telecomPublicKey = null;

function log(line) {
  const time = new Date().toISOString().split('T')[1].split('.')[0];
  console.log(`[BANK ${time}] ${line}`);
}

function printLedger() {
  log('── Ledger ───────────────────────────────────────────────');
  for (const [id, acc] of accounts) {
    log(`  ${id}. ${acc.name.padEnd(16)} ₹${acc.balance.toLocaleString('en-IN')}`);
  }
}

function readJsonBody(req) {
  return new Promise((resolve, reject) => {
    let raw = '';
    req.on('data', (chunk) => {
      raw += chunk;
      if (raw.length > 1_000_000) req.destroy();
    });
    req.on('end', () => {
      try {
        resolve(raw.length ? JSON.parse(raw) : {});
      } catch (e) {
        reject(e);
      }
    });
    req.on('error', reject);
  });
}

function sendJson(res, status, body) {
  const text = JSON.stringify(body);
  res.writeHead(status, { 'Content-Type': 'application/json' });
  res.end(text);
}

function handleRegister(req, res) {
  readJsonBody(req)
    .then((body) => {
      const { deviceId, publicKeyBase64 } = body;
      if (!deviceId || !publicKeyBase64) {
        sendJson(res, 400, { status: 'error', message: 'deviceId and publicKeyBase64 are required' });
        return;
      }
      const isNew = !registeredDevices.has(deviceId);
      registeredDevices.set(deviceId, publicKeyBase64);
      log(`Device ${isNew ? 'registered' : 're-registered'}: ${deviceId.slice(0, 12)}…`);
      sendJson(res, 200, { status: 'registered' });
    })
    .catch(() => sendJson(res, 400, { status: 'error', message: 'Malformed JSON body' }));
}

function verifySignature(publicKeyBase64, signedText, signatureBase64) {
  try {
    const publicKey = crypto.createPublicKey({
      key: Buffer.from(publicKeyBase64, 'base64'),
      format: 'der',
      type: 'spki',
    });
    return crypto.verify(
      'sha256',
      Buffer.from(signedText, 'utf8'),
      publicKey,
      Buffer.from(signatureBase64, 'base64')
    );
  } catch (e) {
    log(`  ✗ Signature verification threw: ${e.message}`);
    return false;
  }
}

// ─────────────────────────────────────────────────────────────────────────
//  Silent Network Authentication — bank ↔ telecom leg
// ─────────────────────────────────────────────────────────────────────────

async function fetchTelecomPublicKey() {
  if (telecomPublicKey) return telecomPublicKey;
  const res = await fetch(`${TELECOM_URL}/public-key`);
  telecomPublicKey = await res.text();
  log('  fetched telecom public key');
  return telecomPublicKey;
}

async function getTelecomAccessToken() {
  if (cachedTelecomToken && cachedTelecomToken.exp > Date.now() / 1000 + 5) {
    return cachedTelecomToken.token;
  }
  log(`  >> POST /oauth2/token  client_id=${CLIENT_ID}  grant_type=client_credentials`);
  const res = await fetch(`${TELECOM_URL}/oauth2/token`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ client_id: CLIENT_ID, client_secret: CLIENT_SECRET, grant_type: 'client_credentials' }),
  });
  if (!res.ok) throw new Error('telecom rejected client credentials');
  const data = await res.json();
  cachedTelecomToken = { token: data.access_token, exp: Math.floor(Date.now() / 1000) + data.expires_in };
  log(`  << access_token cached, expires in ${data.expires_in}s`);
  return cachedTelecomToken.token;
}

function verifyTelecomSignature(payload, signatureB64, publicKeyPem) {
  const signature = Buffer.from(signatureB64, 'base64');
  return crypto.verify('RSA-SHA256', Buffer.from(JSON.stringify(payload)), publicKeyPem, signature);
}

/**
 * Runs the full SNA exchange with the telecom server for one transaction.
 * Returns { authentic, payload } — payload has session_available/sim_swap
 * only when authentic is true (an unverifiable response is never trusted,
 * regardless of what it claims).
 */
async function performSnaVerify({ msisdn, transactionId, deviceTransport, simDemoState }) {
  const token = await getTelecomAccessToken();
  const publicKey = await fetchTelecomPublicKey();

  const verifyBody = { msisdn, txn_id: transactionId, device_transport: deviceTransport, demo_sim_state: simDemoState };
  const rawBody = JSON.stringify(verifyBody);
  const signature = crypto.createHmac('sha256', HMAC_SECRET).update(rawBody).digest('hex');

  log(`  >> POST /v1/sna/verify  Authorization: Bearer ${token.slice(0, 24)}...  X-Signature: ${signature.slice(0, 16)}...`);
  const verifyRes = await fetch(`${TELECOM_URL}/v1/sna/verify`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}`, 'X-Signature': signature },
    body: rawBody,
  });
  const verifyData = await verifyRes.json();
  log(`  << telecom response: ${JSON.stringify(verifyData)}`);

  const { signature: respSig, ...payload } = verifyData;
  const authentic = respSig ? verifyTelecomSignature(payload, respSig, publicKey) : false;
  log(authentic
    ? '  ✓ Telecom response signature verified — authentic'
    : '  ✗ Telecom response signature verification FAILED — discarding');

  return { authentic, payload };
}

// ─────────────────────────────────────────────────────────────────────────

function handleConfirm(req, res) {
  readJsonBody(req)
    .then(async (body) => {
      const {
        deviceId, transactionId, contactId, contactName,
        displayAmount, timestamp, signatureBase64,
        msisdn, deviceTransport, simDemoState,
      } = body;

      log('── Incoming transaction ────────────────────────────────');
      log(`  Device:      ${(deviceId || '').slice(0, 12)}…`);
      log(`  Transaction: ${transactionId}`);
      log(`  Recipient:   ${contactName} (id=${contactId})`);
      log(`  Amount:      ${displayAmount}`);
      log(`  Transport:   ${deviceTransport}   SIM state (demo): ${simDemoState}`);

      const fail = (status, message) => {
        log(`  ✗ ${status.toUpperCase()}: ${message}`);
        sendJson(res, 200, { status, message });
      };

      if (!deviceId || !transactionId || !contactId || !contactName ||
          !displayAmount || !timestamp || !msisdn || !deviceTransport) {
        fail('failed', 'Malformed request — missing fields.');
        return;
      }

      const account = accounts.get(contactId);
      if (!account) {
        fail('failed', 'Unknown recipient account.');
        return;
      }
      log(`  ✓ Recipient account found (balance: ₹${account.balance.toLocaleString('en-IN')})`);

      const txnAgeMs = Date.now() - Date.parse(timestamp);
      if (Number.isNaN(txnAgeMs) || Math.abs(txnAgeMs) > MAX_TIMESTAMP_SKEW_MS) {
        fail('failed', 'Transaction timestamp is stale or invalid — possible replay.');
        return;
      }
      log(`  ✓ Timestamp fresh (${Math.max(0, Math.round(txnAgeMs / 1000))}s old)`);

      if (seenTransactionIds.has(transactionId)) {
        fail('failed', 'Duplicate transaction ID — replay detected.');
        return;
      }
      log('  ✓ Transaction ID not previously seen');

      // ── TEE step — only present when VAMANA-Verify is active on the phone ──
      if (signatureBase64) {
        const publicKeyBase64 = registeredDevices.get(deviceId);
        if (!publicKeyBase64) {
          fail('failed', 'Unknown device — not registered with the bank.');
          return;
        }
        log('  ✓ Device registered');

        const signedText = `${transactionId}|${contactId}|${contactName}|${displayAmount}|${timestamp}`;
        if (!verifySignature(publicKeyBase64, signedText, signatureBase64)) {
          fail('failed', 'TEE signature verification failed.');
          return;
        }
        log('  ✓ TEE signature verified against registered public key');
      } else {
        log('  · No TEE signature presented (VAMANA-Verify inactive) — proceeding on SNA alone');
      }

      // ── SNA step — always runs, independent of the TEE step above ──
      log('  → Starting Silent Network Authentication with telecom…');
      let sna;
      try {
        sna = await performSnaVerify({ msisdn, transactionId, deviceTransport, simDemoState });
      } catch (err) {
        fail('failed', `Could not reach telecom for SNA verification: ${err.message}`);
        return;
      }

      if (!sna.authentic) {
        fail('failed', 'SNA response from telecom could not be trusted (signature invalid).');
        return;
      }

      if (!sna.payload.session_available) {
        fail('cellular_required', 'SNA requires an active cellular session — switch off Wi-Fi and try again.');
        return;
      }

      if (sna.payload.sim_swap) {
        fail('blocked_sim_swap', 'Recent SIM swap detected on this line — transaction blocked for review.');
        return;
      }
      log('  ✓ SNA passed — active cellular session, no SIM swap');

      const amount = parseInt(String(displayAmount).replace(/[^\d]/g, ''), 10);
      if (!Number.isFinite(amount) || amount <= 0) {
        fail('failed', 'Invalid amount.');
        return;
      }

      seenTransactionIds.add(transactionId);
      const oldBalance = account.balance;
      account.balance += amount;
      log(`  ── APPROVED ──`);
      log(`  ${account.name}: ₹${oldBalance.toLocaleString('en-IN')} → ₹${account.balance.toLocaleString('en-IN')}`);
      printLedger();

      sendJson(res, 200, { status: 'success', newBalance: account.balance });
    })
    .catch((e) => sendJson(res, 400, { status: 'error', message: `Malformed JSON body: ${e.message}` }));
}

function handleAccounts(req, res) {
  const snapshot = {};
  for (const [id, acc] of accounts) snapshot[id] = acc;
  sendJson(res, 200, { status: 'ok', accounts: snapshot });
}

const server = http.createServer((req, res) => {
  if (req.method === 'POST' && req.url === '/register') return handleRegister(req, res);
  if (req.method === 'POST' && req.url === '/transactions/confirm') return handleConfirm(req, res);
  if (req.method === 'GET' && req.url === '/accounts') return handleAccounts(req, res);
  sendJson(res, 404, { status: 'error', message: 'Not found' });
});

server.listen(PORT, '127.0.0.1', () => {
  log(`digi-VAMANA banking server listening on http://127.0.0.1:${PORT}`);
  log(`Telecom server expected at ${TELECOM_URL} — start telecom-server.js too`);
  printLedger();
});
