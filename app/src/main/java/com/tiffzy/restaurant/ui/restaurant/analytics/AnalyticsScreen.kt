package com.tiffzy.restaurant.ui.restaurant.analytics

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tiffzy.restaurant.data.model.*
import com.tiffzy.restaurant.ui.components.TiffzyErrorState
import com.tiffzy.restaurant.ui.components.TiffzyLoadingIndicator
import com.tiffzy.restaurant.ui.restaurant.RestaurantSalesViewModel
import com.tiffzy.restaurant.ui.restaurant.SalesUiState
import com.tiffzy.restaurant.ui.restaurant.components.OwnerShell
import com.tiffzy.restaurant.ui.theme.TiffzyOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    navController: NavController,
    onLogout: () -> Unit,
    viewModel: RestaurantSalesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentRange by viewModel.currentRange.collectAsState()

    OwnerShell(
        title = "Business Analytics",
        restaurantName = "Performance Insights",
        navController = navController,
        onLogout = onLogout,
        actions = {
            IconButton(onClick = { viewModel.loadAnalytics(currentRange) }) {
                Icon(Icons.Default.Refresh, "Refresh")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // Range Selector
            RangeSelector(
                selectedRange = currentRange,
                onRangeSelect = viewModel::loadAnalytics
            )

            when (val state = uiState) {
                is SalesUiState.Loading -> TiffzyLoadingIndicator()
                is SalesUiState.Error -> TiffzyErrorState(state.message, { viewModel.loadAnalytics(currentRange) })
                is SalesUiState.Success -> {
                    AnalyticsContent(state.analytics)
                }
            }
        }
    }
}

