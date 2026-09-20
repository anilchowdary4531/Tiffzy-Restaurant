package com.tiffzy.restaurant.ui.restaurant

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tiffzy.restaurant.data.model.AnalyticsResponse
import com.tiffzy.restaurant.data.model.RestaurantSettings
import com.tiffzy.restaurant.data.model.TableSection
import com.tiffzy.restaurant.navigation.Screen
import com.tiffzy.restaurant.ui.components.TiffzyErrorState
import com.tiffzy.restaurant.ui.components.TiffzyLoadingIndicator
import com.tiffzy.restaurant.ui.restaurant.components.OwnerShell
import com.tiffzy.restaurant.ui.restaurant.components.TableSectionView
import com.tiffzy.restaurant.ui.restaurant.components.TableStatusLegend
import com.tiffzy.restaurant.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDashboardScreen(
    navController: NavController,
    onLogout: () -> Unit,
    viewModel: RestaurantDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    OwnerShell(
        title = "Dashboard",
        restaurantName = if (uiState is DashboardUiState.Success) (uiState as DashboardUiState.Success).settings.name else "Tiffzy Owner",
        navController = navController,
        onLogout = { viewModel.logout(onLogout) }
    ) { innerPadding ->
        when (val state = uiState) {
            is DashboardUiState.Loading -> TiffzyLoadingIndicator()
            is DashboardUiState.Error -> TiffzyErrorState(
                message = state.message,
                onRetry = { viewModel.loadDashboard() },
                modifier = Modifier.padding(innerPadding)
            )
            is DashboardUiState.Success -> {
                DashboardContent(
                    analytics = state.analytics,
                    settings = state.settings,
                    tableSections = state.tableSections,
                    onToggleStatus = { viewModel.toggleRestaurantStatus(state.settings.isActive) },
                    onNavigate = { screen -> navController.navigate(screen.route) },
                    modifier = Modifier.fillMaxSize().padding(innerPadding)
                )
            }
            else -> {}
        }
    }
}

@Composable
fun DashboardContent(
    analytics: AnalyticsResponse,
    settings: RestaurantSettings,
    tableSections: List<TableSection>,
    onToggleStatus: () -> Unit,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(Dimens.PaddingLarge)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Today's Overview",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp
                )
                Text(
                    text = settings.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black
                )
            }
            
            StatusIndicator(
                isOpen = settings.isActive,
                onClick = onToggleStatus
            )
        }
        
        Spacer(modifier = Modifier.height(Dimens.PaddingLarge))

        // Sales Summary
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMedium)) {
            StatCard(
                label = "Revenue",
                value = "₹${analytics.overview.totalRevenue.toInt()}",
                icon = Icons.Default.CurrencyRupee,
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
            StatCard(
                label = "Orders",
                value = analytics.overview.totalOrders.toString(),
                icon = Icons.Default.ShoppingBag,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(Dimens.PaddingLarge))

        // Table Summary
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMedium)) {
            MiniStat(label = "Total Tables", value = analytics.realtime.totalTables.toString(), color = Color.Gray, modifier = Modifier.weight(1f))
            MiniStat(label = "Occupied", value = analytics.realtime.activeTables.toString(), color = Color(0xFFFC8019), modifier = Modifier.weight(1f))
            MiniStat(label = "Free", value = (analytics.realtime.totalTables - analytics.realtime.activeTables).toString(), color = Color(0xFF4CAF50), modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(Dimens.PaddingLarge))

        // Table Legend
        TableStatusLegend()

        // Tables by Section
        // Defensive check for null from API
        @Suppress("SENSELESS_COMPARISON")
        val sections = if (tableSections == null) emptyList() else tableSections
        
        sections.forEach { section ->
            TableSectionView(
                section = section,
                onTableClick = { /* TODO: Open Table Details */ }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(Dimens.PaddingLarge))

        // Online Orders Summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Online Orders", fontWeight = FontWeight.Bold)
                    TextButton(onClick = { onNavigate(Screen.OnlineOrders) }) {
                        Text("View All")
                    }
                }
                
                val onlineOrders = analytics.overview.totalOrders
                if (onlineOrders > 0) {
                    Text(text = "You have $onlineOrders active online orders", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text(text = "No active online orders", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens.PaddingExtraLarge))

        // Quick Actions
        Text(
            text = "QUICK ACTIONS",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(Dimens.PaddingMedium))
        
        ActionCard(
            title = "Order Manager",
            subtitle = "Manage active service orders",
            icon = Icons.Default.FlashOn,
            onClick = { onNavigate(Screen.LiveOrders) }
        )
        Spacer(modifier = Modifier.height(Dimens.SpacingSmall))
        ActionCard(
            title = "Billing Desk",
            subtitle = "Create new bills and take payments",
            icon = Icons.AutoMirrored.Filled.ReceiptLong,
            onClick = { onNavigate(Screen.BillingDesk) }
        )
        Spacer(modifier = Modifier.height(Dimens.SpacingSmall))
        ActionCard(
            title = "Menu Studio",
            subtitle = "Manage items and availability",
            icon = Icons.AutoMirrored.Filled.ListAlt,
            onClick = { onNavigate(Screen.MenuStudio) }
        )
        Spacer(modifier = Modifier.height(Dimens.SpacingSmall))
        ActionCard(
            title = "Analytics",
            subtitle = "Revenue and growth reports",
            icon = Icons.Default.BarChart,
            onClick = { onNavigate(Screen.Analytics) }
        )
        Spacer(modifier = Modifier.height(Dimens.SpacingSmall))
        ActionCard(
            title = "Order History",
            subtitle = "View past transactions",
            icon = Icons.Default.History,
            onClick = { onNavigate(Screen.History) }
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun StatusIndicator(isOpen: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = (if (isOpen) Color(0xFF4CAF50) else Color(0xFFF44336)).copy(alpha = 0.1f),
        shape = CircleShape,
        border = BorderStroke(1.dp, if (isOpen) Color(0xFF4CAF50) else Color(0xFFF44336))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isOpen) Color(0xFF4CAF50) else Color(0xFFF44336))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isOpen) "OPEN" else "CLOSED",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (isOpen) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
        }
    }
}

@Composable
fun MiniStat(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, color.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun StatCard(
    label: String, 
    value: String, 
    icon: ImageVector, 
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(Dimens.PaddingMedium)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ActionCard(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(Dimens.PaddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(Dimens.SpacingMedium))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}
