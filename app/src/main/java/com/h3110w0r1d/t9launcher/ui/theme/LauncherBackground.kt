package com.h3110w0r1d.t9launcher.ui.theme

import android.app.WallpaperManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.h3110w0r1d.t9launcher.data.config.LocalAppConfig

/** Wallpaper-derived color is independent of the selected UI accent and custom background. */
@Composable
fun adaptiveBackgroundColor(): Color {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val theme = LocalAppConfig.current.theme
    val dark = if (theme.nightModeFollowSystem) isSystemInDarkTheme() else theme.nightModeEnabled
    val fallback = if (dark) DarkBlueTheme.primaryContainer else LightBlueTheme.primaryContainer
    var wallpaperColor by remember { mutableStateOf<Color?>(null) }
    DisposableEffect(context, configuration) {
        val manager = WallpaperManager.getInstance(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            fun refresh() {
                wallpaperColor = runCatching {
                    manager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)?.primaryColor?.toArgb()?.let { Color(it) }
                }.getOrNull()
            }
            refresh()
            val listener = WallpaperManager.OnColorsChangedListener { _, which ->
                if (which and WallpaperManager.FLAG_SYSTEM != 0) refresh()
            }
            manager.addOnColorsChangedListener(listener, Handler(Looper.getMainLooper()))
            onDispose { manager.removeOnColorsChangedListener(listener) }
        } else {
            onDispose { }
        }
    }
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Read again after wallpaper callbacks/configuration updates, without persisting the result.
        remember(context, configuration, dark, wallpaperColor) {
            if (dark) dynamicDarkColorScheme(context).primaryContainer else dynamicLightColorScheme(context).primaryContainer
        }
    } else {
        wallpaperColor ?: fallback
    }
}
