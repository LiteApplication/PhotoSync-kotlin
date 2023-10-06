package fr.liteapp.photosynckt.network

import android.util.Log
import fr.liteapp.photosynckt.TAG
import okhttp3.Interceptor

/**
 * This interceptor forces the API to consider any response with a non-200 status code as valid.
 */
class HttpExceptionInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        val response = chain.proceed(request)
        Log.d(TAG, "HttpExceptionInterceptor:\n${response.peekBody(1024).string()}")

        if (response.isSuccessful) {
            return response
        }
        return response.newBuilder()
            .code(200)
            .build()
    }
}
