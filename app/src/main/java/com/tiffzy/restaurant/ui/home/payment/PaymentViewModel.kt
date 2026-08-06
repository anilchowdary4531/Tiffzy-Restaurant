package com.tiffzy.restaurant.ui.home.payment

import android.content.Context
import android.content.Intent
import androidx.lifecycle.viewModelScope
import com.phonepe.intent.sdk.api.PhonePe
import com.phonepe.intent.sdk.api.models.PhonePeEnvironment
import com.tiffzy.restaurant.MainActivity
import com.tiffzy.restaurant.core.base.BaseViewModel
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.model.*
import com.tiffzy.restaurant.data.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PaymentStatus {
    object Idle : PaymentStatus()
    object Processing : PaymentStatus()
    data class PaymentInitiated(val intent: Intent) : PaymentStatus()
    object Verifying : PaymentStatus()
    data class Success(val orderNo: String) : PaymentStatus()
    data class Failure(val message: String) : PaymentStatus()
    object Pending : PaymentStatus()
}

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val repository: PaymentRepository,
    @ApplicationContext private val context: Context
) : BaseViewModel() {

    private val _paymentStatus = MutableStateFlow<PaymentStatus>(PaymentStatus.Idle)
    val paymentStatus: StateFlow<PaymentStatus> = _paymentStatus.asStateFlow()

    private var currentOrderId: Int = 0
    private var currentTransactionId: String? = null

    fun initiatePhonePePayment(orderId: Int) {
        currentOrderId = orderId
        viewModelScope.launch {
            _paymentStatus.value = PaymentStatus.Processing
            when (val result = repository.createPayment(orderId, "phonepe")) {
                is Resource.Success -> {
                    val phonepeData = result.data.phonepe
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
                                _paymentStatus.value = PaymentStatus.PaymentInitiated(intent)
                            } else {
                                _paymentStatus.value = PaymentStatus.Failure("Could not initialize PhonePe intent")
                            }
                        } catch (e: Exception) {
                            _paymentStatus.value = PaymentStatus.Failure(e.localizedMessage ?: "PhonePe Error")
                        }
                    } else {
                        _paymentStatus.value = PaymentStatus.Failure("PhonePe data missing from server")
                    }
                }
                is Resource.Error -> {
                    _paymentStatus.value = PaymentStatus.Failure(result.message)
                }
                else -> {}
            }
        }
    }

    fun onPaymentResult(success: Boolean, status: String?) {
        if (success) {
            verifyPayment()
        } else {
            _paymentStatus.value = PaymentStatus.Failure(status ?: "Payment Failed")
        }
    }

    fun verifyPayment() {
        viewModelScope.launch {
            _paymentStatus.value = PaymentStatus.Verifying
            val request = VerifyPaymentRequest(
                orderId = currentOrderId,
                paymentMode = "online",
                status = "SUCCESS"
            )
            when (val result = repository.verifyPayment(request)) {
                is Resource.Success -> {
                    if (result.data.verified) {
                        _paymentStatus.value = PaymentStatus.Success(result.data.order?.orderNo ?: "N/A")
                    } else {
                        _paymentStatus.value = PaymentStatus.Failure("Payment verification failed")
                    }
                }
                is Resource.Error -> {
                    _paymentStatus.value = PaymentStatus.Failure(result.message)
                }
                else -> {}
            }
        }
    }

    fun resetState() {
        _paymentStatus.value = PaymentStatus.Idle
    }
}
