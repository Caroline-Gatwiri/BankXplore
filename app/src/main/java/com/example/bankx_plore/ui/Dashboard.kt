package com.example.bankx_plore.ui

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bankx_plore.R
import com.example.bankx_plore.datastore.DataStoreManager
import com.example.bankx_plore.datastore.UserState
import com.example.bankx_plore.repository.AccountRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class BankService(
    val name: String,
    val description: String,
    val bankLogo: Int
)

val bankServicesList = listOf(
    BankService("Home Loan", "Low interest home loans", R.drawable.ic_family_bank_logo),
    BankService("Credit Card", "Earn rewards with every purchase", R.drawable.ic_family_bank_logo),
    BankService("Investment Plan", "Grow your wealth with us", R.drawable.ic_family_bank_logo)
)

@Composable
fun NewUserDashboard(
    accountRepository: AccountRepository,
    navigateToDocumentUpload: () -> Unit,
    navigateToLinkAccount: () -> Unit,
    navigateToPinCreation: () -> Unit,
    documentsUploaded: Boolean,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    setPinRecentlyCreated: (Boolean) -> Unit,
    pinRecentlyCreated: Boolean
) {
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
    val userName by dataStoreManager.userName.collectAsState(initial = "User")
    val userIdFlow = dataStoreManager.getFlowableCurrentUserId() // Get Flow<String?> for userId
    val userIdState by userIdFlow.collectAsState(initial = null) // Convert to State

    val userId = userIdState ?: -1 // Safely convert userId to Int, default to -1 if null

    // UI States
    var searchQuery by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showUploadDialog by remember { mutableStateOf(false) }
    var showAwaitVerificationDialog by remember { mutableStateOf(false) }
    val userState by dataStoreManager.userState.collectAsState(initial = UserState.EMPTY)
    var shouldCheckPin by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showLoginScreen by remember { mutableStateOf(false) }
    val token by dataStoreManager.userToken.collectAsState(initial = null)
    var accounts by remember { mutableStateOf<List<Account>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // PIN Verification
    LaunchedEffect(userId, token, pinRecentlyCreated) {
        delay(1000)
        if (!pinRecentlyCreated) {
            if (userId > 0 && !token.isNullOrEmpty()) {
                accountRepository.verifyUserPin(
                    userId = userId,
                    token = token!!,
                    onPinExists = {
                        Log.d("NewUserDashboard", "PIN exists for user.")
                    },
                    onPinMissing = {
                        Log.d("NewUserDashboard", "No PIN found. Navigating to PIN Creation.")
                        navigateToPinCreation()
                    },
                    onError = { error ->
                        Toast.makeText(context, "Error verifying PIN: $error", Toast.LENGTH_SHORT)
                            .show()
                    },
                    navigateToPinCreation = navigateToPinCreation,
                    showToast = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                Toast.makeText(context, "Session expired. Please log in again.", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            // Reset the flag after a delay
            Log.d("NewUserDashboard", "Skipping PIN verification due to recent creation.")
            delay(1000)
            setPinRecentlyCreated(false)
        }
    }

    // Log state changes and actions
    LaunchedEffect(userState) {
        when (userState) {
            UserState.DEACTIVATED -> {
                delay(5000)
                showUploadDialog = true
                showAwaitVerificationDialog = false
                Log.d("NewUserDashboard", "User is DEACTIVATED, showing upload dialog.")
            }

            UserState.UNVERIFIED -> {
                showAwaitVerificationDialog = true
                showUploadDialog = false
                Log.d("NewUserDashboard", "User is UNVERIFIED, awaiting verification.")
            }

            UserState.ACTIVATED -> {
                showUploadDialog = false
                showAwaitVerificationDialog = false
                Log.d("NewUserDashboard", "User is ACTIVATED, full access granted.")
            }

            UserState.ARCHIVED -> {
                Log.d("NewUserDashboard", "User state is ARCHIVED, no access.")
            }

            else -> {}
        }
    }
    // Fetch linked accounts and their balances
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
                            Log.e("Dashboard", "Failed to fetch balance: $error")
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



    // Handle navigation based on user state
    fun handleNavigation(action: () -> Unit) {
        when (userState) {
            UserState.ACTIVATED -> action()
            UserState.DEACTIVATED -> showUploadDialog = true
            UserState.UNVERIFIED -> showAwaitVerificationDialog = true
            UserState.ARCHIVED -> {}
            else -> {}
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                selectedItem = selectedItem,
                onItemSelected = { newIndex ->
                    if (userState == UserState.ACTIVATED) {
                        onItemSelected(newIndex)
                    } else if (userState == UserState.DEACTIVATED) {
                        showUploadDialog = true
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Welcome message
                Text(
                    text = "Hello, $userName",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Search bar and menu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search Accounts") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Icon"
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Box {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options"
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Notifications") },
                                onClick = { showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = { showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Logout") },
                                onClick = {
                                    showMenu = false
                                    showLogoutDialog = true
                                }
                            )
                        }
                    }
                    if (showLogoutDialog) {
                        LogoutConfirmationDialog(
                            onConfirm = {
                                onLogout()
                                showLogoutDialog = false
                                performLogout(
                                    context = context,
                                    dataStoreManager = dataStoreManager
                                ) {
                                    showLoginScreen = true
                                }
                            },
                            onDismiss = { showLogoutDialog = false }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Account Cards Section

                Text(
                    text = "Your Accounts",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (isLoading) {
                    Text(
                        text = "Loading accounts...",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                } else if (errorMessage != null) {
                    Text(
                        text = "Error fetching accounts. Please try again later.",
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (accounts.isEmpty()) {
                    Text(
                        text = "No accounts found. Please link your accounts.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(accounts.size) { index ->
                            AccountCard(account = accounts[index])
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Link Accounts button
                Button(
                    onClick = { handleNavigation { navigateToLinkAccount() } },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF052A71),
                        contentColor = Color.White
                    )
                ) {
                    Text("Link Accounts")
                }

                Spacer(modifier = Modifier.height(16.dp))


                Text(
                    text = "Explore Bank Services",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )


                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(bankServicesList.size) { index ->
                        BankServiceCard(bankService = bankServicesList[index])
                    }
                }
            }


            if (showUploadDialog && userState == UserState.DEACTIVATED && !documentsUploaded) {
                UploadDocumentsDialog(
                    onDismiss = { showUploadDialog = false },
                    onUpload = {
                        showUploadDialog = false
                        navigateToDocumentUpload()
                    }
                )
            }

            // Show Await Verification Dialog
            if (showAwaitVerificationDialog && userState == UserState.UNVERIFIED) {
                AwaitVerificationDialog(
                    onDismiss = { showAwaitVerificationDialog = false },
                    onVerificationComplete = {
                        // Set the state to trigger the PIN verification
                        shouldCheckPin = true
                    }
                )
            }
        }

    }
}

@Composable
fun UploadDocumentsDialog(onDismiss: () -> Unit, onUpload: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Upload Required Documents") },
        text = { Text("To continue using all features, please upload your necessary documents.") },
        confirmButton = {
            Button(onClick = { onUpload() }) {
                Text("Upload Now")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Later")
            }
        }
    )
}

@Composable
fun AwaitVerificationDialog(
    onDismiss: () -> Unit,
    onVerificationComplete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Verification Pending") },
        text = { Text("Your documents are under verification. Please wait for approval.") },
        confirmButton = {
            Button(onClick = {
                onDismiss()
                onVerificationComplete()
            }) {
                Text("OK")
            }
        }
    )
}

@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Logout") },
        text = { Text("Are you sure you want to logout?") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Logout")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun performLogout(
    context: Context,
    dataStoreManager: DataStoreManager,
    onNavigateToLogin: () -> Unit
) {

    val coroutineScope = CoroutineScope(Dispatchers.IO)
    coroutineScope.launch {
        dataStoreManager.clearUserToken()
        dataStoreManager.clearCurrentUserId()
        dataStoreManager.clearAllData()

        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
            onNavigateToLogin()
        }
    }
}


@Composable
fun BankServiceCard(bankService: BankService) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier
            .width(150.dp)
            .clickable { /* Handle navigation or service action */ }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = bankService.bankLogo),
                contentDescription = bankService.name,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = bankService.name, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = bankService.description, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun NavigationBar(selectedItem: Int, onItemSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = Color.Black
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_home),
                    contentDescription = "Home"
                )
            },
            selected = selectedItem == 0,
            onClick = { onItemSelected(0) },
            label = { Text("Home") }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_accounts),
                    contentDescription = "Accounts"
                )
            },
            selected = selectedItem == 1,
            onClick = { onItemSelected(1) },
            label = { Text("Accounts") }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_transact),
                    contentDescription = "Transact"
                )
            },
            selected = selectedItem == 2,
            onClick = { onItemSelected(2) },
            label = { Text("Transact") }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_history),
                    contentDescription = "History"
                )
            },
            selected = selectedItem == 3,
            onClick = { onItemSelected(3) },
            label = { Text("History") }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_alerts),
                    contentDescription = "Alerts"
                )
            },
            selected = selectedItem == 4,
            onClick = { onItemSelected(4) },
            label = { Text("Alerts") }
        )
    }
}


