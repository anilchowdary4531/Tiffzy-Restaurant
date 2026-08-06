package com.tiffzy.restaurant.ui.home.address

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.data.model.Address

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressListScreen(
    viewModel: AddressViewModel,
    onBack: () -> Unit,
    onAddAddress: () -> Unit
) {
    val addressesState by viewModel.addressesState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Addresses") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddAddress,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Add New Address") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = addressesState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        EmptyAddressState()
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.data) { address ->
                                AddressItem(
                                    address = address,
                                    onDelete = { viewModel.deleteAddress(address.id) },
                                    onSetDefault = { viewModel.setDefaultAddress(address.id) }
                                )
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
fun AddressItem(
    address: Address,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when(address.label.lowercase()) {
                            "home" -> Icons.Default.Home
                            "work" -> Icons.Default.Work
                            else -> Icons.Default.LocationOn
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = address.label, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (address.isDefault) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "DEFAULT",
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
                
                Row {
                    IconButton(onClick = onSetDefault, enabled = !address.isDefault) {
                        Icon(Icons.Default.CheckCircle, null, tint = if (address.isDefault) Color.Green else Color.Gray)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, null, tint = Color.Red)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = address.line1, style = MaterialTheme.typography.bodyMedium)
            if (!address.line2.isNullOrEmpty()) {
                Text(text = address.line2, style = MaterialTheme.typography.bodySmall)
            }
            Text(text = "${address.city}, ${address.state}", style = MaterialTheme.typography.bodySmall)
            
            if (!address.phone.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = address.phone, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun EmptyAddressState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.LocationOff, null, modifier = Modifier.size(100.dp), tint = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text("No addresses saved", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Add an address to start ordering", color = Color.Gray)
    }
}
