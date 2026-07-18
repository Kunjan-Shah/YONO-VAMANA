package com.digi.digi_vamana.vamana.verify.tee

import android.app.KeyguardManager
import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore

/**
 * Checks whether this device can back a VAMANA-Verify transaction
 * confirmation with genuine hardware-rooted authentication.
 *
 * Mirrors SecureFolderPlusPlus's TeeAvailabilityChecker, adapted to Android's
 * public BiometricPrompt + AndroidKeyStore APIs instead of a custom OP-TEE
 * Trusted Application — a real OP-TEE TA can't be loaded into a consumer
 * phone's TrustZone without OEM signing, so this uses the mechanism that
 * actually works on this device: a hardware-backed key that can only sign
 * after Android's TEE/StrongBox-backed biometric pipeline confirms a match.
 */
class TeeAvailabilityChecker(private val context: Context) {

    data class AvailabilityResult(
        val isAvailable: Boolean,
        val hasHardwareKeystore: Boolean,
        val hasStrongBiometric: Boolean,
        val hasSecureLockScreen: Boolean,
        val reason: String
    )

    /**
     * TEE-backed confirmation can proceed only if all three hold: the
     * Android Keystore is genuinely hardware-backed, the device has a secure
     * lock screen configured, and a Class 3 (BIOMETRIC_STRONG) biometric is
     * enrolled — that class is TEE-backed by Android CDD definition.
     */
    fun check(): AvailabilityResult {
        val hasHwKeystore = isHardwareBackedKeystoreAvailable()
        val hasLockScreen = isSecureLockScreenConfigured()
        val hasStrongBiometric = canAuthenticateWithStrongBiometric()

        val available = hasHwKeystore && hasLockScreen && hasStrongBiometric

        val reason = when {
            !hasHwKeystore -> "No hardware-backed Android Keystore on this device"
            !hasLockScreen -> "No secure lock screen configured"
            !hasStrongBiometric -> "No enrolled hardware-backed biometric"
            else -> "TEE-backed authentication available"
        }

        return AvailabilityResult(
            isAvailable = available,
            hasHardwareKeystore = hasHwKeystore,
            hasStrongBiometric = hasStrongBiometric,
            hasSecureLockScreen = hasLockScreen,
            reason = reason
        )
    }

    /**
     * Generates a temporary EC key in the Android Keystore and checks
     * whether it actually landed inside secure hardware (TrustZone /
     * StrongBox) rather than being emulated in software.
     */
    private fun isHardwareBackedKeystoreAvailable(): Boolean {
        return try {
            val alias = "digi_verify_tee_probe_${System.currentTimeMillis()}"
            val spec = KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            ).setDigests(KeyProperties.DIGEST_SHA256).build()

            val keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore"
            )
            keyPairGenerator.initialize(spec)
            keyPairGenerator.generateKeyPair()

            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            val entry = keyStore.getEntry(alias, null) as? KeyStore.PrivateKeyEntry
                ?: return false

            val hardwareBacked = try {
                val keyFactory = KeyFactory.getInstance(entry.privateKey.algorithm, "AndroidKeyStore")
                val keyInfo = keyFactory.getKeySpec(entry.privateKey, KeyInfo::class.java) as KeyInfo
                keyInfo.isInsideSecureHardware
            } catch (e: Exception) {
                false
            }

            keyStore.deleteEntry(alias)
            hardwareBacked
        } catch (e: Exception) {
            false
        }
    }

    private fun isSecureLockScreenConfigured(): Boolean {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return keyguardManager.isDeviceSecure
    }

    private fun canAuthenticateWithStrongBiometric(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
    }
}
