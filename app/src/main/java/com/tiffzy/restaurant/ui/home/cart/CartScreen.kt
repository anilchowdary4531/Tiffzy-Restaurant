package com.tiffzy.restaurant.ui.home.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.tiffzy.restaurant.data.model.CartItem
import com.tiffzy.restaurant.ui.home.details.QuantitySelector
import com.tiffzy.restaurant.ui.home.details.VegNonVegIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: CartViewModel,
    onBack: () -> Unit,
    onNavigateToCheckout: () -> Unit
) {
    val cart by viewModel.cart.collectAsState()
    val couponState by viewModel.couponState.collectAsState()
    var couponCode by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("My Cart", style = MaterialTheme.typography.titleMedium)
                        if (cart.items.isNotEmpty()) {
                            Text(
                                text = cart.restaurantName ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            if (cart.items.isNotEmpty()) {
                BottomCheckoutBar(
                    total = cart.grandTotal,
                    onCheckout = onNavigateToCheckout
                )
            }
        }
    ) { padding ->
        if (cart.items.isEmpty()) {
            EmptyCartScreen(onBack = onBack)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF5F5F5)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cart Items Section
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            cart.items.forEachIndexed { index, item ->
                                CartItemRow(
                                    item = item,
                                    onAdd = { viewModel.addItem(item) },
                                    onRemove = { viewModel.removeItem(item) }
                                )
                                if (index < cart.items.size - 1) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }

                // Coupon Section
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ConfirmationNumber, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Apply Coupon", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            if (cart.appliedCoupon == null) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = couponCode,
                                        onValueChange = { couponCode = it },
                                        placeholder = { Text("Enter coupon code") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { viewModel.applyCoupon(couponCode) },
                                        modifier = Modifier.height(56.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        enabled = couponState !is UiState.Loading && couponCode.isNotEmpty()
                                    ) {
                                        if (couponState is UiState.Loading) {
                                            CircularProgressIndicator(size = 20.dp, color = Color.White)
                                        } else {
                                            Text("APPLY")
                                        }
                                    }
                                }
                                if (couponState is UiState.Error) {
                                    Text(
                                        text = (couponState as UiState.Error).message,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "'${cart.appliedCoupon?.code}' applied!",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32)
                                        )
                                        Text(
                                            text = "You saved ₹${cart.appliedCoupon?.discountAmount}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                    TextButton(onClick = { viewModel.removeCoupon() }) {
                                        Text("REMOVE", color = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }

                // Bill Details Section
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Bill Details", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(12.dp))
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
                }
                
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            VegNonVegIndicator(isVeg = item.menuItem.isVeg)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = item.menuItem.name, fontWeight = FontWeight.SemiBold)
                if (item.selectedVariant != null) {
                    Text(
                        text = item.selectedVariant.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (item.selectedAddOns.isNotEmpty()) {
                    Text(
                        text = item.selectedAddOns.joinToString { it.name },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            QuantitySelector(
                quantity = item.quantity,
                onAdd = onAdd,
                onRemove = onRemove
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "₹${item.totalPrice.toInt()}",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(60.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }
    }
}

@Composable
fun BillRow(label: String, value: String, color: Color = Color.Unspecified, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = if (isBold) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(text = value, style = if (isBold) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal, color = color)
    }
}

@Composable
fun BottomCheckoutBar(total: Double, onCheckout: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 16.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "₹${String.format("%.2f", total)}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(text = "VIEW DETAILED BILL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            Button(
                onClick = onCheckout,
                modifier = Modifier
                    .width(180.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("CHECKOUT")
            }
        }
    }
}

@Composable
fun EmptyCartScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(100.dp), tint = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Your cart is empty", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Add some items from the menu to get started", color = Color.Gray)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onBack) {
            Text("BROWSE RESTAURANTS")
        }
    }
}

@Composable
fun CircularProgressIndicator(size: androidx.compose.ui.unit.Dp, color: Color) {
    androidx.compose.material3.CircularProgressIndicator(
        modifier = Modifier.size(size),
        color = color,
        strokeWidth = 2.dp
    )
}
