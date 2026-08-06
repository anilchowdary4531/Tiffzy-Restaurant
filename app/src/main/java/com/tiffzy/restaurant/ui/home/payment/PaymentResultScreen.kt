package com.tiffzy.restaurant.ui.home.payment

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PaymentResultScreen(
    status: PaymentStatus,
    onContinue: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (status) {
            is PaymentStatus.Success -> {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Payment Successful!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your order #${status.orderNo} has been placed successfully.",
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(48.dp))
                Button(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("VIEW ORDER DETAILS")
                }
            }
            is PaymentStatus.Failure -> {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Payment Failed",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = status.message,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(48.dp))
                Button(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("RETRY PAYMENT")
                }
                TextButton(onClick = onContinue) {
                    Text("GO BACK TO CART")
                }
            }
            is PaymentStatus.Pending -> {
                Icon(
                    Icons.Default.Pending,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = Color(0xFFFFB300)
                )
                // ... same layout as success/failure ...
            }
            is PaymentStatus.Verifying -> {
                CircularProgressIndicator(modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(24.dp))
                Text("Verifying Payment...", fontWeight = FontWeight.Bold)
                Text("Please do not close the app or press back.", color = Color.Gray, fontSize = 12.sp)
            }
            is PaymentStatus.Processing -> {
                CircularProgressIndicator(modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(24.dp))
                Text("Initiating PhonePe...", fontWeight = FontWeight.Bold)
            }
            else -> {}
        }
    }
}
