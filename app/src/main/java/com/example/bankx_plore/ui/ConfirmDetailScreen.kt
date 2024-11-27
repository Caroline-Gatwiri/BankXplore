package com.example.bankx_plore.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ConfirmDetailsScreen(
    fromAccount: String,
    fromAccountNumber: String,
    toAccount: String,
    toAccountNumber: String,
    amount: String,
    onBackClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Back Arrow and Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                IconButton(onClick = { onBackClick() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Confirm Details",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))


            // From Account Section
            Text(text = "From Account", fontSize = 16.sp)
            Text(text = fromAccount, fontSize = 18.sp)
            Text(
                text = "Savings",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = fromAccountNumber,
                onValueChange = {},
                label = { Text("Account Number") },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))


            // To Account Section
            Text(text = "To Account", fontSize = 16.sp)
            Text(text = toAccount, fontSize = 18.sp)
            Text(
                text = "Checking",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = toAccountNumber,
                onValueChange = {},
                label = { Text("Account Number") },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Section
            OutlinedTextField(
                value = amount,
                onValueChange = {},
                label = { Text("Amount") },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Confirm Button
            Button(
                onClick = { onConfirmClick() },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF052A71),
                    contentColor = Color.White
                )
            ) {
                Text(text = "CONFIRM")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewConfirmDetailsScreen() {
    ConfirmDetailsScreen(
        fromAccount = "ABSA Bank",
        fromAccountNumber = "1234 5678 90123",
        toAccount = "COOP Bank",
        toAccountNumber = "1234 5678 90123",
        amount = "23,258,123",
        onBackClick = { /* Handle back click */ },
        onConfirmClick = { /* Handle confirm click */ }
    )
}
