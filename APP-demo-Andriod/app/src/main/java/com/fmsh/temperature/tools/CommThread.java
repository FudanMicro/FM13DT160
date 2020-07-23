package com.fmsh.temperature.tools;

import android.content.Context;
import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import com.fmsh.temperature.util.LogUtil;
import com.fmsh.temperature.util.MyConstant;
import com.fmsh.temperature.util.NFCUtils;
import com.fmsh.temperature.util.UIUtils;

import java.io.IOException;

/**
 * Created by wyj on 2018/7/4.
 */
public class CommThread extends Thread {
    private Tag mTag;
    private Handler mHandler;
    private  int type;
    private WorkerThreadHandler mWorkerThreadHan;
    public static Context mContext;

    public void setContext(Context context) {
        mContext = context;
    }

    public CommThread(){


    }
    @Override
    public void run() {
        super.run();
        Looper.prepare();
        mWorkerThreadHan = new WorkerThreadHandler();
        Looper.loop();

    }

    public  Handler getWorkerThreadHan() {
        return mWorkerThreadHan;
    }

    private static class WorkerThreadHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int type = msg.what;
            if(type != -1) {
                if (msg.obj instanceof Tag) {

                    Tag tag = (Tag) msg.obj;
                    if(MyConstant.MEASURETYPE == 0){
                        String[] techList = tag.getTechList();
                        for (String tech : techList) {
                            LogUtil.d(tech);
                            if (tech.contains("NfcV")) {
                                NFCUtils.readNfcVData(tag, type);
                            }
                            if (tech.contains("NfcA")) {
                                NFCUtils.readNfcAData(tag, type);
                            }
                        }
                    }

                }
            }

        }
    }



}
