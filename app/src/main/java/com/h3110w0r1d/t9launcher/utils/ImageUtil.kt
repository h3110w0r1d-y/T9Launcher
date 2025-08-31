package com.h3110w0r1d.t9launcher.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Base64
import java.io.ByteArrayOutputStream
import androidx.core.graphics.createBitmap

object ImageUtil {
    fun drawable2IconBitmap(drawable: Drawable): Bitmap {
        // 获取 drawable 长宽

        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight

        drawable.setBounds(-1, -1, width, height)
        // 获取drawable的颜色格式
        val config = if (drawable.getOpacity() != PixelFormat.OPAQUE)
            Bitmap.Config.ARGB_8888
        else
            Bitmap.Config.RGB_565
        // 创建bitmap
        val bitmap = createBitmap(width - 2, height - 2, config)
        // 创建bitmap画布
        val canvas = Canvas(bitmap)
        canvas.drawColor(-0x1) // 白色背景
        // 将drawable 内容画到画布中
        drawable.draw(canvas)
        return bitmap
    }

//    fun drawable2Bitmap(drawable: Drawable): Bitmap {
//        val width = drawable.getIntrinsicWidth()
//        val height = drawable.getIntrinsicHeight()
//        drawable.setBounds(0, 0, width, height)
//        val config = if (drawable.getOpacity() != PixelFormat.OPAQUE)
//            Bitmap.Config.ARGB_8888
//        else
//            Bitmap.Config.RGB_565
//        val bitmap = createBitmap(width, height, config)
//        val canvas = Canvas(bitmap)
//        drawable.draw(canvas)
//        return bitmap
//    }
//
//    fun bitmap2Drawable(bitmap: Bitmap?): Drawable {
//        return BitmapDrawable(bitmap)
//    }
//
//    fun bitmap2Base64(bit: Bitmap): String {
//        val bos = ByteArrayOutputStream()
//        bit.compress(Bitmap.CompressFormat.PNG, 100, bos) //参数100表示不压缩
//        val bytes = bos.toByteArray()
//        return Base64.encodeToString(bytes, Base64.DEFAULT)
//    }
//
//    fun base642Bitmap(base64Data: String): Bitmap? {
//        val bytes = Base64.decode(base64Data, Base64.DEFAULT)
//        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
//    }
//
//    fun Base642Drawable(base64Data: String): Drawable {
//        return bitmap2Drawable(base642Bitmap(base64Data))
//    }
//
//    fun Drawable2Base64(drawable: Drawable): String? {
//        return bitmap2Base64(drawable2Bitmap(drawable))
//    }
//
//    fun Bytes2Bitmap(b: ByteArray): Bitmap {
//        return BitmapFactory.decodeByteArray(b, 0, b.size)
//    }
//
//    fun Bitmap2Bytes(bm: Bitmap): ByteArray {
//        val baos = ByteArrayOutputStream()
//        bm.compress(Bitmap.CompressFormat.PNG, 100, baos)
//        return baos.toByteArray()
//    }
}
