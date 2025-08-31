package com.h3110w0r1d.t9launcher.vo

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable

class AppInfo(
    val packageName: String,
    var appName: String,
    var startCount: Int,
    var appIcon: Drawable,
    isSystemApp: Boolean,
    searchData: MutableList<MutableList<String>>
) {
    var isSystemApp: Boolean

    var matchRate: Float = 0f
    var searchData: MutableList<MutableList<String>>

    class SortByMatchRate : Comparator<AppInfo> {
        override fun compare(p0: AppInfo, p1: AppInfo): Int {
            if (p0.matchRate == p1.matchRate) {
                return 0
            }
            return if (p0.matchRate > p1.matchRate) -1 else 1
        }

    }

    class SortByStartCount : Comparator<AppInfo> {
        override fun compare(p0: AppInfo, p1: AppInfo): Int {
            return p1.startCount - p0.startCount
        }
    }


    init {
        this.startCount = startCount
        this.appIcon = appIcon
        this.isSystemApp = isSystemApp
        this.searchData = searchData
    }

    fun start(ctx: Context): Boolean {
        val intent = ctx.packageManager.getLaunchIntentForPackage(this.packageName)
        if (intent != null) {
            this.startCount += 1
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(intent)
            return true
        }
        return false
    }
}