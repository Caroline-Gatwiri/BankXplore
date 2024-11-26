package com.example.bankx_plore.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Data class for notifications
data class NotificationData(
    val amount: String,
    val sender: String,
    val message: String,
    val date: String
)

// Main Notification Screen Composable
@Composable
fun NotificationsScreen(
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    navigateBackToDashboard: () -> Unit
) {
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
            // Row with Back button and "Notifications" title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start // Align back button to the left
            ) {
                IconButton(onClick = { navigateBackToDashboard() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back to Dashboard"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Notifications (23)",  // Example with 23 notifications
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }

            // Scrollable list of notification cards
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val notifications = listOf(
                    NotificationData("+$1650", "Lena Nguyen", "Lena transfer", "1/7/2021 15:00"),
                    NotificationData("+$1", "Andy Kros", "Thanks, bro", "1/7/2021 15:00")
                )
                items(notifications) { notification ->
                    NotificationCard(notification = notification)
                }
            }
        }
    }
}

// Notification Card Composable
@Composable
fun NotificationCard(notification: NotificationData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF1F5FE)  // Background color for the notification card
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = "Receive Money", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = notification.amount, style = MaterialTheme.typography.titleLarge, color = Color(0xFF04A45A))
            Text(text = "from ${notification.sender}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Message: ${notification.message}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = notification.date, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

// Preview function to test the NotificationsScreen
@Preview(showBackground = true)
@Composable
fun PreviewNotificationScreen() {
    NotificationsScreen(
        selectedItem = 4,  // Mock "Alerts" tab selected
        onItemSelected = {},  // No-op for item selection in preview
        navigateBackToDashboard = {}  // No-op for back navigation in preview
    )
}
