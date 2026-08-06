package com.tiffzy.restaurant.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.data.model.SendOtpResponse
import com.tiffzy.restaurant.data.model.VerifyOtpResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpLoginScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onAuthSuccess: () -> Unit
) {
    val phone by viewModel.phone.collectAsState()
    val otp by viewModel.otp.collectAsState()
    val timerValue by viewModel.timerValue.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var isOtpSent by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) {
            val data = (uiState as UiState.Success).data
            if (data is SendOtpResponse) {
                isOtpSent = true
            } else if (data is VerifyOtpResponse) {
                onAuthSuccess()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OTP Login") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isOtpSent) {
                Text(
                    text = "Enter your phone number to receive an OTP",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                OutlinedTextField(
                    value = phone,
                    onValueChange = { viewModel.phone.value = it },
                    label = { Text("Phone Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { viewModel.sendOtp() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = uiState !is UiState.Loading
                ) {
                    if (uiState is UiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Send OTP")
                    }
                }
            } else {
                Text(
                    text = "We've sent a 6-digit code to",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = phone,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                OutlinedTextField(
                    value = otp,
                    onValueChange = { if (it.length <= 6) viewModel.otp.value = it },
                    label = { Text("Enter OTP") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (timerValue > 0) "Resend in ${timerValue}s" else "Didn't receive code?",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    TextButton(
                        onClick = { viewModel.sendOtp() },
                        enabled = timerValue == 0 && uiState !is UiState.Loading
                    ) {
                        Text("Resend OTP")
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { viewModel.verifyOtp() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = otp.length == 6 && uiState !is UiState.Loading
                ) {
                    if (uiState is UiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Verify & Login")
                    }
                }

                TextButton(onClick = { isOtpSent = false }) {
                    Text("Change Phone Number")
                }
            }

            if (uiState is UiState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (uiState as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
