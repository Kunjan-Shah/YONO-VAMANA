package com.yono.yono_vamana.vamana.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.ui.graphics.vector.ImageVector

/** Identifies one of the five VAMANA architecture layers shown on the dashboard. */
enum class VamanaLayerId {
    INTERCEPT,
    ISOLATE,
    VERIFY,
    INTELLIGENCE,
    CORE
}

/** UI-facing description of a layer. Backed by dummy data until real modules are wired in. */
data class VamanaLayerInfo(
    val id: VamanaLayerId,
    val codename: String,
    val title: String,
    val tagline: String,
    val description: String,
    val status: String,
    val icon: ImageVector,
    val stats: List<Pair<String, String>>
)

/** Static registry of layer metadata consumed by the dashboard and detail screens. */
object VamanaLayerRegistry {

    val layers: List<VamanaLayerInfo> = listOf(
        VamanaLayerInfo(
            id = VamanaLayerId.INTERCEPT,
            codename = "VAMANA-Intercept",
            title = "Intercept",
            tagline = "Message & link classification",
            description = "Screens inbound SMS, notifications, and links in real time, " +
                "flagging suspicious content before it ever reaches you. Acts as the " +
                "first, low-cost checkpoint in the VAMANA cost-escalation model.",
            status = "Stub — awaiting integration",
            icon = Icons.Filled.Link,
            stats = listOf(
                "Messages scanned today" to "1,248",
                "Links classified" to "312",
                "Flagged as suspicious" to "4"
            )
        ),
        VamanaLayerInfo(
            id = VamanaLayerId.ISOLATE,
            codename = "VAMANA-Isolate",
            title = "Isolate",
            tagline = "Isolated execution environment",
            description = "Spins up a sandboxed environment to safely observe or detonate " +
                "untrusted content away from your real banking session, containing " +
                "anything Intercept could not clear with confidence.",
            status = "Stub — awaiting integration",
            icon = Icons.Filled.Layers,
            stats = listOf(
                "Active sandboxes" to "0",
                "Items isolated today" to "2",
                "Escalated to Isolate" to "2"
            )
        ),
        VamanaLayerInfo(
            id = VamanaLayerId.VERIFY,
            codename = "VAMANA-Verify",
            title = "Verify",
            tagline = "Secure UI & transaction display",
            description = "Renders sensitive transaction confirmations through a trusted, " +
                "overlay-resistant display path so what you see is exactly what will " +
                "be authorized — no screen overlays, no accessibility abuse.",
            status = "Stub — awaiting integration",
            icon = Icons.Filled.VerifiedUser,
            stats = listOf(
                "Transactions verified today" to "18",
                "Overlays blocked" to "0",
                "Trust checks passed" to "18 / 18"
            )
        ),
        VamanaLayerInfo(
            id = VamanaLayerId.INTELLIGENCE,
            codename = "VAMANA-Intelligence",
            title = "Intelligence",
            tagline = "Adaptive on-device intelligence",
            description = "Fuses signals from every layer into a single on-device risk " +
                "assessment, continuously adapting its thresholds as new fraud " +
                "patterns are observed — without sending your data off-device.",
            status = "Stub — awaiting integration",
            icon = Icons.Filled.Psychology,
            stats = listOf(
                "Current risk level" to "Low",
                "Signals evaluated" to "56",
                "Model last adapted" to "2026-07-15"
            )
        ),
        VamanaLayerInfo(
            id = VamanaLayerId.CORE,
            codename = "VAMANA-Core",
            title = "Core",
            tagline = "Cost-escalation orchestration engine",
            description = "Coordinates Intercept, Isolate, Verify, and Intelligence under " +
                "a single cost-escalation model: cheap passive checks run first, and " +
                "only content that keeps looking suspicious is escalated to " +
                "progressively more expensive layers of scrutiny.",
            status = "Stub — awaiting integration",
            icon = Icons.Filled.Shield,
            stats = listOf(
                "Escalations today" to "2",
                "Tier 3 blocks" to "0",
                "Layers orchestrated" to "4"
            )
        )
    )

    fun find(id: VamanaLayerId): VamanaLayerInfo = layers.first { it.id == id }
}
