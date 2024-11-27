package com.example.bankx_plore.ui

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bankx_plore.R
import com.example.bankx_plore.datastore.DataStoreManager
import com.example.bankx_plore.datastore.UserState
import com.example.bankx_plore.network.RetrofitInstance
import com.example.bankx_plore.network.SignUpRequest
import com.example.bankx_plore.network.SignUpResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


// Utility validation functions
fun isValidName(name: String): Boolean {
    val valid = name.all { it.isLetter() || it.isWhitespace() }
    Log.d("Validation", "Name validation for '$name': $valid")
    return valid
}

fun isValidEmail(email: String): Boolean {
    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    val valid = email.matches(emailPattern.toRegex())
    Log.d("Validation", "Email validation for '$email' : $valid")
    return valid
}

fun isValidPhoneNumber(phoneNo: String): Boolean {
    val valid = phoneNo.all { it.isDigit() } && phoneNo.length in 10..12
    Log.d("Validation", "Phone number validation for '$phoneNo': $valid")
    return valid
}

fun isValidPassword(password: String): Boolean {
    val passwordPattern =
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&#])[A-Za-z\\d@\$!%*?&#]{8,}\$"
    val valid = password.matches(passwordPattern.toRegex())
    Log.d("Validation", "Password validation: $valid")
    return valid
}

@Composable
fun SignUpScreen(
    onSignUp: (
        String, String, String, String, String, String,
        (String) -> Unit, (String) -> Unit
    ) -> Unit,
    onSignInClick: () -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isChecked by remember { mutableStateOf(false) }
    var signUpError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    var firstNameError by remember { mutableStateOf<String?>(null) }
    var middleNameError by remember { mutableStateOf<String?>(null) }
    var lastNameError by remember { mutableStateOf<String?>(null) }
    var phoneNoError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var emailError by remember {
        mutableStateOf<String?>(null)
    }

    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
    val coroutineScope = rememberCoroutineScope()

    val scrollState = rememberScrollState()
    var passwordVisible by remember { mutableStateOf(false) }


    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_full),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(100.dp)
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = firstName,
                onValueChange = {
                    firstName = it
                    firstNameError =
                        if (!isValidName(it)) "Name must contain only letters" else null
                },
                label = { Text("First name") },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 4.dp),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
            if (firstNameError != null) {
                Text(text = firstNameError!!, color = Color.Red, fontSize = 12.sp)
            }

            OutlinedTextField(
                value = middleName,
                onValueChange = {
                    middleName = it
                    middleNameError =
                        if (!isValidName(it)) "Name must contain only letters" else null
                },
                label = { Text("Middle name") },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 4.dp),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
            if (middleNameError != null) {
                Text(text = middleNameError!!, color = Color.Red, fontSize = 12.sp)
            }

            OutlinedTextField(
                value = lastName,
                onValueChange = {
                    lastName = it
                    lastNameError = if (!isValidName(it)) "Name must contain only letters" else null
                },
                label = { Text("Last name") },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 4.dp),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
            if (lastNameError != null) {
                Text(text = lastNameError!!, color = Color.Red, fontSize = 12.sp)
            }

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = if (!isValidEmail(it)) "Enter a valid Email Adress" else null
                },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 4.dp),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
            if (emailError != null) {
                Text(text = emailError!!, color = Color.Red, fontSize = 12.sp)
            }

            OutlinedTextField(
                value = phoneNo,
                onValueChange = {
                    phoneNo = it
                    phoneNoError =
                        if (!isValidPhoneNumber(it)) "Enter a valid phone number" else null
                },
                label = { Text("Phone no") },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 4.dp),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
            if (phoneNoError != null) {
                Text(text = phoneNoError!!, color = Color.Red, fontSize = 12.sp)
            }

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = if (!isValidPassword(it)) {
                        "Password must contain at least 8 characters, including uppercase, lowercase, numbers, and special characters"
                    } else null
                },
                label = { Text("Password") },
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None else
                    PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = "Show password")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 4.dp),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
            if (passwordError != null) {
                Text(text = passwordError!!, color = Color.Red, fontSize = 12.sp)
            }

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    confirmPasswordError = if (it != password) "Passwords do not match" else null
                },
                label = { Text("Confirm password") },
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None else
                    PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = "Show password")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 4.dp),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
            if (confirmPasswordError != null) {
                Text(text = confirmPasswordError!!, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clickable { isChecked = !isChecked }
            ) {
                Checkbox(checked = isChecked, onCheckedChange = { isChecked = it })
                Text(
                    "By creating your account you agree to our Terms and Conditions.",
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        Log.d("SignUpScreen", "Sign Up button clicked")
                        if (firstNameError == null &&
                            middleNameError == null &&
                            lastNameError == null &&
                            phoneNoError == null &&
                            passwordError == null &&
                            confirmPasswordError == null &&
                            emailError == null &&
                            email.isNotEmpty() &&
                            isChecked
                        ) {
                            isLoading = true
                            signUpError = null
                            onSignUp(
                                firstName, middleName, lastName, email, phoneNo, password,
                                { token ->
                                    coroutineScope.launch {
                                        dataStoreManager.saveUserState(UserState.DEACTIVATED)
                                        Log.d("SignUpScreen", "User state saved as DEACTIVATED.")
                                    }
                                    isLoading = false
                                },
                                { error ->
                                    Log.e("SignUpScreen", "Sign Up failed: $error")
                                    signUpError = error
                                    isLoading = false
                                }
                            )
                        } else {
                            signUpError =
                                "Please ensure all fields are valid and terms are accepted."
                            Log.e("SignUpScreen", "Sign Up validation failed")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(50.dp)
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF052A71),
                        contentColor = Color.White
                    )
                ) {
                    Text("Sign up")
                }
            }

            if (signUpError != null) {
                Text(text = signUpError ?: "", color = Color.Red)
            }

            TextButton(onClick = onSignInClick) {
                Text("Already have an account? Sign in.", color = Color(0xFF052A71))
                Log.d("SignUpScreen", "Sign In button clicked")
            }
        }
    }
}


