package com.example.bankx_plore.ui

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bankx_plore.R
import com.example.bankx_plore.datastore.DataStoreManager
import com.example.bankx_plore.network.LoginRequest
import com.example.bankx_plore.network.LoginResponse
import com.example.bankx_plore.network.RetrofitInstance
import com.example.bankx_plore.repository.AccountRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun LoginScreen(
    onLoginSuccess: (String, String, Int) -> Unit, // Updated to include token, name, and user ID
    onSignUpClick: () -> Unit,
    navigateToPinCreation: () -> Unit,
    navigateToDashboard: () -> Unit,
    accountRepository: AccountRepository
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 30.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(text = "Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(text = "Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (loginError != null) {
                Text(text = loginError ?: "", color = Color.Red)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        isLoading = true
                        loginError = null
                        loginUser(
                            email,
                            password,
                            onLoginSuccess = { token, name, userId ->
                                isLoading = false
                                handleLoginSuccess(
                                    token = token,
                                    name = name,
                                    userId = userId,
                                    navigateToPinCreation = navigateToPinCreation,
                                    navigateToDashboard = navigateToDashboard,
                                    accountRepository = accountRepository,
                                    context = context
                                )
                            },
                            onLoginError = { error ->
                                isLoading = false
                                loginError = error
                            },
                            context = context
                        )
                    },
                    modifier = Modifier.fillMaxWidth(0.8f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF052A71),
                        contentColor = Color.White
                    )
                ) { Text("Login") }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onSignUpClick) {
                Text(text = "Don't have an account? Sign up.", color = Color(0xFF052A71))
            }
        }
    }
}

fun loginUser(
    email: String,
    password: String,
    onLoginSuccess: (String, String, Int) -> Unit, // Include token, name, and userId
    onLoginError: (String) -> Unit,
    context: Context
) {
    val request = LoginRequest(email = email, password = password)

    val call = RetrofitInstance.api.login(request)
    call.enqueue(object : Callback<LoginResponse> {
        override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
            if (response.isSuccessful) {
                val payload = response.body()?.payload
                if (payload != null) {
                    val userId = payload.id?.toInt() ?: -1
                    val token = payload.token ?: ""
                    val name = payload.name ?: "User"

                    // Save user data in DataStore
                    val dataStoreManager = DataStoreManager(context)
                    CoroutineScope(Dispatchers.IO).launch {
                        dataStoreManager.saveCurrentUserId(userId)
                        dataStoreManager.saveUserToken(token)
                        dataStoreManager.saveUserName(name)
                    }

                    onLoginSuccess(token, name, userId)
                } else {
                    onLoginError("Login failed: No user data returned.")
                }
            } else {
                onLoginError("Invalid credentials")
            }
        }

        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
            onLoginError("Failed to connect to the server")
        }
    })
}

fun handleLoginSuccess(
    token: String,
    name: String,
    userId: Int,
    navigateToPinCreation: () -> Unit,
    navigateToDashboard: () -> Unit,
    accountRepository: AccountRepository,
    context: Context
) {
    val dataStoreManager = DataStoreManager(context)
    CoroutineScope(Dispatchers.IO).launch {
        dataStoreManager.saveUserToken(token)
    }

    // Check if PIN exists
    accountRepository.verifyUserPin(
        userId = userId,
        token = token, // Pass the token here
        onPinExists = {
            Log.d("Login", "PIN exists. Navigating to Dashboard.")
            navigateToDashboard()
        },
        onPinMissing = {
            Log.d("Login", "PIN missing. Navigating to PIN Creation.")
            navigateToPinCreation()
        },
        onError = { error ->
            Log.e("Login", "Error verifying PIN: $error")
            Toast.makeText(context, "Error verifying PIN: $error", Toast.LENGTH_SHORT).show() // Optional feedback
        },
        navigateToPinCreation = navigateToPinCreation, // Pass navigation to PIN creation
        showToast = { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show() // Toast for messages
        }
    )
}










@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    LoginScreen(
        onLoginSuccess = { token, name, userId ->
            // Handle successful login
        },
        onSignUpClick = {
            // Handle navigation to sign-up screen
        },
        navigateToPinCreation = TODO(),
        navigateToDashboard = TODO(),
        accountRepository = TODO()
    )
}
