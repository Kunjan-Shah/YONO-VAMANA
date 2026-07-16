package com.yono.yono_vamana.vamana.intercept

/**
 * VAMANA-Intercept — message and link classification layer.
 *
 * Responsible for triaging inbound SMS, notifications, and links before they
 * reach the user, and for handing suspicious content to VAMANA-Isolate /
 * VAMANA-Intelligence for deeper analysis.
 *
 * Stub only — replace [VamanaInterceptStub] with a real implementation.
 */
interface VamanaIntercept {
    fun classifyMessage(message: String): InterceptVerdict
    fun classifyLink(url: String): InterceptVerdict
}

enum class InterceptVerdict {
    SAFE,
    SUSPICIOUS,
    MALICIOUS,
    UNKNOWN
}

class VamanaInterceptStub : VamanaIntercept {
    override fun classifyMessage(message: String): InterceptVerdict {
        // TODO: integrate real on-device message classifier.
        return InterceptVerdict.UNKNOWN
    }

    override fun classifyLink(url: String): InterceptVerdict {
        // TODO: integrate real URL reputation / heuristic engine.
        return InterceptVerdict.UNKNOWN
    }
}
