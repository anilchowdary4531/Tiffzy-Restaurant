package com.tiffzy.restaurant.ui.restaurant.supply

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.data.model.SupplyOrder
import com.tiffzy.restaurant.data.model.SupplyProduct
import com.tiffzy.restaurant.ui.components.TiffzyErrorState
import com.tiffzy.restaurant.ui.components.TiffzyLoadingIndicator
import com.tiffzy.restaurant.ui.restaurant.components.OwnerShell
import com.tiffzy.restaurant.ui.theme.TiffzyOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplyMarketplaceScreen(
    navController: NavController,
    onLogout: () -> Unit,
    viewModel: SupplyViewModel = hiltViewModel()
) {
    val productsState by viewModel.productsState.collectAsState()
    val ordersState by viewModel.ordersState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Browse Ingredients", "Track Orders")

    OwnerShell(
        title = "Supply Marketplace",
        restaurantName = "B2B Supplies",
        navController = navController,
        onLogout = onLogout
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = TiffzyOrange,
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
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> BrowseIngredients(productsState, viewModel::loadProducts)
                1 -> TrackSupplyOrders(ordersState, viewModel::loadOrders)
            }
        }
    }
}

@Composable
fun BrowseIngredients(
    state: UiState<List<SupplyProduct>>,
    onRefresh: () -> Unit
) {
    when (state) {
        is UiState.Loading -> TiffzyLoadingIndicator()
        is UiState.Error -> TiffzyErrorState(state.message, onRefresh)
        is UiState.Success -> {
            @Suppress("SENSELESS_COMPARISON")
            val list = if (state.data == null) emptyList() else state.data
            
            if (list.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No products available in your region.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(list) { product ->
                        ProductCard(product)
                    }
                }
            }
        }
        else -> {}
    }
}

@Composable
fun TrackSupplyOrders(
    state: UiState<List<SupplyOrder>>,
    onRefresh: () -> Unit
) {
    when (state) {
        is UiState.Loading -> TiffzyLoadingIndicator()
        is UiState.Error -> TiffzyErrorState(state.message, onRefresh)
        is UiState.Success -> {
            @Suppress("SENSELESS_COMPARISON")
            val list = if (state.data == null) emptyList() else state.data
            
            if (list.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("You haven't placed any supply orders yet.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(list) { order ->
                        SupplyOrderCard(order)
                    }
                }
            }
        }
        else -> {}
    }
}

@Composable
fun ProductCard(product: SupplyProduct) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(60.dp).background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Inventory, null, tint = Color.Gray)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.name, fontWeight = FontWeight.Bold)
                Text(text = "${product.supplierName} • ${product.category}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(text = "₹${product.price}/${product.unit}", color = TiffzyOrange, fontWeight = FontWeight.Bold)
            }
            Button(onClick = { /* Add to Cart */ }, contentPadding = PaddingValues(horizontal = 12.dp)) {
                Text("ADD")
            }
        }
    }
}

@Composable
fun SupplyOrderCard(order: SupplyOrder) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Order #${order.orderNo}", fontWeight = FontWeight.Bold)
                Text(text = order.status, color = TiffzyOrange, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "${order.itemsCount} Items", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text(text = "₹${order.totalAmount.toInt()}", fontWeight = FontWeight.Bold)
            }
        }
    }
}
