package com.example.bankx_plore.network

import com.google.gson.annotations.SerializedName


data class TransactionRequest(
    var pin: String,
    @SerializedName("transactionRequest") val transactionDetails: TransactionDetails
)

data class TransactionDetails(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("transaction_id") val transactionId: String,
    @SerializedName("transactionType") val transactionType: String,
    @SerializedName("sender_phone_no") val senderPhoneNo: String,
    @SerializedName("sender_id") val senderAccountNumber: String,
    @SerializedName("sender_bank_code") val senderBankCode: String,
    @SerializedName("receiver_id") val receiverAccountNumber: String,
    @SerializedName("receiver_phone_no") val receiverPhoneNo: String,
    @SerializedName("receiver_bank_code") val receiverBankCode: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("currency") val currency: String,
    @SerializedName("transaction_fee") val transactionFee: Double,
    @SerializedName("reference_note") val referenceNote: String
)


