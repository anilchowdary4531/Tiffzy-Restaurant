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
import com.tiffzy.restaurant.ui.auth.*
import com.tiffzy.restaurant.ui.home.HomeScreen
import com.tiffzy.restaurant.ui.home.HomeViewModel
import com.tiffzy.restaurant.ui.home.cart.CartScreen
import com.tiffzy.restaurant.ui.home.cart.CartViewModel
import com.tiffzy.restaurant.ui.home.cart.CheckoutScreen
import com.tiffzy.restaurant.ui.home.cart.CheckoutViewModel
import com.tiffzy.restaurant.ui.home.payment.PaymentResultScreen
import com.tiffzy.restaurant.ui.home.payment.PaymentStatus
import com.tiffzy.restaurant.ui.home.payment.PaymentViewModel
import com.tiffzy.restaurant.ui.home.address.AddressListScreen
import com.tiffzy.restaurant.ui.home.address.AddAddressScreen
import com.tiffzy.restaurant.ui.home.address.AddressViewModel
import com.tiffzy.restaurant.ui.home.details.RestaurantDetailScreen
import com.tiffzy.restaurant.ui.home.details.RestaurantDetailViewModel
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
                    val nextRoute = if (authViewModel.phone.value.startsWith("9")) Screen.Home.route else Screen.Dashboard.route // Simple logic or fetch actual role
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

        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToRestaurant = { restaurant ->
                    navController.navigate(Screen.RestaurantDetail.createRoute(restaurant.slug))
                },
                onNavigateToAddresses = {
                    navController.navigate(Screen.AddressList.route)
                }
            )
        }

        composable(
            route = Screen.RestaurantDetail.route,
            arguments = listOf(navArgument("slug") { type = NavType.StringType })
        ) {
            val detailViewModel: RestaurantDetailViewModel = hiltViewModel()
            RestaurantDetailScreen(
                viewModel = detailViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToCart = {
                    navController.navigate(Screen.Cart.route)
                }
            )
        }

        composable(Screen.Cart.route) {
            val cartViewModel: CartViewModel = hiltViewModel()
            CartScreen(
                viewModel = cartViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToCheckout = {
                    navController.navigate(Screen.Checkout.route)
                }
            )
        }

        composable(Screen.Checkout.route) {
            val checkoutViewModel: CheckoutViewModel = hiltViewModel()
            CheckoutScreen(
                viewModel = checkoutViewModel,
                onBack = { navController.popBackStack() },
                onOrderConfirmed = { orderNo ->
                    val orderId = (checkoutViewModel.orderState.value as? UiState.Success)?.data?.order?.id ?: 0
                    if (checkoutViewModel.paymentMethod.value == "online") {
                        navController.navigate(Screen.Payment.createRoute(orderId)) {
                            popUpTo(Screen.Checkout.route) { inclusive = true }
                        }
                    } else {
                        // navController.navigate(Screen.OrderConfirmation.createRoute(orderNo))
                    }
                }
            )
        }

        composable(
            route = Screen.Payment.route,
            arguments = listOf(navArgument("orderId") { type = NavType.IntType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getInt("orderId") ?: 0
            val paymentViewModel: PaymentViewModel = hiltViewModel()
            val status by paymentViewModel.paymentStatus.collectAsState()
            val context = LocalContext.current as MainActivity

            LaunchedEffect(Unit) {
                paymentViewModel.initiatePhonePePayment(orderId)
            }

            LaunchedEffect(status) {
                if (status is PaymentStatus.PaymentInitiated) {
                    MainActivity.onPaymentResult = { success, resultStatus ->
                        paymentViewModel.onPaymentResult(success, resultStatus)
                    }
                    context.launchPhonePe((status as PaymentStatus.PaymentInitiated).intent)
                }
            }

            PaymentResultScreen(
                status = status,
                onContinue = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onRetry = {
                    paymentViewModel.initiatePhonePePayment(orderId)
                }
            )
        }

        composable(Screen.AddressList.route) {
            val addressViewModel: AddressViewModel = hiltViewModel()
            AddressListScreen(
                viewModel = addressViewModel,
                onBack = { navController.popBackStack() },
                onAddAddress = { navController.navigate(Screen.AddAddress.route) }
            )
        }

        composable(Screen.AddAddress.route) {
            val addressViewModel: AddressViewModel = hiltViewModel()
            AddAddressScreen(
                viewModel = addressViewModel,
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }

        composable(Screen.Dashboard.route) {
            RestaurantDashboardScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onOrdersClick = { navController.navigate(Screen.Orders.route) },
                onMenuClick = { navController.navigate(Screen.Menu.route) },
                onSalesClick = { navController.navigate(Screen.Sales.route) },
                onHistoryClick = { navController.navigate(Screen.History.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) }
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
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }
    }
}
