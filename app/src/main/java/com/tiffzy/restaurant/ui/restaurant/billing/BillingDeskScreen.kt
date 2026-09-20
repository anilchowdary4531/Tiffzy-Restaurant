package com.tiffzy.restaurant.ui.restaurant.billing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.tiffzy.restaurant.data.model.MenuItem
import com.tiffzy.restaurant.ui.restaurant.components.OwnerShell
import com.tiffzy.restaurant.ui.theme.TiffzyOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingDeskScreen(
    navController: NavController,
    onLogout: () -> Unit,
    viewModel: BillingViewModel = hiltViewModel()
) {
    val menuItems by viewModel.menuItems.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val bills by viewModel.bills.collectAsState()
    val activeBillIndex by viewModel.activeBillIndex.collectAsState()
    val activeBill by viewModel.activeBill.collectAsState()
    
    var showCartSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // Defensive check for potential null from API
    @Suppress("SENSELESS_COMPARISON")
    val itemsList = if (menuItems == null) emptyList() else menuItems

    val filteredItems = itemsList.filter {
        (selectedCategory == null || it.category == selectedCategory) &&
        (searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true))
    }

    OwnerShell(
        title = "Billing Desk",
        restaurantName = "Tiffzy Billing",
        navController = navController,
        onLogout = onLogout
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search and Tabs
                BillingHeader(
                    searchQuery = searchQuery,
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    bills = bills,
                    activeIndex = activeBillIndex,
                    onBillSwitch = viewModel::switchBill,
                    onNewBill = viewModel::createNewBill
                )

                // Categories
                CategoryFilter(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelect = viewModel::selectCategory
                )

                // Menu Items
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredItems) { item ->
                        BillingItemCard(
                            item = item,
                            onClick = { viewModel.addItemToBill(item) }
                        )
                    }
                }
                
                // Buffer for Cart Summary
                Spacer(modifier = Modifier.height(72.dp))
            }

            // Floating Cart Summary
            CartSummary(
                activeBill = activeBill,
                onClick = { showCartSheet = true },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        if (showCartSheet) {
            ModalBottomSheet(
                onDismissRequest = { showCartSheet = false },
                sheetState = sheetState
            ) {
                BillingCartContent(
                    bill = activeBill,
                    onUpdateQty = viewModel::updateQuantity,
                    onClear = viewModel::clearCart,
                    onDone = { 
                        showCartSheet = false 
                    }
                )
            }
        }
    }
}

@Composable
fun BillingCartContent(
    bill: BillDraft,
    onUpdateQty: (MenuItem, Int) -> Unit,
    onClear: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .fillMaxHeight(0.7f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Current Bill", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            TextButton(onClick = onClear) {
                Text("CLEAR ALL", color = Color.Red)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(bill.items) { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = item.menuItem.name, fontWeight = FontWeight.Bold)
                        Text(text = "₹${item.menuItem.price.toInt()}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onUpdateQty(item.menuItem, -1) }) {
                            Icon(Icons.Default.RemoveCircleOutline, null, tint = TiffzyOrange)
                        }
                        Text(text = "${item.quantity}", modifier = Modifier.padding(horizontal = 8.dp))
                        IconButton(onClick = { onUpdateQty(item.menuItem, 1) }) {
                            Icon(Icons.Default.AddCircleOutline, null, tint = TiffzyOrange)
                        }
                    }
                    Text(
                        text = "₹${item.total.toInt()}",
                        modifier = Modifier.width(60.dp),
                        textAlign = TextAlign.End,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Grand Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = "₹${bill.items.sumOf { it.total }.toInt()}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = TiffzyOrange
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TiffzyOrange)
        ) {
            Text("GENERATE BILL / KOT")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun BillingHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    bills: List<BillDraft>,
    activeIndex: Int,
    onBillSwitch: (Int) -> Unit,
    onNewBill: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search menu items...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(12.dp)
        )

        ScrollableTabRow(
            selectedTabIndex = activeIndex,
            edgePadding = 16.dp,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeIndex]),
                    color = TiffzyOrange
                )
            },
            containerColor = Color.Transparent
        ) {
            bills.forEachIndexed { index, bill ->
                Tab(
                    selected = activeIndex == index,
                    onClick = { onBillSwitch(index) },
                    text = {
                        Text(
                            text = "Bill ${bill.id}",
                            fontWeight = if (activeIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
            IconButton(onClick = onNewBill) {
                Icon(Icons.Default.Add, "New Bill", tint = TiffzyOrange)
            }
        }
    }
}

@Composable
fun CategoryFilter(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelect: (String?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelect(null) },
                label = { Text("All") }
            )
        }
        items(categories) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelect(category) },
                label = { Text(category) }
            )
        }
    }
}

@Composable
fun BillingItemCard(
    item: MenuItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                AsyncImage(
                    model = item.image,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = CircleShape
                ) {
                    Text(
                        text = "₹${item.price.toInt()}",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun CartSummary(
    activeBill: BillDraft,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (activeBill.items.isEmpty()) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = TiffzyOrange,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "${activeBill.items.sumOf { it.quantity }} Items",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = "₹${activeBill.items.sumOf { it.total }.toInt()}",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "VIEW CART", color = Color.White, fontWeight = FontWeight.Bold)
                Icon(Icons.AutoMirrored.Filled.ArrowRight, null, tint = Color.White)
            }
        }
    }
}
