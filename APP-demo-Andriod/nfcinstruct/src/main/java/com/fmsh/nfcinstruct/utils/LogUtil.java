package com.fmsh.nfcinstruct.utils;


import android.util.Log;

import com.fmsh.nfcinstruct.BuildConfig;

/**
 * Created by wuyajiang on 2018/3/7.
 */
public class LogUtil {
    /**
     * 是否开启debug
     * 注意：使用Eclipse打包的时候记得取消Build Automatically，否则一直是true
     */
    private static boolean isDebug= BuildConfig.DEBUG;
    public static String TAG = "NFCTool";

    public static void v(Object msg){
        if(isDebug){
            Log.v(TAG, String.valueOf(msg));
        }
    }

    public static void v(String tag, Object msg){
        if(isDebug){
            Log.v(tag, String.valueOf(msg));
        }
    }




    public static void e(Object msg){
        if(isDebug){
            Log.e(TAG, String.valueOf(msg));
        }
    }
    /**
     * 错误
     */
    public static void e(String tag, Object msg){
        if(isDebug){
            Log.e(tag, String.valueOf(msg));
        }
    }

    public static void d(Object msg){
        if(isDebug){
            Log.d(TAG, String.valueOf(msg));

        }
    }
    /**
     * 调试
     */
    public static void d(String tag, Object msg){
        if(isDebug){
            Log.d(tag, String.valueOf(msg));

        }
    }

    public static void i(Object msg){
        if(isDebug){
            Log.i(TAG, String.valueOf(msg));

        }
    }
    /**
     * 信息
     */
    public static void i(String tag, Object msg){
        if(isDebug){
            Log.i(tag, String.valueOf(msg));

        }
    }

}