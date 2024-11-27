package com.example.bankx_plore.ui

import androidx.compose.ui.graphics.Color

data class LinkedAccountsResponse(
    val status: Int,
    val message: String,
    val payload: List<LinkedAccount>
)

data class LinkedAccount(
    val id: Int,
    val bankId: Int,
    val accountNumber: String
)

// Account UI model
data class Account(
    val bankName: String,
    val accountNumber: String,
    val bankCode: Int,
    val totalFunds: String = "KES 0.00",
    val bankLogo: Int,
    val cardColor: Color
)