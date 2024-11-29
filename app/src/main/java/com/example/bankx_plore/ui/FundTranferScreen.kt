package com.example.bankx_plore.ui

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeGesturesPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bankx_plore.datastore.DataStoreManager
import com.example.bankx_plore.network.TransactionDetails
import com.example.bankx_plore.network.TransactionRequest
import com.example.bankx_plore.repository.AccountRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@Composable
fun FundTransferScreen(
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    onBackClick: () -> Unit,
    accountRepository: AccountRepository?,
    navigateToPinCodeScreen: (TransactionRequest) -> Unit,
//    navigateToPinCodeScreen:  (TransactionRequest) -> Unit
) {
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
    val coroutineScope = rememberCoroutineScope()

    var selectedFromAccount by remember { mutableStateOf("") }
    var selectedToAccount by remember { mutableStateOf("") }
    var transferAmount by remember { mutableStateOf("") }
    var transactionFee by remember { mutableStateOf(0.0) }
    var totalDeduction by remember { mutableStateOf(0.0) }
    var transactionNote by remember { mutableStateOf("") }
    var accounts by remember { mutableStateOf<List<Account>>(emptyList()) }
    var pin by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf(-1) }
    val bankMap = mapOf("KCB" to 1, "FAMILY" to 2, "ABSA" to 3)
    var selectedBankId by remember { mutableStateOf(0) }
    var expanded by remember { mutableStateOf(false) }
    var selectedBank by remember { mutableStateOf("Select Account") }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            userId = dataStoreManager.getFlowableCurrentUserId().firstOrNull() ?: -1
            if (userId == -1) {
                Log.e("FundTransferScreen", "User ID not found.")
                return@launch
            }

            accountRepository?.getLinkedAccounts(
                onSuccess = { fetchedAccounts -> accounts = fetchedAccounts },
                onFailure = { error ->
                    Log.e(
                        "FundTransferScreen",
                        "Failed to fetch accounts: $error"
                    )
                }
            )
            
        }
    }

    LaunchedEffect(transferAmount) {
        val amount = transferAmount.toDoubleOrNull()
        transactionFee = calculateTransactionFee(amount)
        totalDeduction = (amount ?: 0.0) + transactionFee
    }

    Scaffold(
        bottomBar = {
            NavigationBar(selectedItem = selectedItem, onItemSelected = onItemSelected)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { onBackClick() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Fund Transfer", style = MaterialTheme.typography.headlineMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // From Account Dropdown
            AccountDropDown(
                accounts = accounts,
                selectedAccount = selectedFromAccount,
                onAccountSelected = { selectedFromAccount = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

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
//            DropdownInput(
//                label = "Select Account",
//                options = accounts.map { it.bankName },
//                selectedOption = selectedBank,
//                onOptionSelected = { selectedBank = it }
//
//            )
            DropdownInput(
                label = "Select Receiver Bank",
                options = listOf("KCB BANK", "FAMILY", "ABSA BANK"),
                selectedOption = selectedBank,
                onOptionSelected = { selectedBank = it }
            )

            Spacer(modifier = Modifier.height(16.dp))
            // To Account Input
            OutlinedTextField(
                value = selectedToAccount,
                onValueChange = { selectedToAccount = it },
                label = { Text("Recipient Account Number") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Field
            OutlinedTextField(
                value = transferAmount,
                onValueChange = { transferAmount = it },
                label = { Text("Enter Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Transaction Note Field
            OutlinedTextField(
                value = transactionNote,
                onValueChange = { transactionNote = it },
                label = { Text("Reason For Making Transfer") },
                modifier = Modifier.fillMaxWidth()

            )

            Spacer(modifier = Modifier.height(16.dp))

            // Display Transaction Fee and Total Deduction
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Transaction Fee", fontSize = 16.sp)
                Text("KES ${"%.2f".format(transactionFee)}", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Total Deduction", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    "KES ${"%.2f".format(totalDeduction)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Transfer Button
            Button(
                onClick = {
                    if (selectedFromAccount.isNotEmpty() &&
                        selectedToAccount.isNotEmpty() &&
                        transferAmount.toDoubleOrNull() != null
                    ) {
                        val bankCode = getBankCodeFromBankName(selectedBank)
                        val transactionType = determineTransactionType(transferAmount.toDouble())
                        val transactionRequest = TransactionRequest(
                            pin = pin,
                            transactionDetails = TransactionDetails(
                                userId = userId,
                                transactionId = "TX${System.currentTimeMillis()}",
                                transactionType = transactionType,
                                senderPhoneNo = "1234567890",
                                senderAccountNumber = selectedFromAccount,
                                senderBankCode = getBankCode(selectedFromAccount, accounts),
                                receiverAccountNumber = selectedToAccount,
                                receiverPhoneNo = "0987654321",
                                receiverBankCode = bankCode,
                                amount = transferAmount.toDouble(),
                                currency = "KES",
                                transactionFee = transactionFee,
                                referenceNote = transactionNote
                            )
                        )

                        navigateToPinCodeScreen(transactionRequest)

                    } else {
                        Toast.makeText(
                            context,
                            "Please fill all required fields.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF052A71))
            ) {
                Text("Transfer")
            }
        }
    }
}


fun determineTransactionType(amount: Double?): String {
    return if (amount != null && amount <= 500000) "RTGS" else "EFT"
}

fun calculateTransactionFee(amount: Double?): Double {
    if (amount == null || amount <= 0.0) return 0.0

    return when {
        amount <= 100 -> 0.0
        amount <= 500 -> 6.0
        amount <= 1000 -> 12.0
        amount <= 1500 -> 22.0
        amount <= 2500 -> 32.0
        amount <= 3500 -> 51.0
        amount <= 5000 -> 55.0
        amount <= 7500 -> 75.0
        amount <= 10000 -> 87.0
        amount <= 15000 -> 97.0
        amount <= 20000 -> 102.0
        amount <= 35000 -> 105.0
        amount <= 50000 -> 105.0
        else -> amount * 0.02
    }
}


fun getBankCode(accountNumber: String, accounts: List<Account>): String {
    val bankId = accounts.firstOrNull { it.accountNumber == accountNumber }?.bankCode
        ?: throw IllegalArgumentException("Invalid bank code for account: $accountNumber")
    return when (bankId) {
        1 -> "KCB"
        2 -> "Family"
        3 -> "ABSA"
        else -> throw IllegalArgumentException("Unsupported bank code: $bankId")
    }
}


fun getBankCodeFromBankName(bankName: String): String {
    val bankCodes = mapOf(
        "KCB BANK" to "KCB",
        "FAMILY" to "Family",
        "ABSA BANK" to "ABSA"
    )
    return bankCodes[bankName]
        ?: throw IllegalArgumentException("Invalid bank code for bank name: $bankName")
}



@Composable
fun AccountDropDown(
    accounts: List<Account>,
    selectedAccount: String,
    onAccountSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedAccount,
            onValueChange = { },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            label = { Text("Choose Account") },
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                }
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = { Text(account.bankName) },
                    onClick = {
                        onAccountSelected(account.accountNumber)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DropdownInput(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = { },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                }
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

/*

@Preview(showBackground = true)
@Composable
fun PreviewFundTransferScreen()
    FundTransferScreen(
        selectedItem = 0,
        onItemSelected = {},
        onBackClick = { },
        onSubmitTransaction = { },
        accountRepository = object : AccountRepository {
            override fun getLinkedAccounts(onSuccess: (List<Account>) -> Unit, onFailure: (String) -> Unit) {
                onSuccess(
                    listOf(
                        Account("ABSA", "123456789", 1, "KES 0.00", R.drawable.ic_absa_logo, Color.Cyan),
                        Account("KCB", "987654321", 2, "KES 0.00", R.drawable.ic_kcb_bank, Color.Magenta)
                    )
                )
            }
        }
    )
}
*/
