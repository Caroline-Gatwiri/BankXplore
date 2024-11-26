package com.example.bankx_plore.network


data class LinkAccountRequest(
    val bankId: Int,
    val accountNumber: String
)
