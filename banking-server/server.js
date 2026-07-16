'use strict';

/**
 * YONO-VAMANA banking server — a small, self-contained demo backend for the
 * VAMANA-Verify TEE-backed transaction flow.
 *
 * Verifies that a payment confirmation genuinely came from the phone's
 * TEE-protected signing key (registered once via /register), rejects
 * replayed/stale/tampered requests, and maintains an in-memory ledger of
 * 5 dummy accounts — the same 5 contacts shown on the Transact page.
 *
 * No database, no dependencies beyond Node's standard library. State is
 * in-memory only and resets whenever the server restarts.
 *
 * Run:  node server.js
 * Then: adb reverse tcp:8787 tcp:8787   (so the phone can reach 127.0.0.1:8787)
 */

const http = require('http');
const crypto = require('crypto');

const PORT = 8787;

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

// Reject confirmations whose timestamp is further than this from server time.
const MAX_TIMESTAMP_SKEW_MS = 60_000;

function log(line) {
  const time = new Date().toISOString().split('T')[1].split('.')[0];
  console.log(`[${time}] ${line}`);
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

function handleConfirm(req, res) {
  readJsonBody(req)
    .then((body) => {
      const {
        deviceId, transactionId, contactId, contactName,
        displayAmount, timestamp, signatureBase64,
      } = body;

      log('── Incoming transaction ────────────────────────────────');
      log(`  Device:      ${(deviceId || '').slice(0, 12)}…`);
      log(`  Transaction: ${transactionId}`);
      log(`  Recipient:   ${contactName} (id=${contactId})`);
      log(`  Amount:      ${displayAmount}`);

      const fail = (message) => {
        log(`  ✗ REJECTED: ${message}`);
        sendJson(res, 200, { status: 'failed', message });
      };

      if (!deviceId || !transactionId || !contactId || !contactName ||
          !displayAmount || !timestamp || !signatureBase64) {
        fail('Malformed request — missing fields.');
        return;
      }

      const publicKeyBase64 = registeredDevices.get(deviceId);
      if (!publicKeyBase64) {
        fail('Unknown device — not registered with the bank.');
        return;
      }
      log('  ✓ Device registered');

      const account = accounts.get(contactId);
      if (!account) {
        fail('Unknown recipient account.');
        return;
      }
      log(`  ✓ Recipient account found (balance: ₹${account.balance.toLocaleString('en-IN')})`);

      const txnAgeMs = Date.now() - Date.parse(timestamp);
      if (Number.isNaN(txnAgeMs) || Math.abs(txnAgeMs) > MAX_TIMESTAMP_SKEW_MS) {
        fail('Transaction timestamp is stale or invalid — possible replay.');
        return;
      }
      log(`  ✓ Timestamp fresh (${Math.max(0, Math.round(txnAgeMs / 1000))}s old)`);

      if (seenTransactionIds.has(transactionId)) {
        fail('Duplicate transaction ID — replay detected.');
        return;
      }
      log('  ✓ Transaction ID not previously seen');

      const signedText = `${transactionId}|${contactId}|${contactName}|${displayAmount}|${timestamp}`;
      if (!verifySignature(publicKeyBase64, signedText, signatureBase64)) {
        fail('Signature verification failed — authentication could not be verified.');
        return;
      }
      log('  ✓ Signature verified against registered public key');

      const amount = parseInt(String(displayAmount).replace(/[^\d]/g, ''), 10);
      if (!Number.isFinite(amount) || amount <= 0) {
        fail('Invalid amount.');
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
    .catch(() => sendJson(res, 400, { status: 'error', message: 'Malformed JSON body' }));
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
  log(`YONO-VAMANA banking server listening on http://127.0.0.1:${PORT}`);
  printLedger();
});
