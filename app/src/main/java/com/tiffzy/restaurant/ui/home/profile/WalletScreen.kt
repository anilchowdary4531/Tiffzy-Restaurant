package com.tiffzy.restaurant.ui.home.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiffzy.restaurant.core.base.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit
) {
    val walletState by viewModel.walletState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchWalletHistory()
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
                                Text("Current Balance", color = Color.White.copy(alpha = 0.7f))
                                Text(
                                    "₹${state.data.balance.toInt()}",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                        
                        Text(
                            "Transaction History",
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Bold
                        )
                        
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.data.transactions) { tx ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(tx.description, fontWeight = FontWeight.SemiBold)
                                            Text(tx.createdAt, fontSize = 12.sp, color = Color.Gray)
                                        }
                                        Text(
                                            text = "${if (tx.type == "credit") "+" else "-"} ₹${tx.amount.toInt()}",
                                            fontWeight = FontWeight.Bold,
                                            color = if (tx.type == "credit") Color(0xFF4CAF50) else Color.Red
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                is UiState.Error -> Text(state.message, modifier = Modifier.align(Alignment.Center))
                else -> {}
            }
        }
    }
}
