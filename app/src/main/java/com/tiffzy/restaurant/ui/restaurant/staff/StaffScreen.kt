package com.tiffzy.restaurant.ui.restaurant.staff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.data.model.CreateStaffRequest
import com.tiffzy.restaurant.data.model.StaffMember
import com.tiffzy.restaurant.ui.components.TiffzyErrorState
import com.tiffzy.restaurant.ui.components.TiffzyLoadingIndicator
import com.tiffzy.restaurant.ui.restaurant.components.OwnerShell
import com.tiffzy.restaurant.ui.theme.TiffzyOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffScreen(
    navController: NavController,
    onLogout: () -> Unit,
    viewModel: StaffViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var staffToEdit by remember { mutableStateOf<StaffMember?>(null) }

    OwnerShell(
        title = "Staff",
        restaurantName = "Staff Directory",
        navController = navController,
        onLogout = onLogout,
        actions = {
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.PersonAdd, "Add Staff")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = { Text("Search staff by name or role...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(12.dp)
            )

            when (val state = uiState) {
                is UiState.Loading -> TiffzyLoadingIndicator()
                is UiState.Error -> TiffzyErrorState(state.message, viewModel::loadStaff)
                is UiState.Success -> {
                    val filtered = state.data.filter {
                        searchQuery.isEmpty() || 
                        it.name.contains(searchQuery, ignoreCase = true) ||
                        it.role.contains(searchQuery, ignoreCase = true)
                    }
                    StaffList(
                        staff = filtered, 
                        onToggleStatus = viewModel::toggleStaffStatus, 
                        onGetLink = viewModel::getAccessLink,
                        onEdit = { staffToEdit = it }
                    )
                }
                else -> {}
            }
        }
        
        if (showAddDialog) {
            AddEditStaffDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { request ->
                    viewModel.createStaff(request) { showAddDialog = false }
                }
            )
        }

        staffToEdit?.let { staff ->
            AddEditStaffDialog(
                staff = staff,
                onDismiss = { staffToEdit = null },
                onConfirm = { request ->
                    viewModel.updateStaff(staff.id, request) { staffToEdit = null }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditStaffDialog(
    staff: StaffMember? = null,
    onDismiss: () -> Unit,
    onConfirm: (CreateStaffRequest) -> Unit
) {
    var name by remember { mutableStateOf(staff?.name ?: "") }
    var email by remember { mutableStateOf(staff?.email ?: "") }
    var phone by remember { mutableStateOf(staff?.phone ?: "") }
    var role by remember { mutableStateOf(staff?.role ?: "Waiter") }
    val roles = listOf("Admin", "Manager", "Chef", "Cashier", "Waiter")
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (staff == null) "Add New Staff" else "Edit Staff Details") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        roles.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r) },
                                onClick = {
                                    role = r
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Detailed role-based permissions info
                Surface(
                    color = TiffzyOrange.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "AUTHORIZED ACCESS",
                            style = MaterialTheme.typography.labelSmall,
                            color = TiffzyOrange,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val permissions = when(role) {
                            "Admin" -> listOf("All Management Features", "Financial Reports", "Staff & Permissions", "Restaurant Profile")
                            "Manager" -> listOf("Dashboard Overview", "Menu Management", "Table & QR Control", "Staff Directory")
                            "Chef" -> listOf("Kitchen Live View", "Menu Availability", "Live Order Tracking")
                            "Cashier" -> listOf("Billing Desk", "Online Orders", "Pay Later (Khata)", "Live Orders")
                            else -> listOf("Table Service", "Live Order Status", "Billing Desk (Limited)")
                        }
                        
                        permissions.forEach { permission ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                                Icon(Icons.Default.Check, null, tint = TiffzyOrange, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = permission, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(CreateStaffRequest(name, email, phone, role, staff?.permissions ?: emptyList()))
                },
                enabled = name.isNotBlank() && email.isNotBlank() && phone.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = TiffzyOrange)
            ) {
                Text(if (staff == null) "CREATE" else "UPDATE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL") }
        }
    )
}

@Composable
fun StaffList(
    staff: List<StaffMember>,
    onToggleStatus: (Int, Boolean) -> Unit,
    onGetLink: (Int, (String) -> Unit) -> Unit,
    onEdit: (StaffMember) -> Unit
) {
    // Defensive check for null from API
    @Suppress("SENSELESS_COMPARISON")
    val list = if (staff == null) emptyList() else staff
    
    if (list.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No staff members found", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(list) { member ->
                StaffCard(member, onToggleStatus, onGetLink, onEdit)
            }
        }
    }
}

@Composable
fun StaffCard(
    member: StaffMember,
    onToggleStatus: (Int, Boolean) -> Unit,
    onGetLink: (Int, (String) -> Unit) -> Unit,
    onEdit: (StaffMember) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = member.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(text = member.role.uppercase(), color = TiffzyOrange, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onEdit(member) }) {
                        Icon(Icons.Default.Edit, "Edit", tint = Color.Gray, modifier = Modifier.size(20.dp))
                    }
                    Switch(
                        checked = member.isActive,
                        onCheckedChange = { onToggleStatus(member.id, member.isActive) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Email, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = member.email, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Phone, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = member.phone, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Show Access Tags (Explicit Page Access)
            Text(text = "ACCESSIBLE PAGES", style = MaterialTheme.typography.labelSmall, color = Color.LightGray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val access = when(member.role) {
                    "Admin" -> listOf("All Pages", "Profile", "Analytics")
                    "Manager" -> listOf("Dashboard", "Staff", "Menu", "Tables", "Inventory")
                    "Chef" -> listOf("Kitchen Live", "Menu Availability", "Live Orders")
                    "Cashier" -> listOf("Billing Desk", "Online Orders", "Khata", "History")
                    else -> listOf("Live Orders", "Tables", "Billing")
                }
                access.forEach { tag ->
                    AccessTag(tag)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.5f))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { onGetLink(member.id) { /* Share link */ } }) {
                    Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SHARE ACCESS LINK")
                }
            }
        }
    }
}

@Composable
fun AccessTag(label: String) {
    Surface(
        color = TiffzyOrange.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.padding(bottom = 6.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 10.sp,
            color = TiffzyOrange,
            fontWeight = FontWeight.Bold
        )
    }
}
