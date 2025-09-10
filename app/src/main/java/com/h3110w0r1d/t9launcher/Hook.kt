package com.h3110w0r1d.t9launcher

import android.util.Log
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class Hook : IXposedHookLoadPackage {
    companion object {
        const val LOG_TAG = "T9LauncherHook"
        const val PACKAGE_NAME = "com.h3110w0r1d.t9launcher"
    }

    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        if (lpparam.packageName == PACKAGE_NAME) {
            hookSelf(lpparam)
        } else if (lpparam.packageName == "android") {
            hookAdj(lpparam)
        }
    }

    private fun hookSelf(lpparam: LoadPackageParam) {
        val clazz =
            findClass(
                "$PACKAGE_NAME.utils.XposedUtil",
                lpparam.classLoader,
            )
        findAndHookMethod(
            clazz,
            "isModuleEnabled",
            object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam?) {
                    super.beforeHookedMethod(param)
                    param?.result = true
                }
            },
        )
        findAndHookMethod(
            clazz,
            "getModuleVersion",
            object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam?) {
                    super.beforeHookedMethod(param)
                    param?.result = XposedBridge.getXposedVersion()
                }
            },
        )
    }

    private fun hookAdj(lpparam: LoadPackageParam) {
        val clazz = findClass("com.android.server.am.ProcessList", lpparam.classLoader)
        hookAllMethods(
            clazz,
            "newProcessRecordLocked",
            object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    super.afterHookedMethod(param)
                    if (param.result.toString().contains("system")) return
                    try {
                        val processName = param.result.get<String>("processName")
                        if (processName != PACKAGE_NAME) return
                        val mState = param.result.get<Any>("mState")
                        callMethod(mState, "setMaxAdj", 0)
                    } catch (e: Exception) {
                        Log.e(LOG_TAG, "Failed to hook adj", e)
                    }
                }
            },
        )
    }
}

inline fun <reified T> Any.get(field: String): T? =
    try {
        val clazz = this.javaClass
        val declaredField = clazz.getDeclaredField(field)
        declaredField.isAccessible = true
        declaredField.get(this) as T
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
