package com.digi.digi_vamana.ui.detail

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.digi.digi_vamana.data.IntelligencePreferences
import com.digi.digi_vamana.data.InterceptPreferences
import com.digi.digi_vamana.data.VerifyPreferences
import com.digi.digi_vamana.ui.theme.DigiVamanaTheme
import com.digi.digi_vamana.ui.theme.DigiGreenSuccess
import com.digi.digi_vamana.ui.theme.DigiOrange
import com.digi.digi_vamana.ui.theme.DigiPurple
import com.digi.digi_vamana.ui.theme.DigiPurpleDark
import com.digi.digi_vamana.ui.theme.DigiPurpleDarkest
import com.digi.digi_vamana.ui.theme.DigiPurpleLight
import com.digi.digi_vamana.vamana.intercept.NotificationListenerAccess
import com.digi.digi_vamana.vamana.intercept.SmsNotificationListenerService
import com.digi.digi_vamana.vamana.intelligence.VamanaActivityLog
import com.digi.digi_vamana.vamana.isolate.PolicyEnforcer
import com.digi.digi_vamana.vamana.isolate.WorkProfileManager
import com.digi.digi_vamana.vamana.model.VamanaLayerId
import com.digi.digi_vamana.vamana.model.VamanaLayerInfo
import com.digi.digi_vamana.vamana.model.VamanaLayerRegistry

