package com.h3110w0r1d.t9launcher.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring.StiffnessMediumLow
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.h3110w0r1d.t9launcher.model.LocalGlobalViewModel
import com.h3110w0r1d.t9launcher.ui.screen.AppListStyleScreen
import com.h3110w0r1d.t9launcher.ui.screen.HideAppScreen
import com.h3110w0r1d.t9launcher.ui.screen.HomeScreen
import com.h3110w0r1d.t9launcher.ui.screen.KeyboardStyleScreen
import com.h3110w0r1d.t9launcher.ui.screen.OnboardingScreen
import com.h3110w0r1d.t9launcher.ui.screen.SelectShortcutScreen
import com.h3110w0r1d.t9launcher.ui.screen.SettingScreen
import com.h3110w0r1d.t9launcher.ui.screen.ShortcutScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel = LocalGlobalViewModel.current

    LaunchedEffect(Unit) {
        viewModel.loadAppList()
    }

    val animationSpec =
        tween<IntOffset>(
            durationMillis = StiffnessMediumLow.toInt(),
            easing = FastOutSlowInEasing,
        )

    CompositionLocalProvider(LocalNavController provides navController) {
        NavHost(
            navController = navController,
            startDestination = "home",
            enterTransition = {
                slideInHorizontally(
                    animationSpec = animationSpec,
                    initialOffsetX = { fullWidth -> fullWidth },
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    animationSpec = animationSpec,
                    targetOffsetX = { fullWidth -> -fullWidth / 2 },
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    animationSpec = animationSpec,
                    initialOffsetX = { fullWidth -> -fullWidth / 2 },
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    animationSpec = animationSpec,
                    targetOffsetX = { fullWidth -> fullWidth },
                )
            },
        ) {
            composable("home") { HomeScreen() }
            composable("setting") { SettingScreen() }
            composable("hide_app") { HideAppScreen() }
            composable("app_list_style") { AppListStyleScreen() }
            composable("keyboard_style") { KeyboardStyleScreen() }
            composable("onboarding") { OnboardingScreen() }
            composable("shortcut") { ShortcutScreen() }
            composable("select_shortcut/{index}") { backStackEntry ->
                val index = backStackEntry.arguments?.getString("index")?.toIntOrNull() ?: 0
                SelectShortcutScreen(index)
            }
        }
    }
}

val LocalNavController = staticCompositionLocalOf<NavHostController?> { null }
