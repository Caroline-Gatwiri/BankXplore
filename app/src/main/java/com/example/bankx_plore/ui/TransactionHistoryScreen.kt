package com.example.bankx_plore.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bankx_plore.network.ApiService
import com.example.bankx_plore.network.Transaction
import com.example.bankx_plore.datastore.DataStoreManager
import com.example.bankx_plore.network.TransactionHistoryResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun TransactionHistoryScreen(
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    onBackClick: () -> Unit,
    apiService: ApiService,
    dataStoreManager: DataStoreManager
) {

    var isLoading by remember { mutableStateOf(true) }
    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val token by dataStoreManager.userToken.collectAsState(initial = "")

    val coroutineScope = rememberCoroutineScope()


    fun fetchTransactions(
        apiService: ApiService,
        token: String,
        dataStoreManager: DataStoreManager,
        coroutineScope: CoroutineScope
    ) {
        isLoading = true
        coroutineScope.launch {
            val userId = dataStoreManager.getCurrentId()
            val headers = mapOf("Authorization" to "Bearer $token")

            apiService.getTransactionHistory(headers, userId, page = 0, size= 20)
                .enqueue(object : Callback<TransactionHistoryResponse> {
                    override fun onResponse(
                        call: Call<TransactionHistoryResponse>,
                        response: Response<TransactionHistoryResponse>
                    ) {
                        if (response.isSuccessful) {
                            transactions = response.body()?.content ?: emptyList()
                        } else {
                            errorMessage = "Error: ${response.message()}"
                        }
                        isLoading = false
                    }

                    override fun onFailure(call: Call<TransactionHistoryResponse>, t: Throwable) {
                        errorMessage = "Failed to fetch data: ${t.localizedMessage}"
                        isLoading = false
                    }
                })
        }
    }
    LaunchedEffect(Unit) {
        token?.let { fetchTransactions(apiService, it, dataStoreManager, coroutineScope) }
    }

    Scaffold(
        topBar = {
            TransactionTopBar(
                onBackClick = onBackClick,
                onRefreshClick = {
                    token?.let {
                        fetchTransactions(
                            apiService,
                            it,
                            dataStoreManager,
                            coroutineScope
                        )
                    }
                })
        },
        bottomBar = {
            NavigationBar(selectedItem = selectedItem, onItemSelected = onItemSelected)
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),

                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "Failed to fetch transactions. Please try again.",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(transactions) { transaction ->
                            TransactionItem(transaction)
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionTopBar(
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(vertical = 8.dp)
    ) {
        IconButton(onClick = { onBackClick() }) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "Transaction History",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { onRefreshClick() }) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Refresh",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(text = transaction.referenceNote, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                text = "Sender: ${transaction.senderPhoneNo}",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "Receiver: ${transaction.receiverPhoneNo}",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = "${transaction.amount} ${transaction.currency}",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

//@Preview(showBackground = true)
//@Composable
//fun PreviewTransactionHistoryScreen() {
//    TransactionHistoryScreen(
//        selectedItem = 0,
//        onItemSelected = {},
//        onBackClick = { /* Handle back navigation */ }
//    )
//}
