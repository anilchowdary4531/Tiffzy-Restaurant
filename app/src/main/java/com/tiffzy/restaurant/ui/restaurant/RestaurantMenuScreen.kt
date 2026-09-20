package com.tiffzy.restaurant.ui.restaurant

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.tiffzy.restaurant.data.model.MenuItem
import com.tiffzy.restaurant.ui.components.TiffzyErrorState
import com.tiffzy.restaurant.ui.components.TiffzyLoadingIndicator
import com.tiffzy.restaurant.ui.restaurant.components.OwnerShell
import com.tiffzy.restaurant.ui.theme.TiffzyOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantMenuScreen(
    navController: NavController,
    onAddItem: () -> Unit,
    onEditItem: (MenuItem) -> Unit,
    onLogout: () -> Unit,
    viewModel: RestaurantMenuViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    OwnerShell(
        title = "Menu Studio",
        restaurantName = "Tiffzy Menu",
        navController = navController,
        onLogout = onLogout,
        actions = {
            IconButton(onClick = onAddItem) {
                Icon(Icons.Default.Add, "Add Item")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search dishes...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(12.dp)
            )

            when (val state = uiState) {
                is MenuUiState.Loading -> TiffzyLoadingIndicator()
                is MenuUiState.Error -> TiffzyErrorState(state.message, viewModel::loadMenu)
                is MenuUiState.Success -> {
                    val categories = state.menu.map { it.category }.distinct()
                    
                    // Category Chips
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null },
                                label = { Text("All") }
                            )
                        }
                        items(categories) { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                label = { Text(category) }
                            )
                        }
                    }

                    val filteredMenu = state.menu.filter {
                        (selectedCategory == null || it.category == selectedCategory) &&
                        (searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true))
                    }

                    MenuContent(
                        menuItems = filteredMenu,
                        onEditItem = onEditItem,
                        onToggleAvailability = viewModel::toggleAvailability,
                        onDeleteItem = viewModel::deleteMenuItem
                    )
                }
            }
        }
    }
}

@Composable
fun MenuContent(
    menuItems: List<MenuItem>,
    onEditItem: (MenuItem) -> Unit,
    onToggleAvailability: (MenuItem) -> Unit,
    onDeleteItem: (Int) -> Unit
) {
    if (menuItems.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No items found", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(menuItems) { item ->
                MenuEntryCard(
                    item = item,
                    onEdit = { onEditItem(item) },
                    onToggle = { onToggleAvailability(item) },
                    onDelete = { onDeleteItem(item.id) }
                )
            }
        }
    }
}

@Composable
fun MenuEntryCard(
    item: MenuItem,
    onEdit: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.image,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(text = "₹${item.price.toInt()}", color = TiffzyOrange, fontWeight = FontWeight.Bold)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = item.isAvailable,
                        onCheckedChange = { onToggle() },
                        modifier = Modifier.graphicsLayer(scaleX = 0.7f, scaleY = 0.7f)
                    )
                    Text(
                        text = if (item.isAvailable) "In Stock" else "Sold Out",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (item.isAvailable) Color(0xFF4CAF50) else Color.Red
                    )
                }
            }
            
            Column {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Edit", tint = Color.Gray)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete", tint = Color.Red.copy(alpha = 0.6f))
                }
            }
        }
    }
}


