package com.tiffzy.restaurant.data.remote

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.tiffzy.restaurant.data.model.OrderDetails
import com.tiffzy.restaurant.util.NotificationHelper
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestaurantSocketManager @Inject constructor(private val gson: Gson) {
    private var socket: Socket? = null

    fun connect(context: Context, baseUrl: String, token: String, onOrderUpdate: (OrderDetails) -> Unit) {
        if (socket?.connected() == true) return
        
        try {
            val opts = IO.Options()
            opts.auth = mapOf("token" to token)
            
            val url = if (baseUrl.endsWith("/")) baseUrl.substring(0, baseUrl.length - 1) else baseUrl
            
            socket = IO.socket("$url/staff", opts)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Socket connected to /staff")
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e(TAG, "Socket connect error: ${args.getOrNull(0)}")
            }

            socket?.on("order:created") { args ->
                val data = args.getOrNull(0) as? JSONObject
                data?.let {
                    try {
                        val order = gson.fromJson(it.toString(), OrderDetails::class.java)
                        onOrderUpdate(order)
                        
                        NotificationHelper.sendOrderNotification(
                            context = context,
                            title = "New Order Received!",
                            message = "Order #${order.orderNo.takeLast(6)} for ₹${order.total.toInt()}",
                            orderId = order.id,
                            isStaff = true
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing order:created data", e)
                    }
                }
            }

            socket?.on("order:updated") { args ->
                val data = args.getOrNull(0) as? JSONObject
                data?.let {
                    try {
                        val order = gson.fromJson(it.toString(), OrderDetails::class.java)
                        onOrderUpdate(order)
                        
                        if (order.status.uppercase() == "CANCELLED") {
                            NotificationHelper.sendOrderNotification(
                                context = context,
                                title = "Order Cancelled",
                                message = "Order #${order.orderNo.takeLast(6)} has been cancelled",
                                orderId = order.id,
                                isStaff = true
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing order:updated data", e)
                    }
                }
            }

            socket?.connect()
        } catch (e: Exception) {
            Log.e(TAG, "Socket initialization failed", e)
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
    }

    companion object {
        private const val TAG = "RestaurantSocket"
        
        // Manual singleton removed for Hilt injection
        @Volatile
        private var INSTANCE: RestaurantSocketManager? = null

        fun getInstance(): RestaurantSocketManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RestaurantSocketManager(Gson()).also { INSTANCE = it }
            }
        }
    }
}
