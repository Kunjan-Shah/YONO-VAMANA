package com.yono.yono_vamana.vamana.verify.tee

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Talks to the local banking-server demo backend (banking-server/server.js).
 *
 * The phone reaches it at 127.0.0.1:8787 via `adb reverse tcp:8787 tcp:8787`
 * — the server runs on the dev machine, and adb reverse tunnels that port
 * back to the phone over the existing USB connection, avoiding any Wi-Fi/
 * LAN/firewall configuration.
 */
object BankingServerClient {

    private const val BASE_URL = "http://127.0.0.1:8787"

    data class TransactionResult(
        val success: Boolean,
        val message: String,
        val newBalance: Long?
    )

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

    suspend fun confirmTransaction(
        deviceId: String,
        attestation: TeeAttestation
    ): TransactionResult = withContext(Dispatchers.IO) {
        try {
            val payload = attestation.payload
            val body = JSONObject().apply {
                put("deviceId", deviceId)
                put("transactionId", payload.transactionId)
                put("contactId", payload.contactId)
                put("contactName", payload.contactName)
                put("displayAmount", payload.displayAmount)
                put("timestamp", payload.timestamp)
                put("signatureBase64", Base64.encodeToString(attestation.signature, Base64.NO_WRAP))
            }
            val response = postJson("$BASE_URL/transactions/confirm", body)
            TransactionResult(
                success = response.optString("status") == "success",
                message = response.optString("message", ""),
                newBalance = if (response.has("newBalance")) response.getLong("newBalance") else null
            )
        } catch (e: Exception) {
            TransactionResult(
                success = false,
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
