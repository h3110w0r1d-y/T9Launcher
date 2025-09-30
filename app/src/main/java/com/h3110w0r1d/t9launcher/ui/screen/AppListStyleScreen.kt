package com.h3110w0r1d.t9launcher.ui.screen

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.h3110w0r1d.t9launcher.R
import com.h3110w0r1d.t9launcher.data.config.AppListStyleConfig
import com.h3110w0r1d.t9launcher.data.config.LocalAppConfig
import com.h3110w0r1d.t9launcher.model.LocalGlobalViewModel
import com.h3110w0r1d.t9launcher.ui.LocalNavController
import com.h3110w0r1d.t9launcher.ui.widget.AppItem
import com.h3110w0r1d.t9launcher.ui.widget.StyleSettingCard

@SuppressLint("RestrictedApi", "FrequentlyChangingValue")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListStyleScreen() {
    val navController = LocalNavController.current!!
    val viewModel = LocalGlobalViewModel.current
    val apps by viewModel.searchResultAppList.collectAsState()
    val appConfig = LocalAppConfig.current
    var isChanged by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var previewAppListConfig by remember { mutableStateOf(appConfig.appListStyle) }
    val scrollState = rememberScrollState()

    LaunchedEffect(appConfig) {
        previewAppListConfig = appConfig.appListStyle
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
                title = { Text(stringResource(R.string.app_list_style)) },
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
                            previewAppListConfig = AppListStyleConfig()
                            isChanged = true
                        },
                    ) {
                        Icon(Icons.Default.SettingsBackupRestore, contentDescription = null)
                    }
                    IconButton(
                        enabled = isChanged,
                        onClick = {
                            viewModel.updateAppListStyle(previewAppListConfig)
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
                StyleSettingCard(title = stringResource(R.string.grid_columns)) {
                    Slider(
                        value = previewAppListConfig.gridColumns.toFloat(),
                        onValueChange = {
                            previewAppListConfig = previewAppListConfig.copy(gridColumns = it.toInt())
                            isChanged = true
                        },
                        valueRange = 2f..10f,
                        steps = 7,
                        modifier = Modifier.height(20.dp),
                    )
                }
                StyleSettingCard(title = stringResource(R.string.app_list_height)) {
                    Slider(
                        value = previewAppListConfig.appListHeight,
                        onValueChange = {
                            previewAppListConfig = previewAppListConfig.copy(appListHeight = it)
                            isChanged = true
                        },
                        valueRange = 100f..500f,
                        modifier = Modifier.height(20.dp),
                    )
                }
                StyleSettingCard(title = stringResource(R.string.icon_size)) {
                    Slider(
                        value = previewAppListConfig.iconSize,
                        onValueChange = {
                            previewAppListConfig = previewAppListConfig.copy(iconSize = it)
                            isChanged = true
                        },
                        valueRange = 10f..100f,
                        modifier = Modifier.height(20.dp),
                    )
                }
                StyleSettingCard(title = stringResource(R.string.icon_corner_radius)) {
                    Slider(
                        value = previewAppListConfig.iconCornerRadius.toFloat(),
                        onValueChange = {
                            previewAppListConfig = previewAppListConfig.copy(iconCornerRadius = it.toInt())
                            isChanged = true
                        },
                        valueRange = 0f..50f,
                        modifier = Modifier.height(20.dp),
                    )
                }
                StyleSettingCard(title = stringResource(R.string.app_name_size)) {
                    Slider(
                        value = previewAppListConfig.appNameSize,
                        onValueChange = {
                            previewAppListConfig = previewAppListConfig.copy(appNameSize = it)
                            isChanged = true
                        },
                        valueRange = 8f..16f,
                        modifier = Modifier.height(20.dp),
                    )
                }
                StyleSettingCard(title = stringResource(R.string.icon_horizon_padding)) {
                    Slider(
                        value = previewAppListConfig.iconHorizonPadding,
                        onValueChange = {
                            previewAppListConfig = previewAppListConfig.copy(iconHorizonPadding = it)
                            isChanged = true
                        },
                        valueRange = 0f..20f,
                        modifier = Modifier.height(20.dp),
                    )
                }
                StyleSettingCard(title = stringResource(R.string.icon_vertical_padding)) {
                    Slider(
                        value = previewAppListConfig.iconVerticalPadding,
                        onValueChange = {
                            previewAppListConfig = previewAppListConfig.copy(iconVerticalPadding = it)
                            isChanged = true
                        },
                        valueRange = 0f..20f,
                        modifier = Modifier.height(20.dp),
                    )
                }
                StyleSettingCard(title = stringResource(R.string.row_spacing)) {
                    Slider(
                        value = previewAppListConfig.rowSpacing,
                        onValueChange = {
                            previewAppListConfig = previewAppListConfig.copy(rowSpacing = it)
                            isChanged = true
                        },
                        valueRange = 0f..20f,
                        modifier = Modifier.height(20.dp),
                    )
                }
            }
            Card(
                modifier =
                    Modifier
                        .height(previewAppListConfig.appListHeight.dp),
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
                CompositionLocalProvider(
                    LocalAppConfig provides
                        appConfig.copy(appListStyle = previewAppListConfig),
                ) {
                    Box(modifier = Modifier.padding(10.dp)) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(previewAppListConfig.gridColumns),
                            modifier =
                                Modifier
                                    .fillMaxSize(),
                        ) {
                            items(apps.size) { AppItem(app = apps[it]) }
                        }
                    }
                }
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
                    viewModel.updateAppListStyle(previewAppListConfig)
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