//@Preview(showBackground = true)
//@Composable
//fun PreviewNewUserDashboard() {
//    NewUserDashboard(
//        accountRepository = object : AccountRepository {
//            override fun getLinkedAccounts(onSuccess: (List<Account>) -> Unit, onFailure: (String) -> Unit) {
//                onSuccess(
//                    listOf(
//                        Account("KCB", "123456789", 1, "KES 0.00", R.drawable.ic_kcb_bank, Color.Green),
//                        Account("ABSA", "987654321", 3, "KES 0.00", R.drawable.ic_absa_logo, Color.Red)
//                    )
//                )
//            }
//        },
//        navigateToDocumentUpload = { /* Mock action */ },
//        navigateToLinkAccount = { /* Mock action */ },
//        navigateToPinCreation = { /* Mock action */ },
//        documentsUploaded = true,
//        selectedItem = 0,
//        onItemSelected = {},
//        onLogout = { /* Mock logout */ }
//    )
//}


// @Composable
// fun ArchivedUserDialog(onDismiss: () -> Unit) {
//     AlertDialog(
//         onDismissRequest = { onDismiss() },
//         title = { Text(text = "Account Archived") },
//         text = { Text("Your account is archived and you cannot access the application. Please contact support.") },
//         confirmButton = {
//             Button(onClick = { onDismiss() }) {
//                 Text("OK")
//             }
//         }
//     )
// }

// @Composable
// fun PlaceholderAccountCard() {
//     Card(
//         shape = RoundedCornerShape(16.dp),
//         modifier = Modifier
//             .width(300.dp)
//             .height(130.dp)
//             .padding(8.dp),
//         elevation = CardDefaults.cardElevation(8.dp)
//     ) {
//         Row(
//             modifier = Modifier
//                 .fillMaxWidth()
//                 .padding(16.dp),
//             verticalAlignment = Alignment.CenterVertically
//         ) {
//             Image(
//                 painter = painterResource(id = R.drawable.ic_family_bank_logo),
//                 contentDescription = "Bank Logo",
//                 modifier = Modifier.size(40.dp)
//             )

//             Spacer(modifier = Modifier.width(16.dp))

//             Column(verticalArrangement = Arrangement.Center) {
//                 Text(text = "Total Funds", fontSize = 14.sp, fontWeight = FontWeight.Medium)
//                 Text(text = "$123,456.00", fontSize = 22.sp, fontWeight = FontWeight.Bold)
//                 Spacer(modifier = Modifier.height(4.dp))
//                 Text(text = "+3,000.00 (2.41%)", color = Color.Green, fontSize = 12.sp)
//             }
//         }
//     }
// }