@Composable
fun LayerDetailScreen(layerInfo: VamanaLayerInfo, onBack: () -> Unit) {
    val context = LocalContext.current
    val intelligencePreferences = remember { IntelligencePreferences(context) }
    var isIntelligenceActive by remember { mutableStateOf(intelligencePreferences.isActive) }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(DigiPurpleDarkest, DigiPurpleDark, DigiPurpleLight)
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = androidx.compose.ui.graphics.Color.White
                            )
                        }
                        Text(
                            text = layerInfo.codename,
                            style = MaterialTheme.typography.titleMedium,
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
                        )
                    }
                    Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 4.dp)) {
                        Surface(
                            shape = CircleShape,
                            color = DigiPurpleLight.copy(alpha = 0.35f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    imageVector = layerInfo.icon,
                                    contentDescription = null,
                                    tint = androidx.compose.ui.graphics.Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = layerInfo.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = androidx.compose.ui.graphics.Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = layerInfo.tagline,
                            style = MaterialTheme.typography.bodyMedium,
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f).navigationBarsPadding(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp)
            ) {
                item {
                    StatusChip(status = layerInfo.status)
                    Spacer(modifier = Modifier.height(20.dp))
                    if (layerInfo.id == VamanaLayerId.INTERCEPT) {
                        InterceptActivationSection()
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    if (layerInfo.id == VamanaLayerId.ISOLATE) {
                        IsolateWorkProfileSection()
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    if (layerInfo.id == VamanaLayerId.VERIFY) {
                        VerifyActivationSection()
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    if (layerInfo.id == VamanaLayerId.INTELLIGENCE) {
                        IntelligenceActivationSection(
                            isActive = isIntelligenceActive,
                            onToggle = { checked ->
                                if (checked) {
                                    isIntelligenceActive = checked
                                    intelligencePreferences.isActive = checked
                                    VamanaActivityLog.log(
                                        VamanaActivityLog.Category.LAYER,
                                        "VAMANA-Intelligence activated — activity logging started for all layers."
                                    )
                                } else {
                                    // Log while the gate is still open — flipping the pref first
                                    // would silently swallow this final line.
                                    VamanaActivityLog.log(
                                        VamanaActivityLog.Category.LAYER,
                                        "VAMANA-Intelligence deactivated — activity logging stopped."
                                    )
                                    isIntelligenceActive = checked
                                    intelligencePreferences.isActive = checked
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    Text(
                        text = "Overview",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = layerInfo.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

        if (layerInfo.id == VamanaLayerId.INTELLIGENCE && isIntelligenceActive) {
//            IntelligenceChatOverlay(
//                modifier = Modifier
//                    .align(Alignment.BottomEnd)
//                    .padding(20.dp)
//            )
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun InterceptActivationSection() {
    val context = LocalContext.current
    val interceptPreferences = remember { InterceptPreferences(context) }
    var isActive by remember { mutableStateOf(interceptPreferences.isActive) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    // Listener access can be revoked from Settings behind our back — don't
    // keep claiming Intercept is active if that happened.
    LaunchedEffect(Unit) {
        if (isActive && !NotificationListenerAccess.isEnabled(context)) {
            isActive = false
            interceptPreferences.isActive = false
        }
    }

    val activate: () -> Unit = {
        isActive = true
        interceptPreferences.isActive = true
        statusMessage = null
        SmsNotificationListenerService.requestActivate(context)
        VamanaActivityLog.log(
            VamanaActivityLog.Category.LAYER,
            "VAMANA-Intercept activated — now watching notifications from the default SMS app."
        )
    }

    // Settings doesn't return a meaningful result code for this special-access
    // screen, so the only reliable signal is re-checking the listener grant.
    val listenerSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (NotificationListenerAccess.isEnabled(context)) {
            VamanaActivityLog.log(
                VamanaActivityLog.Category.PERMISSION,
                "Notification listener access granted for VAMANA-Intercept."
            )
            activate()
        } else {
            statusMessage = "Notification access wasn't granted, so VAMANA-Intercept can't watch for SMS alerts."
            VamanaActivityLog.log(
                VamanaActivityLog.Category.PERMISSION,
                "Notification listener access was not granted — VAMANA-Intercept stays inactive."
            )
        }
    }

    val checkListenerAndActivate: () -> Unit = {
        if (NotificationListenerAccess.isEnabled(context)) {
            activate()
        } else {
            statusMessage = "Grant notification access so VAMANA-Intercept can watch for SMS alerts."
            listenerSettingsLauncher.launch(NotificationListenerAccess.settingsIntent())
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            VamanaActivityLog.log(VamanaActivityLog.Category.PERMISSION, "POST_NOTIFICATIONS permission granted.")
            checkListenerAndActivate()
        } else {
            statusMessage = "Notification permission is required to raise SMS alerts."
            VamanaActivityLog.log(VamanaActivityLog.Category.PERMISSION, "POST_NOTIFICATIONS permission denied by user.")
        }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Malicious SMS interception",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isActive) {
                            "Watching notifications from your SMS app for likely threats."
                        } else {
                            "Not yet activated on this device."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = isActive,
                    onCheckedChange = { checked ->
                        if (checked) {
                            statusMessage = null
                            val needsNotificationPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                                PackageManager.PERMISSION_GRANTED
                            if (needsNotificationPermission) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                checkListenerAndActivate()
                            }
                        } else {
                            isActive = false
                            interceptPreferences.isActive = false
                            statusMessage = null
                            SmsNotificationListenerService.requestDeactivate(context)
                            VamanaActivityLog.log(VamanaActivityLog.Category.LAYER, "VAMANA-Intercept deactivated by user.")
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = DigiOrange,
                        checkedThumbColor = Color.White
                    )
                )
            }

            if (isActive) {
                Spacer(modifier = Modifier.height(12.dp))
                ActiveStatusPill(label = "VAMANA-Intercept is active")
            }

            statusMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun IsolateWorkProfileSection() {
    val context = LocalContext.current
    val activity = context as? Activity
    val workProfileManager = remember { WorkProfileManager(context.applicationContext) }
    val isInWorkProfile = remember { workProfileManager.isRunningInWorkProfile() }

    var isProvisioned by remember { mutableStateOf(workProfileManager.isProfileReady()) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var showDeactivateDialog by remember { mutableStateOf(false) }

    val provisioningLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            workProfileManager.markProfileCreated()
            isProvisioned = true
            statusMessage = null
            VamanaActivityLog.log(
                VamanaActivityLog.Category.ISOLATE,
                "Work profile provisioning approved by user — isolated work profile created."
            )
        } else {
            statusMessage = "Setup was cancelled or failed."
            VamanaActivityLog.log(VamanaActivityLog.Category.ISOLATE, "Work profile provisioning was cancelled or failed.")
        }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Isolated work profile",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))

            if (isInWorkProfile) {
                // We're already running inside the isolated profile — there's
                // nothing to "navigate to", just confirm it's active.
                Text(
                    text = "You're currently inside the isolated work profile.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                ActiveStatusPill(label = "VAMANA-Isolate is active")
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { showDeactivateDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Deactivate")
                }

                if (showDeactivateDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeactivateDialog = false },
                        title = { Text("Deactivate VAMANA-Isolate?") },
                        text = {
                            Text(
                                "This permanently removes the isolated work profile and everything " +
                                    "in it, including this app instance. You'll need to activate it " +
                                    "again from the personal profile to use it."
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDeactivateDialog = false
                                    PolicyEnforcer(context).wipeProfileData()
                                }
                            ) {
                                Text("Deactivate", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeactivateDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            } else if (isProvisioned) {
                Text(
                    text = "The isolated work profile is active on this device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { activity?.let { workProfileManager.openWorkProfileInstance(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DigiOrange, contentColor = Color.White)
                ) {
                    Text("Navigate to secure setup")
                }
            } else {
                val blocked = workProfileManager.checkProvisioningBlocked()
                if (blocked != null) {
                    Text(
                        text = blocked.userMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text(
                        text = "Not yet activated on this device.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val intent = workProfileManager.buildProvisioningIntent()
                            if (intent != null) {
                                provisioningLauncher.launch(intent)
                            } else {
                                statusMessage = "Could not start setup."
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DigiOrange, contentColor = Color.White)
                    ) {
                        Text("Activate")
                    }
                }
            }

            statusMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun VerifyActivationSection() {
    val context = LocalContext.current
    val verifyPreferences = remember { VerifyPreferences(context) }
    var isActive by remember { mutableStateOf(verifyPreferences.isActive) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Secure transaction display",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isActive) {
                            "Overlay-resistant transaction rendering is enabled on this device."
                        } else {
                            "Not yet activated on this device."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = isActive,
                    onCheckedChange = { checked ->
                        isActive = checked
                        verifyPreferences.isActive = checked
                        VamanaActivityLog.log(
                            VamanaActivityLog.Category.LAYER,
                            if (checked) {
                                "VAMANA-Verify activated — transactions will require TEE-backed confirmation."
                            } else {
                                "VAMANA-Verify deactivated by user."
                            }
                        )
                    },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = DigiOrange,
                        checkedThumbColor = Color.White
                    )
                )
            }

            if (isActive) {
                Spacer(modifier = Modifier.height(12.dp))
                ActiveStatusPill(label = "VAMANA-Verify is active")
            }
        }
    }
}

@Composable
private fun IntelligenceActivationSection(isActive: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Adaptive on-device intelligence",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isActive) {
                            "Risk signals are being fused into an on-device assessment."
                        } else {
                            "Not yet activated on this device."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = isActive,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = DigiOrange,
                        checkedThumbColor = Color.White
                    )
                )
            }

            if (isActive) {
                Spacer(modifier = Modifier.height(12.dp))
                ActiveStatusPill(label = "VAMANA-Intelligence is active")
            }
        }
    }
}

/**
 * Floating chat entry point for the VAMANA agent, shown bottom-end only while
 * VAMANA-Intelligence is active. The greeting bubble is dismissible on its
 * own — closing it doesn't hide the chat button underneath.
 */
//@Composable
//private fun IntelligenceChatOverlay(modifier: Modifier = Modifier) {
//    var showGreeting by remember { mutableStateOf(true) }
//
//    Column(
//        modifier = modifier,
//        horizontalAlignment = Alignment.End
//    ) {
//        if (showGreeting) {
//            Card(
//                shape = RoundedCornerShape(16.dp),
//                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
//                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
//                modifier = Modifier
//                    .padding(bottom = 12.dp)
//                    .widthIn(max = 220.dp)
//            ) {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier.padding(start = 14.dp, top = 10.dp, bottom = 10.dp, end = 4.dp)
//                ) {
//                    Text(
//                        text = "Hi, I am VAMANA agent",
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.onSurface,
//                        modifier = Modifier.weight(1f)
//                    )
//                    IconButton(
//                        onClick = { showGreeting = false },
//                        modifier = Modifier.size(28.dp)
//                    ) {
//                        Icon(
//                            imageVector = Icons.Filled.Close,
//                            contentDescription = "Dismiss",
//                            modifier = Modifier.size(16.dp)
//                        )
//                    }
//                }
//            }
//        }
//
//        FloatingActionButton(
//            onClick = { /* Stub — no chat backend wired up yet. */ },
//            containerColor = DigiPurple,
//            contentColor = Color.White
//        ) {
//            Icon(imageVector = Icons.AutoMirrored.Filled.Chat, contentDescription = "VAMANA agent chat")
//        }
//    }
//}

@Composable
private fun ActiveStatusPill(label: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = DigiGreenSuccess.copy(alpha = 0.15f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = DigiGreenSuccess,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = DigiGreenSuccess
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun LayerDetailScreenPreview() {
    DigiVamanaTheme {
        LayerDetailScreen(layerInfo = VamanaLayerRegistry.layers.first(), onBack = {})
    }
}
