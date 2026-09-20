package com.tiffzy.restaurant.ui.restaurant

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tiffzy.restaurant.data.model.OrderDetails
import com.tiffzy.restaurant.ui.components.TiffzyEmptyState
import com.tiffzy.restaurant.ui.components.TiffzyErrorState
import com.tiffzy.restaurant.ui.components.TiffzyLoadingIndicator
import com.tiffzy.restaurant.ui.restaurant.components.OwnerShell
import com.tiffzy.restaurant.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantOrderHistoryScreen(
    navController: NavController,
    onOrderClick: (Int) -> Unit,
    onLogout: () -> Unit,
    viewModel: RestaurantOrderHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentFilter by viewModel.currentStatusFilter.collectAsState()

    val filters = listOf(
        "ALL" to null,
        "DELIVERED" to "DELIVERED",
        "CANCELLED" to "CANCELLED",
        "PLACED" to "PLACED"
    )

    LaunchedEffect(Unit) {
        viewModel.loadHistory()
    }

    OwnerShell(
        title = "Order History",
        restaurantName = "Archive",
        navController = navController,
        onLogout = onLogout,
        actions = {
            IconButton(onClick = { viewModel.loadHistory(status = currentFilter) }) {
                Icon(Icons.Default.Refresh, "Refresh")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            // Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = Dimens.PaddingLarge, vertical = Dimens.PaddingSmall),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { (label, value) ->
                    FilterChip(
                        selected = currentFilter == value,
                        onClick = { viewModel.loadHistory(status = value) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            when (val state = uiState) {
                is OrderHistoryUiState.Loading -> TiffzyLoadingIndicator()
                is OrderHistoryUiState.Error -> TiffzyErrorState(
                    message = state.message,
                    onRetry = { viewModel.loadHistory(status = currentFilter) }
                )
                is OrderHistoryUiState.Success -> {
                    HistoryList(
                        orders = state.orders,
                        onOrderClick = onOrderClick
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryList(
    orders: List<OrderDetails>,
    onOrderClick: (Int) -> Unit
) {
    if (orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            TiffzyEmptyState(message = "No matching orders found")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Dimens.PaddingLarge),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMedium)
        ) {
            items(orders, key = { it.id }) { order ->
                RestaurantOrderCard(
                    order = order,
                    onClick = { onOrderClick(order.id) }
                )
            }
        }
    }
}
