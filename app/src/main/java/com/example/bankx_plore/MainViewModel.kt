package com.example.bankx_plore

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bankx_plore.datastore.DataStoreManager
import com.example.bankx_plore.datastore.UserState
import com.example.bankx_plore.network.TransactionRequest
import com.example.bankx_plore.repository.AccountRepository
import com.example.bankx_plore.repository.DocumentRepository
import kotlinx.coroutines.launch

class MainViewModel(
    private val documentRepository: DocumentRepository,
    private val accountRepository: AccountRepository,
    private val dataStoreManager: DataStoreManager,
) : ViewModel() {

    // Declare state variables for managing the document status
    var showUploadDialog = false
    var showAwaitVerificationDialog = false
    val transactionStatus = MutableLiveData<String>()


    // Function to fetch upload status from the repository
    fun fetchUploadStatus(onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                // Fetch the document upload status and return the result using the callback onResult
                documentRepository.fetchUploadStatus { status ->
                    onResult(status)
                }
            } catch (e: Exception) {
                onResult("ERROR")
            }
        }
    }


    fun makeFundTransfer(
        transactionRequest: TransactionRequest,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) = viewModelScope.launch {
        val token = dataStoreManager.getToken()

        accountRepository.initiateTransaction(
            token,
            transactionRequest,
            onSuccess = { response ->
                val message = response.message
                Log.e("--------", message)
                onSuccess(message)
            },
            onFailure = { error ->
                Log.e("---------", error)
                onFailure(error)
            })
    }


    // Handle status changes after fetching the upload status
    fun handleStatusChange(status: String) {
        viewModelScope.launch {
            when (status) {
                "ACTIVATED" -> {
                    dataStoreManager.saveUserState(UserState.ACTIVATED)
                    showUploadDialog = false
                }

                "UNVERIFIED" -> {
                    dataStoreManager.saveUserState(UserState.UNVERIFIED)
                    showAwaitVerificationDialog = true
                }

                else -> {
                    dataStoreManager.saveUserState(UserState.DEACTIVATED)
                    showUploadDialog = true
                }
            }
        }
    }
}
