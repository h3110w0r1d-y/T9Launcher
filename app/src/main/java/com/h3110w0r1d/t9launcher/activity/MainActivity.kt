package com.h3110w0r1d.t9launcher.activity

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.net.toUri
import com.h3110w0r1d.t9launcher.model.AppViewModel
import com.h3110w0r1d.t9launcher.ui.AppNavigation
import com.h3110w0r1d.t9launcher.ui.theme.T9LauncherTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        initStatusBar()
        ignoreBatteryOptimization()

        setContent {
            T9LauncherTheme {
                AppNavigation(appViewModel)
            }
        }
    }

    private fun initStatusBar() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
    }

    @SuppressLint("BatteryLife")
    fun ignoreBatteryOptimization() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        val hasIgnored = powerManager.isIgnoringBatteryOptimizations(this.packageName)
        //  判断当前APP是否有加入电池优化的白名单，如果没有，弹出加入电池优化的白名单的设置对话框。
        if (!hasIgnored) {
            try {
                // 先调用系统显示 电池优化权限
                val intent =
                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = ("package:" + this.packageName).toUri()
                startActivity(intent)
            } catch (_: Exception) {
                // 如果失败了则引导用户到电池优化界面
                try {
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.addCategory(Intent.CATEGORY_LAUNCHER)
                    val cn =
                        ComponentName.unflattenFromString("com.android.settings/.Settings\$HighPowerApplicationsActivity")
                    intent.component = cn
                    startActivity(intent)
                } catch (_: Exception) {
                }
            }
        }
    }
}
