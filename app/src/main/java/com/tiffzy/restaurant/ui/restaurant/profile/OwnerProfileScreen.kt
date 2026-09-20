package com.tiffzy.restaurant.ui.restaurant.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tiffzy.restaurant.data.model.RestaurantSettings
import com.tiffzy.restaurant.data.model.RestaurantSettingsUpdateRequest
import com.tiffzy.restaurant.ui.components.TiffzyErrorState
import com.tiffzy.restaurant.ui.components.TiffzyLoadingIndicator
import com.tiffzy.restaurant.ui.restaurant.RestaurantSettingsViewModel
import com.tiffzy.restaurant.ui.restaurant.SettingsUiState
import com.tiffzy.restaurant.ui.restaurant.components.OwnerShell
import com.tiffzy.restaurant.ui.restaurant.components.UserRoleViewModel
import com.tiffzy.restaurant.ui.theme.Dimens
import com.tiffzy.restaurant.ui.theme.TiffzyOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerProfileScreen(
    navController: NavController,
    onLogout: () -> Unit,
    viewModel: RestaurantSettingsViewModel = hiltViewModel(),
    userRoleViewModel: UserRoleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val userRole by userRoleViewModel.userRole.collectAsState()

    OwnerShell(
        title = "Profile",
        restaurantName = if (uiState is SettingsUiState.Success) (uiState as SettingsUiState.Success).settings.name else "Tiffzy Owner",
        navController = navController,
        onLogout = onLogout
    ) { innerPadding ->
        if (userRole != "Admin") {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Icon(Icons.Default.Lock, null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Access Restricted", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your current role is \"$userRole\". Only Admins can modify restaurant profile settings.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            when (val state = uiState) {
                is SettingsUiState.Loading -> TiffzyLoadingIndicator()
                is SettingsUiState.Error -> TiffzyErrorState(
                    message = state.message,
                    onRetry = { viewModel.loadSettings() },
                    modifier = Modifier.padding(innerPadding)
                )
                is SettingsUiState.Success -> {
                    SettingsContent(
                        settings = state.settings,
                        userRole = userRole,
                        isSaving = isSaving,
                        onSave = { viewModel.updateSettings(it) { /* Show Toast */ } },
                        modifier = Modifier.fillMaxSize().padding(innerPadding)
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
fun SettingsContent(
    settings: RestaurantSettings,
    userRole: String,
    isSaving: Boolean,
    onSave: (RestaurantSettingsUpdateRequest) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(settings.name) }
    var legalName by remember { mutableStateOf(settings.legalName ?: "") }
    var phone by remember { mutableStateOf(settings.phone ?: "") }
    var email by remember { mutableStateOf(settings.email ?: "") }
    var address by remember { mutableStateOf(settings.addressLine1 ?: "") }
    var city by remember { mutableStateOf(settings.city ?: "") }
    var pincode by remember { mutableStateOf(settings.pincode ?: "") }
    var upiId by remember { mutableStateOf(settings.upiId ?: "") }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(Dimens.PaddingLarge),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMedium)
    ) {
        // My Access Section
        Surface(
            color = TiffzyOrange.copy(alpha = 0.08f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when(userRole.uppercase()) {
                        "ADMIN" -> Icons.Default.VerifiedUser
                        "MANAGER" -> Icons.Default.Security
                        else -> Icons.Default.Person
                    }, 
                    null, 
                    tint = TiffzyOrange, 
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = "${userRole.uppercase()} ACCESS", style = MaterialTheme.typography.labelSmall, color = TiffzyOrange, fontWeight = FontWeight.Bold)
                    Text(
                        text = when(userRole.uppercase()) {
                            "ADMIN" -> "Full System Control"
                            "MANAGER" -> "High-Level Management"
                            else -> "Standard Operations"
                        }, 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        SectionTitle("BUSINESS PROFILE")
        
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Restaurant Name") },
            leadingIcon = { Icon(Icons.Default.Business, null) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = legalName,
            onValueChange = { legalName = it },
            label = { Text("Legal Name (for invoices)") },
            modifier = Modifier.fillMaxWidth()
        )

        SectionTitle("CONTACT DETAILS")

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            leadingIcon = { Icon(Icons.Default.Phone, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            leadingIcon = { Icon(Icons.Default.Email, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        SectionTitle("LOCATION")

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") },
            leadingIcon = { Icon(Icons.Default.LocationOn, null) },
            modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall)) {
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = pincode,
                onValueChange = { pincode = it },
                label = { Text("Pincode") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        SectionTitle("PAYMENTS")

        OutlinedTextField(
            value = upiId,
            onValueChange = { upiId = it },
            label = { Text("UPI ID") },
            leadingIcon = { Icon(Icons.Default.QrCode, null) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onSave(
                    RestaurantSettingsUpdateRequest(
                        name = name,
                        legalName = legalName,
                        phone = phone,
                        email = email,
                        addressLine1 = address,
                        city = city,
                        pincode = pincode,
                        upiId = upiId
                    )
                )
            },
            modifier = Modifier.fillMaxWidth().height(Dimens.ButtonHeight),
            shape = MaterialTheme.shapes.medium,
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("SAVE SETTINGS", style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(top = Dimens.PaddingMedium, bottom = Dimens.PaddingSmall)
    )
}
