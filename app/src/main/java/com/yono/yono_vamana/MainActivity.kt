package com.yono.yono_vamana

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.yono.yono_vamana.data.OnboardingPreferences
import com.yono.yono_vamana.navigation.VamanaDestination
import com.yono.yono_vamana.navigation.VamanaNavGraph
import com.yono.yono_vamana.ui.theme.YONOVAMANATheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val onboardingPreferences = OnboardingPreferences(this)
        val startDestination = if (onboardingPreferences.isSetupComplete) {
            VamanaDestination.Dashboard.route
        } else {
            VamanaDestination.SecuritySetup.route
        }

        setContent {
            YONOVAMANATheme {
                VamanaNavGraph(
                    startDestination = startDestination,
                    onSetupComplete = { onboardingPreferences.isSetupComplete = true }
                )
            }
        }
    }
}
