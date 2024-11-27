package com.example.bankx_plore.repository

import android.util.Log
import androidx.compose.ui.graphics.Color
import com.example.bankx_plore.R
import com.example.bankx_plore.network.ApiService
import com.example.bankx_plore.network.LinkAccountRequest
import com.example.bankx_plore.network.LinkAccountResponse
import com.example.bankx_plore.network.PinRequest
import com.example.bankx_plore.network.RetrofitInstance
import com.example.bankx_plore.network.TransactionRequest
import com.example.bankx_plore.network.TransactionResponse
import com.example.bankx_plore.ui.Account
import com.example.bankx_plore.ui.LinkedAccountsResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AccountRepository(private val apiService: ApiService) {

    fun linkAccount(
        bankId: Int,
        accountNumber: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val request = LinkAccountRequest(bankId, accountNumber)

        apiService.linkAccount(request).enqueue(object : Callback<LinkAccountResponse> {
            override fun onResponse(
                call: Call<LinkAccountResponse>,
                response: Response<LinkAccountResponse>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody?.status == 200) {
                        onSuccess(responseBody.payload ?: "Account linked successfully.")
                    } else {
                        onFailure(responseBody?.message ?: "Failed to link account.")
                    }
                } else {
                    onFailure("Server error: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<LinkAccountResponse>, t: Throwable) {
                onFailure("Network error: ${t.message}")
            }
        })
    }


    fun getLinkedAccounts(
        onSuccess: (List<Account>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        apiService.getLinkedAccounts().enqueue(object : Callback<LinkedAccountsResponse> {
            override fun onResponse(
                call: Call<LinkedAccountsResponse>,
                response: Response<LinkedAccountsResponse>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody?.status == 200) {
                        val accounts = responseBody.payload.map { account ->
                            Account(
                                bankName = getBankName(account.bankId),
                                accountNumber = account.accountNumber,
                                bankCode = account.bankId,
                                bankLogo = when (account.bankId) {
                                    1 -> R.drawable.ic_kcb_bank
                                    2 -> R.drawable.ic_family_bank_logo
                                    3 -> R.drawable.ic_absa_logo
                                    else -> R.drawable.coop_logo
                                },
                                cardColor = when (account.bankId) {
                                    1 -> Color(0xFF6BC660) // Green for KCB
                                    2 -> Color(0xFFB3D6FF) // Light Blue for Family Bank
                                    3 -> Color(0xFFFFCDD2) // Light Red for ABSA
                                    else -> Color.LightGray
                                }
                            )
                        }.distinctBy { it.accountNumber }
                        onSuccess(accounts)
                    } else {
                        onFailure("Failed to fetch accounts: ${responseBody?.message ?: "Unknown error"}")
                    }
                } else {
                    onFailure("Server error: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<LinkedAccountsResponse>, t: Throwable) {
                onFailure("Network error: ${t.message}")
            }
        })
    }

    fun getAccountBalance(
        accountNumber: String,
        bankCode: Int,
        onSuccess: (Double) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val route =
            "/banking/${getBankRoute(bankCode)}/transactions/balance?accountNumber=$accountNumber&bankCode=$bankCode"
        Log.d("getAccountBalance", "Fetching balance with route: $route") // Log the URL

        apiService.getAccountBalance(route).enqueue(object : Callback<BalanceResponse> {
            override fun onResponse(
                call: Call<BalanceResponse>,
                response: Response<BalanceResponse>
            ) {
                if (response.isSuccessful) {
                    val balanceResponse = response.body()
                    Log.d("getAccountBalance", "Balance fetched successfully: $balanceResponse")
                    if (balanceResponse != null) {
                        onSuccess(balanceResponse.balance)
                    } else {
                        onFailure("Invalid response from backend")
                    }
                } else {
                    Log.e("getAccountBalance", "Error response: ${response.message()}")
                    onFailure("Server error: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<BalanceResponse>, t: Throwable) {
                Log.e("getAccountBalance", "Network error: ${t.message}")
                onFailure("Network error: ${t.message}")
            }
        })
    }


    fun executeTransaction(
        token: String,
        transactionRequest: TransactionRequest,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        initiateTransaction(
            token,
            transactionRequest,
            onSuccess = { response ->
                if (response.status == "SUCCESS") {
                    onSuccess("Transaction successful: ${response.message}")
                } else {
                    onFailure("Transaction failed: ${response.message}")
                }
            },
            onFailure = { errorMessage ->
                onFailure("Transaction failed: $errorMessage")
            }
        )
    }


    fun initiateTransaction(
        token: String,
        transactionRequest: TransactionRequest,
        onSuccess: (TransactionResponse) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val headers = mapOf("Authorization" to "Bearer $token") // Authorization header

        RetrofitInstance.api.initiateTransaction(headers, transactionRequest)
            .enqueue(object : retrofit2.Callback<TransactionResponse> {
                override fun onResponse(
                    call: Call<TransactionResponse>,
                    response: Response<TransactionResponse>
                ) {
                    if (response.isSuccessful) {
                        val transactionResponse = response.body()
                        if (transactionResponse != null) {
                            onSuccess(transactionResponse)
                        } else {
                            onFailure("Transaction response is null.")
                        }
                    } else {
                        onFailure("Transaction failed with status code: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<TransactionResponse>, t: Throwable) {
                    onFailure("Network error: ${t.message}")
                }
            })
    }


    private fun getBankName(bankId: Int): String {
        return when (bankId) {
            1 -> "KCB BANK"
            2 -> "FAMILY BANK"
            3 -> "ABSA BANK"
            else -> {
                Log.e("getBankName", "Unknown bank ID: $bankId")
                "Unknown Bank"
            }
        }
    }


    private fun getBankRoute(bankCode: Int): String {
        return when (bankCode) {
            1 -> "kcb"
            2 -> "family"
            3 -> "absa"
            else -> throw IllegalArgumentException("Unsupported bank code: $bankCode")
        }
    }

    data class BalanceResponse(
        val currency: String,
        val balance: Double
    )

    fun getBankCode(accountNumber: String, accounts: List<Account>): String {
        val account = accounts.firstOrNull { it.accountNumber == accountNumber }
        if (account == null) {
            Log.e("getBankCode", "Account not found for account number: $accountNumber")
            throw IllegalArgumentException("Invalid bank code for account: $accountNumber")
        }

        return when (account.bankCode) {
            1 -> "KCB"
            2 -> "FAMILY"
            3 -> "ABSA"
            else -> {
                Log.e("getBankCode", "Unsupported bank code: ${account.bankCode}")
                throw IllegalArgumentException("Unsupported bank code: ${account.bankCode}")
            }
        }
    }

    suspend fun checkPin(userId: Int, token: String): Response<Boolean> {
        val headers = mapOf("Authorization" to "Bearer $token")

        return apiService.checkPin(headers, userId)
    }


    fun verifyUserPin(
        userId: Int,
        token: String,
        onPinExists: () -> Unit,
        onPinMissing: () -> Unit,
        onError: (String) -> Unit,
        navigateToPinCreation: () -> Unit,
        showToast: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = checkPin(userId, token)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val pinExists = response.body() ?: false
                        if (pinExists) {
                            onPinExists()
                        } else {
                            navigateToPinCreation()
                            showToast("No PIN found. Please set a new PIN.")
                        }
                    } else {
                        onError("Error verifying PIN: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError("Error: ${e.localizedMessage}")
                }
            }
        }
    }


    fun savePinToBackend(
        userId: Int,
        pin: String,
        token: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val headers = mapOf("Authorization" to "Bearer $token") // Authorization header
        val pinRequest = PinRequest(userId, pin)

        apiService.savePin(headers, pinRequest).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Failed to save PIN on the server. Error code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                onError("Network error: ${t.message}")
            }
        })
    }


}
