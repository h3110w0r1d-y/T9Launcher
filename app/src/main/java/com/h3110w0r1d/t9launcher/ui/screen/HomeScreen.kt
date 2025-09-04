package com.h3110w0r1d.t9launcher.ui.screen

import android.annotation.SuppressLint
import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.h3110w0r1d.t9launcher.R
import com.h3110w0r1d.t9launcher.model.AppViewModel
import com.h3110w0r1d.t9launcher.ui.widget.AppItem
import com.h3110w0r1d.t9launcher.ui.widget.T9Keyboard

@SuppressLint("RestrictedApi", "FrequentlyChangingValue")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: AppViewModel,
) {
    val apps by viewModel.searchResultAppList.collectAsState()
    val appConfig by viewModel.appConfig.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var searchText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val lazyGridState = rememberLazyGridState() // 1. 创建 LazyGridState

    // 3. 使用 LaunchedEffect 监听 apps 的变化
    LaunchedEffect(apps) {
        if (apps.isNotEmpty()) { // 可选：仅在列表不为空时滚动
            lazyGridState.scrollToItem(0)
        }
    }

    DisposableEffect(Unit) {
        viewModel.searchApp(searchText)
        onDispose { }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .clickable(
                    enabled = true,
                    onClick = {
                        viewModel.showHideApp()
                        searchText = ""
                        viewModel.searchApp("")
                        (context as? Activity)?.moveTaskToBack(true)
                    },
                ),
        contentAlignment = Alignment.BottomCenter,
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {},
                    ),
            shape =
                RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                ),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (isLoading) {
                    // 加载进度条
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(210.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            viewModel.refresh()
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(appConfig.appListHeight.dp)
                                .padding(10.dp),
                    ) {
                        LazyVerticalGrid(
                            state = lazyGridState, // 2. 将状态传递给 LazyVerticalGrid
                            columns = GridCells.Fixed(appConfig.gridColumns),
                            modifier =
                                Modifier
                                    .fillMaxSize(),
                        ) {
                            items(apps.size) { i ->
                                var expanded by remember { mutableStateOf(false) }
                                Box {
                                    AppItem(
                                        app = apps[i],
                                        onClick = {
                                            if (apps[i].start(context)) {
                                                viewModel.updateStartCount(apps[i])
                                                searchText = ""
                                                viewModel.searchApp("")
//                                            (context as? Activity)?.moveTaskToBack(true)
                                            }
                                        },
                                        onLongPress = {
                                            expanded = true
                                        },
                                        appConfig = appConfig,
                                    )
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                        shape = RoundedCornerShape(10.dp),
                                        containerColor = colorScheme.surfaceContainer,
                                    ) {
                                        DropdownMenuItem(
                                            leadingIcon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                                            text = { Text(stringResource(id = R.string.app_info)) },
                                            onClick = {
                                                apps[i].detail(context)
                                                expanded = false
                                            },
                                        )
                                        DropdownMenuItem(
                                            leadingIcon = { Icon(Icons.Outlined.ContentCopy, contentDescription = null) },
                                            text = { Text(stringResource(id = R.string.copy_package_name)) },
                                            onClick = {
                                                apps[i].copyPackageName(context)
                                                expanded = false
                                            },
                                        )
                                        DropdownMenuItem(
                                            leadingIcon = { Icon(Icons.Outlined.DeleteForever, contentDescription = null) },
                                            text = { Text(stringResource(id = R.string.uninstall_app)) },
                                            onClick = {
                                                apps[i].uninstall(context)
                                                expanded = false
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                // 搜索文本框
                Text(
                    text = searchText.ifEmpty { " " },
                    modifier =
                        Modifier
                            .alpha(.7f)
                            .background(
                                Color(0x60808080),
                                shape = RoundedCornerShape(100.dp),
                            ).fillMaxWidth(.7f)
                            .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                )

                // T9键盘区域
                val settingString = "⋮"
                val deleteString = "⌫"
                val enterSettingString = stringResource(id = R.string.long_press_open_settings)
                T9Keyboard(
                    onClick = { text ->
                        if (text.all { char -> char.isDigit() }) {
                            searchText += text
                            viewModel.searchApp(searchText)
                        }
                        if (text == deleteString) {
                            if (searchText.isNotEmpty()) {
                                searchText = searchText.dropLast(1)
                            }
                            viewModel.searchApp(searchText)
                        } else if (text == settingString) {
                            Toast.makeText(context, enterSettingString, Toast.LENGTH_SHORT).show()
                        }
                    },
                    onLongClick = { text ->
                        when (text) {
                            deleteString -> {
                                searchText = ""
                                viewModel.searchApp("")
                            }
                            settingString -> {
                                navController.navigate("setting")
                            }
                            else -> {
                                viewModel.showHideApp()
                            }
                        }
                    },
                    appConfig = appConfig,
                )
            }
        }
    }
}
