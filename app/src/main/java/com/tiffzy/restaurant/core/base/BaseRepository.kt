package com.tiffzy.restaurant.core.base

import com.tiffzy.restaurant.core.result.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.UnknownHostException

abstract class BaseRepository {

    suspend fun <T> safeApiCall(apiCall: suspend () -> T): Resource<T> {
        return withContext(Dispatchers.IO) {
            try {
                Resource.Success(apiCall.invoke())
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
                when (throwable) {
                    is UnknownHostException -> Resource.Error("Server not reachable. Please check your internet connection or verify the server address (api.tiffzy.com).")
                    is IOException -> Resource.Error(throwable.message ?: "Network Failure: Please check your internet connection")
                    is HttpException -> {
                        val code = throwable.code()
                        val errorResponse = throwable.response()?.errorBody()?.string()
                        Resource.Error(errorResponse ?: "Something went wrong", code)
                    }
                    else -> Resource.Error(throwable.message ?: "Unknown Error")
                }
            }
        }
    }
}
