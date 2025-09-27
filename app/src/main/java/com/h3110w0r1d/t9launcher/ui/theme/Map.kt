package com.h3110w0r1d.t9launcher.ui.theme

import androidx.compose.ui.graphics.Color

fun getPrimaryColorMap(
    darkTheme: Boolean,
    colorSchema: String,
): Color =
    if (darkTheme) {
        when (colorSchema) {
            "amber" -> DarkAmberTheme.primary
            "blue_grey" -> DarkBlueGreyTheme.primary
            "blue" -> DarkBlueTheme.primary
            "brown" -> DarkBrownTheme.primary
            "cyan" -> DarkCyanTheme.primary
            "deep_orange" -> DarkDeepOrangeTheme.primary
            "deep_purple" -> DarkDeepPurpleTheme.primary
            "green" -> DarkGreenTheme.primary
            "indigo" -> DarkIndigoTheme.primary
            "light_blue" -> DarkLightBlueTheme.primary
            "light_green" -> DarkLightGreenTheme.primary
            "lime" -> DarkLimeTheme.primary
            "orange" -> DarkOrangeTheme.primary
            "pink" -> DarkPinkTheme.primary
            "purple" -> DarkPurpleTheme.primary
            "red" -> DarkRedTheme.primary
            "sakura" -> DarkSakuraTheme.primary
            "teal" -> DarkTealTheme.primary
            "yellow" -> DarkYellowTheme.primary
            else -> DarkBlueTheme.primary
        }
    } else {
        when (colorSchema) {
            "amber" -> LightAmberTheme.primary
            "blue_grey" -> LightBlueGreyTheme.primary
            "blue" -> LightBlueTheme.primary
            "brown" -> LightBrownTheme.primary
            "cyan" -> LightCyanTheme.primary
            "deep_orange" -> LightDeepOrangeTheme.primary
            "deep_purple" -> LightDeepPurpleTheme.primary
            "green" -> LightGreenTheme.primary
            "indigo" -> LightIndigoTheme.primary
            "light_blue" -> LightLightBlueTheme.primary
            "light_green" -> LightLightGreenTheme.primary
            "lime" -> LightLimeTheme.primary
            "orange" -> LightOrangeTheme.primary
            "pink" -> LightPinkTheme.primary
            "purple" -> LightPurpleTheme.primary
            "red" -> LightRedTheme.primary
            "sakura" -> LightSakuraTheme.primary
            "teal" -> LightTealTheme.primary
            "yellow" -> LightYellowTheme.primary
            else -> LightBlueTheme.primary
        }
    }
