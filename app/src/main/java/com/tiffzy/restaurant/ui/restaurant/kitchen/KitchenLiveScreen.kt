package com.tiffzy.restaurant.ui.restaurant.kitchen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun KitchenLiveScreen(
    navController: NavController,
    onLogout: () -> Unit,
    viewModel: KitchenViewModel = hiltViewModel()
) {
    val ordersState by viewModel.ordersState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("PLACED", "PREPARING", "READY", "DELIVERED")

    OwnerShell(
        title = "Kitchen Live",
        restaurantName = "Tiffzy Kitchen",
        navController = navController,
        onLogout = onLogout,
        actions = {
            IconButton(onClick = viewModel::loadLiveOrders) {
                Icon(Icons.Default.Refresh, "Refresh")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // Status Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = TiffzyOrange,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = TiffzyOrange
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            val count = (ordersState as? UiState.Success)?.data?.count { it.status.uppercase() == title } ?: 0
                            Text("$title ($count)")
                        }
                    )
                }
            }

            when (val state = ordersState) {
                is UiState.Loading -> TiffzyLoadingIndicator()
                is UiState.Error -> TiffzyErrorState(state.message, viewModel::loadLiveOrders)
                is UiState.Success -> {
                    val filteredOrders = state.data.filter { it.status.uppercase() == tabs[selectedTab] }
                    OrderList(
                        orders = filteredOrders,
                        onUpdateStatus = viewModel::updateStatus
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
fun OrderList(
    orders: List<OrderDetails>,
    onUpdateStatus: (Int, String) -> Unit
) {
    // Defensive check for potential null from API
    @Suppress("SENSELESS_COMPARISON")
    val list = if (orders == null) emptyList() else orders
    
    if (list.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No orders in this status", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(list) { order ->
                KitchenOrderCard(
                    order = order,
                    onUpdateStatus = { newStatus -> onUpdateStatus(order.id, newStatus) }
                )
            }
        }
    }
}

@Composable
fun KitchenOrderCard(
    order: OrderDetails,
    onUpdateStatus: (String) -> Unit
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
                Column {
                    Text(
                        text = "#${order.orderNo.takeLast(6)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (order.tableNo != null) "Table ${order.tableNo}" else "Takeaway / Online",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                
                // Elapsed Time Placeholder
                Surface(
                    color = Color.Red.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "12m",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = Color.Red,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.5f))
            
            // Items
            order.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "${item.qty} x ${item.itemName}", style = MaterialTheme.typography.bodyMedium)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Actions
            when (order.status.uppercase()) {
                "PLACED" -> {
                    Button(
                        onClick = { onUpdateStatus("PREPARING") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = TiffzyOrange)
                    ) {
                        Text("START PREPARING")
                    }
                }
                "PREPARING" -> {
                    Button(
                        onClick = { onUpdateStatus("READY") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("MARK AS READY")
                    }
                }
                "READY" -> {
                    Button(
                        onClick = { onUpdateStatus("DELIVERED") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                    ) {
                        Text("MARK AS DELIVERED")
                    }
                }
            }
        }
    }
}