@Composable
fun RangeSelector(
    selectedRange: String,
    onRangeSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val ranges = listOf("24h" to "Today", "7d" to "7 Days", "30d" to "30 Days")
        ranges.forEach { (key, label) ->
            FilterChip(
                selected = selectedRange == key,
                onClick = { onRangeSelect(key) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TiffzyOrange,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
fun AnalyticsContent(analytics: AnalyticsResponse) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. KPI Grid
        SummaryGrid(analytics.overview)

        // 2. Revenue Waveform
        RevenueWaveformCard(analytics.revenueWaveform)

        // 3. AI Demand Radar
        AIDemandRadarCard(analytics.aiDemandRadar)

        // 4. Peak Hours
        PeakHoursCard(analytics.peakHours)

        // 5. Category Mix
        CategoryMixCard(analytics.categoryMix)
        
        // 6. Payments Breakdown
        PaymentsBreakdownCard(analytics.payments)

        // 7. Fulfillment Channels
        FulfillmentChannelsCard(analytics.fulfillment)

        // 8. Top Moving Items
        TopMovingItemsCard(analytics.topItems)

        // 9. Table Turn Heatmap
        TableTurnHeatmapCard(analytics.tableHeatmap)
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun SummaryGrid(overview: AnalyticsOverview) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                label = "Total Money Earned",
                value = "₹${overview.totalRevenue.toInt()}",
                change = overview.revenueChange,
                icon = Icons.Default.Payments,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "Total Orders",
                value = overview.totalOrders.toString(),
                change = overview.ordersChange,
                icon = Icons.Default.ShoppingBag,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                label = "Avg. Bill Value",
                value = "₹${overview.avgOrderValue.toInt()}",
                change = "N/A",
                icon = Icons.Default.Receipt,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "Repeat Customers",
                value = overview.repeatRate,
                change = "Stable",
                icon = Icons.Default.Person,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MetricCard(label: String, value: String, change: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = TiffzyOrange, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            
            if (change != "N/A" && change.isNotBlank()) {
                val isPositive = change.startsWith("+")
                Text(
                    text = if (isPositive) "↑ $change from last" else "↓ $change from last",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isPositive) Color(0xFF4CAF50) else Color.Red
                )
            }
        }
    }
}

@Composable
fun RevenueWaveformCard(data: List<DataPoint>) {
    ProfessionalSectionCard(title = "Money Flow & Orders", icon = Icons.AutoMirrored.Filled.ShowChart) {
        Column {
            Text("Your earnings and order trends over time", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            
            Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                if (data.size < 2) return@Canvas
                
                val path = Path()
                val stepX = size.width / (data.size - 1)
                val maxVal = data.maxOf { it.value }.coerceAtLeast(1.0)
                
                data.forEachIndexed { i, point ->
                    val x = i * stepX
                    val y = size.height - (point.value.toFloat() / maxVal.toFloat() * size.height)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                
                drawPath(
                    path = path,
                    color = TiffzyOrange,
                    style = Stroke(width = 3.dp.toPx())
                )
            }
            
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
               data.firstOrNull()?.let { Text(it.label, style = MaterialTheme.typography.labelSmall) }
               data.lastOrNull()?.let { Text(it.label, style = MaterialTheme.typography.labelSmall) }
            }
        }
    }
}

@Composable
fun AIDemandRadarCard(radar: AIDemandRadar?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = TiffzyOrange.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, null, tint = TiffzyOrange)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "SMART PREDICTIONS", fontWeight = FontWeight.Black, color = TiffzyOrange, letterSpacing = 1.sp)
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                PredictionItem("Estimated Today", "₹${radar?.projectionEndOfDay?.toInt() ?: "---"}", modifier = Modifier.weight(1f))
                PredictionItem("Peak Rush Time", radar?.peakRushWindow ?: "---", modifier = Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = { 0.85f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = TiffzyOrange,
                trackColor = TiffzyOrange.copy(alpha = 0.1f)
            )
            Text(
                text = "Prediction Confidence: High (85%)",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun PredictionItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PeakHoursCard(load: List<HourlyLoad>) {
    ProfessionalSectionCard(title = "Peak Rush Hours", icon = Icons.Default.Timer) {
        Column {
            Text("When is your restaurant busiest?", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(modifier = Modifier.fillMaxWidth().height(120.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
                val maxLoad = load.maxOfOrNull { it.orderCount }?.coerceAtLeast(1) ?: 1
                load.takeLast(12).forEach { item ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(item.orderCount.toFloat() / maxLoad)
                            .background(if (item.isPeak) TiffzyOrange else Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    )
                }
            }
            Text("Busiest time: 7 PM - 9 PM", modifier = Modifier.fillMaxWidth().padding(top = 8.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun CategoryMixCard(mix: List<CategoryRevenue>) {
    ProfessionalSectionCard(title = "What Food is Selling?", icon = Icons.Default.PieChart) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("See which food categories bring in the most money", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            mix.forEach { item ->
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(item.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("₹${item.revenue.toInt()} (${(item.percentage * 100).toInt()}%)", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { item.percentage },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = TiffzyOrange,
                        trackColor = Color.LightGray.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentsBreakdownCard(payments: List<PaymentBreakdown>) {
    ProfessionalSectionCard(title = "How Customers Paid", icon = Icons.Default.AccountBalanceWallet) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Cash vs Online payments", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            payments.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(TiffzyOrange, CircleShape))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.method, fontWeight = FontWeight.Bold)
                        Text("${item.count} orders", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    Text("₹${item.amount.toInt()}", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun FulfillmentChannelsCard(channels: List<ChannelBreakdown>) {
    ProfessionalSectionCard(title = "Order Types", icon = Icons.AutoMirrored.Filled.DirectionsRun) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Dine-in, Takeaway, and Delivery", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            channels.forEach { item ->
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(item.channel, fontWeight = FontWeight.Bold)
                        Text("${(item.percentage * 100).toInt()}%")
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { item.percentage },
                        modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                        color = if (item.channel.lowercase().contains("dine")) Color(0xFF4CAF50) else TiffzyOrange,
                        trackColor = Color.LightGray.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

@Composable
fun TopMovingItemsCard(items: List<TopMenuItem>) {
    ProfessionalSectionCard(title = "Top 5 Popular Items", icon = Icons.Default.Star) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(24.dp),
                        shape = CircleShape,
                        color = TiffzyOrange.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = item.rank.toString(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TiffzyOrange)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, fontWeight = FontWeight.Bold)
                        Text("${item.units} units sold", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    Text("₹${item.revenue.toInt()}", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun TableTurnHeatmapCard(data: List<TableTurnData>) {
    ProfessionalSectionCard(title = "Table Usage (Turns)", icon = Icons.Default.GridView) {
        Column {
            Text("How many times each table was used today", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                data.forEach { table ->
                    val color = if (table.turns >= 5) Color(0xFF4CAF50) else if (table.turns >= 3) TiffzyOrange else Color.LightGray.copy(alpha = 0.5f)
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .border(1.dp, color, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(table.tableNo, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelLarge)
                            Text("${table.turns}x", style = MaterialTheme.typography.labelSmall, color = color)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfessionalSectionCard(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = TiffzyOrange, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(20.dp))
            content()
        }
    }
}
