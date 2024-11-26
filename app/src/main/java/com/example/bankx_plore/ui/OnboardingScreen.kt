package com.example.bankx_plore.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bankx_plore.R

@OptIn(ExperimentalFoundationApi::class)



@Composable
fun OnboardingScreen0() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Bank Xplore Logo",
                modifier = Modifier.size(150.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "BANK XPLORE",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Black
            )
        }
    }
}

@Composable
fun OnboardingScreen1(onNext: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.onboarding_screen_1),
                contentDescription = "Manage Multiple Bank Accounts",
                modifier = Modifier.size(250.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Manage Multiple Bank Accounts Seamlessly",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Effortlessly view and manage all your bank accounts in one place, track balances, and transactions across different banks.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF052A71),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp)
            ) {
                Text(text = "Next")
            }
        }
    }
}
@Composable
fun OnboardingScreen2(onNext: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Image or illustration for the third screen
            Image(
                painter = painterResource(id = R.drawable.onboarding_screen_2),
                contentDescription = "Secure and Convenient Multi-Banking",
                modifier = Modifier.size(250.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Title for the screen
            Text(
                text = "Secure and Convenient Multi-Banking",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle/description for the screen
            Text(
                text = "Advanced security features like fingerprint and encryption ensure your multi-bank data remains safe and accessible only to you.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Next Button
            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF052A71),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp)
            ) {
                Text(text = "Next")
            }
        }
    }
}
@Composable
fun OnboardingScreen3(onNext: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Image or illustration for the fourth screen
            Image(
                painter = painterResource(id = R.drawable.onboarding_screen_3),
                contentDescription = "Paying for Everything is Easy and Convenient",
                modifier = Modifier.size(250.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Title for the screen
            Text(
                text = "Paying for Everything is Easy and Convenient",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle/description for the screen
            Text(
                text = "Built-in fingerprint, face recognition and more, keeping you completely safe.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Next Button (Navigates to Login Screen)
            Button(
                onClick = onNext, // Navigate to the login screen after this
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF052A71),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth(0.8f)

                    .height(50.dp)
            ) {
                Text(text = "Next")
            }
        }
    }
}




// Add this function to preview your composable
@Preview(showBackground = true)
@Composable
fun PreviewOnboardingScreen3() {
    OnboardingScreen3 (onNext = {}) // Calling the original composable here
}

