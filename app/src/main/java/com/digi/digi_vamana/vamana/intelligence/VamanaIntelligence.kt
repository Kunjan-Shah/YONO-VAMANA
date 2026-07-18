package com.digi.digi_vamana.vamana.intelligence

/**
 * VAMANA-Intelligence — adaptive on-device intelligence layer.
 *
 * Responsible for fusing signals from the other layers into a single risk
 * assessment, and for adapting thresholds as new fraud patterns emerge.
 *
 * Stub only — replace [VamanaIntelligenceStub] with a real implementation.
 */
interface VamanaIntelligence {
    fun evaluateRisk(signals: List<BehaviorSignal>): RiskAssessment
}

data class BehaviorSignal(
    val name: String,
    val weight: Float
)

data class RiskAssessment(
    val score: Float,
    val level: RiskLevel
)

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

class VamanaIntelligenceStub : VamanaIntelligence {
    override fun evaluateRisk(signals: List<BehaviorSignal>): RiskAssessment {
        // TODO: integrate real on-device adaptive intelligence model.
        return RiskAssessment(score = 0f, level = RiskLevel.LOW)
    }
}
