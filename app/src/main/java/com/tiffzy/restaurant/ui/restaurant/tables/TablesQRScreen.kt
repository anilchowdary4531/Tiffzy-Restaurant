package com.tiffzy.restaurant.ui.restaurant.tables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.data.model.TableData
import com.tiffzy.restaurant.data.model.TableSection
import com.tiffzy.restaurant.ui.components.TiffzyErrorState
import com.tiffzy.restaurant.ui.components.TiffzyLoadingIndicator
import com.tiffzy.restaurant.ui.restaurant.components.OwnerShell
import com.tiffzy.restaurant.ui.theme.TiffzyOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TablesQRScreen(
    navController: NavController,
    onLogout: () -> Unit,
    viewModel: TableViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedSectionId by viewModel.selectedSectionId.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    OwnerShell(
        title = "Tables & QR",
        restaurantName = "Tiffzy Tables",
        navController = navController,
        onLogout = onLogout,
        actions = {
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "Add Table")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // Search and Section Filter
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = { Text("Search table number...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(12.dp)
            )

            when (val state = uiState) {
                is UiState.Loading -> TiffzyLoadingIndicator()
                is UiState.Error -> TiffzyErrorState(state.message, viewModel::loadTables)
                is UiState.Success -> {
                    TableContent(
                        sections = state.data,
                        selectedSectionId = selectedSectionId,
                        onSectionSelect = viewModel::filterBySection,
                        searchQuery = searchQuery
                    )
                }
                else -> {}
            }
        }

        if (showAddDialog) {
            AddTableDialog(
                sections = (uiState as? UiState.Success)?.data ?: emptyList(),
                onDismiss = { showAddDialog = false },
                onConfirm = { number, sectionId, seats ->
                    viewModel.addTable(number, sectionId, seats)
                    showAddDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTableDialog(
    sections: List<TableSection>,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, Int) -> Unit
) {
    var number by remember { mutableStateOf("") }
    var seats by remember { mutableStateOf("2") }
    var selectedSectionId by remember { mutableStateOf(sections.firstOrNull()?.id ?: 0) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Table") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = number,
                    onValueChange = { number = it },
                    label = { Text("Table Number / Name") },
                    placeholder = { Text("e.g. 101 or VIP-1") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = seats,
                    onValueChange = { seats = it },
                    label = { Text("Seat Capacity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                if (sections.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = sections.find { it.id == selectedSectionId }?.name ?: "Select Section",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Section") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            sections.forEach { section ->
                                DropdownMenuItem(
                                    text = { Text(section.name) },
                                    onClick = {
                                        selectedSectionId = section.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(number, selectedSectionId, seats.toIntOrNull() ?: 2)
                },
                enabled = number.isNotBlank() && selectedSectionId != 0,
                colors = ButtonDefaults.buttonColors(containerColor = TiffzyOrange)
            ) {
                Text("ADD TABLE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL") }
        }
    )
}

@Composable
fun TableContent(
    sections: List<TableSection>,
    selectedSectionId: Int?,
    onSectionSelect: (Int?) -> Unit,
    searchQuery: String
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedSectionId == null,
                    onClick = { onSectionSelect(null) },
                    label = { Text("All Sections") }
                )
            }
            items(sections) { section ->
                FilterChip(
                    selected = selectedSectionId == section.id,
                    onClick = { onSectionSelect(section.id) },
                    label = { Text(section.name) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val displaySections = if (selectedSectionId != null) {
                sections.filter { it.id == selectedSectionId }
            } else {
                sections
            }

            items(displaySections) { section ->
                val filteredTables = section.tables.filter {
                    searchQuery.isEmpty() || it.number.contains(searchQuery, ignoreCase = true)
                }
                
                if (filteredTables.isNotEmpty()) {
                    Text(
                        text = section.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    filteredTables.chunked(2).forEach { rowTables ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowTables.forEach { table ->
                                TableQRCard(
                                    table = table,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowTables.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TableQRCard(
    table: TableData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Table ${table.number}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // QR Code Placeholder or Image
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = Color.White,
                border = BorderStroke(1.dp, Color.LightGray)
            ) {
                if (table.qrCodeUrl != null) {
                    AsyncImage(
                        model = table.qrCodeUrl,
                        contentDescription = "QR Code",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.QrCode, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${table.seats} Seats", style = MaterialTheme.typography.labelSmall)
                IconButton(onClick = { /* TODO: Share QR */ }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Share, null, tint = TiffzyOrange, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
