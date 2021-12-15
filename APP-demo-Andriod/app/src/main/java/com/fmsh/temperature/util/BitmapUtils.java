package com.fmsh.temperature.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.PixelCopy;
import android.view.View;

import com.fmsh.temperature.listener.OnBitmapResultListener;

/**
 * @author wuyajiang
 * @date 2021/2/3
 */
public class BitmapUtils {


    public static Bitmap scaleImage(Bitmap bm, int newWidth, int newHeight)
    {
        if (bm == null) {
            return null;
        }
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // 保持宽高比缩放，以长边为主
        float scaleRatio = Math.min(scaleHeight, scaleWidth);
        Matrix matrix = new Matrix();
        matrix.postScale(scaleRatio, scaleRatio);
        Bitmap newBm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);

        // 创建目标大小Bitmap
        Bitmap scaledImage = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(scaledImage);

        // 绘制背景颜色
        Paint paint = new Paint();
        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, canvas.getWidth(),    canvas.getHeight(), paint);

        // 确定画面位置
        float left = 0;
        float top = 0;
        if (width > height){
            top = (float)((newBm.getWidth() - newBm.getHeight()) / 2.0);
        }
        else{
            left = (float)((newBm.getHeight() - newBm.getWidth()) / 2.0);
        }
        canvas.drawBitmap( newBm, left , top, null );
        if (!bm.isRecycled()){
            bm.recycle();
        }
        return scaledImage;
    }

    /**
     * view转bitmap
     */
    public static Bitmap viewConversionBitmap(View v) {
        int w = v.getWidth();
        int h = v.getHeight();
        LogUtil.d(w+"---"+h);

        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmp);

        c.drawColor(Color.WHITE);
        /** 如果不设置canvas画布为白色，则生成透明 */
        v.layout(0, 0, w, h);
        v.draw(c);

        return bmp;
    }

    /**
     * 按比例缩放图片
     *
     * @param origin 原图
     * @param ratio  比例
     * @return 新的bitmap
     */
    public static Bitmap scaleBitmap(Bitmap origin, float ratio) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(ratio, ratio);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, true);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }

    /**
     * 根据给定的宽和高进行拉伸
     *
     * @param origin 原图
     * @param newWidth 新图的宽
     * @param newHeight 新图的高
     * @return new Bitmap
     */
    public static Bitmap scaleBitmap(Bitmap origin, int newWidth, int newHeight) {
        if (origin == null) {
            return null;
        }
        int height = origin.getHeight();
        int width = origin.getWidth();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);// 使用后乘
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, true);
        if (!origin.isRecycled()) {
            origin.recycle();
        }
        return newBM;
    }

    /**
     * View转换成Bitmap
     * @param activity activity
     * @param targetView targetView
     * @param bitmapResultListener 转换成功回调接口
     */
    public static void getBitmapFromView(Activity activity, View targetView, final OnBitmapResultListener bitmapResultListener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //准备一个bitmap对象，用来将copy出来的区域绘制到此对象中
            final Bitmap bitmap = Bitmap.createBitmap(400, 450, Bitmap.Config.RGB_565);
            //获取layout的left-top顶点位置
            final int[] location = new int[2];
            targetView.getLocationInWindow(location);
            //请求转换
            PixelCopy.request(activity.getWindow(),
                    new Rect(location[0], location[1],
                            location[0] + targetView.getWidth(), location[1] + targetView.getHeight()),
                    bitmap, new PixelCopy.OnPixelCopyFinishedListener() {
                        @Override
                        public void onPixelCopyFinished(int copyResult) {
                            //如果成功
                            if (copyResult == PixelCopy.SUCCESS) {
                                //方法回调
                                bitmapResultListener.onResult(bitmap);
                            }
                        }
                    }, new Handler(Looper.getMainLooper()));
        }
    }

}
