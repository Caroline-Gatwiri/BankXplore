package com.example.bankx_plore.ui

import androidx.activity.result.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

@Composable
fun PinCodeScreen(
    onBackClick: () -> Unit,
    onPinEntered: (String, (Boolean, String) -> Unit) -> Unit
) {
    var pinCode by remember { mutableStateOf("") }
    val maxPinLength = 6
    var isErrorDialogVisible by remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showFailureDialog by remember { mutableStateOf(false) }
    var transactionMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = "Enter Your PIN",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF052A71)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // PIN Input Field
        OutlinedTextField(
            value = pinCode,
            onValueChange = { newInput ->
                if (newInput.length <= maxPinLength && newInput.all { it.isDigit() }) {
                    pinCode = newInput
                }
            },
            label = { Text("Enter PIN") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            // Confirm Button
            Button(
                onClick = {
                    isLoading = true
                    if (pinCode.length == maxPinLength) {
                        onPinEntered(pinCode) { success, message ->
                            isLoading = false
                            if (success) {
                                transactionMessage = message
                                showSuccessDialog = true
                            } else {
                                showFailureDialog = true
                                transactionMessage = message
                            }
                        }
                    } else {
                        isErrorDialogVisible = true
                        errorMessage.value = "PIN must be exactly $maxPinLength digits."
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF052A71),
                    contentColor = Color.White
                )
            ) {
                Text("Confirm")
            }
            // Success Dialog
            if (showSuccessDialog) {
                AlertDialog(
                    onDismissRequest = { showSuccessDialog = false },
                    title = { Text("Success") },
                    text = { Text(transactionMessage) },
                    confirmButton = {
                        Button(onClick = {
                            showSuccessDialog = false
                            onBackClick()
                        }) {
                            Text("OK")
                        }
                    }
                )
            }

            // Failure Dialog
            if (showFailureDialog) {
                FailureDialog(
                    message = transactionMessage,
                    onDismiss = { showFailureDialog = false },
                    onRetry = {
                        onBackClick()
                    }
                )
            }
        }
    }

    // Error Dialog
    if (isErrorDialogVisible) {
        ErrorDialog(
            message = errorMessage.value,
            onDismiss = { isErrorDialogVisible = false }
        )
    }
}


@Composable
fun ErrorDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Error", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
        text = { Text(message, fontSize = 16.sp) },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        },
        containerColor = Color.White,
        shape = MaterialTheme.shapes.medium,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    )
}

@Composable
fun SuccessDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Success") },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}


@Composable
fun FailureDialog(message: String, onDismiss: () -> Unit, onRetry: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Failure") },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onRetry) {
                Text("Retry")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
//@Preview(showBackground = true)
//@Composable
//fun PinCodeScreenPreview() {
//    PinCodeScreen(
//        onBackClick = {},
//        onPinEntered = {}
//    )
//}
