package com.example.bankx_plore.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bankx_plore.datastore.DataStoreManager
import com.example.bankx_plore.network.PinRequest
import com.example.bankx_plore.network.RetrofitInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response


// Helper function to check if the PIN is sequential
fun isSequential(pin: String): Boolean {
    val sequentialPatterns = listOf(
        "123456", "234567", "345678", "456789", "543210",
        "654321", "765432", "876543", "987654"
    )
    return sequentialPatterns.contains(pin)
}

@Composable
fun PinCreationScreen(
    onPinCreated: (String) -> Unit,
    onBackToDashboard: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        Text("Create and Confirm Your PIN", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        // Input for PIN
        OutlinedTextField(
            value = pin,
            onValueChange = { if (it.length <= 6) pin = it },
            label = { Text("Enter 6-digit PIN") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Input for Confirm PIN
        OutlinedTextField(
            value = confirmPin,
            onValueChange = { if (it.length <= 6) confirmPin = it },
            label = { Text("Confirm PIN") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Error message display
        if (errorMessage != null) {
            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Confirm button
        Button(
            onClick = {
                if (pin.isEmpty() || confirmPin.isEmpty()) {
                    errorMessage = "Both fields are required."
                } else if (pin != confirmPin) {
                    errorMessage = "PINs do not match. Please try again."
                } else if (pin.length != 6) {
                    errorMessage = "PIN must be exactly 6 digits."
                } else if (isSequential(pin)) {
                    errorMessage = "PIN cannot be sequential (e.g., 123456). Please choose a stronger PIN."
                } else {
                    errorMessage = null
                    coroutineScope.launch {
                        try {
                            val userId = dataStoreManager.getCurrentId()
                            if (userId != null) {
                                val token = dataStoreManager.getToken()
                                val pinRequest = PinRequest(userId.toInt(), pin)

                                // Save PIN to backend
                                savePinToBackend(userId, pin, token, onSuccess = {
//                                    onPinCreated(pin)
                                    onBackToDashboard()

                                    Log.e("GOOD", "NAVIGATE" )
                                }, onError = {
                                    errorMessage = it
                                })
                            } else {
                                errorMessage = "User ID not found."
                            }
                        } catch (e: NumberFormatException) {
                            errorMessage = "Invalid user ID format. Please try again."
                            Log.e("ConfirmButton", "Error parsing user ID: ${e.message}")
                        } catch (e: Exception) {
                            errorMessage = "An error occurred: ${e.message}"
                            Log.e("ConfirmButton", "Unexpected error: ${e.message}")
                        }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF052A71),
                contentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Confirm")
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
    val pinRequest = PinRequest(userId, pin)
    val headers = mapOf("Authorization" to "Bearer $token" )

    RetrofitInstance.api.savePin(headers, pinRequest).enqueue(object : retrofit2.Callback<Void> {
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



@Preview(showBackground = true)
@Composable
fun PreviewPinCreationScreen() {
    PinCreationScreen(
        onPinCreated = { /* Handle PIN creation */ },
        onBackToDashboard = {}
    )
}
