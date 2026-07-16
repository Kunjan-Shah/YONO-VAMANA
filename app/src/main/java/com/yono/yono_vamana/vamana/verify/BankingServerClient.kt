package com.yono.yono_vamana.vamana.verify

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Talks to the local banking-server demo backend (banking-server/server.js),
 * which in turn talks to telecom-server.js for Silent Network Authentication
 * (SNA) — see that pair's doc comments for the full protocol.
 *
 * The phone reaches the bank at 127.0.0.1:8787 via `adb reverse tcp:8787
 * tcp:8787` — the server runs on the dev machine, and adb reverse tunnels
 * that port back to the phone over the existing USB connection, avoiding any
 * Wi-Fi/LAN/firewall configuration. The bank talks to telecom directly on
 * the dev machine (127.0.0.1:8788) — the phone never calls telecom itself,
 * exactly like a real SNA integration where the bank's backend is the one
 * with the telecom relationship, not the app.
 */
object BankingServerClient {

    private const val BASE_URL = "http://127.0.0.1:8787"

    /** The phone's real/demo network context sent with every confirm request. */
    data class SnaContext(
        val msisdn: String,
        val deviceTransport: String,
        val simDemoState: String
    )

    data class TransactionResult(
        val status: String,
        val message: String,
        val newBalance: Long?
    ) {
        val isSuccess: Boolean get() = status == "success"
    }

    /** Registers this device's TEE public key with the bank. Safe to call every time — idempotent. */
    suspend fun registerDevice(deviceId: String, publicKeyBase64: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val body = JSONObject().apply {
                    put("deviceId", deviceId)
                    put("publicKeyBase64", publicKeyBase64)
                }
                postJson("$BASE_URL/register", body)
                true
            } catch (e: Exception) {
                false
            }
        }

    /**
     * [signatureBase64] is null when VAMANA-Verify is inactive — the bank skips the
     * TEE checks entirely in that case and relies on SNA alone. [sna] is always
     * required; SNA runs on every confirmation regardless of the Verify toggle.
     */
    suspend fun confirmTransaction(
        deviceId: String,
        transactionId: String,
        contactId: String,
        contactName: String,
        displayAmount: String,
        timestamp: String,
        signatureBase64: String?,
        sna: SnaContext
    ): TransactionResult = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("deviceId", deviceId)
                put("transactionId", transactionId)
                put("contactId", contactId)
                put("contactName", contactName)
                put("displayAmount", displayAmount)
                put("timestamp", timestamp)
                if (signatureBase64 != null) put("signatureBase64", signatureBase64)
                put("msisdn", sna.msisdn)
                put("deviceTransport", sna.deviceTransport)
                put("simDemoState", sna.simDemoState)
            }
            val response = postJson("$BASE_URL/transactions/confirm", body)
            TransactionResult(
                status = response.optString("status", "error"),
                message = response.optString("message", ""),
                newBalance = if (response.has("newBalance")) response.getLong("newBalance") else null
            )
        } catch (e: Exception) {
            TransactionResult(
                status = "error",
                message = "Could not reach the banking server: ${e.message}",
                newBalance = null
            )
        }
    }

    private fun postJson(urlString: String, body: JSONObject): JSONObject {
        val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 5_000
            readTimeout = 5_000
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
        }
        connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

        val responseCode = connection.responseCode
        val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
        val text = stream.bufferedReader().use { it.readText() }
        return JSONObject(text)
    }
}
