package com.example.bankx_plore.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.bankx_plore.datastore.DataStoreManager
import com.example.bankx_plore.datastore.UserState
import com.example.bankx_plore.network.ApiService
import com.example.bankx_plore.network.UploadStatusResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream
import java.util.Locale

class DocumentRepository(
    private val apiService: ApiService,
    private val dataStoreManager: DataStoreManager,
    private val context: Context
) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // Upload documents and update user state
    fun uploadDocuments(
        idFileUri: Uri,
        kraFileUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        Log.d("DocumentRepository", "Checking if both files are valid images.")

        if (!isImageFile(idFileUri) || !isImageFile(kraFileUri)) {
            Log.e("DocumentRepository", "Both files must be images.")
            onFailure("Both files must be images.")
            return
        }

        val idFilePart = createMultipartFromUri(idFileUri, "id")
        val kraFilePart = createMultipartFromUri(kraFileUri, "kra")

        apiService.uploadDocuments(idFilePart, kraFilePart).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    coroutineScope.launch {
                        dataStoreManager.saveDocumentsUploaded(true)
                        dataStoreManager.saveUserState(UserState.UNVERIFIED)
                        Log.d("DocumentRepository", "Documents uploaded successfully, initial state set to UNVERIFIED.")
                    }
                    fetchUploadStatus { status ->
                        onSuccess(status)
                    }
                } else {
                    Log.e("DocumentRepository", "Upload failed: ${response.message()}")
                    onFailure("Upload failed: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("DocumentRepository", "Upload failed with exception: ${t.message}")
                onFailure("Upload failed: ${t.message}")
            }
        })
    }

    // Fetch verification status and update user state with retry on 403 Forbidden
    fun fetchUploadStatus(onResult: (String) -> Unit) {
        retryFetchUploadStatus(onResult, retryCount = 3)
    }

    private fun retryFetchUploadStatus(onResult: (String) -> Unit, retryCount: Int) {
        apiService.fetchUploadStatus().enqueue(object : Callback<UploadStatusResponse> {
            override fun onResponse(call: Call<UploadStatusResponse>, response: Response<UploadStatusResponse>) {
                if (response.isSuccessful) {
                    val status = response.body()?.payload ?: "UNVERIFIED"
                    coroutineScope.launch {
                        updateUserStateBasedOnStatus(status)
                    }
                    onResult(status)
                } else if (response.code() == 403 && retryCount > 0) {
                    Log.d("DocumentRepository", "403 Forbidden. Retrying in 2s... ($retryCount retries left)")
                    coroutineScope.launch {
                        delay(2000)
                        retryFetchUploadStatus(onResult, retryCount - 1)
                    }
                } else {
                    Log.e("DocumentRepository", "Fetch status failed with code: ${response.code()}")
                    onResult("UNVERIFIED")
                }
            }

            override fun onFailure(call: Call<UploadStatusResponse>, t: Throwable) {
                if (retryCount > 0) {
                    Log.d("DocumentRepository", "Network failure. Retrying in 2s... ($retryCount retries left)")
                    coroutineScope.launch {
                        delay(2000)
                        retryFetchUploadStatus(onResult, retryCount - 1)
                    }
                } else {
                    Log.e("DocumentRepository", "Max retries reached. Setting state to UNVERIFIED.")
                    coroutineScope.launch {
                        dataStoreManager.saveUserState(UserState.UNVERIFIED)
                    }
                    onResult("UNVERIFIED")
                }
            }
        })
    }

    private suspend fun updateUserStateBasedOnStatus(status: String) {
        val newState = when (status) {
            "ACTIVATED" -> UserState.ACTIVATED
            "UNVERIFIED" -> UserState.UNVERIFIED
            else -> UserState.DEACTIVATED
        }
        val currentState = dataStoreManager.getCurrentUserState()
        if (currentState != newState) {
            Log.d("DocumentRepository", "Updating user state from $currentState to $newState.")
            dataStoreManager.saveUserState(newState)
        } else {
            Log.d("DocumentRepository", "User state is already $newState, no update needed.")
        }
    }

    // Helper functions to create MultipartBody.Part from URI, get media type, file name, and validate image file type
    private fun createMultipartFromUri(uri: Uri, formName: String): MultipartBody.Part {
        val contentResolver = context.contentResolver
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val fileName = getFileNameFromUri(contentResolver, uri)
        val bytes = inputStream?.readBytes() ?: ByteArray(0)
        inputStream?.close()

        val mediaType = getMediaTypeFromUri(uri)
        val requestBody = bytes.toRequestBody(mediaType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(formName, fileName, requestBody)
    }

    private fun getMediaTypeFromUri(uri: Uri): String {
        val extension = uri.lastPathSegment?.substringAfterLast('.', "")?.toLowerCase(Locale.getDefault())
        return when (extension) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            else -> "application/octet-stream"
        }
    }

    private fun getFileNameFromUri(contentResolver: android.content.ContentResolver, uri: Uri): String {
        var name = "unknown"
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    name = it.getString(nameIndex)
                }
            }
        }
        return name
    }

    private fun isImageFile(uri: Uri): Boolean {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri)
        return mimeType != null && mimeType.startsWith("image/")
    }
}
