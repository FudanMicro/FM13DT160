package com.fmsh.temperature.util;


import com.fmsh.temperature.BuildConfig;
import com.orhanobut.logger.Logger;


/**
 * Created by wuyajiang on 2018/3/7.
 */
public class LogUtil {
    /**
     * 是否开启debug
     * 注意：使用Eclipse打包的时候记得取消Build Automatically，否则一直是true
     */
    private static boolean isDebug= BuildConfig.DEBUG;
    public static String TAG = "LogUtil";


    /**
     * 错误
     */
    public static void e(String msg){
        if(isDebug){
            Logger.t(TAG).e(msg+"");
        }
    }
    /**
     * 调试
     */
    public static void d(String msg){
        if(isDebug){
            Logger.t(TAG).d( msg+"");
        }
    }
    /**
     * 信息
     */
    public static void i(String msg){
        if(isDebug){
            Logger.t(TAG).i( msg+"");
        }
    }

}