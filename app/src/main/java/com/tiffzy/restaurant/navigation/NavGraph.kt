package com.tiffzy.restaurant.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.tiffzy.restaurant.ui.auth.AuthViewModel
import com.tiffzy.restaurant.ui.auth.LoginScreen
import com.tiffzy.restaurant.ui.auth.OtpScreen
import com.tiffzy.restaurant.ui.auth.SplashScreen
import com.tiffzy.restaurant.ui.restaurant.*

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                viewModel = authViewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToRestaurantDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onOtpSent = {
                    navController.navigate(Screen.Otp.route)
                },
                onStaffLoggedIn = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Otp.route) {
            OtpScreen(
                viewModel = authViewModel,
                onAuthenticated = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBack = {
                    authViewModel.resetState()
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Dashboard.route) {
            RestaurantDashboardScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onOrdersClick = {
                    navController.navigate(Screen.Orders.route)
                },
                onMenuClick = {
                    navController.navigate(Screen.Menu.route)
                },
                onSalesClick = {
                    navController.navigate(Screen.Sales.route)
                },
                onHistoryClick = {
                    navController.navigate(Screen.History.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Orders.route) {
            val viewModel: RestaurantOrdersViewModel = hiltViewModel()
            RestaurantOrdersScreen(
                onOrderClick = { orderId ->
                    navController.navigate(Screen.OrderDetail.createRoute(orderId))
                },
                viewModel = viewModel
            )
        }

        composable(Screen.History.route) {
            val viewModel: RestaurantOrderHistoryViewModel = hiltViewModel()
            RestaurantOrderHistoryScreen(
                onOrderClick = { orderId ->
                    navController.navigate(Screen.OrderDetail.createRoute(orderId))
                },
                onBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }

        composable(
            route = Screen.OrderDetail.route,
            arguments = listOf(navArgument("orderId") { type = NavType.IntType }),
            deepLinks = listOf(navDeepLink { uriPattern = "tiffzy://restaurant/order/{orderId}" })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getInt("orderId") ?: 0
            val viewModel: RestaurantOrdersViewModel = hiltViewModel()
            RestaurantOrderDetailScreen(
                orderId = orderId,
                onBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }

        composable(Screen.Menu.route) {
            val viewModel: RestaurantMenuViewModel = hiltViewModel()
            RestaurantMenuScreen(
                onAddItem = { navController.navigate(Screen.AddMenuItem.route) },
                onEditItem = { item ->
                    navController.navigate(Screen.EditMenuItem.createRoute(item.id))
                },
                onBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }

        composable(Screen.AddMenuItem.route) {
            val viewModel: RestaurantMenuViewModel = hiltViewModel()
            AddEditMenuItemScreen(
                onBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }

        composable(
            route = Screen.EditMenuItem.route,
            arguments = listOf(navArgument("menuId") { type = NavType.IntType })
        ) { backStackEntry ->
            val menuId = backStackEntry.arguments?.getInt("menuId") ?: 0
            val viewModel: RestaurantMenuViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()
            val menuItem = (uiState as? MenuUiState.Success)?.menu?.find { it.id == menuId }
            
            AddEditMenuItemScreen(
                menuItem = menuItem,
                onBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }

        composable(Screen.Sales.route) {
            val viewModel: RestaurantSalesViewModel = hiltViewModel()
            RestaurantSalesScreen(
                onBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }

        composable(Screen.Settings.route) {
            val viewModel: RestaurantSettingsViewModel = hiltViewModel()
            RestaurantSettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }
    }
}
