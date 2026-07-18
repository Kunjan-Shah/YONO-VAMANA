package com.digi.digi_vamana.ui.transact

import android.provider.Settings
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.digi.digi_vamana.data.VerifyPreferences
import com.digi.digi_vamana.ui.theme.DigiVamanaTheme
import com.digi.digi_vamana.ui.theme.DigiGreenSuccess
import com.digi.digi_vamana.ui.theme.DigiOrange
import com.digi.digi_vamana.ui.theme.DigiPurpleDark
import com.digi.digi_vamana.ui.theme.DigiPurpleDarkest
import com.digi.digi_vamana.ui.theme.DigiPurpleLight
import com.digi.digi_vamana.vamana.verify.BankingServerClient
import com.digi.digi_vamana.vamana.verify.tee.FallbackPolicy
import com.digi.digi_vamana.vamana.verify.tee.TeeConfirmResult
import com.digi.digi_vamana.vamana.verify.tee.TeeSigningKeyManager
import com.digi.digi_vamana.vamana.verify.tee.TeeTransactionAuthenticator
import com.digi.digi_vamana.vamana.verify.tee.TeeTransactionPayload
import com.digi.digi_vamana.vamana.verify.sna.CellularNetworkProvider
import com.digi.digi_vamana.vamana.verify.sna.SnaDemoConfig
import com.digi.digi_vamana.vamana.intelligence.VamanaActivityLog
import kotlinx.coroutines.launch
import java.time.Instant

/** adb logcat -s VamanaSNA:I — the "mobile phone terminal", viewed on the dev machine over USB. */
private const val SNA_LOG_TAG = "VamanaSNA"

