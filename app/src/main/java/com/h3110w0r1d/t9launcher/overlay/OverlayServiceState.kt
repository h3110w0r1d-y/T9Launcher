package com.h3110w0r1d.t9launcher.overlay

import android.annotation.SuppressLint
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Actual connection state, independent of the user's saved preference. */
object OverlayServiceState {
    val usesSystemShortcut: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    private val visibleHosts = mutableSetOf<Any>()
    internal val appVisible: Boolean get() = visibleHosts.isNotEmpty()
    private val mutableConnected = MutableStateFlow(false)
    val connected = mutableConnected.asStateFlow()

    @SuppressLint("StaticFieldLeak")
    internal var service: LauncherAccessibilityService? = null
        set(value) {
            field = value
            mutableConnected.value = value != null
        }

    fun resetPosition() {
        service?.resetPosition()
    }

    /** Called on the main thread by Activity lifecycle callbacks. */
    fun setHostVisible(
        host: Any,
        visible: Boolean,
    ) {
        if (visible) visibleHosts.add(host) else visibleHosts.remove(host)
        service?.onHostVisibilityChanged()
    }
}
