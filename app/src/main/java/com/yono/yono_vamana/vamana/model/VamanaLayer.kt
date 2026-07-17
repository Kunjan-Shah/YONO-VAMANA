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
    INTELLIGENCE
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
            tagline = "Screens SMS links before you ever tap them",
            description = "The moment an SMS or WhatsApp message reaches your phone, " +
                "VAMANA-Intercept's on-device classifier scores it for the phishing and " +
                "fake-APK patterns behind fraud like fake YONO app scams. Instead of a " +
                "passive banner you can swipe away and forget, a suspicious message " +
                "triggers a persistent, plain-language warning that stays on screen " +
                "until you've seen it. Stopping fraud here, before installation, means " +
                "everything downstream — your device, your session, your money — " +
                "never comes under threat.",
            status = "Layer 1 · Notification interception",
            icon = Icons.Filled.Link,
            stats = listOf(
                "Messages & links screened today" to "212",
                "Malicious APK links blocked" to "5",
                "Time to alert" to "< 1 second"
            )
        ),
        VamanaLayerInfo(
            id = VamanaLayerId.ISOLATE,
            codename = "VAMANA-Isolate",
            title = "Isolate",
            tagline = "Runs YONO in a sandbox nothing else can reach",
            description = "VAMANA-Isolate runs your banking session inside a separate, " +
                "managed work profile — a walled-off copy of your device that other " +
                "apps, screen recorders, and remote-access tools cannot see into. " +
                "Overlay attempts, screen captures, and cross-profile snooping are " +
                "blocked at the OS level before they ever reach your banking data. If " +
                "something ever slips past Intercept, it detonates here, fully " +
                "contained, with your real session and credentials completely out of " +
                "reach.",
            status = "Layer 2 · Isolated execution",
            icon = Icons.Filled.Layers,
            stats = listOf(
                "Overlay attempts blocked" to "0",
                "Screen recording attempts blocked" to "0",
                "Isolation health checks passed" to "6 / 6"
            )
        ),
        VamanaLayerInfo(
            id = VamanaLayerId.VERIFY,
            codename = "VAMANA-Verify",
            title = "Verify",
            tagline = "What you see is what you sign — no OTP",
            description = "VAMANA-Verify renders every transaction confirmation through a " +
                "trusted display path that overlay attacks and screen-scraping malware " +
                "cannot read or manipulate, so the amount and payee you approve are " +
                "exactly what reaches the bank. Approval is cryptographically bound to " +
                "your biometric via hardware-backed keys in the device's Trusted " +
                "Execution Environment. And rather than a text-message OTP that can be " +
                "intercepted or phished, VAMANA-Verify authenticates over your mobile " +
                "carrier's own network with zero taps required, closing the single " +
                "weakest link in today's banking security.",
            status = "Layer 3–4 · Secure display & network auth",
            icon = Icons.Filled.VerifiedUser,
            stats = listOf(
                "Transactions verified today" to "18",
                "Overlay attempts blocked" to "0",
                "OTP-free network authentications" to "18 / 18"
            )
        ),
        VamanaLayerInfo(
            id = VamanaLayerId.INTELLIGENCE,
            codename = "VAMANA-Intelligence",
            title = "Intelligence",
            tagline = "Learns from every attack, in under a day",
            description = "VAMANA-Intelligence fuses signals from Intercept, Isolate, and " +
                "Verify into a single on-device risk score, catching combinations no " +
                "single layer would flag alone. When something new and genuinely " +
                "suspicious surfaces, a hybrid pipeline escalates it to a server-side " +
                "forensic LLM for deep analysis — turning what used to be a 30–90 day " +
                "gap between a new scam appearing and a defense shipping for it into " +
                "under 24 hours, and pushing that update back out to every device " +
                "automatically.",
            status = "Layer 5 · Adaptive intelligence",
            icon = Icons.Filled.Psychology,
            stats = listOf(
                "Current risk level" to "Low",
                "Signals fused today" to "56",
                "Attack-to-defense turnaround" to "< 24 hrs"
            )
        ),
//        VamanaLayerInfo(
//            id = VamanaLayerId.CORE,
//            codename = "VAMANA-Core",
//            title = "Core",
//            tagline = "Cost-escalation orchestration engine",
//            description = "Coordinates Intercept, Isolate, Verify, and Intelligence under " +
//                "a single cost-escalation model: cheap passive checks run first, and " +
//                "only content that keeps looking suspicious is escalated to " +
//                "progressively more expensive layers of scrutiny.",
//            status = "Stub — awaiting integration",
//            icon = Icons.Filled.Shield,
//            stats = listOf(
//                "Escalations today" to "2",
//                "Tier 3 blocks" to "0",
//                "Layers orchestrated" to "4"
//            )
//        )
    )

    fun find(id: VamanaLayerId): VamanaLayerInfo = layers.first { it.id == id }
}
