package com.h3110w0r1d.t9launcher.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

public class Image {
    public static Bitmap DrawableToBitmap(Drawable drawable) {

        // 获取 drawable 长宽
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();

        drawable.setBounds(-1, -1, width, height);
        // 获取drawable的颜色格式
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        // 创建bitmap
        Bitmap bitmap = Bitmap.createBitmap(width-2, height-2, config);
        // 创建bitmap画布
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(0xffffffff);
        // 将drawable 内容画到画布中
        drawable.draw(canvas);
        return bitmap;
    }
}
