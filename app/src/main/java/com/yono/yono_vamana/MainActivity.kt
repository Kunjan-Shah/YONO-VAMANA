package com.yono.yono_vamana

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.yono.yono_vamana.data.OnboardingPreferences
import com.yono.yono_vamana.navigation.VamanaDestination
import com.yono.yono_vamana.navigation.VamanaNavGraph
import com.yono.yono_vamana.ui.health.HealthCheckBlockedScreen
import com.yono.yono_vamana.ui.theme.YONOVAMANATheme
import com.yono.yono_vamana.vamana.isolate.WorkProfileManager
import com.yono.yono_vamana.vamana.isolate.health.HealthCheckEngine

// FragmentActivity (not ComponentActivity) — androidx.biometric.BiometricPrompt
// requires a FragmentActivity host. Fully Compose-compatible: FragmentActivity
// extends ComponentActivity, so setContent {} and everything else is unchanged.
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val isWorkProfile = WorkProfileManager(this).isRunningInWorkProfile()

        // The VAMANA-Isolate work profile must pass its health checks every
        // time it's opened — from its own launcher icon or via "Navigate to
        // secure setup" — before showing any app content.
        val healthCheckReport = if (isWorkProfile) {
            HealthCheckEngine(this).runAllChecks()
        } else {
            null
        }

        val onboardingPreferences = OnboardingPreferences(this)
        val startDestination = if (isWorkProfile || onboardingPreferences.isSetupComplete) {
            // Onboarding preferences are per-profile SharedPreferences, so
            // the work-profile instance would otherwise always look like a
            // fresh install. Reaching the work profile at all already implies
            // setup happened (from the personal-profile Isolate screen) —
            // go straight to the dashboard.
            VamanaDestination.Dashboard.route
        } else {
            VamanaDestination.SecuritySetup.route
        }

        setContent {
            YONOVAMANATheme {
                if (healthCheckReport != null && !healthCheckReport.isLaunchAllowed) {
                    HealthCheckBlockedScreen(
                        report = healthCheckReport,
                        onExit = { finishAffinity() }
                    )
                } else {
                    VamanaNavGraph(
                        startDestination = startDestination,
                        onSetupComplete = { onboardingPreferences.isSetupComplete = true }
                    )
                }
            }
        }
    }
}
