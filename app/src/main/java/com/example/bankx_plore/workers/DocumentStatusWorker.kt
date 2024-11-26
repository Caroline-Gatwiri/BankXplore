package com.example.bankx_plore.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bankx_plore.datastore.DataStoreManager
import com.example.bankx_plore.datastore.UserState
import com.example.bankx_plore.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class DocumentStatusWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val dataStoreManager = DataStoreManager(context)
    private val apiService = RetrofitInstance.api

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d("DocumentStatusWorker", "Checking document status...")

            // Make the network call to fetch the document upload status
            val response = apiService.fetchUploadStatus().execute()

            // Check if the response is successful
            if (response.isSuccessful) {
                val uploadStatus = response.body()

                // Log the full response and payload
                Log.d("DocumentStatusWorker", "API Response: ${response.body()}")
                Log.d("DocumentStatusWorker", "Document status received: ${uploadStatus?.payload}")

                if (uploadStatus != null) {
                    val payloadStatus = uploadStatus.payload

                    // Check if the state is different before updating it
                    val currentState = dataStoreManager.getCurrentUserState()

                    if (currentState != UserState.valueOf(payloadStatus)) {
                        // Update user state based on the payload status
                        when (payloadStatus) {
                            "ACTIVATED" -> {
                                Log.d("DocumentStatusWorker", "User verified, updating state to ACTIVATED.")
                                dataStoreManager.saveUserState(UserState.ACTIVATED)
                            }
                            "UNVERIFIED" -> {
                                Log.d("DocumentStatusWorker", "User unverified, updating state to UNVERIFIED.")
                                dataStoreManager.saveUserState(UserState.UNVERIFIED)
                            }
                            else -> {
                                Log.d("DocumentStatusWorker", "User deactivated or unknown state, updating state to DEACTIVATED.")
                                dataStoreManager.saveUserState(UserState.DEACTIVATED)
                            }
                        }
                    } else {
                        Log.d("DocumentStatusWorker", "User state is already ${currentState.name}, no update required.")
                    }
                }
            } else {
                Log.e("DocumentStatusWorker", "API request failed: ${response.message()}")
                return@withContext Result.retry()
            }

            // Return success to indicate that the worker completed successfully
            return@withContext Result.success()

        } catch (e: HttpException) {
            Log.e("DocumentStatusWorker", "Error fetching status: ${e.message()}")
            e.printStackTrace()
            // Retry in case of network or HTTP errors
            return@withContext Result.retry()
        } catch (e: Exception) {
            Log.e("DocumentStatusWorker", "General error: ${e.message}")
            e.printStackTrace()
            // Retry in case of any other exception
            return@withContext Result.retry()
        }
    }
}


