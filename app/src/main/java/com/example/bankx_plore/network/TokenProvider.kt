package com.example.bankx_plore.network

interface TokenProvider {
   suspend fun getToken(): String
}
