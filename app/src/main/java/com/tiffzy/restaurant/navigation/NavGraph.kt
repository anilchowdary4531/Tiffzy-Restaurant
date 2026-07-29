package com.tiffzy.restaurant.navigation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tiffzy.restaurant.ui.auth.AuthViewModel
import com.tiffzy.restaurant.ui.auth.LoginScreen
import com.tiffzy.restaurant.ui.auth.OtpScreen
import com.tiffzy.restaurant.ui.auth.SplashScreen
import com.tiffzy.restaurant.ui.restaurant.AddEditMenuItemScreen
import com.tiffzy.restaurant.ui.restaurant.RestaurantDashboardScreen
import com.tiffzy.restaurant.ui.restaurant.RestaurantDashboardViewModel
import com.tiffzy.restaurant.ui.restaurant.RestaurantMenuScreen
import com.tiffzy.restaurant.ui.restaurant.RestaurantMenuViewModel
import com.tiffzy.restaurant.ui.restaurant.RestaurantOrderDetailScreen
import com.tiffzy.restaurant.ui.restaurant.RestaurantOrderHistoryScreen
import com.tiffzy.restaurant.ui.restaurant.RestaurantOrdersScreen
import com.tiffzy.restaurant.ui.restaurant.RestaurantOrdersViewModel
import com.tiffzy.restaurant.ui.restaurant.RestaurantSalesScreen
import com.tiffzy.restaurant.ui.restaurant.RestaurantSalesViewModel
import com.tiffzy.restaurant.ui.restaurant.RestaurantSettingsScreen
import com.tiffzy.restaurant.ui.restaurant.RestaurantSettingsViewModel
import com.tiffzy.restaurant.ui.restaurant.MenuUiState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink

object Routes {
    const val Splash = "splash"
    const val Login = "login"
    const val Otp = "otp"
    const val RestaurantDashboard = "restaurant_dashboard"
    const val RestaurantOrders = "restaurant_orders"
    const val RestaurantMenu = "restaurant_menu"
    const val RestaurantSales = "restaurant_sales"
    const val RestaurantSettings = "restaurant_settings"
    const val RestaurantHistory = "restaurant_history"
    const val RestaurantOrderDetail = "restaurant_order_detail/{orderId}"
    const val RestaurantAddMenuItem = "restaurant_add_menu_item"
    const val RestaurantEditMenuItem = "restaurant_edit_menu_item/{menuId}"
    
    fun restaurantOrderDetail(orderId: Int) = "restaurant_order_detail/$orderId"
    fun restaurantEditMenuItem(menuId: Int) = "restaurant_edit_menu_item/$menuId"
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Splash
    ) {
        composable(Routes.Splash) {
            SplashScreen(
                viewModel = authViewModel,
                onNavigateToHome = {
                    // In Restaurant app, "Home" is the Dashboard
                    navController.navigate(Routes.RestaurantDashboard) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Routes.Login) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                },
                onNavigateToRestaurantDashboard = {
                    navController.navigate(Routes.RestaurantDashboard) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Login) {
            LoginScreen(
                viewModel = authViewModel,
                onOtpSent = {
                    navController.navigate(Routes.Otp)
                },
                onStaffLoggedIn = {
                    navController.navigate(Routes.RestaurantDashboard) {
                        popUpTo(Routes.Login) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Otp) {
            OtpScreen(
                viewModel = authViewModel,
                onAuthenticated = {
                    // Logic to check if user is staff should be in ViewModel or handled here
                    navController.navigate(Routes.RestaurantDashboard) {
                        popUpTo(Routes.Login) { inclusive = true }
                    }
                },
                onBack = {
                    authViewModel.resetState()
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.RestaurantDashboard) {
            RestaurantDashboardScreen(
                onLogout = {
                    navController.navigate(Routes.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onOrdersClick = {
                    navController.navigate(Routes.RestaurantOrders)
                },
                onMenuClick = {
                    navController.navigate(Routes.RestaurantMenu)
                },
                onSalesClick = {
                    navController.navigate(Routes.RestaurantSales)
                },
                onHistoryClick = {
                    navController.navigate(Routes.RestaurantHistory)
                },
                onSettingsClick = {
                    navController.navigate(Routes.RestaurantSettings)
                }
            )
        }

        composable(Routes.RestaurantOrders) {
            val restaurantOrdersViewModel: RestaurantOrdersViewModel = viewModel()
            RestaurantOrdersScreen(
                onOrderClick = { orderId ->
                    navController.navigate(Routes.restaurantOrderDetail(orderId))
                },
                viewModel = restaurantOrdersViewModel
            )
        }

        composable(Routes.RestaurantHistory) {
            RestaurantOrderHistoryScreen(
                onOrderClick = { orderId ->
                    navController.navigate(Routes.restaurantOrderDetail(orderId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.RestaurantOrderDetail,
            arguments = listOf(navArgument("orderId") { type = NavType.IntType }),
            deepLinks = listOf(navDeepLink { uriPattern = "tiffzy://restaurant/order/{orderId}" })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getInt("orderId") ?: 0
            val restaurantOrdersViewModel: RestaurantOrdersViewModel = viewModel()
            RestaurantOrderDetailScreen(
                orderId = orderId,
                onBack = { navController.popBackStack() },
                viewModel = restaurantOrdersViewModel
            )
        }

        composable(Routes.RestaurantMenu) {
            val restaurantMenuViewModel: RestaurantMenuViewModel = viewModel()
            RestaurantMenuScreen(
                onAddItem = { navController.navigate(Routes.RestaurantAddMenuItem) },
                onEditItem = { item ->
                    navController.navigate(Routes.restaurantEditMenuItem(item.id))
                },
                onBack = { navController.popBackStack() },
                viewModel = restaurantMenuViewModel
            )
        }

        composable(Routes.RestaurantAddMenuItem) {
            val restaurantMenuViewModel: RestaurantMenuViewModel = viewModel()
            AddEditMenuItemScreen(
                onBack = { navController.popBackStack() },
                viewModel = restaurantMenuViewModel
            )
        }

        composable(
            route = Routes.RestaurantEditMenuItem,
            arguments = listOf(navArgument("menuId") { type = NavType.IntType })
        ) { backStackEntry ->
            val menuId = backStackEntry.arguments?.getInt("menuId") ?: 0
            val restaurantMenuViewModel: RestaurantMenuViewModel = viewModel()
            val uiState by restaurantMenuViewModel.uiState.collectAsState()
            val menuItem = (uiState as? MenuUiState.Success)?.menu?.find { it.id == menuId }
            
            AddEditMenuItemScreen(
                menuItem = menuItem,
                onBack = { navController.popBackStack() },
                viewModel = restaurantMenuViewModel
            )
        }

        composable(Routes.RestaurantSales) {
            RestaurantSalesScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.RestaurantSettings) {
            RestaurantSettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Routes.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
