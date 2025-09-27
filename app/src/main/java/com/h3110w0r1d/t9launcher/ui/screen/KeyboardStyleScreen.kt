package com.h3110w0r1d.t9launcher.ui.screen

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.h3110w0r1d.t9launcher.R
import com.h3110w0r1d.t9launcher.model.AppConfig
import com.h3110w0r1d.t9launcher.model.AppViewModel
import com.h3110w0r1d.t9launcher.ui.widget.StyleSettingCard
import com.h3110w0r1d.t9launcher.ui.widget.T9Keyboard

@SuppressLint("RestrictedApi", "FrequentlyChangingValue")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyboardStyleScreen(
    navController: NavHostController,
    viewModel: AppViewModel,
) {
    val appConfig by viewModel.appConfig.collectAsState()
    val appMap by viewModel.appMap.collectAsState()
    var isChanged by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var previewAppConfig by remember { mutableStateOf(appConfig) }
    val scrollState = rememberScrollState()
    LaunchedEffect(appConfig) {
        previewAppConfig = appConfig
    }

    BackHandler(enabled = isChanged) {
        if (isChanged) {
            showSaveDialog = true
        } else {
            navController.popBackStack()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.keyboard_style)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isChanged) {
                            showSaveDialog = true
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            previewAppConfig = AppConfig()
                            isChanged = true
                        },
                    ) {
                        Icon(Icons.Default.SettingsBackupRestore, contentDescription = null)
                    }
                    IconButton(
                        enabled = isChanged,
                        onClick = {
                            viewModel.updateKeyboardStyle(previewAppConfig)
                            navController.popBackStack()
                        },
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp),
            ) {
                StyleSettingCard(title = stringResource(R.string.keyboard_height)) {
                    Slider(
                        value = previewAppConfig.keyboardButtonHeight,
                        onValueChange = {
                            previewAppConfig = previewAppConfig.copy(keyboardButtonHeight = it)
                            isChanged = true
                        },
                        valueRange = 30f..90f,
                        modifier = Modifier.height(20.dp),
                    )
                }
                StyleSettingCard(title = stringResource(R.string.keyboard_width)) {
                    Slider(
                        value = previewAppConfig.keyboardWidth,
                        onValueChange = {
                            previewAppConfig = previewAppConfig.copy(keyboardWidth = it)
                            isChanged = true
                        },
                        valueRange = .5f..1f,
                        modifier = Modifier.height(20.dp),
                    )
                }
                StyleSettingCard(title = stringResource(R.string.keyboard_bottom_padding)) {
                    Slider(
                        value = previewAppConfig.keyboardBottomPadding,
                        onValueChange = {
                            previewAppConfig = previewAppConfig.copy(keyboardBottomPadding = it)
                            isChanged = true
                        },
                        valueRange = 0f..100f,
                        modifier = Modifier.height(20.dp),
                    )
                }
                StyleSettingCard(title = stringResource(R.string.shortcut_icon_size)) {
                    Slider(
                        value = previewAppConfig.keyboardQSIconSize,
                        onValueChange = {
                            previewAppConfig = previewAppConfig.copy(keyboardQSIconSize = it)
                            isChanged = true
                        },
                        valueRange = 20f..80f,
                        modifier = Modifier.height(20.dp),
                    )
                }
                StyleSettingCard(title = stringResource(R.string.shortcut_icon_opacity)) {
                    Slider(
                        value = previewAppConfig.keyboardQSIconAlpha,
                        onValueChange = {
                            previewAppConfig = previewAppConfig.copy(keyboardQSIconAlpha = it)
                            isChanged = true
                        },
                        valueRange = 0f..1f,
                        modifier = Modifier.height(20.dp),
                    )
                }
            }
            Card(
                shape =
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                    ),
                elevation =
                    CardDefaults.cardElevation(
                        defaultElevation = 10.dp,
                    ),
            ) {
                T9Keyboard(appConfig = previewAppConfig, appMap = appMap)
            }
        }
    }
    // 新增保存提示弹窗
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text(stringResource(R.string.save_changes_title)) },
            text = { Text(stringResource(R.string.save_changes_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateKeyboardStyle(previewAppConfig)
                    showSaveDialog = false
                    navController.popBackStack()
                }) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSaveDialog = false
                    navController.popBackStack()
                }) {
                    Text(stringResource(R.string.dont_save))
                }
                TextButton(onClick = {
                    showSaveDialog = false
                }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}
