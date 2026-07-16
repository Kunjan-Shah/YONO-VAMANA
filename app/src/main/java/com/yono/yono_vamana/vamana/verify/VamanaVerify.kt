package com.yono.yono_vamana.vamana.verify

/**
 * VAMANA-Verify — secure UI / transaction display layer.
 *
 * Responsible for rendering sensitive transaction details through a trusted
 * path that resists overlay attacks, screen scraping, and accessibility abuse.
 *
 * Stub only — replace [VamanaVerifyStub] with a real implementation.
 */
interface VamanaVerify {
    fun renderSecureTransaction(details: TransactionDetails): SecureRenderResult
}

data class TransactionDetails(
    val payee: String,
    val amount: String,
    val reference: String
)

data class SecureRenderResult(
    val trusted: Boolean,
    val overlayDetected: Boolean
)

class VamanaVerifyStub : VamanaVerify {
    override fun renderSecureTransaction(details: TransactionDetails): SecureRenderResult {
        // TODO: integrate real secure UI / overlay-detection rendering pipeline.
        return SecureRenderResult(trusted = true, overlayDetected = false)
    }
}
