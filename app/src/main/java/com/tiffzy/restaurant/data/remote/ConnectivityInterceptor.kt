package com.tiffzy.restaurant.data.remote

import com.tiffzy.restaurant.util.NetworkHelper
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

class ConnectivityInterceptor @Inject constructor(
    private val networkHelper: NetworkHelper
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!networkHelper.isNetworkConnected()) {
            throw NoConnectivityException()
        }
        return chain.proceed(chain.request())
    }
}

class NoConnectivityException : IOException() {
    override val message: String
        get() = "No internet connection. Please check your network settings."
}
