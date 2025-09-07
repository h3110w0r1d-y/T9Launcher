package com.h3110w0r1d.t9launcher.ui

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.h3110w0r1d.t9launcher.model.AppViewModel
import com.h3110w0r1d.t9launcher.ui.screen.AppListStyleScreen
import com.h3110w0r1d.t9launcher.ui.screen.HideAppScreen
import com.h3110w0r1d.t9launcher.ui.screen.HomeScreen
import com.h3110w0r1d.t9launcher.ui.screen.KeyboardStyleScreen
import com.h3110w0r1d.t9launcher.ui.screen.SettingScreen

@Composable
fun AppNavigation(viewModel: AppViewModel) {
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        viewModel.loadAppList()
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
        composable("home") { HomeScreen(navController, viewModel) }
        composable("setting") { SettingScreen(navController, viewModel) }
        composable("hide_app") { HideAppScreen(navController, viewModel) }
        composable("app_list_style") { AppListStyleScreen(navController, viewModel) }
        composable("keyboard_style") { KeyboardStyleScreen(navController, viewModel) }
    }
}
