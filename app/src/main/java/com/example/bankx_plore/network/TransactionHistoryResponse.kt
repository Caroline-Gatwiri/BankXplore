package com.example.bankx_plore.network

import com.google.gson.annotations.SerializedName

data class TransactionHistoryResponse(
    val size: Int,
    val totalPages: Int,
    val page: Int,
    val content: List<Transaction>,
    val totalElements: Int
)

data class Transaction(
    @SerializedName("transaction_id")  val transactionId: String,
    @SerializedName("user_id")  val userId: Int,
    @SerializedName ("transactionType") val transactionType: String,
    @SerializedName("sender_phone_no")  val senderPhoneNo: String,
    @SerializedName("sender_id")  val senderId: String,
    @SerializedName("sender_bank_code")  val senderBankCode: String,
    @SerializedName("receiver_phone_no")   val receiverPhoneNo: String,
    @SerializedName("receiver_id")   val receiverId: String,
    @SerializedName("receiver_bank_code")   val receiverBankCode: String,
    @SerializedName("amount")  val amount: Double,
    @SerializedName("currency")   val currency: String,
    @SerializedName("reference_note")   val referenceNote: String,
    @SerializedName("transaction_fee")  val transactionFee: Double
)
