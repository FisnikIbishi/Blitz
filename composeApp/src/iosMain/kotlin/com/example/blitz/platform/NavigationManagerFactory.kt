package com.example.blitz.platform

import androidx.compose.runtime.Composable

@Composable
actual fun getNavigationManager(): NavigationManager {
    return IOSNavigationManager()
}
