package com.fmsh.temperature.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.LocaleList;
import android.os.Looper;
import android.os.StatFs;
import android.view.LayoutInflater;
import android.view.PixelCopy;
import android.view.View;
import android.view.ViewGroup;

import com.fmsh.temperature.App;
import com.fmsh.temperature.listener.OnBitmapResultListener;

import java.util.Locale;


/**
 * Created by wuyajiang on 2018/3/7.
 */
public class UIUtils {
    /**
     * 获取全局上下文
     *
     * @return
     */
    public static Context getContext() {
        return App.getmContext();
    }

    public static Handler getHandler(){
        return App.getHandler();
    }
    public static  void setHandler( Handler handler){
        App.setHandler(handler);
    }


    /**
     * 获取资源文件
     *
     * @return
     */
    public static Resources getResources() {
        return getContext().getResources();
    }

    /**
     * 获取资源文件字符串
     *
     * @param id
     * @return
     */
    public static String getString(int id) {
        return getResources().getString(id);
    }

    public static int getColor(int id) {
        return getResources().getColor(id);
    }

    public static Drawable getDrawable(int id) {
        return getResources().getDrawable(id);
    }

    public static String[] getStringArry(int id) {
        return getResources().getStringArray(id);
    }

    public static boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    /**
     * 将px转换成与之对应的dp
     *
     * @param px
     * @return
     */
    public static int getPx2Dp(int px) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    /**
     * 将dp转换成与之对应的px
     *
     * @param dp
     * @return
     */
    public static int getDp2Px(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    /**
     * 将px转换成sp
     *
     * @param px
     * @return
     */
    public static int getPx2Sp(int px) {
        float scaledDensity = getResources().getDisplayMetrics().scaledDensity;
        return (int) (px / scaledDensity + 0.5f);
    }

    /**
     * 将sp转换成px
     *
     * @param dp
     * @return
     */
    public static int getSp2Px(int dp) {
        float scaledDensity = getResources().getDisplayMetrics().scaledDensity;
        return (int) (dp * scaledDensity + 0.5f);
    }

    /**
     * 获取dimens下的尺寸大小
     *
     * @param id
     * @return
     */
    public static int getResDimens(int id) {
        return getResources().getDimensionPixelSize(id);
    }

    /**
     * 获取布局view
     *
     * @param viewGroup
     * @param layoutId
     * @return
     */
    public static View getLayoutView(ViewGroup viewGroup, int layoutId) {
        return LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, viewGroup, false);
    }


    /** 获取SD可用容量 */
    private static long getAvailableStorage(Context context) {
        String root = context.getExternalFilesDir(null).getPath();
        StatFs statFs = new StatFs(root);
        long blockSize = statFs.getBlockSize();
        long availableBlocks = statFs.getAvailableBlocks();
        long availableSize = blockSize * availableBlocks;
        // Formatter.formatFileSize(context, availableSize);
        return availableSize;
    }


    /**
     * View转换成Bitmap
     * @param activity activity
     * @param targetView targetView
     * @param bitmapResultListener 转换成功回调接口
     */
    public static void getBitmapFromView( Activity activity, View targetView, final OnBitmapResultListener bitmapResultListener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //准备一个bitmap对象，用来将copy出来的区域绘制到此对象中
            final Bitmap bitmap = Bitmap.createBitmap(400, 450, Bitmap.Config.ARGB_8888);
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


    /**
     * 获取当前系统语言
     * @return
     */
    public static  String getCurrentLanguage(){
        Locale locale = getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        LogUtil.d(language);
        return language;
    }


}
