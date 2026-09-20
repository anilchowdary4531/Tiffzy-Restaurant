package com.tiffzy.restaurant.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.tiffzy.restaurant.MainActivity
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.ui.auth.*
import com.tiffzy.restaurant.ui.home.HomeScreen
import com.tiffzy.restaurant.ui.home.HomeViewModel
import com.tiffzy.restaurant.ui.home.address.AddAddressScreen
import com.tiffzy.restaurant.ui.home.address.AddressListScreen
import com.tiffzy.restaurant.ui.home.cart.CartScreen
import com.tiffzy.restaurant.ui.home.cart.CartViewModel
import com.tiffzy.restaurant.ui.home.cart.CheckoutScreen
import com.tiffzy.restaurant.ui.home.cart.CheckoutViewModel
import com.tiffzy.restaurant.ui.home.details.RestaurantDetailScreen
import com.tiffzy.restaurant.ui.home.details.RestaurantDetailViewModel
import com.tiffzy.restaurant.ui.home.notifications.NotificationListScreen
import com.tiffzy.restaurant.ui.home.notifications.NotificationViewModel
import com.tiffzy.restaurant.ui.home.orders.OrderListScreen
import com.tiffzy.restaurant.ui.home.orders.OrderTrackingScreen
import com.tiffzy.restaurant.ui.home.orders.OrderTrackingViewModel
import com.tiffzy.restaurant.ui.home.orders.OrderViewModel
import com.tiffzy.restaurant.ui.home.payment.PaymentResultScreen
import com.tiffzy.restaurant.ui.home.payment.PaymentStatus
import com.tiffzy.restaurant.ui.home.payment.PaymentViewModel
import com.tiffzy.restaurant.ui.home.profile.*
import com.tiffzy.restaurant.ui.home.reviews.ReviewListScreen
import com.tiffzy.restaurant.ui.home.reviews.ReviewViewModel
import com.tiffzy.restaurant.ui.home.reviews.WriteReviewScreen
import com.tiffzy.restaurant.ui.restaurant.*
import com.tiffzy.restaurant.ui.restaurant.analytics.AnalyticsScreen
import com.tiffzy.restaurant.ui.restaurant.billing.BillingDeskScreen
import com.tiffzy.restaurant.ui.restaurant.kitchen.KitchenLiveScreen
import com.tiffzy.restaurant.ui.restaurant.orders.OnlineOrdersScreen
import com.tiffzy.restaurant.ui.restaurant.paylater.PayLaterScreen
import com.tiffzy.restaurant.ui.restaurant.profile.OwnerProfileScreen
import com.tiffzy.restaurant.ui.restaurant.staff.StaffScreen
import com.tiffzy.restaurant.ui.restaurant.supply.SupplyMarketplaceScreen
import com.tiffzy.restaurant.ui.restaurant.tables.TablesQRScreen
import com.tiffzy.restaurant.util.SessionEvent
import com.tiffzy.restaurant.util.SessionEventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val sessionViewModel: SessionEventViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        sessionViewModel.eventBus.events.collect { event ->
            if (event is SessionEvent.SessionExpired) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // --- Auth Screens ---
        composable(Screen.Splash.route) {
            SplashScreen(
                viewModel = authViewModel,
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    authViewModel.completeOnboarding()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToOtpLogin = { navController.navigate(Screen.OtpLogin.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onLoginSuccess = {
                    val nextRoute = if (authViewModel.phone.value.startsWith("9")) Screen.Home.route else Screen.Dashboard.route
                    navController.navigate(nextRoute) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.OtpLogin.route) {
            OtpLoginScreen(
                viewModel = authViewModel,
                onNavigateBack = {
                    authViewModel.resetState()
                    navController.popBackStack()
                },
                onAuthSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onNavigateBack = {
                    authViewModel.resetState()
                    navController.popBackStack()
                },
                onResetSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                    }
                }
            )
        }

        // --- Customer Flow ---
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = hiltViewModel(),
                onNavigateToRestaurant = { r -> navController.navigate(Screen.RestaurantDetail.createRoute(r.slug)) },
                onNavigateToAddresses = { navController.navigate(Screen.AddressList.route) }
            )
        }

        composable(
            route = Screen.RestaurantDetail.route,
            arguments = listOf(navArgument("slug") { type = NavType.StringType })
        ) {
            RestaurantDetailScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() },
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                onSeeAllReviews = { s -> navController.navigate(Screen.ReviewList.createRoute(s)) },
                onWriteReview = { s -> navController.navigate(Screen.WriteReview.createRoute(s)) }
            )
        }

        composable(Screen.Cart.route) {
            CartScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() },
                onNavigateToCheckout = { navController.navigate(Screen.Checkout.route) }
            )
        }

        composable(Screen.Checkout.route) {
            CheckoutScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() },
                onOrderConfirmed = { _ -> }
            )
        }

        // --- Owner Flow (Hamburger Items) ---
        
        composable(Screen.Dashboard.route) {
            RestaurantDashboardScreen(
                navController = navController,
                onLogout = { 
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable(Screen.BillingDesk.route) {
            BillingDeskScreen(
                navController = navController,
                onLogout = { 
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable(Screen.LiveOrders.route) {
            RestaurantOrdersScreen(
                navController = navController,
                onLogout = { 
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                },
                onOrderClick = { id -> navController.navigate(Screen.OrderDetail.createRoute(id)) }
            )
        }

        composable(Screen.OnlineOrders.route) {
            OnlineOrdersScreen(
                navController = navController,
                onLogout = { 
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable(Screen.MenuStudio.route) {
            RestaurantMenuScreen(
                navController = navController,
                onAddItem = { navController.navigate(Screen.AddMenuItem.route) },
                onEditItem = { item -> navController.navigate(Screen.EditMenuItem.createRoute(item.id)) },
                onLogout = { 
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable(Screen.TablesQR.route) {
            TablesQRScreen(
                navController = navController,
                onLogout = { 
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable(Screen.KitchenLive.route) {
            KitchenLiveScreen(
                navController = navController,
                onLogout = { 
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable(Screen.Analytics.route) {
            AnalyticsScreen(
                navController = navController,
                onLogout = { 
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable(Screen.PayLater.route) {
            PayLaterScreen(
                navController = navController,
                onLogout = { 
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable(Screen.SupplyMarketplace.route) {
            SupplyMarketplaceScreen(
                navController = navController,
                onLogout = { 
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable(Screen.Staff.route) {
            StaffScreen(
                navController = navController,
                onLogout = { 
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable(Screen.OwnerProfile.route) {
            OwnerProfileScreen(
                navController = navController,
                onLogout = { 
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable(Screen.History.route) {
            RestaurantOrderHistoryScreen(
                navController = navController,
                onOrderClick = { id -> navController.navigate(Screen.OrderDetail.createRoute(id)) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable(Screen.Sales.route) {
            RestaurantSalesScreen(
                navController = navController,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable(Screen.OrderDetail.route) {
            RestaurantOrderDetailScreen(
                orderId = it.arguments?.getInt("orderId") ?: 0,
                navController = navController,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                },
                viewModel = hiltViewModel()
            )
        }

        composable(Screen.AddMenuItem.route) {
            AddEditMenuItemScreen(
                navController = navController,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                },
                onBack = { navController.popBackStack() },
                viewModel = hiltViewModel()
            )
        }

        composable(
            route = Screen.EditMenuItem.route,
            arguments = listOf(navArgument("menuId") { type = NavType.IntType })
        ) {
            val menuId = it.arguments?.getInt("menuId") ?: 0
            val viewModel: RestaurantMenuViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()
            val menuItem = (uiState as? MenuUiState.Success)?.menu?.find { item -> item.id == menuId }
            AddEditMenuItemScreen(
                menuItem = menuItem,
                navController = navController,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                },
                onBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }
    }
}
