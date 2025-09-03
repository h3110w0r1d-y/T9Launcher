package com.h3110w0r1d.t9launcher.ui

import android.util.Log
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.h3110w0r1d.t9launcher.model.AppViewModel
import com.h3110w0r1d.t9launcher.ui.screen.HideAppScreen
import com.h3110w0r1d.t9launcher.ui.screen.HomeScreen
import com.h3110w0r1d.t9launcher.ui.screen.SettingScreen

@Composable
fun AppNavigation(viewModel: AppViewModel = hiltViewModel()) {
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        viewModel.loadAppList()
    }
    DisposableEffect(Unit) {
        Log.d("MainComposableLifecycle", "MainComposable entered composition")

        onDispose {
            Log.d("MainComposableLifecycle", "MainComposable exited composition")
        }
    }

    NavHost(
        navController = navController,
        startDestination = "home",
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth },
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth },
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
            )
        },
    ) {
        composable(
            "home",
        ) {
            HomeScreen(navController, viewModel)
        }

        composable("setting") {
            SettingScreen(navController, viewModel)
        }

        composable("hide_app") {
            HideAppScreen(navController, viewModel)
        }
    }
}
