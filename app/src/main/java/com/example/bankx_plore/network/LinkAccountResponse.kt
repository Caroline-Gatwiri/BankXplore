package com.example.bankx_plore.network

data class LinkAccountResponse(
    val status: Int,                // e.g., 409 or 200
    val message: String,            // e.g., "Error occurred" or "Success"
    val payload: String?            // e.g., "Failed to link account..please counter check the account number and bank name"
)
