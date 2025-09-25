package com.h3110w0r1d.t9launcher

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        registerPackageReceiver()
    }

    override fun onTerminate() {
        super.onTerminate()
        unregisterReceiver(PackageReceiver())
    }

    private fun registerPackageReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED)
        intentFilter.addDataScheme("package")
        registerReceiver(PackageReceiver(), intentFilter)
    }
}
