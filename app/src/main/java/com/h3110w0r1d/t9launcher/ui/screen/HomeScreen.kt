package com.h3110w0r1d.t9launcher.ui.screen

import android.annotation.SuppressLint
import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavHostController
import com.h3110w0r1d.t9launcher.R
import com.h3110w0r1d.t9launcher.model.AppViewModel
import com.h3110w0r1d.t9launcher.ui.widget.AppDropdownMenu
import com.h3110w0r1d.t9launcher.ui.widget.AppItem
import com.h3110w0r1d.t9launcher.ui.widget.T9Keyboard

@SuppressLint("RestrictedApi", "FrequentlyChangingValue", "ShowToast")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: AppViewModel,
) {
    val apps by viewModel.searchResultAppList.collectAsState()
    val appMap by viewModel.appMap.collectAsState()
    val appConfig by viewModel.appConfig.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var searchText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val lazyGridState = rememberLazyGridState() // 1. 创建 LazyGridState
    val lastToastTime = remember { mutableLongStateOf(0L) }

    // 监听 apps 的变化
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
            ConstraintLayout(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
            ) {
                val (listRef, inputRef, keyboardRef) = createRefs()
                if (isLoading) {
                    // 加载进度条
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(appConfig.appListHeight.dp)
                                .constrainAs(listRef) {
                                    bottom.linkTo(inputRef.top)
                                },
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
                                .height(appConfig.appListHeight.dp)
                                .padding(10.dp)
                                .constrainAs(listRef) {
                                    bottom.linkTo(inputRef.top)
                                },
                    ) {
                        LazyVerticalGrid(
                            state = lazyGridState,
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
                                                (context as? Activity)?.moveTaskToBack(true)
                                            }
                                        },
                                        onLongPress = {
                                            expanded = true
                                        },
                                        appConfig = appConfig,
                                    )
                                    AppDropdownMenu(apps[i], expanded) { expanded = it }
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
                            .padding(vertical = 8.dp)
                            .constrainAs(inputRef) {
                                bottom.linkTo(keyboardRef.top)
                                centerHorizontallyTo(parent)
                            },
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                )

                // T9键盘区域
                val enterSettingString = stringResource(id = R.string.long_press_open_settings)
                T9Keyboard(
                    modifier =
                        Modifier
                            .constrainAs(keyboardRef) {
                                bottom.linkTo(parent.bottom)
                            },
                    onClick = { text ->
                        if (text.all { char -> char.isDigit() }) {
                            if (viewModel.searchApp(searchText + text) || isLoading) {
                                searchText += text
                            }
                        }
                        if (text == "delete") {
                            if (searchText.isNotEmpty()) {
                                searchText = searchText.dropLast(1)
                            }
                            viewModel.searchApp(searchText)
                        } else if (text == "setting") {
                            if (System.currentTimeMillis() - lastToastTime.longValue > 2000) {
                                Toast.makeText(context, enterSettingString, Toast.LENGTH_SHORT).show()
                                lastToastTime.longValue = System.currentTimeMillis()
                            }
                        }
                    },
                    onLongClick = { text ->
                        when (text) {
                            "delete" -> {
                                searchText = ""
                                viewModel.searchApp("")
                            }
                            "setting" -> {
                                navController.navigate("setting")
                            }
                            else -> {
                                if (text.toInt() > 0) {
                                    val appInfo = appMap[appConfig.shortcutConfig[text.toInt() - 1]]
                                    if (appInfo != null) {
                                        if (appInfo.start(context)) {
                                            viewModel.updateStartCount(appInfo)
                                            searchText = ""
                                            viewModel.searchApp("")
                                            (context as? Activity)?.moveTaskToBack(true)
                                            return@T9Keyboard
                                        }
                                    }
                                }
                                viewModel.showHideApp()
                            }
                        }
                    },
                    onCancel = {
                        searchText = ""
                        viewModel.searchApp("")
                    },
                    appConfig = appConfig,
                    appMap = appMap,
                )
            }
        }
    }
}
