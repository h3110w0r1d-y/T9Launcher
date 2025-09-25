package com.h3110w0r1d.t9launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.h3110w0r1d.t9launcher.di.AppRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PackageReceiver : BroadcastReceiver() {
    @Inject
    lateinit var appRepository: AppRepository

    // 使用SupervisorJob确保一个协程失败不会影响其他协程
    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val packageName = intent.data?.schemeSpecificPart
        val action = intent.action
        
        // 在IO线程中异步执行数据更新，避免阻塞主线程
        receiverScope.launch {
            try {
                when (action) {
                    "android.intent.action.PACKAGE_ADDED" -> {
                        Log.d("PackageReceiver", "Package added: $packageName")
                        appRepository.addApp(packageName)
                    }
                    "android.intent.action.PACKAGE_REMOVED" -> {
                        Log.d("PackageReceiver", "Package removed: $packageName")
                        appRepository.removeApp(packageName)
                    }
                    "android.intent.action.PACKAGE_REPLACED" -> {
                        Log.d("PackageReceiver", "Package replaced: $packageName")
                        appRepository.updateApp(packageName)
                    }
                }
            } catch (e: Exception) {
                // 记录错误，但不影响应用运行
                e.printStackTrace()
            }
        }
    }
}
