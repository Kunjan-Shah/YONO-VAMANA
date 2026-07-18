'use strict';

/**
 * digi-VAMANA telecom (MNO) server — the third leg of the Silent Network
 * Authentication demo, alongside server.js (bank) and the Android app (phone).
 *
 * Real, working cryptography: an RSA-2048 keypair generated at startup, RS256
 * JWT signing/verification for the bank's OAuth2 client-credentials token,
 * HMAC-SHA256 verification of the bank's signed verify request, and
 * RSA-SHA256 signing of every verify response — server.js actually verifies
 * that signature and rejects a tampered or forged response, it doesn't just
 * trust whatever comes back.
 *
 * Simulated, clearly marked below: the subscriber directory and the SIM-swap
 * flag. There's no real telecom partnership behind this demo, so those are
 * read from request fields the phone supplies. A real MNO would derive both
 * itself from its own HLR/UDM and the requesting connection's source IP —
 * never take a client's word for its own SIM state. That's the entire
 * security argument for SNA: the bank isn't asking the phone "were you
 * swapped?", it's asking the network operator.
 *
 * Run:  node telecom-server.js
 * Then: adb reverse tcp:8788 tcp:8788   (so the phone can reach 127.0.0.1:8788,
 *       only needed if the phone ever talks to this server directly — in this
 *       demo it doesn't: the bank talks to telecom on the transaction's behalf,
 *       exactly like a real SNA integration. The reverse tunnel here is for
 *       your own convenience testing this server standalone with curl.)
 */

const http = require('http');
const crypto = require('crypto');

const PORT = 8788;
const BANK_CLIENT_ID = 'digi-VAMANA-BANK';
const BANK_CLIENT_SECRET = 'demo-client-secret-change-me'; // must match server.js
const HMAC_SECRET = 'demo-hmac-session-key-change-me'; // must match server.js

const { publicKey, privateKey } = crypto.generateKeyPairSync('rsa', {
  modulusLength: 2048,
  publicKeyEncoding: { type: 'spki', format: 'pem' },
  privateKeyEncoding: { type: 'pkcs8', format: 'pem' },
});

// DEMO ONLY — stands in for a real HLR/UDM subscriber lookup keyed by MSISDN
// (the payer's own phone number, not the recipient's account).
const SUBSCRIBERS = {
  '+919000000482': { cell: 'DEL-North-14B', rsrp: -87 },
};

function log(line) {
  const time = new Date().toISOString().split('T')[1].split('.')[0];
  console.log(`[TELECOM ${time}] ${line}`);
}

function base64url(input) {
  return Buffer.from(input).toString('base64').replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

function signJwt(payload) {
  const header = { alg: 'RS256', typ: 'JWT' };
  const signingInput = `${base64url(JSON.stringify(header))}.${base64url(JSON.stringify(payload))}`;
  const signature = crypto.sign('RSA-SHA256', Buffer.from(signingInput), privateKey);
  return `${signingInput}.${base64url(signature)}`;
}

function verifyJwt(token) {
  const parts = token.split('.');
  if (parts.length !== 3) return null;
  const [encHeader, encPayload, encSig] = parts;
  const signingInput = `${encHeader}.${encPayload}`;
  const signature = Buffer.from(encSig.replace(/-/g, '+').replace(/_/g, '/'), 'base64');
  const ok = crypto.verify('RSA-SHA256', Buffer.from(signingInput), publicKey, signature);
  if (!ok) return null;
  const payload = JSON.parse(Buffer.from(encPayload, 'base64').toString('utf8'));
  if (payload.exp && Date.now() / 1000 > payload.exp) return null;
  return payload;
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
  res.writeHead(status, { 'Content-Type': 'application/json' });
  res.end(JSON.stringify(body));
}

function handlePublicKey(req, res) {
  res.writeHead(200, { 'Content-Type': 'text/plain' });
  res.end(publicKey);
}

function handleToken(req, res) {
  readJsonBody(req)
    .then((body) => {
      const { client_id, client_secret, grant_type } = body;
      log(`POST /oauth2/token  client_id=${client_id}  grant_type=${grant_type}`);

      if (grant_type !== 'client_credentials' || client_id !== BANK_CLIENT_ID || client_secret !== BANK_CLIENT_SECRET) {
        log('  -> rejected: invalid client credentials');
        sendJson(res, 401, { error: 'invalid_client' });
        return;
      }

      const exp = Math.floor(Date.now() / 1000) + 300;
      const token = signJwt({ iss: 'digi-vamana-telecom', sub: client_id, scope: 'sna.verify', exp });
      log('  -> access_token issued (RS256), expires in 300s');
      sendJson(res, 200, { access_token: token, token_type: 'Bearer', expires_in: 300 });
    })
    .catch(() => sendJson(res, 400, { error: 'malformed_json' }));
}

function handleVerify(req, res) {
  readJsonBody(req)
    .then((body) => {
      log(`POST /v1/sna/verify  from ${req.socket.remoteAddress}`);

      const auth = req.headers.authorization || '';
      const token = auth.startsWith('Bearer ') ? auth.slice(7) : null;
      const claims = token && verifyJwt(token);
      if (!claims) {
        log('  -> rejected: bearer token invalid or expired');
        sendJson(res, 401, { error: 'invalid_token' });
        return;
      }
      log(`  bearer token valid, iss=${claims.iss}`);

      const rawBody = JSON.stringify(body);
      const providedSig = req.headers['x-signature'] || '';
      const expectedSig = crypto.createHmac('sha256', HMAC_SECRET).update(rawBody).digest('hex');
      if (providedSig !== expectedSig) {
        log('  -> rejected: HMAC signature mismatch');
        sendJson(res, 401, { error: 'bad_signature' });
        return;
      }
      log('  HMAC payload signature verified — integrity OK');

      // ---- DEMO ONLY below: a real MNO derives this itself, it does not trust the caller ----
      const { msisdn, device_transport, demo_sim_state } = body;
      log(`  session lookup: MSISDN ${msisdn} (HLR/UDM query)...`);

      let result;
      if (device_transport !== 'CELLULAR') {
        log('  APN=none — device not on an active cellular session');
        result = { session_available: false };
      } else {
        const sub = SUBSCRIBERS[msisdn] || { cell: 'UNKNOWN', rsrp: null };
        const simSwap = demo_sim_state === 'swapped';
        log(`  APN=cellular  cell=${sub.cell}  session=ACTIVE`);
        log(`  sim_binding=MATCH  sim_swap=${simSwap}`);
        result = { session_available: true, match: true, sim_swap: simSwap };
      }
      // ---- end demo-only section ----

      const payload = { ...result, ts: Date.now() };
      const signature = crypto.sign('RSA-SHA256', Buffer.from(JSON.stringify(payload)), privateKey).toString('base64');
      log('  response signed (RSA-SHA256) and returned to bank');

      sendJson(res, 200, { ...payload, signature });
    })
    .catch(() => sendJson(res, 400, { error: 'malformed_json' }));
}

const server = http.createServer((req, res) => {
  if (req.method === 'GET' && req.url === '/public-key') return handlePublicKey(req, res);
  if (req.method === 'POST' && req.url === '/oauth2/token') return handleToken(req, res);
  if (req.method === 'POST' && req.url === '/v1/sna/verify') return handleVerify(req, res);
  sendJson(res, 404, { error: 'not_found' });
});

server.listen(PORT, '127.0.0.1', () => {
  log(`digi-VAMANA telecom server listening on http://127.0.0.1:${PORT}`);
  log('Public key served at GET /public-key');
});
