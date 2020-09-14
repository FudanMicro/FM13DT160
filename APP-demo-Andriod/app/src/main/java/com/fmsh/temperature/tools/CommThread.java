package com.fmsh.temperature.tools;

import android.content.Context;
import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import com.fmsh.nfcinstruct.GeneralNFC;
import com.fmsh.nfcinstruct.callback.OnResultCallback;
import com.fmsh.temperature.util.LogUtil;
import com.fmsh.temperature.util.MyConstant;
import com.fmsh.temperature.util.NFCUtils;
import com.fmsh.temperature.util.SpUtils;
import com.fmsh.temperature.util.TransUtil;
import com.fmsh.temperature.util.UIUtils;

import java.io.IOException;

/**
 * Created by wyj on 2018/7/4.
 */
public class CommThread extends Thread {
    private Tag mTag;
    private Handler mHandler;
    private int type;
    private WorkerThreadHandler mWorkerThreadHan;
    public static Context mContext;

    public void setContext(Context context) {
        mContext = context;
    }

    public CommThread() {


    }

    @Override
    public void run() {
        super.run();
        Looper.prepare();
        mWorkerThreadHan = new WorkerThreadHandler();
        Looper.loop();

    }



    public Handler getWorkerThreadHan() {
        return mWorkerThreadHan;
    }

    private static class WorkerThreadHandler extends Handler {
        private int mType;
        private OnResultCallback mOnResultCallback = new OnResultCallback() {
            @Override
            public void onResult(boolean status, String... response) {
                sendMessage(mType,status,response);
            }

            @Override
            public void onFailed(String errorMsg) {
                UIUtils.getHandler().sendEmptyMessage(-1);
            }
        };


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mType = msg.what;
            int arg1 = msg.arg1;
            LogUtil.d(arg1 + "");
            if (mType != -1) {
                if (msg.obj instanceof Tag) {
                    Tag tag = (Tag) msg.obj;
                    GeneralNFC.getInstance().setTag(tag);
                    switch (mType) {
                        case 0:
                            GeneralNFC.getInstance().getBasicData(mOnResultCallback);
                            break;
                        case 1:
                            GeneralNFC.getInstance().checkWakeUp(mOnResultCallback);
                            break;
                        case 2:
                            GeneralNFC.getInstance().doSleep(mOnResultCallback);
                            break;
                        case 3:
                            GeneralNFC.getInstance().initUHF(mOnResultCallback);
                            break;
                        case 4:
                            GeneralNFC.getInstance().turnOnLED(mOnResultCallback);
                            break;
                        case 5:
                            GeneralNFC.getInstance().turnOffLED(mOnResultCallback);
                            break;
                        case 6:
                            GeneralNFC.getInstance().checkStatus(mOnResultCallback);
                            break;
                        case 7:
                            GeneralNFC.getInstance().startLogging(SpUtils.getIntValue(MyConstant.delayTime,0),
                                    SpUtils.getIntValue(MyConstant.intervalTime,1),
                                    SpUtils.getIntValue(MyConstant.tpCount,10),
                                    SpUtils.getIntValue(MyConstant.tpMin,0),
                                    SpUtils.getIntValue(MyConstant.tpMax,40)
                                    ,mOnResultCallback);
                            break;
                        case 8:
                            Bundle msgData = msg.getData();
                            GeneralNFC.getInstance().stopLogging(msgData.getString("pwd"),mOnResultCallback);
                            break;
                        case 9:
                            Bundle data2 = msg.getData();
                            boolean filed = data2.getBoolean("filed");
                            GeneralNFC.getInstance().getLoggingResult(filed,mOnResultCallback);
                            break;
                        case 10:
                            Bundle data = msg.getData();
                            String dataString = data.getString("data");
                            try {
                                GeneralNFC.getInstance().sendInstruct(TransUtil.hexToByte(dataString),mOnResultCallback);
                            }catch (Exception e){
                                e.printStackTrace();
                                UIUtils.getHandler().sendEmptyMessage(-1);
                            }
                            break;
                        case 11:
                            GeneralNFC.getInstance().configPrimitiveMode(mOnResultCallback);
                            break;
                        case 12:
                            Bundle data1 = msg.getData();
                            int mode = data1.getInt("mode");
                            GeneralNFC.getInstance().configStandardMode(mode,mOnResultCallback);
                            break;
                        case 13:
                            Bundle bundle = msg.getData();
                            String pwd = bundle.getString("pwd");
                            byte[] address = bundle.getByteArray("address");
                            GeneralNFC.getInstance().settingPassword(pwd,address,mOnResultCallback);
                            break;
                        case 14:
                            Bundle bundle1 = msg.getData();
                            GeneralNFC.getInstance().updatePassword(bundle1.getString("oldPwd"),bundle1.getString("newPwd"),bundle1.getByteArray("address"),mOnResultCallback);
                            break;
                        default:
                            break;
                    }

                }
            }

        }

        private   void sendMessage(int what,boolean status, String[] data){
            Bundle bundle = new Bundle();
            bundle.putBoolean("status",status);
            bundle.putStringArray("data",data);
            Message message = new Message();
            message.what = what;
            message.obj = bundle;
            if (UIUtils.getHandler() != null){
                UIUtils.getHandler().sendMessage(message);
            }
        }
    }


}
