package com.tiffzy.restaurant.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object OtpLogin : Screen("otp_login")
    object ForgotPassword : Screen("forgot_password")
    object Home : Screen("home")
    object RestaurantDetail : Screen("restaurant_detail/{slug}") {
        fun createRoute(slug: String) = "restaurant_detail/$slug"
    }
    object Cart : Screen("cart")
    object AddressList : Screen("address_list")
    object AddAddress : Screen("add_address")
    object Checkout : Screen("checkout")
    object Payment : Screen("payment/{orderId}") {
        fun createRoute(orderId: Int) = "payment/$orderId"
    }
    object OrderList : Screen("order_list")
    object OrderTracking : Screen("order_tracking/{orderId}") {
        fun createRoute(orderId: Int) = "order_tracking/$orderId"
    }
    object Notifications : Screen("notifications")
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object Wallet : Screen("wallet")
    object SavedCards : Screen("saved_cards")
    object ReviewList : Screen("reviews/{slug}") {
        fun createRoute(slug: String) = "reviews/$slug"
    }
    object WriteReview : Screen("write_review/{slug}") {
        fun createRoute(slug: String) = "write_review/$slug"
    }
    object Dashboard : Screen("restaurant_dashboard")
    object Orders : Screen("restaurant_orders")
    object Menu : Screen("restaurant_menu")
    object Sales : Screen("restaurant_sales")
    object Settings : Screen("restaurant_settings")
    object History : Screen("restaurant_history")
    
    object OrderDetail : Screen("restaurant_order_detail/{orderId}") {
        fun createRoute(orderId: Int) = "restaurant_order_detail/$orderId"
    }
    
    object AddMenuItem : Screen("restaurant_add_menu_item")
    object EditMenuItem : Screen("restaurant_edit_menu_item/{menuId}") {
        fun createRoute(menuId: Int) = "restaurant_edit_menu_item/$menuId"
    }
}
