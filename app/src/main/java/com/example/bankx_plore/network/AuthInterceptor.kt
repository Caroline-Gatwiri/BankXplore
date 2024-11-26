package com.example.bankx_plore.network

import android.util.Base64
import android.util.Log
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class AuthInterceptor(private val tokenProvider: TokenProvider) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        val token = runBlocking { tokenProvider.getToken() }

        if (token.isNotEmpty()) {
            if (isTokenExpired(token)) {
                Log.e("AuthInterceptor", "Token expired. Redirecting to login.")
                throw IOException("Token expired")
            }
            requestBuilder.addHeader("Authorization", "Bearer $token")
        } else {
            Log.e("AuthInterceptor", "No token found. Redirecting to login.")
            throw IOException("No token available")
        }

        Log.d("AuthInterceptor", "Token: $token")
        return chain.proceed(requestBuilder.build())
    }

    // Function to check token expiry
    private fun isTokenExpired(token: String): Boolean {
        val parts = token.split(".")
        if (parts.size == 3) {
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val exp = JSONObject(payload).optLong("exp")
            return System.currentTimeMillis() / 1000 > exp
        }
        return true
    }

}
