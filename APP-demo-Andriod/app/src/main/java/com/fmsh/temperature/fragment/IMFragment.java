package com.fmsh.temperature.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.fmsh.temperature.R;
import com.fmsh.temperature.adapter.RecyclerAdapter;
import com.fmsh.temperature.decorator.GridDividerItemDecoration;
import com.fmsh.temperature.listener.OnItemClickListener;
import com.fmsh.temperature.tools.BroadcastManager;
import com.fmsh.temperature.util.HintDialog;
import com.fmsh.temperature.util.LogUtil;
import com.fmsh.temperature.util.MyConstant;
import com.fmsh.temperature.util.NFCUtils;
import com.fmsh.temperature.util.TransUtil;
import com.fmsh.temperature.util.UIUtils;
import com.qmuiteam.qmui.widget.QMUILoadingView;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;

/**
 * @author wuyajiang
 * @date 2019/9/12
 */
public class IMFragment extends BaseFragment {
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    private UiHandler mUiHandler = new UiHandler(this);
    private QMUICommonListItemView mItemView;
    private QMUICommonListItemView mItemView1;
    private QMUICommonListItemView mItemView2;
    private QMUICommonListItemView mItemView3;
    private QMUICommonListItemView mItemView4;
    private QMUICommonListItemView mItemView5;
    private String[] mTile = {UIUtils.getString(R.string.config_text2),
            UIUtils.getString(R.string.config_text15),
            UIUtils.getString(R.string.config_text16),
            UIUtils.getString(R.string.text_led_on),
            UIUtils.getString(R.string.text_led_off),
            UIUtils.getString(R.string.config_text9),
    };
    private QMUIGroupListView mGroupListView;
    private RecyclerAdapter mRecyclerAdapter;
    public int mStatu = 0;


    @Override
    protected int setView() {
        return R.layout.fragment_im;
    }

    private QMUICommonListItemView createItem(String text) {
        QMUICommonListItemView itemView = mGroupListView.createItemView(text);
        QMUILoadingView qmuiLoadingView = new QMUILoadingView(mContext);
        itemView.addAccessoryCustomView(qmuiLoadingView);
        return itemView;
    }


    @Override
    protected void init(View view) {
        mGroupListView = new QMUIGroupListView(mContext);
        mRecyclerAdapter = new RecyclerAdapter(mContext);
        mRecyclerAdapter.setHeaderView(mGroupListView);

        int spanCount = 3;
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
        mRecyclerView.addItemDecoration(new GridDividerItemDecoration(getContext(), spanCount, 0));
        mRecyclerView.setAdapter(mRecyclerAdapter);
        List<String> strings = Arrays.asList(mTile);
        mRecyclerAdapter.setList(strings);

        mRecyclerView.setNestedScrollingEnabled(false);
        mItemView = createItem(UIUtils.getString(R.string.text_main10));
        mItemView1 = createItem(UIUtils.getString(R.string.text_main1));
        mItemView2 = createItem(UIUtils.getString(R.string.text_main7));
        mItemView3 = createItem("UID");
        mItemView4 = createItem(UIUtils.getString(R.string.text_type));
        mItemView5 = createItem(UIUtils.getString(R.string.text_state));
        QMUIGroupListView.newSection(getContext())
                .setTitle(UIUtils.getString(R.string.text_result))
                .addItemView(mItemView, null)
                .addItemView(mItemView1, null)
                .addItemView(mItemView2, null)
                .addItemView(mItemView3, null)
                .addItemView(mItemView4, null)
                .addItemView(mItemView5, null)
                .addTo(this.mGroupListView);
        BroadcastManager.getInstance(mContext).addAction("instruct", new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LogUtil.d("instruct");
                if (mContext.mTag != null && MyConstant.FLAG == 0) {
                    mItemView3.setDetailText(mContext.mStrId.toUpperCase());
                    String[] techList = mContext.mTag.getTechList();
                    UIUtils.setHandler(mUiHandler);
                    for (String str : techList) {
                        if (str.contains("NfcA")) {
                            mItemView4.setDetailText("Type-A  14443");
                            sendInstruct(true);
                            break;
                        }
                        if (str.contains("NfcV")) {
                            mItemView4.setDetailText("Type-V  15693");
                            sendInstruct(false);

                            break;
                        }
                    }

                }

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void sendInstruct(boolean isA) {
        if (isA) {
            switch (mStatu) {
                case 0:
                    mContext.loading();
                    NFCUtils.startA(mContext.mTag, 11);
                    NFCUtils.startA(mContext.mTag, 28);
                    NFCUtils.startA(mContext.mTag, 34);
                    break;
                case 1:
                    NFCUtils.startA(mContext.mTag, 2);
                    break;
                case 2:
                    NFCUtils.startA(mContext.mTag, 24);
                    break;
                case 3:
                    NFCUtils.startA(mContext.mTag, 25);
                    break;
                case 4:
                    NFCUtils.startA(mContext.mTag, 26);
                    break;
                case 5:
                    NFCUtils.startA(mContext.mTag, 27);
                    break;
                case 6:
                    NFCUtils.startA(mContext.mTag, 9);
                    break;
                case 7:
                    Message message = new Message();
                    message.obj = mContext.mTag;
                    message.what = 3;
                    mContext.mCommThread.getWorkerThreadHan().sendMessage(message);
                    break;
                case 8:
                    break;
                case 9:
                    MyConstant.FLAG = 1;
                    //停止测温
                    NFCUtils.startA(mContext.mTag, 35);
                    break;
                default:
                    break;
            }

        } else {

            switch (mStatu) {
                case 0:
                    mContext.loading();
                    NFCUtils.startV(mContext.mTag, 11);
                    NFCUtils.startV(mContext.mTag, 28);
                    NFCUtils.startV(mContext.mTag, 34);
                    break;
                case 1:
                    NFCUtils.startV(mContext.mTag, 2);
                    break;
                case 2:
                    NFCUtils.startV(mContext.mTag, 24);
                    break;
                case 3:
                    NFCUtils.startV(mContext.mTag, 25);
                    break;
                case 4:
                    NFCUtils.startV(mContext.mTag, 26);
                    break;
                case 5:
                    NFCUtils.startV(mContext.mTag, 27);
                    break;
                case 6:
                    NFCUtils.startV(mContext.mTag, 9);
                    break;
                case 7:
                    MyConstant.FLAG = 1;
                    Message message = new Message();
                    message.obj = mContext.mTag;
                    message.what = 3;
                    mContext.mCommThread.getWorkerThreadHan().sendMessage(message);
                    break;
                case 8:

                    break;
                case 9:
                    MyConstant.FLAG = 1;
                    //停止测温
                    NFCUtils.startV(mContext.mTag, 35);
                    break;
                default:
                    break;
            }
        }

    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        mRecyclerAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void itemClickListener(int position) {
                switch (position) {
                    case 0:
                        mStatu = 1;
                        break;
                    case 1:
                        mStatu = 2;
                        break;
                    case 2:
                        mStatu = 3;
                        break;
                    case 3:
                        mStatu = 4;
                        break;
                    case 4:
                        mStatu = 5;
                        break;
                    case 5:
                        mStatu = 6;
                        break;
                    default:
                        break;
                }

            }
        });

    }

