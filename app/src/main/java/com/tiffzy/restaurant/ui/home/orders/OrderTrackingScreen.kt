package com.tiffzy.restaurant.ui.home.orders

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.data.model.OrderDetails
import com.tiffzy.restaurant.data.model.OrderStatusEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    viewModel: OrderTrackingViewModel,
    onBack: () -> Unit
) {
    val orderState by viewModel.orderState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Track Order") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF5F5F5))) {
            when (val state = orderState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is UiState.Success -> {
                    OrderTrackingContent(
                        order = state.data,
                        onCancel = { viewModel.cancelOrder() },
                        onCallDriver = { phone ->
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            context.startActivity(intent)
                        }
                    )
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
fun OrderTrackingContent(
    order: OrderDetails,
    onCancel: () -> Unit,
    onCallDriver: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Arriving in ${order.estimatedTime ?: "30-40 mins"}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Order #${order.orderNo.takeLast(6)} • ${order.status.replace("_", " ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LinearProgressIndicator(
                    progress = getStatusProgress(order.status),
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape)
                )
            }
        }

        // Driver Card
        if (order.driver != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = order.driver.image ?: "https://via.placeholder.com/100",
                        contentDescription = null,
                        modifier = Modifier.size(50.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = order.driver.name, fontWeight = FontWeight.Bold)
                        Text(text = "Your Delivery Partner", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    IconButton(
                        onClick = { onCallDriver(order.driver.phone) },
                        modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    ) {
                        Icon(Icons.Default.Phone, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Order Items
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Order Summary", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                order.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "${item.qty} x ${item.itemName}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "₹${item.total.toInt()}", fontWeight = FontWeight.SemiBold)
                    }
                }
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Total Amount", fontWeight = FontWeight.Bold)
                    Text(text = "₹${order.total.toInt()}", fontWeight = FontWeight.ExtraBold, color = Color.Black)
                }
            }
        }

        // Action Buttons
        if (order.status.lowercase() == "placed") {
            Button(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Red),
                border = BorderStroke(1.dp, Color.Red),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("CANCEL ORDER")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

fun getStatusProgress(status: String): Float {
    return when (status.lowercase()) {
        "placed" -> 0.2f
        "confirmed" -> 0.4f
        "preparing" -> 0.6f
        "out_for_delivery" -> 0.8f
        "delivered" -> 1.0f
        else -> 0f
    }
}
