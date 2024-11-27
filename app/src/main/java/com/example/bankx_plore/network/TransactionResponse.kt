package com.example.bankx_plore.network

import com.google.gson.annotations.SerializedName

data class TransactionResponse(
    @SerializedName("transaction_id") val transactionId: String,
    val status: String,
    val message: String,
    @SerializedName("transaction_fee") val transactionFee: Double, // Added for API compatibility
    @SerializedName("confirmation_code") val confirmationCode: String?, // Nullable for optional field
    val timestamp: String
)
