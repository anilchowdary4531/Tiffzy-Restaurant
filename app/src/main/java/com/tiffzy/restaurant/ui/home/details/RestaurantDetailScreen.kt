package com.tiffzy.restaurant.ui.home.details

import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.data.model.Restaurant
import com.tiffzy.restaurant.data.model.RestaurantDetailResponse
import com.tiffzy.restaurant.data.model.Review
import com.tiffzy.restaurant.ui.home.details.RestaurantMenuScreen
import com.tiffzy.restaurant.ui.home.details.RestaurantMenuViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailScreen(
    viewModel: RestaurantDetailViewModel,
    onBack: () -> Unit,
    onNavigateToCart: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Menu", "Reviews", "Photos")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { (uiState as? UiState.Success)?.data?.restaurant?.name?.let { Text(it, style = MaterialTheme.typography.titleMedium) } },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Check out this restaurant on Tiffzy!")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, null))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if ((uiState as? UiState.Success)?.data?.restaurant?.isFavorite == true) 
                                Icons.Default.Favorite 
                            else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if ((uiState as? UiState.Success)?.data?.restaurant?.isFavorite == true) 
                                Color.Red 
                            else LocalContentColor.current
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> {
                    val menuViewModel: RestaurantMenuViewModel = hiltViewModel()
                    RestaurantMenuScreen(
                        viewModel = menuViewModel,
                        onNavigateToCart = onNavigateToCart
                    )
                }
                1 -> {
                    (uiState as? UiState.Success)?.let { state ->
                        Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
                            state.data.reviews.forEach { ReviewItem(it) }
                        }
                    }
                }
                2 -> {
                    (uiState as? UiState.Success)?.let { state ->
                        Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
                            RestaurantPhotos(state.data.restaurant.images)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RestaurantPhotos(images: List<String>) {
    // Basic implementation for photos tab
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        images.chunked(2).forEach { rowImages ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowImages.forEach { image ->
                    AsyncImage(
                        model = image,
                        contentDescription = null,
                        modifier = Modifier.weight(1f).height(150.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                if (rowImages.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun RestaurantDetailContent(
    data: RestaurantDetailResponse,
    modifier: Modifier = Modifier
) {
    val restaurant = data.restaurant
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Banner
        AsyncImage(
            model = restaurant.bannerUrl ?: "https://via.placeholder.com/800x400",
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.padding(16.dp)) {
            // Restaurant Info Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = restaurant.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = restaurant.cuisines.joinToString(", "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${restaurant.city}, ${restaurant.state}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status Badge
                Surface(
                    color = if (restaurant.isActive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (restaurant.isActive) "Open Now" else "Closed",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (restaurant.isActive) Color(0xFF2E7D32) else Color(0xFFC62828),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(Icons.Default.Star, "${restaurant.rating}", "${restaurant.reviewCount}+ Ratings", Color(0xFFFFB300))
                StatItem(Icons.Default.Timer, restaurant.deliveryTime ?: "30 mins", "Delivery Time")
                StatItem(Icons.Default.DirectionsRun, restaurant.distance ?: "2.5 km", "Distance")
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Restaurant Images
            if (restaurant.images.isNotEmpty()) {
                Text(
                    text = "Photos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(restaurant.images) { image ->
                        AsyncImage(
                            model = image,
                            contentDescription = null,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Opening Hours
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Opening Hours: ${restaurant.openingHours ?: "10:00 AM - 11:00 PM"}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Offers Section
            Text(
                text = "Offers For You",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(3) { // Mocking 3 offers
                    OfferCard()
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Reviews Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reviews",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { /* All reviews */ }) {
                    Text("See All")
                }
            }
            
            data.reviews.take(3).forEach { review ->
                ReviewItem(review)
            }
        }
    }
}

@Composable
fun StatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String, iconTint: Color = MaterialTheme.colorScheme.primary) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = iconTint)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun OfferCard() {
    Card(
        modifier = Modifier.width(200.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.LocalOffer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = "50% OFF up to ₹100", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(text = "USE WELCOME50", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun ReviewItem(review: Review) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = review.userImage ?: "https://via.placeholder.com/40",
                contentDescription = null,
                modifier = Modifier.size(32.dp).clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = review.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = review.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                color = Color(0xFF4CAF50),
                shape = RoundedCornerShape(4.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${review.rating}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = review.comment,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        Divider(modifier = Modifier.padding(top = 8.dp), thickness = 0.5.dp)
    }
}
