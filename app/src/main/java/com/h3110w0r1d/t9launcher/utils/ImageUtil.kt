package com.h3110w0r1d.t9launcher.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import androidx.core.graphics.createBitmap

object ImageUtil {
    fun drawable2IconBitmap(drawable: Drawable): Bitmap {
        // 获取 drawable 长宽
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight

        drawable.setBounds(-1, -1, width, height)
        // 获取drawable的颜色格式
        val config =
            if (drawable.opacity != PixelFormat.OPAQUE) {
                Bitmap.Config.ARGB_8888
            } else {
                Bitmap.Config.RGB_565
            }
        // 创建bitmap
        val bitmap = createBitmap(width - 2, height - 2, config)
        // 创建bitmap画布
        val canvas = Canvas(bitmap)
        canvas.drawColor(-0x1) // 白色背景
        // 将drawable 内容画到画布中
        drawable.draw(canvas)
        return bitmap
    }
}
