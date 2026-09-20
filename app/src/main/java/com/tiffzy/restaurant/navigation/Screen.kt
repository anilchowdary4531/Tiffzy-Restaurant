package com.tiffzy.restaurant.navigation

sealed class Screen(val route: String) {
    // Auth & General
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object OtpLogin : Screen("otp_login")
    object ForgotPassword : Screen("forgot_password")
    
    // Customer Screens
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

    // Owner Screens (Hamburger Order)
    object Dashboard : Screen("owner_dashboard")
    object BillingDesk : Screen("owner_billing_desk")
    object LiveOrders : Screen("owner_live_orders")
    object OnlineOrders : Screen("owner_online_orders")
    object MenuStudio : Screen("owner_menu_studio")
    object TablesQR : Screen("owner_tables_qr")
    object KitchenLive : Screen("owner_kitchen_live")
    object Analytics : Screen("owner_analytics")
    object PayLater : Screen("owner_pay_later")
    object SupplyMarketplace : Screen("owner_supply_marketplace")
    object Staff : Screen("owner_staff")
    object OwnerProfile : Screen("owner_profile")

    // Owner Sub-screens
    object OrderDetail : Screen("restaurant_order_detail/{orderId}") {
        fun createRoute(orderId: Int) = "restaurant_order_detail/$orderId"
    }
    object AddMenuItem : Screen("restaurant_add_menu_item")
    object EditMenuItem : Screen("restaurant_edit_menu_item/{menuId}") {
        fun createRoute(menuId: Int) = "restaurant_edit_menu_item/$menuId"
    }
    
    // Legacy mapping (keep for transition or delete if sure)
    object Orders : Screen("restaurant_orders")
    object Menu : Screen("restaurant_menu")
    object Sales : Screen("restaurant_sales")
    object Settings : Screen("restaurant_settings")
    object History : Screen("restaurant_history")
}
