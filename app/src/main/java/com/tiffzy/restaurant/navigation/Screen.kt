package com.tiffzy.restaurant.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Otp : Screen("otp")
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
