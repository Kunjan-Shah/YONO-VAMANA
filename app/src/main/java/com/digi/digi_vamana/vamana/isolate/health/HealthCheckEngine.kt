package com.digi.digi_vamana.vamana.isolate.health

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import com.digi.digi_vamana.vamana.intelligence.VamanaActivityLog
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

data class HealthCheckResult(
    val checkName: String,
    val passed: Boolean,
    val failureReason: String? = null,
    val remediationHint: String? = null,
    val severity: Severity = Severity.BLOCKING
) {
    enum class Severity { BLOCKING, WARNING }

    companion object {
        fun pass(checkName: String): HealthCheckResult =
            HealthCheckResult(checkName = checkName, passed = true)

        fun fail(checkName: String, reason: String, hint: String? = null): HealthCheckResult =
            HealthCheckResult(
                checkName = checkName,
                passed = false,
                failureReason = reason,
                remediationHint = hint,
                severity = Severity.BLOCKING
            )

        fun warn(checkName: String, reason: String, hint: String? = null): HealthCheckResult =
            HealthCheckResult(
                checkName = checkName,
                passed = true,
                failureReason = reason,
                remediationHint = hint,
                severity = Severity.WARNING
            )
    }
}

data class HealthCheckReport(val results: List<HealthCheckResult>) {
    /** Checks that must pass before the isolated work profile is allowed to open. */
    val blockingFailures: List<HealthCheckResult>
        get() = results.filter { !it.passed && it.severity == HealthCheckResult.Severity.BLOCKING }

    /** Advisory-only issues that are surfaced but don't block launch. */
    val warnings: List<HealthCheckResult>
        get() = results.filter { it.severity == HealthCheckResult.Severity.WARNING && it.failureReason != null }

    val isLaunchAllowed: Boolean
        get() = blockingFailures.isEmpty()
}

/**
 * Pre-launch security posture checks for the VAMANA-Isolate work profile.
 *
 * Mirrors the four checks SecureFolderPlusPlus's HealthCheckEngine runs
 * (the rest of that engine's checks are disabled there too):
 *   1. ADB_ENABLED  — blocking
 *   2. DEVICE_ROOTED — blocking
 *   3. BOOTLOADER   — warning (unlocked bootloader / test-keys build)
 *   4. PATCH_LEVEL  — warning (security patch older than the minimum)
 */
class HealthCheckEngine(private val context: Context) {

    fun runAllChecks(): HealthCheckReport {
        val results = listOf(
            checkAdbEnabled(),
            checkDeviceRooted(),
            checkBootloaderStatus(),
            checkPatchLevel()
        )
        val passed = results.count { it.passed }
        val summary = "Work profile health check completed: $passed/${results.size} checks passed" +
            if (passed < results.size) {
                " (failed: ${results.filterNot { it.passed }.joinToString { it.checkName }})."
            } else {
                "."
            }
        VamanaActivityLog.log(VamanaActivityLog.Category.ISOLATE, summary)
        return HealthCheckReport(results)
    }

    // ── Check 1: USB Debugging ────────────────────────────────────────────────

    private fun checkAdbEnabled(): HealthCheckResult {
        val enabled = Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0)
        return if (enabled != 0) {
            HealthCheckResult.fail(
                checkName = "ADB_ENABLED",
                reason = "USB Debugging is turned on.",
                hint = "Settings → Developer Options → turn off USB Debugging."
            )
        } else {
            HealthCheckResult.pass("ADB_ENABLED")
        }
    }

    // ── Check 2: Root Detection ───────────────────────────────────────────────

    private fun checkDeviceRooted(): HealthCheckResult {
        val rooted = checkSuBinaries() || checkRootPackages()
        return if (rooted) {
            HealthCheckResult.fail(
                checkName = "DEVICE_ROOTED",
                reason = "This device appears to be rooted.",
                hint = "The isolated work profile cannot run on a rooted device."
            )
        } else {
            HealthCheckResult.pass("DEVICE_ROOTED")
        }
    }

    private fun checkSuBinaries(): Boolean {
        val paths = listOf(
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/bin/su",
            "/data/local/xbin/su",
            "/system/app/Superuser.apk",
            "/system/app/SuperSU.apk"
        )
        return paths.any { File(it).exists() }
    }

    private fun checkRootPackages(): Boolean {
        val rootPkgs = listOf(
            "com.topjohnwu.magisk",
            "eu.chainfire.supersu",
            "com.noshufou.android.su",
            "com.koushikdutta.superuser",
            "com.thirdparty.superuser"
        )
        return rootPkgs.any { pkg ->
            try {
                context.packageManager.getPackageInfo(pkg, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    }

    // ── Check 3: Bootloader ───────────────────────────────────────────────────

    private fun checkBootloaderStatus(): HealthCheckResult {
        val tags = Build.TAGS ?: ""
        return if (tags.contains("test-keys") || tags.contains("dev-keys")) {
            HealthCheckResult.warn(
                checkName = "BOOTLOADER",
                reason = "Device may have an unlocked bootloader (tags: $tags).",
                hint = "A locked bootloader provides stronger security."
            )
        } else {
            HealthCheckResult.pass("BOOTLOADER")
        }
    }

    // ── Check 4: Security Patch Level ─────────────────────────────────────────

    private fun checkPatchLevel(): HealthCheckResult {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val patchDate = sdf.parse(Build.VERSION.SECURITY_PATCH)
                ?: return HealthCheckResult.warn("PATCH_LEVEL", "Could not read patch date.")
            val minMonth = MIN_PATCH_LEVEL_MONTH.toString().padStart(2, '0')
            val minDate = sdf.parse("$MIN_PATCH_LEVEL_YEAR-$minMonth-01")!!
            if (patchDate.before(minDate)) {
                HealthCheckResult.warn(
                    checkName = "PATCH_LEVEL",
                    reason = "Security patch (${Build.VERSION.SECURITY_PATCH}) is outdated.",
                    hint = "Settings → System → Software Update."
                )
            } else {
                HealthCheckResult.pass("PATCH_LEVEL")
            }
        } catch (e: Exception) {
            HealthCheckResult.warn("PATCH_LEVEL", "Could not verify patch level.")
        }
    }

    private companion object {
        const val MIN_PATCH_LEVEL_YEAR = 2024
        const val MIN_PATCH_LEVEL_MONTH = 1
    }
}
