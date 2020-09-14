package com.fmsh.temperature;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import com.fmsh.nfcinstruct.GeneralNFC;
import com.fmsh.temperature.util.LogUtil;
import com.qmuiteam.qmui.arch.QMUISwipeBackActivityManager;


/**
 * Created by wyj on 2018/7/2.
 */
public class App extends Application {
    private static Context mContext;
    private static Handler handler;
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        QMUISwipeBackActivityManager.init(this);
        LogUtil.d("ApplicationonCreate");
    }

    public static Context getmContext() {
        return mContext;
    }

    public static android.os.Handler getHandler() {
        return handler;
    }

    public static void setHandler(Handler handler) {
        App.handler = handler;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        LogUtil.d("Application");
    }
}
