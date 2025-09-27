package com.h3110w0r1d.t9launcher.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun T9LauncherTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean = true,
    customColorScheme: String = "blue",
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme =
        if (!dynamicColor) {
            if (darkTheme) {
                when (customColorScheme) {
                    "amber" -> DarkAmberTheme
                    "blue_grey" -> DarkBlueGreyTheme
                    "blue" -> DarkBlueTheme
                    "brown" -> DarkBrownTheme
                    "cyan" -> DarkCyanTheme
                    "deep_orange" -> DarkDeepOrangeTheme
                    "deep_purple" -> DarkDeepPurpleTheme
                    "green" -> DarkGreenTheme
                    "indigo" -> DarkIndigoTheme
                    "light_blue" -> DarkLightBlueTheme
                    "light_green" -> DarkLightGreenTheme
                    "lime" -> DarkLimeTheme
                    "orange" -> DarkOrangeTheme
                    "pink" -> DarkPinkTheme
                    "purple" -> DarkPurpleTheme
                    "red" -> DarkRedTheme
                    "sakura" -> DarkSakuraTheme
                    "teal" -> DarkTealTheme
                    "yellow" -> DarkYellowTheme
                    else -> DarkBlueTheme
                }
            } else {
                when (customColorScheme) {
                    "amber" -> LightAmberTheme
                    "blue_grey" -> LightBlueGreyTheme
                    "blue" -> LightBlueTheme
                    "brown" -> LightBrownTheme
                    "cyan" -> LightCyanTheme
                    "deep_orange" -> LightDeepOrangeTheme
                    "deep_purple" -> LightDeepPurpleTheme
                    "green" -> LightGreenTheme
                    "indigo" -> LightIndigoTheme
                    "light_blue" -> LightLightBlueTheme
                    "light_green" -> LightLightGreenTheme
                    "lime" -> LightLimeTheme
                    "orange" -> LightOrangeTheme
                    "pink" -> LightPinkTheme
                    "purple" -> LightPurpleTheme
                    "red" -> LightRedTheme
                    "sakura" -> LightSakuraTheme
                    "teal" -> LightTealTheme
                    "yellow" -> LightYellowTheme
                    else -> LightBlueTheme
                }
            }
        } else {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                }

                darkTheme -> DarkBlueTheme
                else -> LightBlueTheme
            }
        }
    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
