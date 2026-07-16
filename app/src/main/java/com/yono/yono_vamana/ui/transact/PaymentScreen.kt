package com.yono.yono_vamana.ui.transact

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
import com.yono.yono_vamana.data.VerifyPreferences
import com.yono.yono_vamana.ui.theme.YONOVAMANATheme
import com.yono.yono_vamana.ui.theme.YonoGreenSuccess
import com.yono.yono_vamana.ui.theme.YonoOrange
import com.yono.yono_vamana.ui.theme.YonoPurpleDark
import com.yono.yono_vamana.ui.theme.YonoPurpleDarkest
import com.yono.yono_vamana.ui.theme.YonoPurpleLight
import com.yono.yono_vamana.vamana.verify.tee.FallbackPolicy
import com.yono.yono_vamana.vamana.verify.tee.TeeConfirmResult
import com.yono.yono_vamana.vamana.verify.tee.TeeTransactionAuthenticator
import com.yono.yono_vamana.vamana.verify.tee.TeeTransactionPayload
import kotlinx.coroutines.launch

@Composable
fun PaymentScreen(contact: DummyContact, onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val verifyPreferences = remember { VerifyPreferences(context) }
    val coroutineScope = rememberCoroutineScope()

    var amount by remember { mutableStateOf("") }
    var isConfirmed by remember { mutableStateOf(false) }
    var isAuthenticating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successNote by remember { mutableStateOf<String?>(null) }

    fun confirmPayment() {
        errorMessage = null

        if (!verifyPreferences.isActive) {
            // VAMANA-Verify is off — behave as before, no authentication gate.
            successNote = null
            isConfirmed = true
            return
        }

        val fragmentActivity = activity
        if (fragmentActivity == null) {
            errorMessage = "Could not start secure authentication."
            return
        }

        isAuthenticating = true
        coroutineScope.launch {
            val payload = TeeTransactionPayload(
                transactionId = "txn_${System.currentTimeMillis()}",
                contactName = contact.name,
                displayAmount = "₹$amount"
            )
            val result = TeeTransactionAuthenticator(fragmentActivity).confirmTransaction(payload)
            isAuthenticating = false

            when (result) {
                is TeeConfirmResult.Success -> {
                    successNote = "Authenticated via device TEE."
                    isConfirmed = true
                }

                is TeeConfirmResult.Cancelled -> {
                    errorMessage = "Authentication was cancelled."
                }

                is TeeConfirmResult.Unavailable -> {
                    val amountRupees = amount.toLongOrNull() ?: 0L
                    when (val decision = FallbackPolicy.evaluate(amountRupees, result.reason)) {
                        is FallbackPolicy.Decision.Blocked -> {
                            errorMessage = decision.message
                        }
                        is FallbackPolicy.Decision.ProceedWithWarning -> {
                            successNote = decision.message
                            isConfirmed = true
                        }
                    }
                }

                is TeeConfirmResult.Error -> {
                    errorMessage = result.message
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
                if (isConfirmed) {
                    PaymentSuccessCard(
                        contact = contact,
                        amount = amount,
                        note = successNote,
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
                            focusedBorderColor = YonoPurpleDark,
                            focusedLabelColor = YonoPurpleDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = ::confirmPayment,
                        enabled = amount.isNotBlank() && !isAuthenticating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = YonoOrange,
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
    onDone: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = YonoGreenSuccess.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(shape = CircleShape, color = YonoGreenSuccess.copy(alpha = 0.2f), modifier = Modifier.size(56.dp)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = YonoGreenSuccess,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Payment confirmed",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = YonoGreenSuccess
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
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onDone,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = YonoGreenSuccess, contentColor = Color.White)
            ) {
                Text("Done")
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
