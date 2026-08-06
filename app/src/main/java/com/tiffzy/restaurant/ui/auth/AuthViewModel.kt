package com.tiffzy.restaurant.ui.auth

import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.tiffzy.restaurant.core.base.BaseViewModel
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object OtpSent : AuthUiState()
    object Authenticated : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _timerValue = MutableStateFlow(0)
    val timerValue: StateFlow<Int> = _timerValue.asStateFlow()

    private var timerJob: Job? = null

    var phone: String = ""
    var email: String? = null
    var otp: String = ""
    var name: String? = null
    
    var staffEmail: String = ""
    var staffPassword: String = ""

    fun sendOtp() {
        if (phone.length < 10) {
            _uiState.value = AuthUiState.Error("Enter a valid 10-digit phone number")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = repository.sendOtp(phone, email)) {
                is Resource.Success -> {
                    _uiState.value = AuthUiState.OtpSent
                    startTimer()
                }
                is Resource.Error -> {
                    _uiState.value = AuthUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun verifyOtp() {
        if (otp.length != 6) {
            _uiState.value = AuthUiState.Error("Enter a valid 6-digit OTP")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = repository.verifyOtp(phone, otp, name, email)) {
                is Resource.Success -> {
                    _uiState.value = AuthUiState.Authenticated
                    registerFcmToken()
                }
                is Resource.Error -> {
                    _uiState.value = AuthUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    private fun registerFcmToken() {
        viewModelScope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                repository.registerFcmToken(token)
            } catch (e: Exception) {
                // Ignore FCM failure
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        _timerValue.value = 60
        timerJob = viewModelScope.launch {
            while (_timerValue.value > 0) {
                delay(1000)
                _timerValue.value -= 1
            }
        }
    }

    fun resendOtp() {
        if (_timerValue.value == 0) {
            sendOtp()
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    fun staffLogin() {
        if (staffEmail.isEmpty() || staffPassword.isEmpty()) {
            _uiState.value = AuthUiState.Error("Email and password are required")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = repository.login(staffEmail, staffPassword)) {
                is Resource.Success -> {
                    _uiState.value = AuthUiState.Authenticated
                }
                is Resource.Error -> {
                    _uiState.value = AuthUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    suspend fun getAccountType(): String? = repository.getAccountType()

    suspend fun checkAuthStatus(): Boolean {
        val token = repository.getAuthToken()
        return if (token != null) {
            _uiState.value = AuthUiState.Authenticated
            registerFcmToken()
            true
        } else {
            false
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.value = AuthUiState.Idle
        }
    }
}
