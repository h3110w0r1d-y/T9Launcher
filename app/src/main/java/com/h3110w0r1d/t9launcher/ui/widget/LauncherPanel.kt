package com.h3110w0r1d.t9launcher.ui.widget

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.h3110w0r1d.t9launcher.R
import com.h3110w0r1d.t9launcher.data.app.AppInfo
import com.h3110w0r1d.t9launcher.data.config.LocalAppConfig
import com.h3110w0r1d.t9launcher.model.LocalGlobalViewModel

@SuppressLint("FrequentlyChangingValue", "ShowToast")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LauncherPanel(
    overlay: Boolean = false,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val viewModel = LocalGlobalViewModel.current
    val apps by viewModel.searchResultAppList.collectAsState()
    val appMap by viewModel.appMap.collectAsState()
    val appConfig = LocalAppConfig.current
    val fullScreen = !overlay && appConfig.theme.fullScreenEnabled
    val cardColors = CardDefaults.cardColors()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var searchText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val lazyGridState = rememberLazyGridState()
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(overlay) {
        if (overlay) focusRequester.requestFocus()
    }
    val lastToastTime = remember { mutableLongStateOf(0L) }

    fun clearSearch() {
        searchText = ""
        viewModel.searchApp("")
    }

    // 检测是否正在滚动
    var isScrolling by remember { mutableStateOf(false) }

    val isScrolledToTop by remember {
        derivedStateOf {
            lazyGridState.firstVisibleItemIndex == 0 &&
                lazyGridState.firstVisibleItemScrollOffset == 0
        }
    }
    // 监听滚动状态变化
    LaunchedEffect(lazyGridState.isScrollInProgress) {
        isScrolling =
            if (lazyGridState.isScrollInProgress) {
                !isScrolledToTop
            } else {
                false
            }
    }

    val is12Key: Boolean = LocalConfiguration.current.keyboard == Configuration.KEYBOARD_12KEY

    // 监听 apps 的变化
    LaunchedEffect(apps) {
        if (apps.isNotEmpty()) { // 可选：仅在列表不为空时滚动
            lazyGridState.scrollToItem(0)
        }
    }

    LaunchedEffect(Unit) {
        clearSearch()
        if (overlay) viewModel.refreshRecentStartCounts()
    }
    // Navigation entries stop when opening settings; only the Activity stop dismisses home.
    val lifecycleOwner = if (overlay) LocalLifecycleOwner.current else (context as? LifecycleOwner ?: LocalLifecycleOwner.current)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (!overlay && event == Lifecycle.Event.ON_STOP) {
                clearSearch()
                onDismiss()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    var selectedApp by remember { mutableStateOf<AppInfo?>(null) }
    BackHandler(enabled = overlay) {
        if (selectedApp != null) selectedApp = null else onDismiss()
    }
    fun launchApp(app: AppInfo): Boolean {
        val started = app.start(context)
        if (started) {
            viewModel.updateStartCount(app)
            if (overlay) onDismiss()
        } else if (overlay) {
            Toast.makeText(context, R.string.overlay_launch_failed, Toast.LENGTH_SHORT).show()
        }
        return started
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(if (overlay) Color.Black.copy(alpha = .2f) else Color.Transparent)
                .clickable(
                    enabled = true,
                    onClick = {
                        onDismiss()
                    },
                ).onKeyEvent { keyEvent: KeyEvent ->
                    if (keyEvent.type != KeyEventType.KeyDown) return@onKeyEvent false
                    when (keyEvent.key) {
                        // 实体键盘上的数字键，其 keyCode 通常与 Key.Zero 到 Key.Nine 对应
                        Key.Zero, Key.One, Key.Two, Key.Three, Key.Four,
                        Key.Five, Key.Six, Key.Seven, Key.Eight, Key.Nine,
                        -> {
                            val number = keyEvent.nativeKeyEvent.keyCode - Key.Zero.keyCode
                            if (number in 0..9) {
                                if (viewModel.searchApp(searchText + number) || isLoading) {
                                    searchText += number.toString()
                                }
                            }
                            return@onKeyEvent true
                        }

                        Key.Multiply -> {
                            onOpenSettings()
                            return@onKeyEvent true
                        }

                        Key.Pound -> {
                            if (searchText.isNotEmpty()) {
                                searchText = searchText.dropLast(1)
                            }
                            viewModel.searchApp(searchText)
                            return@onKeyEvent true
                        }

                        else -> {
                            false
                        }
                    }
                }.then(if (overlay) Modifier.focusRequester(focusRequester).focusable() else Modifier),
        contentAlignment = Alignment.BottomCenter,
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .then(
                        if (overlay) {
                            Modifier.windowInsetsPadding(
                                WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                            ).padding(top = 64.dp)
                        } else Modifier,
                    )
                    .then(if (fullScreen) Modifier.fillMaxSize() else Modifier)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {},
                    ),
            shape =
                RoundedCornerShape(
                    topStart = if (fullScreen) 0.dp else 20.dp,
                    topEnd = if (fullScreen) 0.dp else 20.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp,
                ),
            colors = cardColors,
        ) {
            LauncherContentLayout(
                modifier = Modifier.fillMaxWidth().then(
                    if (fullScreen) Modifier.fillMaxSize().safeDrawingPadding()
                    else if (overlay) Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                    else Modifier.navigationBarsPadding(),
                ),
                fullScreen = fullScreen,
                overlay = overlay,
                listHeight = appConfig.appListStyle.appListHeight.dp,
                list = {
                    val listModifier = Modifier.fillMaxSize()
                    if (isLoading) {
                        // 加载进度条
                        Box(
                            modifier = listModifier,
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        val pullRefreshState = rememberPullToRefreshState()
                        val shouldEnablePullToRefresh by remember {
                            derivedStateOf {
                                isScrolledToTop && !isScrolling
                            }
                        }
                        Box(
                            modifier =
                                listModifier
                                    .padding(10.dp)
                                    .pullToRefresh(
                                        isRefreshing = isRefreshing,
                                        state = pullRefreshState,
                                        enabled = shouldEnablePullToRefresh,
                                        onRefresh = { viewModel.refresh() },
                                    ),
                        ) {
                            LazyVerticalGrid(
                                state = lazyGridState,
                                columns = GridCells.Fixed(appConfig.appListStyle.gridColumns),
                                modifier =
                                    Modifier
                                        .fillMaxSize(),
                            ) {
                                items(apps.size, key = { apps[it].componentId() }) { i ->
                                    var expanded by remember { mutableStateOf(false) }
                                    Box {
                                        AppItem(
                                            app = apps[i],
                                            onClick = {
                                                launchApp(apps[i])
                                            },
                                            onLongPress = {
                                                if (overlay) selectedApp = apps[i] else expanded = true
                                            },
                                        )
                                        if (!overlay) AppDropdownMenu(apps[i], expanded) { expanded = it }
                                    }
                                }
                            }

                            Indicator(
                                modifier = Modifier.align(Alignment.TopCenter),
                                isRefreshing = isRefreshing,
                                state = pullRefreshState,
                            )
                        }
                    }
                },
                controls = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                        if (!is12Key) {
                            // T9键盘区域
                            val enterSettingString = stringResource(id = R.string.long_press_open_settings)
                            T9Keyboard(
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
                                            clearSearch()
                                        }

                                        "setting" -> {
                                            onOpenSettings()
                                        }

                                        else -> {
                                            if (text.toInt() > 0) {
                                                val appInfo = appMap[appConfig.shortcutConfig[text.toInt() - 1]]
                                                if (appInfo != null) {
                                                    if (launchApp(appInfo)) {
                                                        return@T9Keyboard
                                                    }
                                                }
                                            }
                                            viewModel.showHideApp()
                                        }
                                    }
                                },
                                onCancel = {
                                    clearSearch()
                                },
                                appMap = appMap,
                            )
                        }
                    }
                },
            )
        }
        selectedApp?.let { app ->
            OverlayAppMenu(app, onDismiss = { selectedApp = null }, onExternalAction = onDismiss)
        }
    }
}

