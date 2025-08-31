package com.h3110w0r1d.t9launcher

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import com.h3110w0r1d.t9launcher.model.AppViewModel

class App : Application() {
	val appViewModel: AppViewModel by lazy { AppViewModel(this) }

    override fun onCreate() {
        super.onCreate()
        register()
    }

    private fun register() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED)
        intentFilter.addDataScheme("package")
        registerReceiver(PackageReceiver(), intentFilter)
    }
}
