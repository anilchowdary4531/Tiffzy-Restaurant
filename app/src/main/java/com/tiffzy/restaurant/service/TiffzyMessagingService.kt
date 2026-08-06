package com.tiffzy.restaurant.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tiffzy.restaurant.data.local.SessionManager
import com.tiffzy.restaurant.data.model.RegisterFcmTokenRequest
import com.tiffzy.restaurant.data.remote.ApiService
import com.tiffzy.restaurant.util.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TiffzyMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var apiService: ApiService
    
    @Inject
    lateinit var sessionManager: SessionManager

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        
        scope.launch {
            val authToken = sessionManager.authToken.first()
            if (!authToken.isNullOrEmpty()) {
                try {
                    apiService.registerFcmToken(RegisterFcmTokenRequest(token))
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to register token", e)
                }
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        val isOrderEvent = remoteMessage.data.containsKey("orderId") || 
                          remoteMessage.data["type"]?.contains("ORDER", ignoreCase = true) == true ||
                          remoteMessage.notification?.title?.contains("Order", ignoreCase = true) == true
        
        if (!isOrderEvent) return

        remoteMessage.notification?.let {
            val orderId = remoteMessage.data["orderId"]?.toIntOrNull() ?: 0
            NotificationHelper.sendOrderNotification(this, it.title ?: "Order Update", it.body ?: "", orderId)
        } ?: run {
            if (remoteMessage.data.isNotEmpty()) {
                val title = remoteMessage.data["title"] ?: "Order Update"
                val message = remoteMessage.data["body"] ?: remoteMessage.data["message"] ?: "Your order has been updated"
                val orderId = remoteMessage.data["orderId"]?.toIntOrNull() ?: 0
                NotificationHelper.sendOrderNotification(this, title, message, orderId)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    companion object {
        private const val TAG = "TiffzyMessagingService"
    }
}
