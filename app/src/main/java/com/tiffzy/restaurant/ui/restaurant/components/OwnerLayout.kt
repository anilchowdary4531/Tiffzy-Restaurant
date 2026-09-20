package com.tiffzy.restaurant.ui.restaurant.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiffzy.restaurant.navigation.Screen
import com.tiffzy.restaurant.ui.components.BrandLogo
import com.tiffzy.restaurant.ui.theme.TiffzyOrange

// Dark brown background as per requirement
val DrawerBackground = Color(0xFF2C1810)
val SelectedItemColor = TiffzyOrange

data class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val screen: Screen?,
    val allowedRoles: List<String> = emptyList(),
    val isLogout: Boolean = false
)

val ownerNavigationItems = listOf(
    NavigationItem("Dashboard", Icons.Default.Dashboard, Screen.Dashboard, listOf("Admin", "Manager", "Cashier")),
    NavigationItem("Billing Desk", Icons.Default.ReceiptLong, Screen.BillingDesk, listOf("Admin", "Manager", "Cashier", "Waiter")),
    NavigationItem("Live Orders", Icons.Default.FlashOn, Screen.LiveOrders, listOf("Admin", "Manager", "Chef", "Waiter", "Cashier")),
    NavigationItem("Online Orders", Icons.Default.Public, Screen.OnlineOrders, listOf("Admin", "Manager", "Cashier")),
    NavigationItem("Menu Studio", Icons.Default.RestaurantMenu, Screen.MenuStudio, listOf("Admin", "Manager", "Chef")),
    NavigationItem("Tables & QR", Icons.Default.QrCode, Screen.TablesQR, listOf("Admin", "Manager", "Waiter")),
    NavigationItem("Kitchen Live", Icons.Default.Kitchen, Screen.KitchenLive, listOf("Admin", "Manager", "Chef")),
    NavigationItem("Analytics", Icons.Default.BarChart, Screen.Analytics, listOf("Admin", "Manager")),
    NavigationItem("Pay Later", Icons.Default.HistoryEdu, Screen.PayLater, listOf("Admin", "Manager", "Cashier")),
    NavigationItem("Supply Marketplace", Icons.Default.ShoppingCart, Screen.SupplyMarketplace, listOf("Admin", "Manager")),
    NavigationItem("Staff", Icons.Default.People, Screen.Staff, listOf("Admin", "Manager")),
    NavigationItem("Profile", Icons.Default.Person, Screen.OwnerProfile, listOf("Admin")),
    NavigationItem("Logout", Icons.Default.ExitToApp, null, emptyList(), isLogout = true)
)

@Composable
fun OwnerDrawerContent(
    currentRoute: String?,
    userRole: String = "Admin",
    onItemClick: (NavigationItem) -> Unit,
    restaurantName: String = "Tiffzy Owner"
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(DrawerBackground)
            .padding(16.dp)
    ) {
        // Drawer Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BrandLogo(modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "TIFFZY",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = restaurantName,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(bottom = 16.dp))

        // Navigation Items
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            ownerNavigationItems.forEach { item ->
                // Filter items based on role
                val isAuthorized = item.isLogout || 
                                 userRole.equals("Admin", ignoreCase = true) || 
                                 userRole.equals("Owner", ignoreCase = true) ||
                                 item.allowedRoles.any { it.equals(userRole, ignoreCase = true) }

                if (isAuthorized) {
                    val isSelected = currentRoute == item.screen?.route
                    DrawerItem(
                        item = item,
                        isSelected = isSelected,
                        onClick = { onItemClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun DrawerItem(
    item: NavigationItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = if (isSelected) SelectedItemColor else Color.Transparent,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = item.title,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerTopBar(
    title: String,
    restaurantName: String,
    onMenuClick: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = restaurantName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier
                    .padding(8.dp)
                    .background(TiffzyOrange, RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.White
                )
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}
