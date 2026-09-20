package com.tiffzy.restaurant.ui.restaurant.paylater

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.data.model.PayLaterAccount
import com.tiffzy.restaurant.ui.components.TiffzyErrorState
import com.tiffzy.restaurant.ui.components.TiffzyLoadingIndicator
import com.tiffzy.restaurant.ui.restaurant.components.OwnerShell
import com.tiffzy.restaurant.ui.theme.TiffzyOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayLaterScreen(
    navController: NavController,
    onLogout: () -> Unit,
    viewModel: PayLaterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    OwnerShell(
        title = "Pay Later (Khata)",
        restaurantName = "Customer Credit",
        navController = navController,
        onLogout = onLogout,
        actions = {
            IconButton(onClick = { /* TODO: Add Customer */ }) {
                Icon(Icons.Default.PersonAdd, "Add Customer")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = { Text("Search by name or phone...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(12.dp)
            )

            when (val state = uiState) {
                is UiState.Loading -> TiffzyLoadingIndicator()
                is UiState.Error -> TiffzyErrorState(state.message, viewModel::loadAccounts)
                is UiState.Success -> {
                    val filtered = state.data.filter {
                        searchQuery.isEmpty() || 
                        it.customerName.contains(searchQuery, ignoreCase = true) ||
                        it.customerPhone.contains(searchQuery)
                    }
                    PayLaterContent(filtered)
                }
                else -> {}
            }
        }
    }
}

@Composable
fun PayLaterContent(accounts: List<PayLaterAccount>) {
    val totalOutstanding = accounts.sumOf { it.outstandingBalance }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Summary Card
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Total Outstanding", style = MaterialTheme.typography.labelSmall)
                    Text(text = "₹${totalOutstanding.toInt()}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Customers", style = MaterialTheme.typography.labelSmall)
                    Text(text = "${accounts.size}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(accounts) { account ->
                AccountCard(account)
            }
        }
    }
}

@Composable
fun AccountCard(account: PayLaterAccount) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = account.customerName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(text = account.customerPhone, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (account.outstandingBalance / account.limit).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                    color = if (account.outstandingBalance > account.limit * 0.8) Color.Red else TiffzyOrange,
                    trackColor = Color.LightGray.copy(alpha = 0.3f)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "₹${account.outstandingBalance.toInt()}", fontWeight = FontWeight.Black, color = Color.Red)
                Text(text = "Limit ₹${account.limit.toInt()}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}
