package com.example.bankx_plore.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

@Composable
fun OnboardingNavigation(navigateToLogin: () -> Unit) {
    // State to keep track of which screen is currently being displayed
    var currentScreen by remember { mutableIntStateOf(0) }

    // Automatically switch from the first screen after 3 seconds
    if (currentScreen == 0) {
        LaunchedEffect(Unit) {
            delay(3000) // 3-second delay for the first screen (splash screen)
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
        3 -> OnboardingScreen3(onNext = { navigateToLogin() }) // Fourth screen navigates to Login
    }
}
