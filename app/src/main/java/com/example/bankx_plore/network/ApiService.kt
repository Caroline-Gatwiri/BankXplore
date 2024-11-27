package com.example.bankx_plore.network

import com.example.bankx_plore.repository.AccountRepository
import com.example.bankx_plore.ui.LinkedAccountsResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.Url


data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val status: Int?,
    val message: String?,
    val payload: Payload?
)



interface ApiService {
    @POST("/kyc/auth/login")
    fun login(
        @Body loginRequest: LoginRequest
    ): Call<LoginResponse>

    @POST("/kyc/auth/register")
    fun signUp(
        @Body signUpRequest: SignUpRequest
    ): Call<SignUpResponse>
    @Multipart
    @POST("/kyc/api/files/upload")
    fun uploadDocuments(
        @Part id: MultipartBody.Part, // Part for ID
        @Part kra: MultipartBody.Part  // Part for KRA Pin
    ): Call<ResponseBody>

    @GET("/kyc/upload/status")
    fun fetchUploadStatus(): Call<UploadStatusResponse>

    @POST("/kyc/link")
    fun linkAccount(@Body request: LinkAccountRequest
    ): Call<LinkAccountResponse>

    // Fetch linked accounts for the current user
    @GET("kyc/users/accounts")
    fun getLinkedAccounts(): Call<LinkedAccountsResponse>

    @GET
    fun getAccountBalance(
        @Url route: String // The dynamic URL for account balance fetching.
    ): Call<AccountRepository.BalanceResponse>

    @POST("/transactions/initiate")
    fun initiateTransaction(
        @HeaderMap headers: Map<String, String>,
        @Body transactionRequest: TransactionRequest): Call<TransactionResponse>

    @POST("/transactions/check-pin")
    suspend fun checkPin(
        @HeaderMap headers: Map<String, String>,
        @Query("user_id") userId: Int
    ): Response<Boolean>


    @POST("/transactions/save-pin")
    fun savePin(
        @HeaderMap headers: Map<String, String>,
        @Body pinRequest: PinRequest
    ): Call<Void>

}


data class PinRequest(
    val userId: Int,
    val pin: String
)