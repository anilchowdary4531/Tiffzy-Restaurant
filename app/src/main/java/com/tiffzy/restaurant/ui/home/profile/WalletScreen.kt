package com.tiffzy.restaurant.ui.home.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiffzy.restaurant.MainActivity
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.ui.home.payment.PaymentStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: WalletViewModel,
    onBack: () -> Unit
) {
    val walletState by viewModel.walletState.collectAsState()
    val rechargeStatus by viewModel.rechargeStatus.collectAsState()
    var showRechargeDialog by remember { mutableStateOf(false) }
    var amountText by remember { mutableStateOf("") }
    val context = LocalContext.current as MainActivity

    LaunchedEffect(rechargeStatus) {
        if (rechargeStatus is PaymentStatus.PaymentInitiated) {
            MainActivity.onPaymentResult = { success, _ ->
                viewModel.onPaymentResult(success)
            }
            context.launchPhonePe((rechargeStatus as PaymentStatus.PaymentInitiated).intent)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tiffzy Wallet") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF5F5F5))) {
            when (val state = walletState) {
                is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is UiState.Success -> {
                    Column {
                        // Balance Card
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text("Available Balance", color = Color.White.copy(alpha = 0.7f))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "₹${state.data.balance.toInt()}",
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Button(
                                        onClick = { showRechargeDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("ADD MONEY", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        
                        // Quick Stats / Info
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            InfoBadge("Instant Refunds", Color(0xFFE3F2FD), Color(0xFF1976D2))
                            InfoBadge("Safe Payments", Color(0xFFE8F5E9), Color(0xFF2E7D32))
                        }

                        Text(
                            "Transaction History",
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        if (state.data.transactions.isEmpty()) {
                            EmptyHistory()
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.data.transactions) { tx ->
                                    TransactionItem(tx)
                                }
                            }
                        }
                    }
                }
                is UiState.Error -> Text(state.message, modifier = Modifier.align(Alignment.Center))
                else -> {}
            }

            if (rechargeStatus is PaymentStatus.Verifying || rechargeStatus is PaymentStatus.Processing) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.5f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Card {
                            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(Modifier.height(16.dp))
                                Text(if (rechargeStatus is PaymentStatus.Verifying) "Verifying Recharge..." else "Connecting to PhonePe...")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRechargeDialog) {
        AlertDialog(
            onDismissRequest = { showRechargeDialog = false },
            title = { Text("Recharge Wallet") },
            text = {
                Column {
                    Text("Enter amount to add to your Tiffzy Wallet", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount") },
                        prefix = { Text("₹") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("100", "500", "1000").forEach { quickAmount ->
                            OutlinedButton(onClick = { amountText = quickAmount }) {
                                Text("₹$quickAmount")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull()
                        if (amt != null && amt > 0) {
                            viewModel.initiateRecharge(amt)
                            showRechargeDialog = false
                        }
                    },
                    enabled = amountText.isNotEmpty()
                ) {
                    Text("PROCEED")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRechargeDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (rechargeStatus is PaymentStatus.Success) {
        AlertDialog(
            onDismissRequest = { viewModel.resetRechargeStatus() },
            title = { Text("Recharge Successful") },
            text = { Text("₹$amountText has been added to your Tiffzy Wallet.") },
            confirmButton = {
                Button(onClick = { viewModel.resetRechargeStatus() }) { Text("OK") }
            }
        )
    }
}

@Composable
fun TransactionItem(tx: com.tiffzy.restaurant.data.model.WalletTransaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = tx.description, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(text = tx.createdAt, fontSize = 11.sp, color = Color.Gray)
            }
            Text(
                text = "${if (tx.type == "credit") "+" else "-"} ₹${tx.amount.toInt()}",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = if (tx.type == "credit") Color(0xFF4CAF50) else Color.Red
            )
        }
    }
}

@Composable
fun InfoBadge(text: String, bgColor: Color, textColor: Color) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun EmptyHistory() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.History, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
        Spacer(Modifier.height(16.dp))
        Text("No transactions yet", color = Color.Gray)
    }
}
