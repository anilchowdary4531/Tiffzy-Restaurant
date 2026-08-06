package com.tiffzy.restaurant.ui.home.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.data.model.OrderDetails
import com.tiffzy.restaurant.data.model.OrderGroup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderListScreen(
    viewModel: OrderViewModel,
    onOrderClick: (Int) -> Unit,
    onBack: () -> Unit
) {
    val ordersState by viewModel.ordersState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Orders") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF5F5F5))) {
            when (val state = ordersState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is UiState.Success -> {
                    if (state.data.groups.isEmpty()) {
                        EmptyOrdersState()
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            state.data.groups.forEach { group ->
                                item {
                                    Text(
                                        text = group.restaurant?.name ?: "Unknown Restaurant",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                                items(group.orders) { order ->
                                    OrderCard(
                                        order = order,
                                        onClick = { onOrderClick(order.id) },
                                        onReorder = { viewModel.reorder(order.id) { /* Handle navigation */ } }
                                    )
                                }
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    Text(state.message, modifier = Modifier.align(Alignment.Center), color = Color.Red)
                }
                else -> {}
            }
        }
    }
}

@Composable
fun OrderCard(
    order: OrderDetails,
    onClick: () -> Unit,
    onReorder: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Order #${order.orderNo.takeLast(6)}", fontWeight = FontWeight.Bold)
                    Text(text = order.createdAt, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                
                Surface(
                    color = getStatusColor(order.status).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = order.status.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = getStatusColor(order.status),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

            order.items.take(2).forEach { item ->
                Text(
                    text = "${item.qty} x ${item.itemName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
            if (order.items.size > 2) {
                Text(
                    text = "+ ${order.items.size - 2} more items",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "₹${order.total.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                
                OutlinedButton(
                    onClick = onReorder,
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("REORDER", fontSize = 12.sp)
                }
            }
        }
    }
}

fun getStatusColor(status: String): Color {
    return when (status.lowercase()) {
        "placed" -> Color(0xFF2196F3)
        "confirmed", "preparing" -> Color(0xFFFF9800)
        "out_for_delivery" -> Color(0xFF9C27B0)
        "delivered" -> Color(0xFF4CAF50)
        "cancelled" -> Color(0xFFF44336)
        else -> Color.Gray
    }
}

@Composable
fun EmptyOrdersState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.History, null, modifier = Modifier.size(100.dp), tint = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text("No orders yet", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Your food journey hasn't started yet!", color = Color.Gray)
    }
}
