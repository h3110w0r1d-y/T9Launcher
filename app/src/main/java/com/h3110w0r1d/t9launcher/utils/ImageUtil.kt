package com.h3110w0r1d.t9launcher.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class ImageUtil {
    public static Bitmap Drawable2IconBitmap(Drawable drawable) {

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
        canvas.drawColor(0xffffffff); // 白色背景
        // 将drawable 内容画到画布中
        drawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap Drawable2Bitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        drawable.setBounds(0, 0, width, height);
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        return bitmap;
    }

    public static Drawable Bitmap2Drawable(Bitmap bitmap) {
        return new BitmapDrawable(bitmap);
    }

    public static String Bitmap2Base64(Bitmap bit){
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        bit.compress(Bitmap.CompressFormat.PNG, 100, bos);//参数100表示不压缩
        byte[] bytes=bos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static Bitmap Base642Bitmap(String base64Data){
        byte[] bytes= Base64.decode(base64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public static Drawable Base642Drawable(String base64Data){
        return Bitmap2Drawable(Base642Bitmap(base64Data));
    }

    public static String Drawable2Base64(Drawable drawable){
        return Bitmap2Base64(Drawable2Bitmap(drawable));
    }

    public static Bitmap Bytes2Bitmap(byte[] b){
        return BitmapFactory.decodeByteArray(b, 0, b.length);
    }

    public static byte[] Bitmap2Bytes(Bitmap bm){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

}
