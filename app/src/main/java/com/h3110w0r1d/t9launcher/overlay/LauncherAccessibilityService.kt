package com.h3110w0r1d.t9launcher.overlay

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowInsets
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.core.view.doOnAttach
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.h3110w0r1d.t9launcher.R
import com.h3110w0r1d.t9launcher.activity.MainActivity
import com.h3110w0r1d.t9launcher.data.app.AppRepository
import com.h3110w0r1d.t9launcher.data.config.AppConfigManager
import com.h3110w0r1d.t9launcher.data.config.LocalAppConfig
import com.h3110w0r1d.t9launcher.model.AppViewModel
import com.h3110w0r1d.t9launcher.model.LocalGlobalViewModel
import com.h3110w0r1d.t9launcher.ui.theme.AppTheme
import com.h3110w0r1d.t9launcher.ui.widget.LauncherPanel
import com.h3110w0r1d.t9launcher.utils.PinyinUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

@SuppressLint("AccessibilityPolicy")
@AndroidEntryPoint
class LauncherAccessibilityService : AccessibilityService() {
    @Inject lateinit var configManager: AppConfigManager

    @Inject lateinit var appRepository: AppRepository

    @Inject lateinit var pinyinUtil: PinyinUtil

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val handler = Handler(Looper.getMainLooper())
    private val windowManager by lazy { getSystemService(WINDOW_SERVICE) as WindowManager }
    private var button: FrameLayout? = null
    private var buttonIcon: ImageView? = null
    private var buttonParams: WindowManager.LayoutParams? = null
    private var panel: FrameLayout? = null
    private var composition: ComposeView? = null
    private var panelOwner: OverlayWindowOwner? = null
    private var unregisterBack: (() -> Unit)? = null
    private var screenAvailable = true
    private var receiverRegistered = false
    private var connected = false
    private var closingSystemPanel = false
    private var openingSettings = false
    private val settingsLaunchTimeout =
        Runnable {
            openingSettings = false
            syncButton()
        }
    private var configJob: Job? = null
    private var dragging = false
    private var dockRight = true
    private var verticalPosition = .5f
    private val tuckButton =
        Runnable {
            buttonIcon
                ?.animate()
                ?.alpha(.45f)
                ?.translationX(
                    (if (dockRight) 1 else -1) * dp(16).toFloat(),
                )?.setDuration(200)
                ?.start()
        }
    private val screenReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(
                context: Context?,
                intent: Intent?,
            ) {
                if (intent?.action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
                    // HOME / recents should dismiss the launcher rather than leave it above the new task.
                    val reason = intent.getStringExtra("reason")
                    if (reason == "homekey" || reason == "recentapps") dismissPanel()
                    return
                }
                screenAvailable = intent?.action != Intent.ACTION_SCREEN_OFF
                if (!screenAvailable && OverlayServiceState.usesSystemShortcut) {
                    closeSystemPanel()
                    return
                }
                if (!canShow()) hideWindows() else syncButton()
            }
        }

    override fun onServiceConnected() {
        super.onServiceConnected()
        if (connected) return
        connected = true
        closingSystemPanel = false
        OverlayServiceState.service = this
        screenAvailable = (getSystemService(POWER_SERVICE) as PowerManager).isInteractive
        ContextCompat.registerReceiver(
            this,
            screenReceiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_USER_PRESENT)
                addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            },
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        receiverRegistered = true
        configJob =
            scope.launch {
                configManager.appConfig.collect { config ->
                    dockRight = config.overlay.dockRight
                    verticalPosition = config.overlay.verticalPosition.coerceIn(0f, 1f)
                    if (OverlayServiceState.usesSystemShortcut && config.isConfigInitialized && OverlayServiceState.appVisible) {
                        closeSystemPanel()
                    } else if (!canShow()) {
                        hideWindows()
                    } else {
                        syncButton()
                    }
                }
            }
    }

    private fun canShow(): Boolean =
        connected && screenAvailable &&
            !closingSystemPanel &&
            !OverlayServiceState.appVisible && !openingSettings &&
            configManager.appConfig.value.isConfigInitialized &&
            (OverlayServiceState.usesSystemShortcut || configManager.appConfig.value.overlay.enabled) &&
            !(getSystemService(KEYGUARD_SERVICE) as KeyguardManager).isKeyguardLocked

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).roundToInt()

    @Suppress("DEPRECATION")
    private fun displaySize(): Point {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = windowManager.currentWindowMetrics
            val insets =
                metrics.windowInsets.getInsetsIgnoringVisibility(
                    WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout(),
                )
            return Point(
                (metrics.bounds.width() - insets.left - insets.right).coerceAtLeast(1),
                (metrics.bounds.height() - insets.top - insets.bottom).coerceAtLeast(1),
            )
        }
        return Point().also { windowManager.defaultDisplay.getSize(it) }
    }

    private fun syncButton() {
        if (panel != null || !canShow()) return
        if (OverlayServiceState.usesSystemShortcut) {
            showPanel()
            return
        }
        if (button == null) createButton()
        positionButton()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createButton() {
        val context = ContextThemeWrapper(this, R.style.Theme_T9Launcher)
        val icon =
            ImageView(context).apply {
                setImageResource(R.mipmap.ic_launcher)
                scaleType = ImageView.ScaleType.FIT_CENTER
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }
        val root =
            FrameLayout(context).apply {
                clipChildren = true
                contentDescription = getString(R.string.overlay_button_description)
                isClickable = true
                isFocusable = true
                addView(icon, FrameLayout.LayoutParams(dp(48), dp(48)))
                setOnClickListener { showPanel() }
            }
        val params =
            WindowManager
                .LayoutParams(
                    dp(48),
                    dp(48),
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    PixelFormat.TRANSLUCENT,
                ).apply { gravity = Gravity.TOP or Gravity.START }
        var downX = 0f
        var downY = 0f
        var startX = 0
        var startY = 0
        val slop = ViewConfiguration.get(this).scaledTouchSlop
        root.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    handler.removeCallbacks(tuckButton)
                    icon.animate().cancel()
                    icon.alpha = 1f
                    icon.translationX = 0f
                    downX = event.rawX
                    downY = event.rawY
                    startX = params.x
                    startY = params.y
                    dragging = false
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - downX
                    val dy = event.rawY - downY
                    if (abs(dx) > slop || abs(dy) > slop) dragging = true
                    if (dragging) {
                        val bounds = displaySize()
                        params.x = (startX + dx.roundToInt()).coerceIn(0, (bounds.x - dp(48)).coerceAtLeast(0))
                        params.y = (startY + dy.roundToInt()).coerceIn(0, (bounds.y - dp(48)).coerceAtLeast(0))
                        updateButtonLayout()
                    }
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (dragging) {
                        val bounds = displaySize()
                        dockRight = params.x + dp(24) >= bounds.x / 2
                        verticalPosition = params.y.toFloat() / (bounds.y - dp(48)).coerceAtLeast(1)
                        dragging = false
                        positionButton()
                        scope.launch { configManager.updateOverlayPosition(dockRight, verticalPosition) }
                    } else if (event.actionMasked == MotionEvent.ACTION_UP) {
                        view.performClick()
                    }
                    scheduleTuck()
                    true
                }

                else -> {
                    false
                }
            }
        }
        button = root
        buttonIcon = icon
        buttonParams = params
        positionButton(update = false)
        try {
            windowManager.addView(root, params)
            scheduleTuck()
        } catch (_: RuntimeException) {
            button = null
            buttonIcon = null
            buttonParams = null
            showError()
        }
    }

    private fun positionButton(update: Boolean = true) {
        if (dragging) return
        val bounds = displaySize()
        buttonParams?.apply {
            x = if (dockRight) (bounds.x - dp(48)).coerceAtLeast(0) else 0
            y = ((bounds.y - dp(48)).coerceAtLeast(0) * verticalPosition).roundToInt()
        }
        if (update) updateButtonLayout()
    }

    private fun updateButtonLayout() {
        val view = button ?: return
        if (view.isAttachedToWindow) runCatching { windowManager.updateViewLayout(view, buttonParams) }
    }

    private fun scheduleTuck() {
        handler.removeCallbacks(tuckButton)
        if (button != null) handler.postDelayed(tuckButton, 2500)
    }

    fun resetPosition() {
        handler.post {
            dockRight = true
            verticalPosition = .5f
            positionButton()
            buttonIcon?.translationX = 0f
            scheduleTuck()
            scope.launch { configManager.updateOverlayPosition(true, .5f) }
        }
    }

    private fun showPanel() {
        if (!canShow() || panel != null) return
        removeButton()
        val owner = OverlayWindowOwner(::dismissPanel)
        panelOwner = owner
        val context = ContextThemeWrapper(this, R.style.Theme_T9Launcher)
        val root =
            object : FrameLayout(context) {
                override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                    if (event.keyCode == KeyEvent.KEYCODE_BACK) {
                        if (event.action == KeyEvent.ACTION_UP && !event.isCanceled) owner.onBackPressedDispatcher.onBackPressed()
                        return true
                    }
                    return super.dispatchKeyEvent(event)
                }
            }
        root.setViewTreeLifecycleOwner(owner)
        root.setViewTreeSavedStateRegistryOwner(owner)
        root.setViewTreeViewModelStoreOwner(owner)
        val viewModel =
            ViewModelProvider(
                owner,
                object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T =
                        AppViewModel(appRepository, configManager, pinyinUtil) as T
                },
            )[AppViewModel::class.java]
        viewModel.loadAppList()
        val compose =
            ComposeView(context).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    val config by configManager.appConfig.collectAsState()
                    val dark = if (config.theme.nightModeFollowSystem) isSystemInDarkTheme() else config.theme.nightModeEnabled
                    AppTheme(
                        darkTheme = dark,
                        dynamicColor = config.theme.isUseSystemColor,
                        customColorScheme = config.theme.themeColor,
                        pureBlackDarkTheme = config.theme.pureBlackDarkTheme,
                    ) {
                        CompositionLocalProvider(
                            LocalAppConfig provides config,
                            LocalGlobalViewModel provides viewModel,
                            LocalOnBackPressedDispatcherOwner provides owner,
                        ) {
                            LauncherPanel(overlay = true, onDismiss = ::dismissPanel, onOpenSettings = ::openSettings)
                        }
                    }
                }
            }
        root.addView(compose, FrameLayout.LayoutParams(-1, -1))
        @Suppress("DEPRECATION")
        root.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        val params =
            WindowManager
                .LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR,
                    PixelFormat.TRANSLUCENT,
                ).apply {
                    gravity = Gravity.TOP or Gravity.START
                    softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) setFitInsetsTypes(0)
                }
        panel = root
        composition = compose
        try {
            windowManager.addView(root, params)
            owner.resume()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                root.doOnAttach {
                    root.findOnBackInvokedDispatcher()?.let { dispatcher ->
                        val callback = OnBackInvokedCallback { owner.onBackPressedDispatcher.onBackPressed() }
                        dispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT, callback)
                        unregisterBack = { dispatcher.unregisterOnBackInvokedCallback(callback) }
                    }
                }
            }
        } catch (_: RuntimeException) {
            removePanel()
            showError()
            if (OverlayServiceState.usesSystemShortcut) closeSystemPanel() else syncButton()
        }
    }

    private fun openSettings() {
        // Leave the pointer-input coroutine before destroying its composition, and request the
        // Activity while the interactive overlay is still attached.
        handler.post {
            if (!canShow() || panel == null) return@post
            try {
                startActivity(
                    Intent(this, MainActivity::class.java).apply {
                        putExtra("open_settings", true)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    },
                )
                openingSettings = true
                hideWindows()
                if (OverlayServiceState.usesSystemShortcut) {
                    closeSystemPanel()
                    return@post
                }
                handler.removeCallbacks(settingsLaunchTimeout)
                handler.postDelayed(settingsLaunchTimeout, 3000)
            } catch (_: RuntimeException) {
                showError()
            }
        }
    }

    internal fun onHostVisibilityChanged() {
        if (OverlayServiceState.appVisible) {
            openingSettings = false
            handler.removeCallbacks(settingsLaunchTimeout)
            hideWindows()
            if (OverlayServiceState.usesSystemShortcut && connected) closeSystemPanel()
        } else {
            syncButton()
        }
    }

    private fun dismissPanel() {
        if (OverlayServiceState.usesSystemShortcut) {
            closeSystemPanel()
            return
        }
        removePanel()
        syncButton()
    }

    /** Keep the service toggle in sync with panel visibility so the next shortcut tap opens it. */
    internal fun closeSystemPanel() {
        if (!OverlayServiceState.usesSystemShortcut || !connected || closingSystemPanel) return
        closingSystemPanel = true
        hideWindows()
        disableSelf()
    }

    private fun removePanel() {
        unregisterBack?.invoke()
        unregisterBack = null
        composition?.disposeComposition()
        panelOwner?.destroy()
        panel?.let { runCatching { windowManager.removeViewImmediate(it) } }
        composition = null
        panel = null
        panelOwner = null
    }

    private fun removeButton() {
        handler.removeCallbacks(tuckButton)
        buttonIcon?.animate()?.cancel()
        button?.let { runCatching { windowManager.removeViewImmediate(it) } }
        button = null
        buttonIcon = null
        buttonParams = null
    }

    private fun hideWindows() {
        removePanel()
        removeButton()
    }

    private fun showError() = Toast.makeText(this, R.string.overlay_error, Toast.LENGTH_SHORT).show()

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // A fresh window receives updated insets and density after rotation or display changes.
        val wasExpanded = panel != null
        hideWindows()
        if (wasExpanded && canShow()) showPanel() else syncButton()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() {
        dismissPanel()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        disconnect()
        return super.onUnbind(intent)
    }

    private fun disconnect() {
        connected = false
        openingSettings = false
        handler.removeCallbacks(settingsLaunchTimeout)
        configJob?.cancel()
        configJob = null
        if (receiverRegistered) {
            unregisterReceiver(screenReceiver)
            receiverRegistered = false
        }
        if (OverlayServiceState.service === this) OverlayServiceState.service = null
        hideWindows()
    }

    override fun onDestroy() {
        disconnect()
        handler.removeCallbacksAndMessages(null)
        scope.cancel()
        super.onDestroy()
    }
}
