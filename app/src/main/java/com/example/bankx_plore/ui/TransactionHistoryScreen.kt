package com.example.bankx_plore.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun TransactionHistoryScreen(
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    // Simulated loading state for refresh
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
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

                // Top Bar with Back Arrow, Title, and Refresh Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Replaced the deprecated icon
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
                    IconButton(onClick = {
                        // Simulate refresh action
                        isLoading = true
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Handle refresh by showing a CircularProgressIndicator
                if (isLoading) {
                    LaunchedEffect(Unit) {
                        delay(2000)  // Simulate a delay for refresh
                        isLoading = false  // Reset loading state
                    }
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    // Scrollable Transaction List
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "Today",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        items(sampleTransactions) { transaction ->
                            TransactionItem(
                                title = transaction.title,
                                category = transaction.category,
                                amount = transaction.amount
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(title: String, category: String, amount: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Title and Category
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = category, fontSize = 14.sp, color = Color.Gray)
        }
        Spacer(modifier = Modifier.width(16.dp))
        // Amount
        Text(text = amount, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

// Sample data for transactions
val sampleTransactions = listOf(
    Transaction("Apple Store", "Entertainment", "- \$5.99"),
    Transaction("Spotify", "Music", "- \$12.99"),
    Transaction("Money Transfer", "Transaction", "+ \$300"),
    Transaction("Grocery", "Shopping", "- \$88.00"),
    Transaction("Apple Store", "Entertainment", "- \$5.99"),
    Transaction("Spotify", "Music", "- \$12.99"),
    Transaction("Money Transfer", "Transaction", "+ \$300"),
    Transaction("Grocery", "Shopping", "- \$88.00")
)

data class Transaction(val title: String, val category: String, val amount: String)

@Preview(showBackground = true)
@Composable
fun PreviewTransactionHistoryScreen() {
    TransactionHistoryScreen(
        selectedItem = 0,
        onItemSelected = {},
        onBackClick = { /* Handle back navigation */ }
    )
}
