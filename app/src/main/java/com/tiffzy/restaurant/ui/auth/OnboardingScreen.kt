package com.tiffzy.restaurant.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiffzy.restaurant.R
import kotlinx.coroutines.launch

data class OnboardingItem(
    val title: String,
    val description: String,
    val image: Int
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val items = listOf(
        OnboardingItem(
            "Manage Orders",
            "Handle incoming orders in real-time with our intuitive dashboard.",
            R.drawable.tiffzy_logo // Replace with actual onboarding images
        ),
        OnboardingItem(
            "Track Sales",
            "Monitor your daily revenue and order trends effortlessly.",
            R.drawable.tiffzy_logo
        ),
        OnboardingItem(
            "Update Menu",
            "Add, edit or disable menu items instantly for your customers.",
            R.drawable.tiffzy_logo
        )
    )

    val pagerState = rememberPagerState(pageCount = { items.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = painterResource(id = items[page].image),
                    contentDescription = null,
                    modifier = Modifier.size(280.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = items[page].title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = items[page].description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(items.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                
                Surface(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(8.dp),
                    shape = MaterialTheme.shapes.small,
                    color = color
                ) {}
            }
        }

        Button(
            onClick = {
                if (pagerState.currentPage < items.size - 1) {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                } else {
                    onFinish()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(if (pagerState.currentPage < items.size - 1) "Next" else "Get Started")
        }
        
        TextButton(
            onClick = onFinish,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Skip")
        }
    }
}
