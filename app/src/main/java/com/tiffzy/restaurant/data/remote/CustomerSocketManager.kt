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
class CustomerSocketManager @Inject constructor(private val gson: Gson) {
    private var socket: Socket? = null

    fun connect(context: Context, baseUrl: String, token: String, onOrderUpdate: (OrderDetails) -> Unit) {
        if (socket?.connected() == true) return
        
        try {
            val opts = IO.Options()
            opts.auth = mapOf("token" to token)
            
            val url = if (baseUrl.endsWith("/")) baseUrl.substring(0, baseUrl.length - 1) else baseUrl
            socket = IO.socket("$url/customer", opts)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Socket connected to /customer")
            }

            socket?.on("order:status_updated") { args ->
                val data = args.getOrNull(0) as? JSONObject
                data?.let {
                    try {
                        val order = gson.fromJson(it.toString(), OrderDetails::class.java)
                        onOrderUpdate(order)
                        
                        NotificationHelper.sendOrderNotification(
                            context = context,
                            title = "Order ${order.status.replace("_", " ").lowercase()}",
                            message = "Your order #${order.orderNo.takeLast(6)} is now ${order.status}",
                            orderId = order.id,
                            isStaff = false
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing order update", e)
                    }
                }
            }

            socket?.connect()
        } catch (e: Exception) {
            Log.e(TAG, "Socket failed", e)
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
    }

    companion object {
        private const val TAG = "CustomerSocket"
    }
}
