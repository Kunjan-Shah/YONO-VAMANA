package com.digi.digi_vamana.vamana.verify.tee

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey

/**
 * Owns the AndroidKeyStore key used to sign VAMANA-Verify transaction
 * confirmations. The private key never leaves the TEE/StrongBox.
 *
 * setUserAuthenticationRequired(true) is left with no validity duration,
 * which Android ties to per-operation authentication: the key can only be
 * used inside a BiometricPrompt CryptoObject flow, and a fresh biometric
 * match is required for every single signing — never a time-window reuse.
 */
object TeeSigningKeyManager {

    private const val KEY_ALIAS = "vamana_verify_signing_key"

    /** Returns the existing signing key for this profile, generating it on first use. */
    fun getOrCreateKey(): PrivateKey {
        val keyStore = keyStore()
        (keyStore.getKey(KEY_ALIAS, null) as? PrivateKey)?.let { return it }
        generateKey()
        return keyStore().getKey(KEY_ALIAS, null) as PrivateKey
    }

    /**
     * The public half of the signing key, X.509 SubjectPublicKeyInfo DER
     * bytes, base64-encoded — what the banking server needs to verify
     * signatures produced by this device's key.
     */
    fun getPublicKeyBase64(): String {
        val keyStore = keyStore()
        if (keyStore.getCertificate(KEY_ALIAS) == null) generateKey()
        val certificate = keyStore().getCertificate(KEY_ALIAS)
        return Base64.encodeToString(certificate.publicKey.encoded, Base64.NO_WRAP)
    }

    private fun keyStore(): KeyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    private fun generateKey() {
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setUserAuthenticationRequired(true)
            .build()

        val keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore")
        keyPairGenerator.initialize(spec)
        keyPairGenerator.generateKeyPair()
    }
}
