package com.tiffzy.restaurant.ui.auth

import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.tiffzy.restaurant.core.base.BaseViewModel
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.model.LoginResponse
import com.tiffzy.restaurant.data.model.RegisterRequest
import com.tiffzy.restaurant.data.model.ResetPasswordRequest
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

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<UiState<Any>>(UiState.Idle)
    val uiState: StateFlow<UiState<Any>> = _uiState.asStateFlow()

    private val _timerValue = MutableStateFlow(0)
    val timerValue: StateFlow<Int> = _timerValue.asStateFlow()

    private var timerJob: Job? = null

    // Forms
    var phone = MutableStateFlow("")
    var email = MutableStateFlow("")
    var password = MutableStateFlow("")
    var name = MutableStateFlow("")
    var restaurantName = MutableStateFlow("")
    var otp = MutableStateFlow("")
    var newPassword = MutableStateFlow("")
    var rememberMe = MutableStateFlow(true)

    fun sendOtp(isForgotPassword: Boolean = false) {
        val targetPhone = phone.value
        val targetEmail = email.value

        if (isForgotPassword && targetEmail.isEmpty()) {
            _uiState.value = UiState.Error("Email is required")
            return
        }
        if (!isForgotPassword && targetPhone.length < 10) {
            _uiState.value = UiState.Error("Enter a valid 10-digit phone number")
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = if (isForgotPassword) {
                repository.forgotPassword(targetEmail)
            } else {
                repository.sendOtp(targetPhone)
            }

            when (result) {
                is Resource.Success -> {
                    _uiState.value = UiState.Success(result.data)
                    startTimer()
                }
                is Resource.Error -> {
                    _uiState.value = UiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun verifyOtp() {
        if (otp.value.length != 6) {
            _uiState.value = UiState.Error("Enter a valid 6-digit OTP")
            return
        }
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = repository.verifyOtp(phone.value, otp.value)) {
                is Resource.Success -> {
                    _uiState.value = UiState.Success(result.data)
                    registerFcmToken()
                }
                is Resource.Error -> {
                    _uiState.value = UiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun login() {
        if (email.value.isEmpty() || password.value.isEmpty()) {
            _uiState.value = UiState.Error("Email and password are required")
            return
        }
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = repository.login(email.value, password.value)) {
                is Resource.Success -> {
                    _uiState.value = UiState.Success(result.data)
                    registerFcmToken()
                }
                is Resource.Error -> {
                    _uiState.value = UiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun register() {
        if (name.value.isEmpty() || email.value.isEmpty() || phone.value.isEmpty() || 
            password.value.isEmpty() || restaurantName.value.isEmpty()) {
            _uiState.value = UiState.Error("All fields are required")
            return
        }
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val request = RegisterRequest(
                name = name.value,
                email = email.value,
                phone = phone.value,
                password = password.value,
                restaurantName = restaurantName.value
            )
            when (val result = repository.register(request)) {
                is Resource.Success -> {
                    _uiState.value = UiState.Success(result.data)
                    registerFcmToken()
                }
                is Resource.Error -> {
                    _uiState.value = UiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun resetPassword() {
        if (email.value.isEmpty() || otp.value.isEmpty() || newPassword.value.isEmpty()) {
            _uiState.value = UiState.Error("All fields are required")
            return
        }
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val request = ResetPasswordRequest(
                email = email.value,
                otp = otp.value,
                newPassword = newPassword.value
            )
            when (val result = repository.resetPassword(request)) {
                is Resource.Success -> {
                    _uiState.value = UiState.Success(result.data)
                }
                is Resource.Error -> {
                    _uiState.value = UiState.Error(result.message)
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

    fun resetState() {
        _uiState.value = UiState.Idle
    }

    suspend fun checkAuthStatus(): Boolean {
        val token = repository.getAuthToken()
        return token != null
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.value = UiState.Idle
        }
    }

    suspend fun isOnboardingCompleted(): Boolean {
        return repository.isOnboardingCompleted()
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            repository.setOnboardingCompleted()
        }
    }
}
