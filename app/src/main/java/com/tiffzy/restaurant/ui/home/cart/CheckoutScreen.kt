package com.tiffzy.restaurant.ui.home.cart

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.data.model.Address
import com.tiffzy.restaurant.data.model.OrderResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: CheckoutViewModel,
    onBack: () -> Unit,
    onOrderConfirmed: (String) -> Unit
) {
    val cart by viewModel.cart.collectAsState()
    val addresses by viewModel.addresses.collectAsState()
    val selectedAddress by viewModel.selectedAddress.collectAsState()
    val walletBalance by viewModel.walletBalance.collectAsState()
    val orderState by viewModel.orderState.collectAsState()
    val deliveryInstructions by viewModel.deliveryInstructions.collectAsState()
    val paymentMethod by viewModel.paymentMethod.collectAsState()
    val useWallet by viewModel.useWallet.collectAsState()

    LaunchedEffect(orderState) {
        if (orderState is UiState.Success) {
            onOrderConfirmed((orderState as UiState.Success<OrderResponse>).data.order.orderNo)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 16.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "₹${String.format("%.2f", cart.grandTotal)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(text = "TOTAL AMOUNT", style = MaterialTheme.typography.labelSmall)
                    }
                    Button(
                        onClick = { viewModel.placeOrder() },
                        modifier = Modifier.width(180.dp).height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        enabled = orderState !is UiState.Loading && selectedAddress != null
                    ) {
                        if (orderState is UiState.Loading) {
                            CircularProgressIndicator(size = 24.dp, color = Color.White)
                        } else {
                            Text("PLACE ORDER")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Delivery Address Section
            SectionHeader(title = "Delivery Address", icon = Icons.Default.LocationOn)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (selectedAddress != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (selectedAddress!!.label.lowercase() == "home") Icons.Default.Home else Icons.Default.Work,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = selectedAddress!!.label, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = selectedAddress!!.line1,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        TextButton(onClick = { /* Open address selection */ }, modifier = Modifier.padding(top = 8.dp)) {
                            Text("CHANGE ADDRESS")
                        }
                    } else {
                        Text("No address selected", color = Color.Red)
                        Button(onClick = { /* Navigate to add address */ }) {
                            Text("ADD ADDRESS")
                        }
                    }
                }
            }

            // Delivery Instructions
            SectionHeader(title = "Delivery Instructions", icon = Icons.Default.Description)
            OutlinedTextField(
                value = deliveryInstructions,
                onValueChange = { viewModel.deliveryInstructions.value = it },
                placeholder = { Text("e.g. Ring the bell, Leave at the gate...") },
                modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            // Order Summary
            SectionHeader(title = "Order Summary", icon = Icons.Default.Restaurant)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    cart.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "${item.quantity} x ${item.menuItem.name}", modifier = Modifier.weight(1f))
                            Text(text = "₹${item.totalPrice.toInt()}", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Wallet Section
            if (walletBalance > 0) {
                SectionHeader(title = "Tiffzy Wallet", icon = Icons.Default.AccountBalanceWallet)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Balance: ₹$walletBalance", fontWeight = FontWeight.Bold)
                            Text(text = "Use wallet for this order", style = MaterialTheme.typography.labelSmall)
                        }
                        Switch(
                            checked = useWallet,
                            onCheckedChange = { viewModel.useWallet.value = it }
                        )
                    }
                }
            }

            // Bill Details Section
            SectionHeader(title = "Bill Details", icon = Icons.Default.Receipt)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    BillRow("Item Total", "₹${cart.subtotal}")
                    if (cart.discount > 0) {
                        BillRow("Coupon Discount", "-₹${cart.discount}", color = Color(0xFF2E7D32))
                    }
                    BillRow("Delivery Charge", "₹${cart.deliveryCharge}")
                    BillRow("Packing Charge", "₹${cart.packingCharge}")
                    BillRow("GST (5%)", "₹${String.format("%.2f", cart.gstAmount)}")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    BillRow("To Pay", "₹${String.format("%.2f", cart.grandTotal)}", isBold = true)
                }
            }

            // Payment Selection
            SectionHeader(title = "Payment Method", icon = Icons.Default.Payment)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    PaymentMethodRow(
                        label = "Cash on Delivery",
                        icon = Icons.Default.Payments,
                        selected = paymentMethod == "cod",
                        onSelect = { viewModel.paymentMethod.value = "cod" }
                    )
                    PaymentMethodRow(
                        label = "Online Payment",
                        icon = Icons.Default.Public,
                        selected = paymentMethod == "online",
                        onSelect = { viewModel.paymentMethod.value = "online" }
                    )
                }
            }

            if (orderState is UiState.Error) {
                Text(
                    text = (orderState as UiState.Error).message,
                    color = Color.Red,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
