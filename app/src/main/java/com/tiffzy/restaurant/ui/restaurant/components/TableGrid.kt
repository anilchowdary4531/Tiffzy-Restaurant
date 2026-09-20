package com.tiffzy.restaurant.ui.restaurant.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiffzy.restaurant.data.model.TableData
import com.tiffzy.restaurant.data.model.TableSection
import com.tiffzy.restaurant.ui.theme.TiffzyOrange

// Status Colors
val StatusBlank = Color(0xFFE0E0E0) // Light Grey
val StatusRunning = Color(0xFF2196F3) // Blue
val StatusPrinted = Color(0xFFFF9800) // Orange
val StatusPaid = Color(0xFF4CAF50) // Green
val StatusKOT = Color(0xFF9C27B0) // Purple

@Composable
fun TableStatusLegend() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem("Blank", StatusBlank)
        LegendItem("Running", StatusRunning)
        LegendItem("Printed", StatusPrinted)
        LegendItem("Paid", StatusPaid)
        LegendItem("KOT", StatusKOT)
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun TableSectionView(
    section: TableSection,
    onTableClick: (TableData) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = section.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        // Defensive check for potential null from API
        @Suppress("SENSELESS_COMPARISON")
        val tables = if (section.tables == null) emptyList() else section.tables
        
        val columns = 3 
        val rows = (tables.size + columns - 1) / columns
        for (i in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (j in 0 until columns) {
                    val index = i * columns + j
                    if (index < tables.size) {
                        TableCard(
                            table = tables[index],
                            onClick = { onTableClick(tables[index]) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun TableCard(
    table: TableData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when (table.status.uppercase()) {
        "BLANK" -> StatusBlank
        "RUNNING" -> StatusRunning
        "PRINTED" -> StatusPrinted
        "PAID" -> StatusPaid
        "RUNNING_KOT" -> StatusKOT
        else -> StatusBlank
    }

    val onStatusColor = if (table.status.uppercase() == "BLANK") Color.Black else Color.White

    Card(
        modifier = modifier
            .aspectRatio(1.2f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = statusColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = table.number,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = onStatusColor
            )
            Text(
                text = "${table.seats} Seats",
                style = MaterialTheme.typography.labelSmall,
                color = onStatusColor.copy(alpha = 0.8f)
            )
            if (table.currentOrderTotal != null && table.currentOrderTotal > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "₹${table.currentOrderTotal.toInt()}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = onStatusColor
                )
            }
        }
    }
}
