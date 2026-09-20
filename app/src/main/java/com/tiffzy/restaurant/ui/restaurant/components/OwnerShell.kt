package com.tiffzy.restaurant.ui.restaurant.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.tiffzy.restaurant.util.ConnectivityObserver
import com.tiffzy.restaurant.util.NetworkStatusViewModel
import kotlinx.coroutines.launch

@Composable
fun OwnerShell(
    title: String,
    restaurantName: String,
    navController: NavController,
    onLogout: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    networkStatusViewModel: NetworkStatusViewModel = hiltViewModel(),
    userRoleViewModel: UserRoleViewModel = hiltViewModel(),
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val networkStatus by networkStatusViewModel.networkStatus.collectAsState()
    val userRole by userRoleViewModel.userRole.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            OwnerDrawerContent(
                currentRoute = currentRoute,
                userRole = userRole,
                restaurantName = restaurantName,
                onItemClick = { item ->
                    scope.launch { drawerState.close() }
                    if (item.isLogout) {
                        onLogout()
                    } else {
                        item.screen?.let { screen ->
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                Column {
                    OwnerTopBar(
                        title = title,
                        restaurantName = restaurantName,
                        onMenuClick = {
                            scope.launch { drawerState.open() }
                        },
                        actions = actions
                    )
                    
                    if (networkStatus != ConnectivityObserver.Status.Available) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WifiOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Offline Mode: No Internet Connection",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            content = content
        )
    }
}
