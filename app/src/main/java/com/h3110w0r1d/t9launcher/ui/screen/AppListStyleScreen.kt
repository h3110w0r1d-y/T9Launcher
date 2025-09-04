package com.h3110w0r1d.t9launcher.ui.screen

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
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
import com.h3110w0r1d.t9launcher.ui.widget.AppItem
import com.h3110w0r1d.t9launcher.ui.widget.StyleSettingCard

@SuppressLint("RestrictedApi", "FrequentlyChangingValue")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListStyleScreen(
    navController: NavHostController,
    viewModel: AppViewModel,
) {
    val apps by viewModel.searchResultAppList.collectAsState()
    val appConfig by viewModel.appConfig.collectAsState()
    var isChanged by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var previewAppConfig by remember { mutableStateOf(appConfig) }
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
                            previewAppConfig = AppConfig()
                            isChanged = true
                        },
                    ) {
                        Icon(Icons.Default.SettingsBackupRestore, contentDescription = null)
                    }
                    IconButton(
                        enabled = isChanged,
                        onClick = {
                            viewModel.updateAppListStyle(previewAppConfig)
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
            LazyColumn(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
            ) {
                item {
                    StyleSettingCard(title = stringResource(R.string.grid_columns)) {
                        Slider(
                            value = previewAppConfig.gridColumns.toFloat(),
                            onValueChange = {
                                previewAppConfig = previewAppConfig.copy(gridColumns = it.toInt())
                                isChanged = true
                            },
                            valueRange = 2f..10f,
                            steps = 7,
                            modifier = Modifier.height(20.dp),
                        )
                    }
                    StyleSettingCard(title = stringResource(R.string.app_list_height)) {
                        Slider(
                            value = previewAppConfig.appListHeight,
                            onValueChange = {
                                previewAppConfig = previewAppConfig.copy(appListHeight = it)
                                isChanged = true
                            },
                            valueRange = 100f..500f,
                            modifier = Modifier.height(20.dp),
                        )
                    }
                    StyleSettingCard(title = stringResource(R.string.icon_size)) {
                        Slider(
                            value = previewAppConfig.iconSize,
                            onValueChange = {
                                previewAppConfig = previewAppConfig.copy(iconSize = it)
                                isChanged = true
                            },
                            valueRange = 10f..100f,
                            modifier = Modifier.height(20.dp),
                        )
                    }
                    StyleSettingCard(title = stringResource(R.string.icon_horizon_padding)) {
                        Slider(
                            value = previewAppConfig.iconHorizonPadding,
                            onValueChange = {
                                previewAppConfig = previewAppConfig.copy(iconHorizonPadding = it)
                                isChanged = true
                            },
                            valueRange = 0f..20f,
                            modifier = Modifier.height(20.dp),
                        )
                    }
                    StyleSettingCard(title = stringResource(R.string.icon_vertical_padding)) {
                        Slider(
                            value = previewAppConfig.iconVerticalPadding,
                            onValueChange = {
                                previewAppConfig = previewAppConfig.copy(iconVerticalPadding = it)
                                isChanged = true
                            },
                            valueRange = 0f..20f,
                            modifier = Modifier.height(20.dp),
                        )
                    }
                    StyleSettingCard(title = stringResource(R.string.row_spacing)) {
                        Slider(
                            value = previewAppConfig.rowSpacing,
                            onValueChange = {
                                previewAppConfig = previewAppConfig.copy(rowSpacing = it)
                                isChanged = true
                            },
                            valueRange = 0f..20f,
                            modifier = Modifier.height(20.dp),
                        )
                    }
                }
            }
            Card(
                modifier =
                    Modifier
                        .height(previewAppConfig.appListHeight.dp),
                shape =
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                    ),
                elevation =
                    androidx.compose.material3.CardDefaults.cardElevation(
                        defaultElevation = 10.dp,
                    ),
            ) {
                Box(modifier = Modifier.padding(10.dp)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(previewAppConfig.gridColumns),
                        modifier =
                            Modifier
                                .fillMaxSize(),
                    ) {
                        items(apps.size) { i ->
                            AppItem(app = apps[i], appConfig = previewAppConfig)
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
                    viewModel.updateAppListStyle(previewAppConfig)
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
