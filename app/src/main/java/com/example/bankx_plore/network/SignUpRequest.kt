package com.example.bankx_plore.network

import com.google.gson.annotations.SerializedName

data class SignUpRequest(
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("middle_name")
    val middleName: String,
    @SerializedName("last_name")
    val lastName: String,
    val email: String,
    @SerializedName("phone_no")
    val phoneNo: String,
    val password: String,
    val authorities: String = "USER"
)

