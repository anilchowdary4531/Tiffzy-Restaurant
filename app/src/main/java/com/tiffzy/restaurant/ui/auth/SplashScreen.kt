package com.tiffzy.restaurant.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiffzy.restaurant.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    viewModel: AuthViewModel,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(1500)
        val isLoggedIn = viewModel.checkAuthStatus()
        val isOnboardingDone = viewModel.isOnboardingCompleted()

        if (isLoggedIn) {
            val accountType = viewModel.getAccountType()
            if (accountType == "staff") {
                onNavigateToDashboard()
            } else {
                onNavigateToHome()
            }
        } else if (!isOnboardingDone) {
            onNavigateToOnboarding()
        } else {
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.tiffzy_logo),
                contentDescription = "Tiffzy Logo",
                modifier = Modifier.size(180.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "PREMIUM LUXURY DINING",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp
            )
        }
    }
}
