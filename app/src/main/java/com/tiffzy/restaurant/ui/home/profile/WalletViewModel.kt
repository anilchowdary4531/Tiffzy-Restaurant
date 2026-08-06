package com.tiffzy.restaurant.ui.home.profile

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.phonepe.intent.sdk.api.PhonePe
import com.phonepe.intent.sdk.api.models.PhonePeEnvironment
import com.tiffzy.restaurant.core.base.BaseViewModel
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.model.*
import com.tiffzy.restaurant.data.repository.ProfileRepository
import com.tiffzy.restaurant.ui.home.payment.PaymentStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val repository: ProfileRepository,
    @ApplicationContext private val context: Context
) : BaseViewModel() {

    private val _walletState = MutableStateFlow<UiState<WalletHistoryResponse>>(UiState.Loading)
    val walletState: StateFlow<UiState<WalletHistoryResponse>> = _walletState.asStateFlow()

    private val _rechargeStatus = MutableStateFlow<PaymentStatus>(PaymentStatus.Idle)
    val rechargeStatus: StateFlow<PaymentStatus> = _rechargeStatus.asStateFlow()

    private var currentRechargeId: Int = 0

    init {
        fetchWalletHistory()
    }

    fun fetchWalletHistory() {
        viewModelScope.launch {
            _walletState.value = UiState.Loading
            when (val result = repository.getWalletHistory()) {
                is Resource.Success -> _walletState.value = UiState.Success(result.data)
                is Resource.Error -> _walletState.value = UiState.Error(result.message)
                else -> {}
            }
        }
    }

    fun initiateRecharge(amount: Double) {
        viewModelScope.launch {
            _rechargeStatus.value = PaymentStatus.Processing
            when (val result = repository.rechargeWallet(amount, "phonepe")) {
                is Resource.Success -> {
                    val phonepeData = result.data.phonepe
                    currentRechargeId = result.data.rechargeId
                    if (phonepeData != null) {
                        try {
                            PhonePe.init(context, PhonePeEnvironment.RELEASE, phonepeData.merchantId, phonepeData.appId)
                            val intent = PhonePe.getImplicitIntent(
                                context,
                                phonepeData.base64Payload,
                                phonepeData.checksum,
                                phonepeData.apiEndPoint
                            )
                            if (intent != null) {
                                _rechargeStatus.value = PaymentStatus.PaymentInitiated(intent)
                            } else {
                                _rechargeStatus.value = PaymentStatus.Failure("Could not initialize PhonePe")
                            }
                        } catch (e: Exception) {
                            _rechargeStatus.value = PaymentStatus.Failure(e.localizedMessage ?: "Error")
                        }
                    }
                }
                is Resource.Error -> _rechargeStatus.value = PaymentStatus.Failure(result.message)
                else -> {}
            }
        }
    }

    fun onPaymentResult(success: Boolean) {
        if (success) {
            verifyRecharge()
        } else {
            _rechargeStatus.value = PaymentStatus.Failure("Payment Failed")
        }
    }

    private fun verifyRecharge() {
        viewModelScope.launch {
            _rechargeStatus.value = PaymentStatus.Verifying
            val request = VerifyPaymentRequest(
                status = "SUCCESS",
                paymentMode = "online"
            )
            when (val result = repository.verifyRecharge(request)) {
                is Resource.Success -> {
                    if (result.data.verified) {
                        _rechargeStatus.value = PaymentStatus.Success("WALLET")
                        fetchWalletHistory()
                    } else {
                        _rechargeStatus.value = PaymentStatus.Failure("Verification failed")
                    }
                }
                is Resource.Error -> _rechargeStatus.value = PaymentStatus.Failure(result.message)
                else -> {}
            }
        }
    }

    fun resetRechargeStatus() {
        _rechargeStatus.value = PaymentStatus.Idle
    }
}
