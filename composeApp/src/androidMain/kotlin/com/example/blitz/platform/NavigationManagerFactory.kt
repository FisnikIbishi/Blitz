package com.example.blitz.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun getNavigationManager(): NavigationManager {
    val context = LocalContext.current
    return AndroidNavigationManager(context)
}
