package com.yono.yono_vamana.ui.transact

import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.yono.yono_vamana.data.VerifyPreferences
import com.yono.yono_vamana.ui.theme.YONOVAMANATheme
import com.yono.yono_vamana.ui.theme.YonoGreenSuccess
import com.yono.yono_vamana.ui.theme.YonoOrange
import com.yono.yono_vamana.ui.theme.YonoPurple
import com.yono.yono_vamana.ui.theme.YonoPurpleDark
import com.yono.yono_vamana.ui.theme.YonoPurpleDarkest
import com.yono.yono_vamana.ui.theme.YonoPurpleLight
import com.yono.yono_vamana.vamana.verify.tee.BankingServerClient
import com.yono.yono_vamana.vamana.verify.tee.FallbackPolicy
import com.yono.yono_vamana.vamana.verify.tee.TeeConfirmResult
import com.yono.yono_vamana.vamana.verify.tee.TeeSigningKeyManager
import com.yono.yono_vamana.vamana.verify.tee.TeeTransactionAuthenticator
import com.yono.yono_vamana.vamana.verify.tee.TeeTransactionPayload
import kotlinx.coroutines.launch

/** Drives the full-screen payment confirmation experience — each state below is a distinct screen. */
private sealed class PaymentUiState {
    data object Form : PaymentUiState()
    data class Processing(val message: String) : PaymentUiState()
    data class Success(val newBalance: Long?, val note: String?) : PaymentUiState()
    data class Failed(val message: String) : PaymentUiState()
}

@Composable
fun PaymentScreen(contact: DummyContact, onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val verifyPreferences = remember { VerifyPreferences(context) }
    val coroutineScope = rememberCoroutineScope()

    var amount by remember { mutableStateOf("") }
    var uiState by remember { mutableStateOf<PaymentUiState>(PaymentUiState.Form) }

    fun confirmPayment() {
        if (!verifyPreferences.isActive) {
            // VAMANA-Verify is off — behave as before, no authentication gate, no bank server call.
            uiState = PaymentUiState.Success(newBalance = null, note = null)
            return
        }

        val fragmentActivity = activity
        if (fragmentActivity == null) {
            uiState = PaymentUiState.Failed("Could not start secure authentication.")
            return
        }

        uiState = PaymentUiState.Processing("Verifying via secure hardware…")
        coroutineScope.launch {
            val payload = TeeTransactionPayload(
                transactionId = "txn_${System.currentTimeMillis()}",
                contactId = contact.id,
                contactName = contact.name,
                displayAmount = "₹$amount"
            )
            val result = TeeTransactionAuthenticator(fragmentActivity).confirmTransaction(payload)

            when (result) {
                is TeeConfirmResult.Success -> {
                    uiState = PaymentUiState.Processing("Confirming with bank…")
                    val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                        ?: "unknown_device"
                    BankingServerClient.registerDevice(deviceId, TeeSigningKeyManager.getPublicKeyBase64())
                    val txResult = BankingServerClient.confirmTransaction(deviceId, result.attestation)
                    uiState = if (txResult.success) {
                        PaymentUiState.Success(txResult.newBalance, "Authenticated via device TEE.")
                    } else {
                        PaymentUiState.Failed(txResult.message.ifBlank { "The bank rejected this transaction." })
                    }
                }

                is TeeConfirmResult.Cancelled -> {
                    uiState = PaymentUiState.Failed("Authentication was cancelled.")
                }

                is TeeConfirmResult.Unavailable -> {
                    val amountRupees = amount.toLongOrNull() ?: 0L
                    uiState = when (val decision = FallbackPolicy.evaluate(amountRupees, result.reason)) {
                        is FallbackPolicy.Decision.Blocked -> PaymentUiState.Failed(decision.message)
                        is FallbackPolicy.Decision.ProceedWithWarning ->
                            PaymentUiState.Success(newBalance = null, note = decision.message)
                    }
                }

                is TeeConfirmResult.Error -> {
                    uiState = PaymentUiState.Failed(result.message)
                }
            }
        }
    }

    when (val state = uiState) {
        is PaymentUiState.Form -> PaymentFormScreen(
            contact = contact,
            amount = amount,
            onAmountChange = { amount = it },
            onBack = onBack,
            onConfirm = ::confirmPayment
        )
        is PaymentUiState.Processing -> TeeProcessingScreen(
            contact = contact,
            amount = amount,
            message = state.message
        )
        is PaymentUiState.Success -> PaymentResultScreen(
            isSuccess = true,
            title = "Payment confirmed",
            message = "₹$amount paid to ${contact.name} (dummy transaction).",
            note = state.note,
            newBalance = state.newBalance,
            onPrimaryAction = onBack,
            onRetry = null
        )
        is PaymentUiState.Failed -> PaymentResultScreen(
            isSuccess = false,
            title = "Payment failed",
            message = state.message,
            note = null,
            newBalance = null,
            onPrimaryAction = onBack,
            onRetry = { uiState = PaymentUiState.Form }
        )
    }
}

@Composable
private fun PaymentFormScreen(
    contact: DummyContact,
    amount: String,
    onAmountChange: (String) -> Unit,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(YonoPurpleDarkest, YonoPurpleDark, YonoPurpleLight)
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
                OutlinedTextField(
                    value = amount,
                    onValueChange = { input -> onAmountChange(input.filter { it.isDigit() }) },
                    label = { Text("Amount (₹)") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YonoPurpleDark,
                        focusedLabelColor = YonoPurpleDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onConfirm,
                    enabled = amount.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = YonoOrange,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Confirm",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Full-screen backdrop shown while the TEE flow is running. The system
 * BiometricPrompt dialog appears on top of this screen (Android owns that
 * dialog — no app can render its own biometric UI), but the surrounding
 * experience is now a dedicated full-screen moment instead of a small
 * in-form card, and the transaction details stay visible throughout.
 */
@Composable
private fun TeeProcessingScreen(contact: DummyContact, amount: String, message: String) {
    Surface(modifier = Modifier.fillMaxSize(), color = YonoPurpleDarkest) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(shape = CircleShape, color = YonoPurpleLight.copy(alpha = 0.25f), modifier = Modifier.size(96.dp)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Filled.Fingerprint,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(52.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "₹$amount to ${contact.name}",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            CircularProgressIndicator(color = YonoOrange, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun PaymentResultScreen(
    isSuccess: Boolean,
    title: String,
    message: String,
    note: String?,
    newBalance: Long?,
    onPrimaryAction: () -> Unit,
    onRetry: (() -> Unit)?
) {
    val accentColor = if (isSuccess) YonoGreenSuccess else MaterialTheme.colorScheme.error
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(shape = CircleShape, color = accentColor.copy(alpha = 0.15f), modifier = Modifier.size(88.dp)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = if (isSuccess) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            note?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            newBalance?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = accentColor.copy(alpha = 0.08f)
                ) {
                    Text(
                        text = "New balance at bank: ₹$it",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = onPrimaryAction,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isSuccess) "Done" else "Back to Transact")
            }
            onRetry?.let { retry ->
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = retry,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = YonoPurple),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Try again")
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun PaymentScreenPreview() {
    YONOVAMANATheme {
        PaymentScreen(contact = DummyContacts.contacts.first(), onBack = {})
    }
}
