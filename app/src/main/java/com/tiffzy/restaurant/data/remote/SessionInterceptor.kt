package com.tiffzy.restaurant.data.remote

import com.tiffzy.restaurant.data.local.SessionManager
import com.tiffzy.restaurant.util.SessionEvent
import com.tiffzy.restaurant.util.SessionEventBus
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class SessionInterceptor @Inject constructor(
    private val sessionManager: SessionManager,
    private val eventBus: SessionEventBus
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        if (response.code == 401) {
            runBlocking {
                sessionManager.logout()
                eventBus.emit(SessionEvent.SessionExpired)
            }
        }

        return response
    }
}