/** Measure controls first so font scale and keyboard padding count toward the available space. */
@Composable
private fun LauncherContentLayout(
    modifier: Modifier,
    fullScreen: Boolean,
    overlay: Boolean,
    listHeight: Dp,
    list: @Composable () -> Unit,
    controls: @Composable () -> Unit,
) {
    SubcomposeLayout(modifier) { constraints ->
        val maxHeight = constraints.maxHeight
        val reservedList = if (overlay) minOf(96.dp.roundToPx(), maxHeight / 3) else 0
        val controlsHeight = (maxHeight - reservedList).coerceAtLeast(0)
        val controlsPlaceable = subcompose("controls") {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) { controls() }
        }.single().measure(Constraints(maxWidth = constraints.maxWidth, maxHeight = controlsHeight))
        val remaining = (maxHeight - controlsPlaceable.height).coerceAtLeast(0)
        val actualListHeight = if (fullScreen) remaining else listHeight.roundToPx().coerceIn(0, remaining)
        val listPlaceable = subcompose("list", list).single().measure(
            Constraints.fixed(constraints.maxWidth, actualListHeight),
        )
        layout(constraints.maxWidth, listPlaceable.height + controlsPlaceable.height) {
            listPlaceable.placeRelative(0, 0)
            controlsPlaceable.placeRelative(0, listPlaceable.height)
        }
    }
}