fun signUpUser(
    context: Context,
    firstName: String,
    middleName: String,
    lastName: String,
    email: String,
    phoneNo: String,
    password: String,
    onSignUpSuccess: (String) -> Unit,
    onSignUpError: (String) -> Unit
) {
    val dataStoreManager = DataStoreManager(context) // Use the passed context

    val request = SignUpRequest(
        firstName = firstName,
        middleName = middleName,
        lastName = lastName,
        email = email,
        phoneNo = phoneNo,
        password = password,
        authorities = "USER"
    )

    Log.d("SignUpScreen", "Starting sign-up for email: $email")
    val call = RetrofitInstance.api.signUp(request)

    call.enqueue(object : Callback<SignUpResponse> {
        override fun onResponse(call: Call<SignUpResponse>, response: Response<SignUpResponse>) {
            if (response.isSuccessful) {
                val payload = response.body()?.payload
                if (payload != null) {
                    val userName = payload.name ?: "User"
                    val userId = payload.id?.toInt() ?: -1
                    if (userId != -1) {
                        Log.d(
                            "SignUpScreen",
                            "User ID: $userId and Phone: $phoneNo saved in SQLite."
                        )
                    }

                    // Save the user ID in DataStore (inside a coroutine)
                    kotlinx.coroutines.GlobalScope.launch {
                        dataStoreManager.saveCurrentUserId(userId)
                        Log.d("SignUpScreen", "User ID saved in DataStore.")
                        dataStoreManager.saveUserName(userName)
                    }

                    onSignUpSuccess(payload.token ?: "")
                } else {
                    onSignUpError("Sign-up successful but no payload received.")
                }
            } else {
                Log.e("SignUpScreen", "Sign-up failed with code: ${response.code()}")
                onSignUpError("Sign-up failed: ${response.message()}")
            }
        }

        override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
            Log.e("SignUpScreen", "Sign-up request failed: ${t.message}")
            onSignUpError("Sign-up failed: ${t.message}")
        }
    })
}


@Preview(showBackground = true)
@Composable
fun PreviewSignUpScreen() {
    SignUpScreen(
        onSignUp = { firstName, middleName, lastName, email, phoneNo, password, onSuccess, onError ->
            Log.d("SignUpPreview", "Sign-up preview clicked")
        },
        onSignInClick = {
            Log.d("SignUpPreview", "Sign-in preview clicked")
        }
    )
}
