package com.h3110w0r1d.t9launcher.data.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.core.net.toUri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AppInfo(
    val className: String,
    val packageName: String,
    var appName: String,
    var startCount: Int,
    var appIcon: ImageBitmap,
    var isSystemApp: Boolean,
    var searchData: ArrayList<ArrayList<String>>,
) {
    var matchRate: Float = 0f
    private val _matchRange: MutableStateFlow<Pair<Int, Int>> = MutableStateFlow(Pair(0, 0))
    val matchRange: StateFlow<Pair<Int, Int>> = _matchRange
    private val _annotatedName: MutableStateFlow<AnnotatedString> =
        MutableStateFlow(
            androidx.compose.ui.text
                .AnnotatedString(appName),
        )
    val annotatedName: StateFlow<AnnotatedString> = _annotatedName

    class SortByMatchRate : Comparator<AppInfo> {
        override fun compare(
            p0: AppInfo,
            p1: AppInfo,
        ): Int {
            if (p0.matchRate == p1.matchRate) {
                return 0
            }
            return if (p0.matchRate > p1.matchRate) -1 else 1
        }
    }

    class SortByStartCount : Comparator<AppInfo> {
        override fun compare(
            p0: AppInfo,
            p1: AppInfo,
        ): Int = p1.startCount - p0.startCount
    }

    fun setMatchRange(
        start: Int,
        end: Int,
    ) {
        if (start == 0 && end == 0) {
            return
        }
        _matchRange.value = Pair(start, end)
    }

    fun updateAnnotatedName(highlightColor: Color) {
        _annotatedName.value =
            buildAnnotatedString {
                for (i in searchData.indices) {
                    if (matchRange.value.first <= i && i < matchRange.value.second) {
                        withStyle(
                            style =
                                SpanStyle(
                                    color = highlightColor,
                                    fontWeight = FontWeight.Companion.SemiBold,
                                ),
                        ) {
                            append(searchData[i].last())
                        }
                    } else {
                        append(searchData[i].last())
                    }
                }
            }
    }

    fun start(ctx: Context): Boolean {
        val componentName = ComponentName(packageName, className)
        val intent =
            Intent(Intent.ACTION_MAIN).apply {
                component = componentName
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        try {
            ctx.startActivity(intent)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun detail(context: Context) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = "package:$packageName".toUri()
        context.startActivity(intent)
    }

    fun uninstall(context: Context) {
        val intent = Intent(Intent.ACTION_DELETE)
        intent.data = "package:$packageName".toUri()
        context.startActivity(intent)
    }

    fun copyPackageName(context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("text", packageName)
        clipboard.setPrimaryClip(clip)
    }

    fun componentId(): String = "$packageName/$className"
}