@Composable
fun PaymentScreen(contact: DummyContact, onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val verifyPreferences = remember { VerifyPreferences(context) }
    val coroutineScope = rememberCoroutineScope()

    var amount by remember { mutableStateOf("") }
    var simState by remember { mutableStateOf("bound") } // bound | swapped — demo control, see CellularNetworkProvider doc
    var isConfirmed by remember { mutableStateOf(false) }
    var isAuthenticating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successNote by remember { mutableStateOf<String?>(null) }
    var newBalance by remember { mutableStateOf<Long?>(null) }

    fun trace(line: String) {
        Log.i(SNA_LOG_TAG, line)
        VamanaActivityLog.log(VamanaActivityLog.Category.TRANSACTION, line)
    }

    fun confirmPayment() {
        errorMessage = null
        trace("── Confirm tapped: ₹$amount to ${contact.name} ──────────────")

        val transactionId = "txn_${System.currentTimeMillis()}"
        val timestamp = Instant.now().toString()

        isAuthenticating = true
        coroutineScope.launch {
            // ── Real network detection (not simulated) — the SNA transport signal ──
            val netProvider = CellularNetworkProvider(context)
            val activeTransport = netProvider.detectActiveTransport()
            trace("ConnectivityManager: active transport = $activeTransport")

            var deviceTransport = activeTransport
            if (activeTransport != "CELLULAR") {
                trace("requesting Network bound to TRANSPORT_CELLULAR…")
                val cellularNetwork = netProvider.requestCellularNetwork()
                trace(if (cellularNetwork != null) "cellular network bound" else "no cellular network available")
                if (cellularNetwork != null) deviceTransport = "CELLULAR"
            }
            netProvider.release()

            // ── TEE step — only when VAMANA-Verify is active; SNA below always runs ──
            var signatureBase64: String? = null
            var teeNote: String? = null

            if (verifyPreferences.isActive) {
                val fragmentActivity = activity
                if (fragmentActivity == null) {
                    isAuthenticating = false
                    errorMessage = "Could not start secure authentication."
                    return@launch
                }
                trace("VAMANA-Verify active — requesting TEE authentication…")
                val payload = TeeTransactionPayload(
                    transactionId = transactionId,
                    contactId = contact.id,
                    contactName = contact.name,
                    displayAmount = "₹$amount",
                    timestamp = timestamp
                )
                when (val result = TeeTransactionAuthenticator(fragmentActivity).confirmTransaction(payload)) {
                    is TeeConfirmResult.Success -> {
                        trace("TEE signature obtained")
                        signatureBase64 = Base64.encodeToString(result.attestation.signature, Base64.NO_WRAP)
                        val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                            ?: "unknown_device"
                        BankingServerClient.registerDevice(deviceId, TeeSigningKeyManager.getPublicKeyBase64())
                        teeNote = "TEE"
                    }
                    is TeeConfirmResult.Cancelled -> {
                        isAuthenticating = false
                        errorMessage = "Authentication was cancelled."
                        return@launch
                    }
                    is TeeConfirmResult.Unavailable -> {
                        val amountRupees = amount.toLongOrNull() ?: 0L
                        when (val decision = FallbackPolicy.evaluate(amountRupees, result.reason)) {
                            is FallbackPolicy.Decision.Blocked -> {
                                isAuthenticating = false
                                errorMessage = decision.message
                                return@launch
                            }
                            is FallbackPolicy.Decision.ProceedWithWarning -> {
                                trace("TEE unavailable (${result.reason}) — proceeding on SNA alone")
                            }
                        }
                    }
                    is TeeConfirmResult.Error -> {
                        isAuthenticating = false
                        errorMessage = result.message
                        return@launch
                    }
                }
            } else {
                trace("VAMANA-Verify inactive — skipping TEE, SNA only")
            }

            // ── SNA step — always runs, independent of the TEE step above ──
            trace("POST /transactions/confirm  transport=$deviceTransport  sim=$simState")
            val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                ?: "unknown_device"
            val txResult = BankingServerClient.confirmTransaction(
                deviceId = deviceId,
                transactionId = transactionId,
                contactId = contact.id,
                contactName = contact.name,
                displayAmount = "₹$amount",
                timestamp = timestamp,
                signatureBase64 = signatureBase64,
                sna = BankingServerClient.SnaContext(
                    msisdn = SnaDemoConfig.DEMO_MSISDN,
                    deviceTransport = deviceTransport,
                    simDemoState = simState
                )
            )
            trace("<- ${txResult.status}  ${txResult.message}")
            isAuthenticating = false

            when (txResult.status) {
                "success" -> {
                    successNote = if (teeNote != null) {
                        "Authenticated via device TEE and Silent Network Authentication."
                    } else {
                        "Authenticated via Silent Network Authentication."
                    }
                    newBalance = txResult.newBalance
                    isConfirmed = true
                    trace("Payment of ₹$amount to ${contact.name} confirmed. New balance: ₹${txResult.newBalance}.")
                }
                else -> {
                    errorMessage = txResult.message.ifBlank { "The bank rejected this transaction." }
                    trace("Payment of ₹$amount to ${contact.name} failed: $errorMessage")
                }
            }
        }
    }

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
                    .padding(bottom = 24.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = "Transact",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    Text(
                        text = "You are paying ${contact.name}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp)
                    )
                    Text(
                        text = contact.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(20.dp)
            ) {
                if (isConfirmed) {
                    PaymentSuccessCard(
                        contact = contact,
                        amount = amount,
                        note = successNote,
                        newBalance = newBalance,
                        onDone = onBack
                    )
                } else {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { input -> amount = input.filter { it.isDigit() } },
                        label = { Text("Amount (₹)") },
                        singleLine = true,
                        enabled = !isAuthenticating,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DigiPurpleDark,
                            focusedLabelColor = DigiPurpleDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "SIM binding (demo control)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row {
                        FilterChip(
                            selected = simState == "bound",
                            onClick = { simState = "bound" },
                            label = { Text("Bound") },
                            enabled = !isAuthenticating,
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = DigiGreenSuccess.copy(alpha = 0.2f))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            selected = simState == "swapped",
                            onClick = { simState = "swapped" },
                            label = { Text("Swapped") },
                            enabled = !isAuthenticating,
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.errorContainer)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = ::confirmPayment,
                        enabled = amount.isNotBlank() && !isAuthenticating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DigiOrange,
                            contentColor = Color.White
                        )
                    ) {
                        if (isAuthenticating) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = "Confirm",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    errorMessage?.let {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentSuccessCard(
    contact: DummyContact,
    amount: String,
    note: String?,
    newBalance: Long?,
    onDone: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DigiGreenSuccess.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(shape = CircleShape, color = DigiGreenSuccess.copy(alpha = 0.2f), modifier = Modifier.size(56.dp)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = DigiGreenSuccess,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Payment confirmed",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = DigiGreenSuccess
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "₹$amount paid to ${contact.name} (dummy transaction).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            note?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            newBalance?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "New balance at bank: ₹$it",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = DigiGreenSuccess
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onDone,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DigiGreenSuccess, contentColor = Color.White)
            ) {
                Text("Done")
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun PaymentScreenPreview() {
    DigiVamanaTheme {
        PaymentScreen(contact = DummyContacts.contacts.first(), onBack = {})
    }
}
