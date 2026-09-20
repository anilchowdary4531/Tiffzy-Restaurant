package com.tiffzy.restaurant.ui.restaurant.orders

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.data.model.OrderDetails
import com.tiffzy.restaurant.ui.components.TiffzyErrorState
import com.tiffzy.restaurant.ui.components.TiffzyLoadingIndicator
import com.tiffzy.restaurant.ui.restaurant.components.OwnerShell
import com.tiffzy.restaurant.ui.theme.TiffzyOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineOrdersScreen(
    navController: NavController,
    onLogout: () -> Unit,
    viewModel: OnlineOrdersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    OwnerShell(
        title = "Online Orders",
        restaurantName = "Tiffzy Delivery",
        navController = navController,
        onLogout = onLogout,
        actions = {
            IconButton(onClick = viewModel::loadOnlineOrders) {
                Icon(Icons.Default.Refresh, "Refresh")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = { Text("Search by Order ID or Customer...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(12.dp)
            )

            when (val state = uiState) {
                is UiState.Loading -> TiffzyLoadingIndicator()
                is UiState.Error -> TiffzyErrorState(state.message, viewModel::loadOnlineOrders)
                is UiState.Success -> {
                    val filtered = state.data.filter {
                        searchQuery.isEmpty() || 
                        it.orderNo.contains(searchQuery, ignoreCase = true) ||
                        (it.customerName?.contains(searchQuery, ignoreCase = true) == true)
                    }
                    OnlineOrderList(filtered, viewModel::updateStatus)
                }
                else -> {}
            }
        }
    }
}

@Composable
fun OnlineOrderList(
    orders: List<OrderDetails>,
    onUpdateStatus: (Int, String) -> Unit
) {
    // Defensive check for potential null from API
    @Suppress("SENSELESS_COMPARISON")
    val list = if (orders == null) emptyList() else orders
    
    if (list.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No online orders found", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(list) { order ->
                OnlineOrderCard(order, onUpdateStatus)
            }
        }
    }
}

@Composable
fun OnlineOrderCard(
    order: OrderDetails,
    onUpdateStatus: (Int, String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order #${order.orderNo.takeLast(6)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Badge(
                    containerColor = TiffzyOrange.copy(alpha = 0.1f),
                    contentColor = TiffzyOrange
                ) {
                    Text(order.status, modifier = Modifier.padding(horizontal = 4.dp))
                }
            }
            
            Text(
                text = order.customerName ?: "Guest Customer",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = order.phone ?: "No phone",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.5f))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "${order.items.size} Items", style = MaterialTheme.typography.bodySmall)
                Text(text = "Total: ₹${order.total.toInt()}", fontWeight = FontWeight.Bold)
            }
            
            if (order.status.uppercase() == "PLACED") {
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { onUpdateStatus(order.id, "CANCELLED") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                    ) {
                        Text("REJECT")
                    }
                    Button(
                        onClick = { onUpdateStatus(order.id, "ACCEPTED") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("ACCEPT")
                    }
                }
            }
        }
    }
}
