package com.example.bankx_plore.network

import com.google.gson.annotations.SerializedName


data class Payload(
    @SerializedName("token") val token: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("id") val id: String?
)