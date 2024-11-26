package com.example.bankx_plore.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.bankx_plore.repository.AccountRepository

@Composable
fun LinkAccountScreen(
    accountRepository: AccountRepository,
    onBackPress: (String) -> Unit,
    navigateToDashboard: () -> Unit,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    origin: String
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedBank by remember { mutableStateOf("Select Account") }
    var accountNumber by remember { mutableStateOf("") }
    val bankMap = mapOf("KCB" to 1, "FAMILY" to 2, "ABSA" to 3)
    var selectedBankId by remember { mutableStateOf(0) }

    // State for dialog visibility and message
    var dialogMessage by remember { mutableStateOf("") }
    var isDialogVisible by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar(selectedItem = selectedItem, onItemSelected = { newIndex ->
                onItemSelected(newIndex)
            })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Custom Top Bar with Back Arrow
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onBackPress(origin) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Link Your Bank Account",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            // Dropdown menu for bank selection
            OutlinedButton(
                onClick = { expanded = !expanded },
                interactionSource = remember { MutableInteractionSource() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
            ) {
                Text(selectedBank, modifier = Modifier.weight(1f))
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Dropdown")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .offset(y = 8.dp)
            ) {
                bankMap.forEach { (bankName, bankId) ->
                    DropdownMenuItem(
                        text = { Text(bankName) },
                        onClick = {
                            selectedBank = bankName
                            selectedBankId = bankId
                            expanded = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = accountNumber,
                onValueChange = { accountNumber = it },
                label = { Text("Account Number") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Link button
            Button(
                onClick = {
                    if (accountNumber.isNotEmpty() && selectedBankId != 0) {
                        accountRepository.linkAccount(
                            bankId = selectedBankId,
                            accountNumber = accountNumber,
                            onSuccess = {
                                dialogMessage = "Linked account successfully"
                                isError = false
                                isDialogVisible = true // Show the success dialog
                            },
                            onFailure = {
                                dialogMessage = it
                                isError = true
                                isDialogVisible = true // Show the failure dialog
                            }
                        )
                    } else {
                        dialogMessage = "Please select a bank and enter an account number."
                        isError = true
                        isDialogVisible = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF052A71))
            ) {
                Text("LINK", color = Color.White, fontSize = 16.sp)
            }
        }
    }

    // Show the dialog if needed
    if (isDialogVisible) {
        AlertDialog(
            onDismissRequest = {
                isDialogVisible = false
                if (!isError) navigateToDashboard() // Navigate back on success
            },
            title = { Text(if (isError) "NOT SUCCESSFUL" else "SUCCESSFUL", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            text = { Text(dialogMessage, fontSize = 16.sp) },
            confirmButton = {
                Button(onClick = {
                    isDialogVisible = false
                    if (!isError) navigateToDashboard() // Navigate back on success
                }) {
                    Text("OK")
                }
            },
            containerColor = Color.White,
            shape = MaterialTheme.shapes.medium,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewLinkAccountScreen() {
    LinkAccountScreen(
        accountRepository = AccountRepository(
            apiService = TODO()
        ), // Replace with a mock in tests
        onBackPress = { /* Handle back press */ },
        selectedItem = 0,
        onItemSelected = {},
        navigateToDashboard = {},
        origin = "dashboard" // Mock origin for preview
    )
}

