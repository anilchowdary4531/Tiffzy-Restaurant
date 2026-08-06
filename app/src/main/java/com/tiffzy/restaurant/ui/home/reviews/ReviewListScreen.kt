package com.tiffzy.restaurant.ui.home.reviews

import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.data.model.Review

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewListScreen(
    slug: String,
    viewModel: ReviewViewModel,
    onWriteReview: () -> Unit,
    onEditReview: (Review) -> Unit,
    onBack: () -> Unit
) {
    val reviewsState by viewModel.reviewsState.collectAsState()

    LaunchedEffect(slug) {
        viewModel.fetchReviews(slug)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reviews") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onWriteReview) {
                Icon(Icons.Default.Add, contentDescription = "Write Review")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF5F5F5))) {
            when (val state = reviewsState) {
                is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is UiState.Success -> {
                    Column {
                        // Rating Header
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier.padding(24.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${state.data.averageRating}",
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Row {
                                        repeat(5) { index ->
                                            Icon(
                                                Icons.Default.Star,
                                                null,
                                                tint = if (index < state.data.averageRating.toInt()) Color(0xFFFFB300) else Color.LightGray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Text("${state.data.totalReviews} Reviews", fontSize = 12.sp, color = Color.Gray)
                                }
                                
                                Spacer(modifier = Modifier.width(32.dp))
                                
                                // Rating Bars Placeholder
                                Column(modifier = Modifier.weight(1f)) {
                                    RatingBarRow(5, 0.8f)
                                    RatingBarRow(4, 0.15f)
                                    RatingBarRow(3, 0.03f)
                                    RatingBarRow(2, 0.01f)
                                    RatingBarRow(1, 0.01f)
                                }
                            }
                        }

                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.data.reviews) { review ->
                                ReviewItemCard(
                                    review = review,
                                    onEdit = { onEditReview(review) },
                                    onDelete = { viewModel.deleteReview(review.id, slug) }
                                )
                            }
                        }
                    }
                }
                is UiState.Error -> Text(state.message, modifier = Modifier.align(Alignment.Center))
                else -> {}
            }
        }
    }
}

@Composable
fun RatingBarRow(stars: Int, percentage: Float) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 1.dp)) {
        Text("$stars", fontSize = 10.sp, modifier = Modifier.width(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        LinearProgressIndicator(
            progress = percentage,
            modifier = Modifier.weight(1f).height(4.dp).clip(CircleShape),
            color = Color(0xFFFFB300),
            trackColor = Color(0xFFEEEEEE)
        )
    }
}

@Composable
fun ReviewItemCard(
    review: Review,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = review.userImage ?: "https://via.placeholder.com/40",
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = review.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = review.date, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, null, tint = Color.Gray)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = { 
                                showMenu = false
                                onEdit() 
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = { 
                                showMenu = false
                                onDelete() 
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                        )
                    }
                }

                Surface(
                    color = Color(0xFF4CAF50),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "${review.rating.toInt()}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.Star, null, tint = Color.White, modifier = Modifier.size(10.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = review.comment, style = MaterialTheme.typography.bodyMedium)
            
            if (review.images.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    review.images.take(4).forEach { imageUrl ->
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}
