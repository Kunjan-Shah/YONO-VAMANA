package com.digi.digi_vamana.vamana.verify.tee

import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.digi.digi_vamana.vamana.intelligence.VamanaActivityLog
import kotlinx.coroutines.suspendCancellableCoroutine
import java.security.Signature
import java.time.Instant
import kotlin.coroutines.resume

/** The transaction details bound into the signed attestation. */
data class TeeTransactionPayload(
    val transactionId: String,
    val contactId: String,
    val contactName: String,
    val displayAmount: String,
    val timestamp: String = Instant.now().toString()
) {
    /**
     * Must match the banking server's reconstruction of the signed text
     * byte-for-byte (see banking-server/server.js's handleConfirm) — field
     * order and the '|' delimiter are part of the contract between them.
     */
    fun toSignableBytes(): ByteArray =
        "$transactionId|$contactId|$contactName|$displayAmount|$timestamp".toByteArray(Charsets.UTF_8)
}

/** Cryptographic proof that this exact payload was signed after a real biometric match. */
data class TeeAttestation(
    val payload: TeeTransactionPayload,
    val signature: ByteArray
)

sealed class TeeConfirmResult {
    data class Success(val attestation: TeeAttestation) : TeeConfirmResult()
    data object Cancelled : TeeConfirmResult()
    data class Unavailable(val reason: String) : TeeConfirmResult()
    data class Error(val message: String) : TeeConfirmResult()
}

/**
 * Orchestrates TEE-backed transaction confirmation for VAMANA-Verify:
 * checks device availability, then shows BiometricPrompt bound to a
 * CryptoObject so the transaction payload is only signed after a genuine
 * fingerprint match succeeds inside TrustZone/StrongBox.
 */
class TeeTransactionAuthenticator(private val activity: FragmentActivity) {

    suspend fun confirmTransaction(payload: TeeTransactionPayload): TeeConfirmResult {
        VamanaActivityLog.log(
            VamanaActivityLog.Category.VERIFY,
            "TEE transaction confirmation requested for ${payload.displayAmount} to ${payload.contactName}."
        )
        val availability = TeeAvailabilityChecker(activity).check()
        if (!availability.isAvailable) {
            VamanaActivityLog.log(
                VamanaActivityLog.Category.VERIFY,
                "TEE-backed authentication unavailable (${availability.reason})."
            )
            return TeeConfirmResult.Unavailable(availability.reason)
        }

        val signature = try {
            Signature.getInstance("SHA256withECDSA").apply {
                initSign(TeeSigningKeyManager.getOrCreateKey())
            }
        } catch (e: Exception) {
            return TeeConfirmResult.Error("Could not prepare the signing key: ${e.message}")
        }

        return authenticateAndSign(payload, signature)
    }

    private suspend fun authenticateAndSign(
        payload: TeeTransactionPayload,
        signature: Signature
    ): TeeConfirmResult = suspendCancellableCoroutine { continuation ->
        val executor = ContextCompat.getMainExecutor(activity)
        var attemptCount = 0

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                val cryptoSignature = result.cryptoObject?.signature
                if (cryptoSignature == null) {
                    VamanaActivityLog.log(
                        VamanaActivityLog.Category.VERIFY,
                        "Fingerprint authentication returned no signature object — treating as an error."
                    )
                    if (continuation.isActive) {
                        continuation.resume(TeeConfirmResult.Error("No signature object was returned."))
                    }
                    return
                }
                val outcome = try {
                    cryptoSignature.update(payload.toSignableBytes())
                    val signed = TeeConfirmResult.Success(TeeAttestation(payload, cryptoSignature.sign()))
                    VamanaActivityLog.log(
                        VamanaActivityLog.Category.VERIFY,
                        "Fingerprint matched for ${payload.displayAmount} to ${payload.contactName} after " +
                            "${attemptCount + 1} attempt(s) — transaction signed inside the TEE."
                    )
                    signed
                } catch (e: Exception) {
                    VamanaActivityLog.log(VamanaActivityLog.Category.VERIFY, "Signing failed after fingerprint match: ${e.message}")
                    TeeConfirmResult.Error("Signing failed: ${e.message}")
                }
                if (continuation.isActive) continuation.resume(outcome)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (!continuation.isActive) return
                val outcome = when (errorCode) {
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                    BiometricPrompt.ERROR_CANCELED -> {
                        VamanaActivityLog.log(
                            VamanaActivityLog.Category.VERIFY,
                            "Fingerprint authentication cancelled by user after $attemptCount failed attempt(s)."
                        )
                        TeeConfirmResult.Cancelled
                    }
                    else -> {
                        VamanaActivityLog.log(VamanaActivityLog.Category.VERIFY, "Fingerprint authentication error: $errString")
                        TeeConfirmResult.Error(errString.toString())
                    }
                }
                continuation.resume(outcome)
            }

            // A single mismatched fingerprint — the prompt keeps listening for
            // another attempt, so the coroutine only resolves on a terminal callback.
            override fun onAuthenticationFailed() {
                attemptCount++
                VamanaActivityLog.log(
                    VamanaActivityLog.Category.VERIFY,
                    "Fingerprint did not match for ${payload.displayAmount} to ${payload.contactName} (attempt $attemptCount)."
                )
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Confirm payment")
            .setSubtitle("You are paying ${payload.displayAmount} to ${payload.contactName}")
            .setDescription("Authenticate with your fingerprint to approve this transaction.")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .build()

        VamanaActivityLog.log(
            VamanaActivityLog.Category.VERIFY,
            "Fingerprint prompt shown for ${payload.displayAmount} to ${payload.contactName}."
        )
        prompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(signature))
    }
}
