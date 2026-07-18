package com.digi.digi_vamana.vamana.verify.tee

/**
 * Mirrors SecureFolderPlusPlus's FallbackHandler tiering: when TEE-backed
 * authentication isn't available on this device, block high-value transfers
 * outright and let low-value ones through with a visible warning, rather
 * than silently downgrading security either way.
 */
object FallbackPolicy {

    /** Transfers at or above this amount are blocked outright when TEE is unavailable. */
    const val HIGH_VALUE_THRESHOLD_RUPEES = 10_000L

    sealed class Decision {
        data class Blocked(val message: String) : Decision()
        data class ProceedWithWarning(val message: String) : Decision()
    }

    fun evaluate(amountRupees: Long, unavailableReason: String): Decision {
        return if (amountRupees >= HIGH_VALUE_THRESHOLD_RUPEES) {
            Decision.Blocked(
                "Payments of ₹$HIGH_VALUE_THRESHOLD_RUPEES or more require TEE-backed " +
                    "authentication, which isn't available on this device ($unavailableReason)."
            )
        } else {
            Decision.ProceedWithWarning(
                "TEE-backed authentication isn't available on this device " +
                    "($unavailableReason). Proceeding under standard security."
            )
        }
    }
}
