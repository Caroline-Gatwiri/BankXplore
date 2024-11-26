package com.example.bankx_plore.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bankx_plore.R
import com.example.bankx_plore.datastore.DataStoreManager
import com.example.bankx_plore.network.RetrofitInstance
import com.example.bankx_plore.repository.DocumentRepository
import kotlinx.coroutines.launch

@Composable
fun DocumentUploadScreen(
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    documentsUploaded: Boolean,
    navigateBackToDashboard: () -> Unit,
    onDocumentsUploaded: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var idFileUri by remember { mutableStateOf<Uri?>(null) }
    var kraFileUri by remember { mutableStateOf<Uri?>(null) }

    // Launchers for selecting image files
    val idFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? -> idFileUri = uri }
    )

    val kraFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? -> kraFileUri = uri }
    )

    // Initialize DataStoreManager and DocumentRepository
    val dataStoreManager = remember { DataStoreManager(context) }
    val apiService = RetrofitInstance.create(dataStoreManager)
    val documentRepository = DocumentRepository(apiService, dataStoreManager, context)

    val scope = rememberCoroutineScope()

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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Header with back button
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
                    text = "Upload Documents",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Please Ensure all your Documents are Images",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(8.dp))
            // ID Document Upload Box
            UploadBox(
                label = if (idFileUri != null) "ID Selected" else "Click to Upload ID",
                onClick = { idFilePickerLauncher.launch(arrayOf("image/*")) }  // Filter for image files
            )

            // KRA Document Upload Box
            UploadBox(
                label = if (kraFileUri != null) "KRA Pin Selected" else "Click to Upload KRA Pin",
                onClick = { kraFilePickerLauncher.launch(arrayOf("image/*")) }  // Filter for image files
            )

            // Submit Button
            Button(
                onClick = {
                    if (idFileUri != null && kraFileUri != null) {
                        scope.launch {
                            documentRepository.uploadDocuments(
                                idFileUri = idFileUri!!,
                                kraFileUri = kraFileUri!!,
                                onSuccess = {
                                    onDocumentsUploaded(true)
                                    Toast.makeText(context, "Upload successful", Toast.LENGTH_SHORT).show()
                                    navigateBackToDashboard()
                                },
                                onFailure = { errorMessage ->
                                    Toast.makeText(context, "Upload failed: $errorMessage", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    } else {
                        Toast.makeText(context, "Please select both ID and KRA documents", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF052A71), contentColor = Color.White)
            ) {
                Text("SUBMIT")
            }
        }
    }
}


@Composable
fun UploadBox(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(vertical = 8.dp)
            .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = R.drawable.ic_upload),
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                tint = Color(0xFF9C27B0)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = label, color = Color.Black)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDocumentUploadScreen() {
    DocumentUploadScreen(
        selectedItem = 0,
        onItemSelected = {},
        documentsUploaded = false,
        navigateBackToDashboard = {},
        onDocumentsUploaded = {}
    )
}

@Preview(showBackground = true, name = "After Upload")
@Composable
fun PreviewDocumentUploadScreenAfterUpload() {
    DocumentUploadScreen(
        selectedItem = 0,
        onItemSelected = {},
        documentsUploaded = true,
        navigateBackToDashboard = {},
        onDocumentsUploaded = {}
    )
}
