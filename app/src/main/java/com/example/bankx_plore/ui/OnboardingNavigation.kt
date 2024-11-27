package com.example.bankx_plore.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.bankx_plore.datastore.DataStoreManager
import kotlinx.coroutines.delay

@Composable
fun OnboardingNavigation(
    navigateToLogin: () -> Unit,
    dataStoreManager: DataStoreManager
) {
    var currentScreen by remember { mutableIntStateOf(0) }
//    //val isOnboardingCompleted by dataStoreManager.isOnboardingCompleted.collectAsState(initial = false)
//
//    // Check if onboarding is completed and navigate to login screen immediately
//    if (isOnboardingCompleted) {
//        LaunchedEffect(Unit) {
//            navigateToLogin() // Navigate directly to the login screen
//        }
//        return // Skip rendering the onboarding flow if already completed
//    }

    // Handle the automatic screen transition for the first onboarding screen
    if (currentScreen == 0) {
        LaunchedEffect(Unit) {
            delay(3000) // 3 seconds delay for the first splash screen
            currentScreen = 1 // Move to the next screen
        }
    }

    // Handle back button navigation: Enable only on second screen and beyond
    BackHandler(enabled = currentScreen > 1) {
        if (currentScreen > 1) {
            currentScreen -= 1 // Go back to the previous screen
        }
    }

    // Handle navigation based on current screen state
    when (currentScreen) {
        0 -> OnboardingScreen0() // First screen (Splash screen with logo)
        1 -> OnboardingScreen1(onNext = { currentScreen = 2 }) // Second screen
        2 -> OnboardingScreen2(onNext = { currentScreen = 3 }) // Third screen
        3 -> OnboardingScreen3(onNext = { navigateToLogin()})
            }
}

