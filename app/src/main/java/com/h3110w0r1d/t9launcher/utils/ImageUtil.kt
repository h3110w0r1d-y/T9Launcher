package com.h3110w0r1d.t9launcher.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.graphics.createBitmap
import kotlin.math.min

object ImageUtil {
    fun drawable2IconBitmap(drawable: Drawable): Bitmap {
        // 获取 drawable 长宽
        val width = min(drawable.intrinsicWidth, 512)
        val height = min(drawable.intrinsicHeight, 512)
        drawable.setBounds(-1, -1, width - 1, height - 1)
        // 创建bitmap
        val bitmap = createBitmap(width - 2, height - 2, Bitmap.Config.RGB_565)
        // 创建bitmap画布
        val canvas = Canvas(bitmap)
        canvas.drawColor(-0x1) // 白色背景
        // 将drawable 内容画到画布中
        drawable.draw(canvas)
        return bitmap
    }
}
