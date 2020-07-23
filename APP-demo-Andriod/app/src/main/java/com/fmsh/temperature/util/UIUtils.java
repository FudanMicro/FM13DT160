package com.fmsh.temperature.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fmsh.temperature.App;


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



}
