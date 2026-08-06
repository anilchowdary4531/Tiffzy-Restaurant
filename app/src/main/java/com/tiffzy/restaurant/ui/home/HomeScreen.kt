package com.tiffzy.restaurant.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.compose.AsyncImage
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.data.model.Restaurant
import com.tiffzy.restaurant.ui.home.components.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToRestaurant: (Restaurant) -> Unit
) {
    val homeState by viewModel.homeState.collectAsState()
    val location by viewModel.currentLocation.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val nearbyRestaurants = viewModel.nearbyRestaurants.collectAsLazyPagingItems()

    val isRefreshing = homeState is UiState.Loading
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.refreshHome() }
    )

    Scaffold(
        topBar = {
            HomeHeader(
                location = location,
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.onSearchQueryChange(it) }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pullRefresh(pullRefreshState)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                when (val state = homeState) {
                    is UiState.Success -> {
                        val data = state.data
                        
                        // Offers / Banners Section
                        item {
                            OfferBannerSection(offers = data.offers)
                        }

                        // Categories Section
                        item {
                            CategorySection(
                                categories = data.categories,
                                onCategoryClick = { /* TODO */ }
                            )
                        }

                        // Popular Restaurants
                        item {
                            RestaurantHorizontalSection(
                                title = "Popular Restaurants",
                                restaurants = data.popularRestaurants,
                                onRestaurantClick = onNavigateToRestaurant
                            )
                        }

                        // Recommended
                        item {
                            RestaurantHorizontalSection(
                                title = "Recommended for you",
                                restaurants = data.recommendedRestaurants,
                                onRestaurantClick = onNavigateToRestaurant
                            )
                        }
                        
                        // Top Rated
                        item {
                            RestaurantHorizontalSection(
                                title = "Top Rated Restaurants",
                                restaurants = data.topRatedRestaurants,
                                onRestaurantClick = onNavigateToRestaurant
                            )
                        }
                        
                        // Recently Viewed
                        item {
                            RestaurantHorizontalSection(
                                title = "Recently Viewed",
                                restaurants = data.recentlyViewed,
                                onRestaurantClick = onNavigateToRestaurant
                            )
                        }
                    }
                    is UiState.Error -> {
                        item {
                            ErrorState(message = state.message, onRetry = { viewModel.refreshHome() })
                        }
                    }
                    else -> {}
                }

                // Nearby Restaurants (Paging)
                item {
                    Text(
                        text = "Nearby Restaurants",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                items(nearbyRestaurants) { restaurant ->
                    restaurant?.let {
                        RestaurantRowItem(
                            restaurant = it,
                            onClick = { onNavigateToRestaurant(it) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                // Paging Load States
                when (nearbyRestaurants.loadState.append) {
                    is LoadState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                    is LoadState.Error -> {
                        item {
                            Text(
                                text = "Error loading more restaurants",
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    else -> {}
                }
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun RestaurantHorizontalSection(
    title: String,
    restaurants: List<Restaurant>,
    onRestaurantClick: (Restaurant) -> Unit
) {
    if (restaurants.isEmpty()) return
    
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, bottom = 12.dp)
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(restaurants) { restaurant ->
                RestaurantCard(
                    restaurant = restaurant,
                    onClick = { onRestaurantClick(restaurant) }
                )
            }
        }
    }
}

@Composable
fun OfferBannerSection(offers: List<com.tiffzy.restaurant.data.model.Offer>) {
    if (offers.isEmpty()) return
    
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        items(offers) { offer ->
            Card(
                modifier = Modifier
                    .width(300.dp)
                    .height(150.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                AsyncImage(
                    model = offer.bannerUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
        Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) {
            Text("Retry")
        }
    }
}
