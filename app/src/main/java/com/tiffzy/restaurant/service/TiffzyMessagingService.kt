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
        
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Tiffzy Update"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: remoteMessage.data["message"] ?: ""
        val type = remoteMessage.data["type"] ?: "promotion"
        val orderId = remoteMessage.data["orderId"]?.toIntOrNull()

        when {
            orderId != null || type.contains("ORDER", ignoreCase = true) -> {
                NotificationHelper.sendOrderNotification(this, title, body, orderId ?: 0)
            }
            type.lowercase() == "offer" || type.lowercase() == "promotion" -> {
                NotificationHelper.sendPromotionNotification(this, title, body)
            }
            else -> {
                NotificationHelper.sendPromotionNotification(this, title, body)
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
