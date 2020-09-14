package com.fmsh.temperature.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.fmsh.temperature.tools.BroadcastManager;
import com.fmsh.temperature.tools.CommThread;
import com.fmsh.temperature.util.ActivityUtils;
import com.fmsh.temperature.util.HintDialog;
import com.fmsh.temperature.util.LogUtil;
import com.fmsh.temperature.util.MyConstant;
import com.fmsh.temperature.util.NFCUtils;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import butterknife.ButterKnife;

/**
 * @author wuyajiang
 * @date 2019/9/18
 */
public abstract class BaseActivity extends AppCompatActivity {
    /**
     * 获取布局id
     * @return
     */
    protected abstract int getLayoutId();
    public Activity mContext;
    private NfcAdapter mDefaultAdapter;
    public String mStrId;
    public CommThread mCommThread;
    public Tag mTag;
    private QMUITipDialog mTipDialog;
    public boolean isSendBroadCast = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        ButterKnife.bind(this);
        this.mContext = this;
        ActivityUtils.instance.addActivity(this);
        QMUIStatusBarHelper.translucent(this);
        mDefaultAdapter = NfcAdapter.getDefaultAdapter(this);

        mCommThread = new CommThread();
        mCommThread.start();
        mCommThread.setContext(this);
        mTipDialog = HintDialog.loadingDialog(this);
        initView();
        initData();
    }

    /**
     * 初始化数据
     */
    protected abstract void initData();

    /**
     * 初始化view
     */
    protected abstract void initView();

    @Override
    protected void onResume() {
        super.onResume();
        if (mDefaultAdapter != null) {
            enableReaderMode();
        }

    }
    public void loading(){
        mTipDialog.show();

    }
    public void dismiss(){
        if(mTipDialog != null){
            mTipDialog.dismiss();
        }
    }

    @TargetApi(19)
    public void enableReaderMode() {
        //19 4.4
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_NFC_V;
            Bundle option = new Bundle();
            // 延迟对卡片的检测
            option.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 1000);
            if (mDefaultAdapter != null) {
                mDefaultAdapter.enableReaderMode(this, new MyReaderCallback(), READER_FLAGS, option);
            }

        }
    }

    @TargetApi(19)
    public void startReaderModeA() {
        if (mDefaultAdapter != null) {
            mDefaultAdapter.enableReaderMode(this, new MyReaderCallback(), NfcAdapter.FLAG_READER_NFC_A, null);
        }

    }


    private class MyReaderCallback implements NfcAdapter.ReaderCallback {
        @Override
        public void onTagDiscovered(Tag tag) {
            mTag = tag;
            mStrId = NFCUtils.bytesToHexString(tag.getId(),':');
            BroadcastManager.getInstance(mContext).sendBroadcast("instruct");
            if(isSendBroadCast){
                BroadcastManager.getInstance(mContext).sendBroadcast("record");
            }


        }
    }
    @Override
    protected void onPause() {
        if (mDefaultAdapter != null) {
            mDefaultAdapter.disableReaderMode(this);
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityUtils.instance.removeActivity(this);
    }

}
