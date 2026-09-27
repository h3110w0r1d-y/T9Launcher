package com.h3110w0r1d.t9launcher.ui.screen

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import com.h3110w0r1d.t9launcher.data.config.LocalAppConfig
import com.h3110w0r1d.t9launcher.ui.LocalNavController
import com.h3110w0r1d.t9launcher.ui.theme.adaptiveBackgroundColor
import com.h3110w0r1d.t9launcher.ui.widget.LauncherPanel

@Composable
fun HomeScreen() {
    val navController = LocalNavController.current!!
    val appConfig = LocalAppConfig.current
    val fullScreen = appConfig.theme.fullScreenEnabled
    val cardColors = CardDefaults.cardColors()
    val background =
        if (appConfig.theme.showWallpaper) {
            Color.Transparent
        } else {
            appConfig.theme.backgroundColor?.let { Color(it) } ?: adaptiveBackgroundColor()
        }
    val context = LocalContext.current
    val darkTheme = if (appConfig.theme.nightModeFollowSystem) isSystemInDarkTheme() else appConfig.theme.nightModeEnabled
    val window = (context as? Activity)?.window
    SideEffect {
        window?.let {
            WindowCompat.getInsetsController(it, it.decorView).isAppearanceLightStatusBars =
                when {
                    fullScreen -> cardColors.containerColor.luminance() > 0.5f
                    appConfig.theme.showWallpaper -> !darkTheme
                    else -> background.luminance() > 0.5f
                }
        }
    }
    DisposableEffect(window, darkTheme) {
        onDispose {
            window?.let { WindowCompat.getInsetsController(it, it.decorView).isAppearanceLightStatusBars = !darkTheme }
        }
    }
    Box(Modifier.fillMaxSize().background(background)) {
        LauncherPanel(
            onDismiss = { (context as? Activity)?.moveTaskToBack(true) },
            onOpenSettings = { navController.navigate("setting") },
        )
    }
}
