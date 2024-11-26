package com.example.bankx_plore.ui

import android.util.Log
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bankx_plore.repository.AccountRepository

@Composable
fun AccountsScreen(
    accountRepository: AccountRepository,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    navigateToLinkAccount: () -> Unit,
    navigateBackToDashboard: () -> Unit
) {
    var accounts by remember { mutableStateOf<List<Account>>(emptyList()) } // Fetched accounts
    var isLoading by remember { mutableStateOf(true) } // Loading state
    var errorMessage by remember { mutableStateOf<String?>(null) } // Error state

    // Fetch accounts when the screen loads and also the balances
    LaunchedEffect(Unit) {
        accountRepository.getLinkedAccounts(
            onSuccess = { fetchedAccounts ->
                val accountsWithBalances = fetchedAccounts.map { account ->
                    var accountWithBalance = account
                    accountRepository.getAccountBalance(
                        accountNumber = account.accountNumber,
                        bankCode = account.bankCode,
                        onSuccess = { balance ->
                            accountWithBalance = account.copy(totalFunds = "KES $balance")
                            accounts = accounts.map { acc ->
                                if (acc.accountNumber == account.accountNumber) accountWithBalance else acc
                            }
                        },
                        onFailure = { error ->
                            Log.e("AccountsScreen", "Failed to fetch balance: $error")
                        }
                    )
                    accountWithBalance
                }
                accounts = accountsWithBalances
                isLoading = false
            },
            onFailure = { error ->
                errorMessage = error
                isLoading = false
            }
        )
    }


    Scaffold(
        bottomBar = {
            NavigationBar(
                selectedItem = selectedItem,
                onItemSelected = onItemSelected
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Back button and title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navigateBackToDashboard() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back to Dashboard"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "My Accounts",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start
                )
            }

            // Link Accounts button
            Button(
                onClick = {
                    navigateToLinkAccount()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF052A71),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Link Accounts",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Link Accounts")
            }

            // Error message
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    modifier = Modifier.padding(vertical = 16.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Loading state
            if (isLoading) {
                Text(
                    text = "Loading accounts...",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                // List of account cards
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(accounts.size) { index ->
                        AccountCard(account = accounts[index])
                    }
                }
            }
        }
    }
}

@Composable
fun AccountCard(account: Account) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = account.cardColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bank logo
            Image(
                painter = painterResource(id = account.bankLogo),
                contentDescription = account.bankName,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))

            // Account details
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = account.bankName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = account.accountNumber,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = account.totalFunds, // Use the updated balance here
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun PreviewAccountsScreen() {
    // Previewing the AccountsScreen with mock back navigation
    AccountsScreen(
        accountRepository = AccountRepository(apiService = TODO()),
        selectedItem = 1,
        onItemSelected = {},
        navigateBackToDashboard = {},
        navigateToLinkAccount = TODO()
    )
}
