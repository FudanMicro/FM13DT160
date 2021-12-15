package com.fmsh.temperature.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fmsh.nfcinstruct.GeneralNFC;
import com.fmsh.temperature.R;
import com.fmsh.temperature.tools.BroadcastManager;
import com.fmsh.temperature.util.ActivityUtils;
import com.fmsh.temperature.util.HintDialog;
import com.fmsh.temperature.util.NFCUtils;
import com.fmsh.temperature.util.TransUtil;
import com.fmsh.temperature.util.UIUtils;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author wuyajiang
 * @date 2020/8/19
 */
public class NfcCommandActivity extends BaseActivity {
    @BindView(R.id.topbar)
    QMUITopBarLayout topbar;
    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.text)
    TextView text;
    @BindView(R.id.et_input)
    EditText etInput;
    @BindView(R.id.ll_container)
    LinearLayout llContainer;
    @BindView(R.id.btn_send)
    QMUIRoundButton btnSend;
    @BindView(R.id.tvContent)
    TextView tvContent;

    private StringBuffer mBuffer = new StringBuffer();
    private Handler mHandler = new MyHandler(this);
    private String mSendData;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_nfc_command;
    }

    @Override
    protected void initData() {
        topbar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        topbar.setTitle(R.string.nfc_instruct);

    }

    @Override
    protected void initView() {

    }

    @OnClick(R.id.btn_send)
    public void onViewClicked() {
        mSendData = etInput.getText().toString().trim();
        if(mSendData.isEmpty()){
            HintDialog.messageDialog(mContext,UIUtils.getString(R.string.empty_command));
            return;
        }

        if(mTag == null){
            HintDialog.messageDialog(mContext,UIUtils.getString(R.string.text_nfc));
            return;
        }

        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("data", mSendData);
        message.obj = mTag;
        message.setData(bundle);
        message.what = 10;
        UIUtils.setHandler(mHandler);
        mCommThread.getWorkerThreadHan().sendMessage(message);
    }


    private static class MyHandler extends Handler {

        private final WeakReference<NfcCommandActivity> mRecordFragmentWeakReference;

        public MyHandler(NfcCommandActivity nfcCommandActivity) {
            mRecordFragmentWeakReference = new WeakReference<>(nfcCommandActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            NfcCommandActivity nfcCommandActivity = mRecordFragmentWeakReference.get();
            if(nfcCommandActivity != null){
                if(msg.what == 10){
                    Bundle bundle = (Bundle) msg.obj;
                    boolean status = bundle.getBoolean("status");
                    String[] data = bundle.getStringArray("data");
                    if(status){
                        nfcCommandActivity.mBuffer.append("Send:  "+ NFCUtils.bytesToHexString(TransUtil.hexToByte(nfcCommandActivity.mSendData),':').toUpperCase());
                        nfcCommandActivity.mBuffer.append("\n");
                        nfcCommandActivity.mBuffer.append("Receive:  "+NFCUtils.bytesToHexString(TransUtil.hexToByte(data[0]),':').toUpperCase());
                        nfcCommandActivity.mBuffer.append("\n\n");
                        nfcCommandActivity.tvContent.setText(nfcCommandActivity.mBuffer.toString());

                    }else {
                        HintDialog.messageDialog(ActivityUtils.instance.getCurrentActivity(), UIUtils.getString(R.string.text_fail));
                    }

                }else {
                    HintDialog.messageDialog(ActivityUtils.instance.getCurrentActivity(), UIUtils.getString(R.string.text_fail));
                }
            }

        }
    }
}
