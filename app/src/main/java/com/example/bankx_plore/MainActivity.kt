package com.example.bankx_plore

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.bankx_plore.datastore.DataStoreManager
import com.example.bankx_plore.datastore.UserState
import com.example.bankx_plore.network.RetrofitInstance
import com.example.bankx_plore.network.TransactionRequest
import com.example.bankx_plore.repository.AccountRepository
import com.example.bankx_plore.repository.DocumentRepository
import com.example.bankx_plore.ui.AccountsScreen
import com.example.bankx_plore.ui.DocumentUploadScreen
import com.example.bankx_plore.ui.FundTransferScreen
import com.example.bankx_plore.ui.LinkAccountScreen
import com.example.bankx_plore.ui.LoginScreen
import com.example.bankx_plore.ui.NavigationBar
import com.example.bankx_plore.ui.NewUserDashboard
import com.example.bankx_plore.ui.NotificationsScreen
import com.example.bankx_plore.ui.OnboardingNavigation
import com.example.bankx_plore.ui.PinCodeScreen
import com.example.bankx_plore.ui.PinCreationScreen
import com.example.bankx_plore.ui.SignUpScreen
import com.example.bankx_plore.ui.TransactionHistoryScreen
import com.example.bankx_plore.ui.performLogout
import com.example.bankx_plore.ui.signUpUser
import com.example.bankx_plore.workers.DocumentStatusWorker
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class MainActivity : ComponentActivity() {

    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var documentRepository: DocumentRepository
    private lateinit var mainViewModel: MainViewModel
    private lateinit var accountRepository: AccountRepository

    private var transactionRequest: TransactionRequest? = null


    private var linkAccountScreenOrigin: String =
        "dashboard"
    private var onPinCodeVerifiedCallback: ((String) -> Unit)? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize DataStoreManager and DocumentRepository
        dataStoreManager = DataStoreManager(applicationContext)
        documentRepository = DocumentRepository(
            RetrofitInstance.create(dataStoreManager),
            dataStoreManager,
            applicationContext
        )


        // Initialize AccountRepository
        accountRepository = AccountRepository(
            RetrofitInstance.create(dataStoreManager)
        )

        // Initialize the ViewModel using the factory
        val viewModelFactory =
            MainViewModelFactory(documentRepository, accountRepository, dataStoreManager)
        mainViewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        enableEdgeToEdge()

        // Schedule the DocumentStatusWorker to check document verification status periodically
        val documentStatusWorkRequest = PeriodicWorkRequestBuilder<DocumentStatusWorker>(
            1, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DocumentStatusCheck",
            ExistingPeriodicWorkPolicy.KEEP,
            documentStatusWorkRequest
        )

        setContent {
            MaterialTheme {

                val context = LocalContext.current
                // State variables to manage screen visibility
                var showLoginScreen by remember { mutableStateOf(true) }
                var showDashboardScreen by remember { mutableStateOf(false) }
                var showOnboardingScreen by remember { mutableStateOf(true) }
                var showSignUpScreen by remember { mutableStateOf(false) }
                var showDocumentUploadScreen by remember { mutableStateOf(false) }
                var showLinkAccountScreen by remember { mutableStateOf(false) }
                var showAccountsScreen by remember { mutableStateOf(false) }
                var showNotificationsScreen by remember { mutableStateOf(false) }
                var showFundTransferScreen by remember { mutableStateOf(false) }
                var showTransactionHistoryScreen by remember { mutableStateOf(false) }
                var showPinCreationScreen by remember { mutableStateOf(false) }

                var showPinCodeScreen by remember { mutableStateOf(false) }
                var pendingTransactionRequest by remember { mutableStateOf<TransactionRequest?>(null) }


                var pinRecentlyCreated by remember { mutableStateOf(false) }

                // User state and token variables
                var userToken by remember { mutableStateOf("") }
                var isUserLoggedIn by remember { mutableStateOf(false) }

                // Document upload states
                var selectedItem by remember { mutableStateOf(0) }
                var documentsUploaded by remember { mutableStateOf(false) }

                // States for dialog visibility
                var showUploadDialog by remember { mutableStateOf(false) }
                var showAwaitVerificationDialog by remember { mutableStateOf(false) }
                var showArchivedDialog by remember { mutableStateOf(false) }

                // Collect user state
                val userState by dataStoreManager.userState.collectAsState(initial = UserState.EMPTY)

//                LaunchedEffect(Unit) {
//                    dataStoreManager.isOnboardingCompleted.collect { isCompleted ->
//                        if (isCompleted) {
//                            showOnboardingScreen = false
//                            showLoginScreen = true
//                        } else {
//                            showOnboardingScreen = true
//                        }
//                    }
//                }
                // Handle document status updates
                LaunchedEffect(userState, isUserLoggedIn) {
                    if (isUserLoggedIn) {
                        mainViewModel.fetchUploadStatus { status ->
                            mainViewModel.handleStatusChange(status)
                        }
                    }
                }

                // Handle UI changes based on user state
                LaunchedEffect(userState) {
                    when (userState) {
                        UserState.DEACTIVATED -> {
                            showUploadDialog = true
                            showAwaitVerificationDialog = false
                            showArchivedDialog = false
                        }

                        UserState.UNVERIFIED -> {
                            showAwaitVerificationDialog = true
                            showUploadDialog = false
                            showArchivedDialog = false
                        }

                        UserState.ACTIVATED -> {
                            showUploadDialog = false
                            showAwaitVerificationDialog = false
                            showArchivedDialog = false
                        }

                        UserState.ARCHIVED -> {
                            showArchivedDialog = true
                            showUploadDialog = false
                            showAwaitVerificationDialog = false
                        }

                        else -> {}
                    }
                }


                Surface {
                    when {
                        showOnboardingScreen -> {
                            OnboardingNavigation(
                                navigateToLogin = {
                                    showOnboardingScreen = false
                                    showLoginScreen = true
                                },
                                dataStoreManager = dataStoreManager
                            )
                        }

                        showLoginScreen -> {
                            LoginScreen(
                                onLoginSuccess = { token, name, userId ->
                                    lifecycleScope.launch {
                                        dataStoreManager.saveUserToken(token)
                                        dataStoreManager.saveUserName(name)
                                        dataStoreManager.saveCurrentUserId(userId.toInt())
                                    }
                                    userToken = token
                                    isUserLoggedIn = true
                                    showLoginScreen = false
                                    showDashboardScreen = true
                                },
                                onSignUpClick = {
                                    showLoginScreen = false
                                    showSignUpScreen = true
                                },
                                navigateToPinCreation = {
                                    showLoginScreen = false
                                    showPinCreationScreen = true
                                },
                                navigateToDashboard = {
                                    showLoginScreen = false
                                    showDashboardScreen = true
                                    isUserLoggedIn = true
                                },
                                accountRepository = accountRepository
                            )
                        }

                        showSignUpScreen -> {
                            SignUpScreen(
                                onSignUp = { firstName, middleName, lastName, email, phoneNo, password, onSuccess, onError ->

                                    signUpUser(
                                        context,
                                        firstName, middleName, lastName, email, phoneNo, password,
                                        onSignUpSuccess = { token ->
                                            lifecycleScope.launch {
                                                dataStoreManager.saveUserToken(token)
                                                dataStoreManager.saveUserState(UserState.DEACTIVATED)
                                            }
                                            userToken = token
                                            isUserLoggedIn = true
                                            showSignUpScreen = false
                                            showDashboardScreen = true
                                            onSuccess(token)
                                        },
                                        onSignUpError = { errorMessage ->
                                            onError(errorMessage)
                                        }
                                    )
                                },
                                onSignInClick = {
                                    showSignUpScreen = false
                                    showLoginScreen = true
                                }
                            )
                        }

                        showDashboardScreen -> {
                            Scaffold(
                                bottomBar = {
                                    if (isUserLoggedIn && showDashboardScreen) {
                                        NavigationBar(
                                            selectedItem = selectedItem,
                                            onItemSelected = { newItem ->
                                                selectedItem = newItem
                                                when (newItem) {
                                                    1 -> {
                                                        showDashboardScreen = false
                                                        showAccountsScreen = true
                                                    }

                                                    2 -> {
                                                        showDashboardScreen = false
                                                        showFundTransferScreen = true
                                                    }

                                                    3 -> {
                                                        showDashboardScreen = false
                                                        showTransactionHistoryScreen = true
                                                    }

                                                    4 -> {
                                                        showDashboardScreen = false
                                                        showNotificationsScreen = true
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            ) { paddingValues ->
                                NewUserDashboard(
                                    accountRepository = accountRepository,
                                    navigateToDocumentUpload = {
                                        showDashboardScreen = false
                                        showDocumentUploadScreen = true
                                    },
                                    navigateToLinkAccount = {
                                        showDashboardScreen = false
                                        showLinkAccountScreen = true
                                        linkAccountScreenOrigin = "dashboard"
                                    },
                                    navigateToPinCreation = {
                                        showDashboardScreen = false
                                        showPinCreationScreen = true
                                    },
                                    documentsUploaded = documentsUploaded,
                                    selectedItem = selectedItem,
                                    onItemSelected = { newItem ->
                                        selectedItem = newItem
                                        if (userState == UserState.ACTIVATED) {
                                            when (newItem) {
                                                1 -> {
                                                    showDashboardScreen = false
                                                    showAccountsScreen = true
                                                }

                                                2 -> {
                                                    showDashboardScreen = false
                                                    showFundTransferScreen = true
                                                }

                                                3 -> {
                                                    showDashboardScreen = false
                                                    showTransactionHistoryScreen = true
                                                }

                                                4 -> {
                                                    showDashboardScreen = false
                                                    showNotificationsScreen = true
                                                }
                                            }
                                        }
                                    },
                                    onLogout = {
                                        showLoginScreen = true

                                        performLogout(
                                            context = this,
                                            dataStoreManager = dataStoreManager
                                        ) {

                                            showDashboardScreen = false
                                            isUserLoggedIn = false
                                            userToken = ""
                                            selectedItem = 0
                                        }
                                    },
                                    modifier = Modifier.padding(paddingValues),
                                    setPinRecentlyCreated = { value -> pinRecentlyCreated = value },
                                    pinRecentlyCreated = pinRecentlyCreated
                                )
                            }
                        }

                        showLinkAccountScreen -> {
                            LinkAccountScreen(
                                accountRepository = accountRepository,
                                onBackPress = { origin ->
                                    when (origin) {
                                        "dashboard" -> {
                                            showLinkAccountScreen = false
                                            showDashboardScreen = true
                                        }

                                        "accounts" -> {
                                            showLinkAccountScreen = false
                                            showAccountsScreen = true
                                        }
                                    }
                                },
                                navigateToDashboard = {
                                    showLinkAccountScreen = false
                                    showDashboardScreen = true
                                },
                                selectedItem = selectedItem,
                                onItemSelected = { newItem ->
                                    selectedItem = newItem
                                    when (newItem) {
                                        0 -> {
                                            showLinkAccountScreen = false
                                            showDashboardScreen = true
                                        }

                                        1 -> {
                                            showLinkAccountScreen = false
                                            showAccountsScreen = true
                                        }
                                    }
                                },
                                origin = linkAccountScreenOrigin
                            )
                        }

                        showAccountsScreen -> {
                            AccountsScreen(
                                accountRepository = accountRepository,
                                selectedItem = selectedItem,
                                onItemSelected = { newItem ->
                                    selectedItem = newItem
                                    when (newItem) {
                                        0 -> {
                                            showAccountsScreen = false
                                            showDashboardScreen = true
                                        }
                                        1 -> {
                                            showAccountsScreen = false
                                            showLinkAccountScreen = true
                                            linkAccountScreenOrigin = "accounts"
                                        }
                                        2 -> {
                                            showAccountsScreen = false
                                            showFundTransferScreen = true
                                        }
                                        3 -> {
                                            showAccountsScreen = false
                                            showTransactionHistoryScreen = true
                                        }
                                        4 -> {
                                            showAccountsScreen = false
                                            showNotificationsScreen = true
                                        }
                                    }
                                },
                                navigateBackToDashboard = {
                                    showAccountsScreen = false
                                    showDashboardScreen = true
                                },
                                navigateToLinkAccount = {
                                    showAccountsScreen = false
                                    showLinkAccountScreen = true
                                    linkAccountScreenOrigin = "accounts"
                                }
                            )
                        }

                        showDocumentUploadScreen -> {
                            DocumentUploadScreen(
                                selectedItem = selectedItem,
                                onItemSelected = { newItem -> selectedItem = newItem },
                                documentsUploaded = documentsUploaded,
                                navigateBackToDashboard = {
                                    showDocumentUploadScreen = false
                                    showDashboardScreen = true
                                },
                                onDocumentsUploaded = { uploaded ->
                                    documentsUploaded = uploaded
                                    if (uploaded) {
                                        showDashboardScreen = true
                                    }
                                }
                            )
                        }

                        showTransactionHistoryScreen -> {
                            TransactionHistoryScreen(
                                onBackClick = {
                                    showTransactionHistoryScreen = false
                                    showDashboardScreen = true
                                },
                                selectedItem = selectedItem,
                                onItemSelected = { newItem ->
                                    selectedItem = newItem
                                    when (newItem) {
                                        0 -> {
                                            showTransactionHistoryScreen = false
                                            showDashboardScreen = true
                                        }
                                        1 -> {
                                            showTransactionHistoryScreen = false
                                            showAccountsScreen = true
                                        }
                                        2 -> {
                                            showTransactionHistoryScreen = false
                                            showFundTransferScreen = true
                                        }
                                        3 -> {
                                            showTransactionHistoryScreen = true
                                            showNotificationsScreen = false
                                        }
                                        4 -> {
                                            showTransactionHistoryScreen = false
                                            showNotificationsScreen= true

                                        }

                                    }
                                },
                                apiService = RetrofitInstance.create(dataStoreManager),
                                dataStoreManager = dataStoreManager
                            )
                        }

                        showFundTransferScreen -> {
                            FundTransferScreen(
                                selectedItem = selectedItem,
                                onItemSelected = { newItem ->
                                    selectedItem = newItem
                                    when (newItem) {
                                        0 -> {
                                            showFundTransferScreen = false
                                            showDashboardScreen = true
                                        }
                                        1 -> {
                                            showFundTransferScreen = false
                                            showAccountsScreen = true
                                        }
                                        2 -> {
                                            showFundTransferScreen =true
                                            showDashboardScreen = false
                                        }
                                        3 ->{
                                            showTransactionHistoryScreen = true
                                            showNotificationsScreen = false
                                        }
                                        4 -> {
                                            showTransactionHistoryScreen=true
                                            showFundTransferScreen = false
                                        }
                                    }
                                },
                                onBackClick = {
                                    showFundTransferScreen = false
                                    showDashboardScreen = true
                                },
                                accountRepository = accountRepository,
                                navigateToPinCodeScreen = { request ->
                                    transactionRequest = request
                                    showFundTransferScreen = false
                                    showPinCodeScreen = true
                                }
                            )
                        }

                        showPinCodeScreen -> {
                            PinCodeScreen(
                                onBackClick = {
                                    showPinCodeScreen = false; showFundTransferScreen = true
                                }, // Navigate back to FundTransferScreen
                                onPinEntered = { pinCode, resultHandler ->
                                    transactionRequest?.let { req ->
                                        req.pin = pinCode
                                        mainViewModel.makeFundTransfer(
                                            req,
                                            onSuccess = { message ->
                                                resultHandler(
                                                    true,
                                                    message
                                                )
                                            },
                                            onFailure = { error ->
                                                resultHandler(
                                                    false,
                                                    error
                                                )
                                            }
                                        )
                                    }
                                }
                            )
                        }


                        showPinCreationScreen -> {
                            PinCreationScreen(
                                onPinCreated = { pin ->
                                    lifecycleScope.launch {
                                        val userId = dataStoreManager.getCurrentId()
                                        val token = dataStoreManager.getToken()

                                        if (userId != null && token != null) {
                                            accountRepository.savePinToBackend(
                                                userId = userId,
                                                pin = pin,
                                                token = token,
                                                onSuccess = {
                                                    lifecycleScope.launch {
                                                        dataStoreManager.saveUserPin(
                                                            userId.toInt(),
                                                            pin
                                                        ) // Save locally
                                                        pinRecentlyCreated = true // Set this flag
                                                        Toast.makeText(
                                                            this@MainActivity,
                                                            "PIN created successfully!",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                        showPinCreationScreen = false
                                                        showDashboardScreen = true
                                                    }
                                                },
                                                onError = { error ->
                                                    Toast.makeText(
                                                        this@MainActivity,
                                                        "Failed to create PIN: $error",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            )
                                        } else {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Failed to retrieve user ID or token.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }

                                },
                                onBackToDashboard = {
                                    showPinCreationScreen = false
                                    showDashboardScreen = true
                                }
                            )
                        }


                        showNotificationsScreen -> {
                            NotificationsScreen(
                                selectedItem = selectedItem,
                                onItemSelected = { newItem ->
                                    selectedItem = newItem
                                    when (newItem) {
                                        0 -> {
                                            showNotificationsScreen = false
                                            showDashboardScreen = true
                                        }

                                        1 -> {
                                            showNotificationsScreen = false
                                            showAccountsScreen = true
                                        }

                                        2 -> {
                                            showNotificationsScreen = false
                                            showFundTransferScreen = true
                                        }

                                        3 -> {
                                            showNotificationsScreen = false
                                            showTransactionHistoryScreen = true

                                        }
                                    }

                                },
                                navigateBackToDashboard = {
                                    showNotificationsScreen = false
                                    showDashboardScreen = true
                                }
                            )
                        }


                        //1. bug check pin
                        //2. user upload status
                        //3. make FT include pin code
                        //4. Loading status for sending txn request
                        //5. Select destination bank identifier
                        //6. Navigate depending on request status
                        //7. Fetch TXN history
                        //8. Create the transaction screen well.


                    }
                }
            }
        }
    }
}


//private fun showSuccessDialog(message: String) {
//    AlertDialog.Builder(this)
//        .setTitle("Success")
//        .setMessage(message)
//        .setPositiveButton("OK") { dialog, _ ->
//            dialog.dismiss()
//        }
//        .show()
//}
//private fun showErrorDialog(message: String) {
//    AlertDialog.Builder(this)
//        .setTitle("Error")
//        .setMessage(message)
//        .setPositiveButton("OK") { dialog, _ ->
//            dialog.dismiss()
//        }
//        .show()
//}