    private static class UiHandler extends Handler {

        private final WeakReference<IMFragment> mImFragmentWeakReference;

        public UiHandler(IMFragment imFragment) {
            mImFragmentWeakReference = new WeakReference<>(imFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            IMFragment imFragment = mImFragmentWeakReference.get();
            if (imFragment != null) {
                String result = (String) msg.obj;
                LogUtil.d(result+ "----"+msg.what);
                switch (msg.what) {
                    case 11:
                        imFragment.mItemView1.setDetailText(result);
                        imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_success));

                        break;
                    case 28:
                        result = result.substring(result.length() - 2, result.length()) + result.substring(result.length() - 4, result.length() - 2);
                        double i = Integer.parseInt(result, 16) / 8192.00 * 2.5;
                        DecimalFormat decimalFormat = new DecimalFormat("0.00");
                        imFragment.mItemView2.setDetailText(decimalFormat.format(i) + "V");
                        imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_success));

                        break;
                    case 34:
                        imFragment.mContext.dismiss();
                        if (result.length() >= 4) {
                            String fieldStrength = result.substring(result.length() - 3, result.length() - 2);
                            int fieldStrengthNumber = Integer.parseInt(fieldStrength, 16);
                            imFragment.mItemView.setDetailText(String.valueOf(fieldStrengthNumber));
                        }
                        imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_success));

                        break;
                    case 2:
                        if ("00FFFF".equals(result)) {
                            imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_no_wake_up));
                        } else {
                            imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_wake_up));
                        }
                        break;
                    case 24:
                        if ("000000".equals(result)) {
                            imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_success));
                        } else {
                            imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_fail));

                        }
                        break;
                    case 25:
                        if ("000000".equals(result)) {
                            imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_init));
                        } else {
                            imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_fail));

                        }
                        break;
                    case 26:
                        if ("000000".equals(result)) {
                            imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_led_on));
                        } else {
                            imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_fail));

                        }
                        break;
                    case 27:
                        if ("000000".equals(result)) {
                            imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_led_off));
                        } else {
                            imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_fail));

                        }
                        break;
                    case 9:
                        byte[] bytes = TransUtil.hexStringToBytes(result);
                        int flag = bytes[2] & 0x10;
                        if (flag == 16) {
                            imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.hint_text5));
                        } else {
                            imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.hint_text6));
                        }
                        break;
                    case 35:
                        if(result.contains("0100")){
                            HintDialog.messageDialog(imFragment.mContext,UIUtils.getString(R.string.hint_text3));
                        }else {
                            HintDialog.messageDialog(imFragment.mContext,UIUtils.getString(R.string.hint_text4));
                        }
                        break;
                    default:
                        imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_error));
                        imFragment.mContext.dismiss();
                        HintDialog.faileDialog(imFragment.mContext, UIUtils.getString(R.string.text_error));
                        break;
                }
                if (msg.what != -1) {
                    imFragment.mStatu = 0;
                }

            }

        }
    }

    @Override
    public void onDestroy() {
        if (mUiHandler != null) {
            mUiHandler.removeCallbacks(null);
            mUiHandler = null;
        }
        super.onDestroy();
    }
}
