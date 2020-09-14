package com.fmsh.temperature.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.fmsh.temperature.R;
import com.fmsh.temperature.activity.NfcCommandActivity;
import com.fmsh.temperature.adapter.RecyclerAdapter;
import com.fmsh.temperature.decorator.GridDividerItemDecoration;
import com.fmsh.temperature.listener.OnItemClickListener;
import com.fmsh.temperature.tools.BroadcastManager;
import com.fmsh.temperature.util.ActivityUtils;
import com.fmsh.temperature.util.HintDialog;
import com.fmsh.temperature.util.LogUtil;
import com.fmsh.temperature.util.TransUtil;
import com.fmsh.temperature.util.UIUtils;
import com.qmuiteam.qmui.widget.QMUILoadingView;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogBuilder;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;

import java.lang.ref.WeakReference;
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
            UIUtils.getString(R.string.configure1),
            UIUtils.getString(R.string.configure2),
            UIUtils.getString(R.string.nfc_instruct),

    };
    private QMUIGroupListView mGroupListView;
    private RecyclerAdapter mRecyclerAdapter;
    public int mStatu = 0;

    private int mMode = 0;
    private Bundle mPasswordBundle;


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
        TextView textView =mItemView5.getDetailTextView();
                textView.setTypeface(Typeface.DEFAULT_BOLD);
                textView.setTextColor(Color.BLACK);
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
                mContext.disNFCDialog();
                if (mContext.mTag != null && mContext.FLAG == 0) {
                    mItemView3.setDetailText(mContext.mStrId.toUpperCase());
                    String[] techList = mContext.mTag.getTechList();
                    UIUtils.setHandler(mUiHandler);
                    sendInstruct();
                    for (String str : techList) {
                        if (str.contains("NfcA")) {
                            mItemView4.setDetailText("Type-A  14443");
                            break;
                        }
                        if (str.contains("NfcV")) {
                            mItemView4.setDetailText("Type-V  15693");
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

    public String mStopRTC = "00000000";
    public void sendInstruct() {
        Message message = new Message();
        message.obj = mContext.mTag;
        message.arg1 = 1;
        message.what = mStatu;
        Bundle bundle = new Bundle();
            switch (mStatu) {
                case 0:
                    mContext.loading();
                    break;
                case 8:
                    mContext.FLAG = 1;
                    bundle.putString("pwd",mStopRTC);
                    message.setData(bundle);
                    //停止测温
                    break;
                case 12:

                    bundle.putInt("mode",mMode);
                    message.setData(bundle);
                    break;
                case 13:
                case 14:
                    message.setData(mPasswordBundle);
                    break;
                default:
                    break;
        }
        mContext.mCommThread.getWorkerThreadHan().sendMessage(message);
        mStopRTC = "00000000";
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        mRecyclerAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void itemClickListener(int position) {
                switch (position) {
                    case 0:
                        // 测量基础数据
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
                    case 6:
                        mStatu = 11;
                        break;
                    case 7:
                        selectModel();
                        mStatu = 12;
                        break;
                    case 8:
                        startActivity(new Intent(mContext, NfcCommandActivity.class));
                        break;
                    default:
                        break;
                }
                if(position != 8 && position != 7){
                    mContext.nfcDialog();
                }

            }
        });



    }
    private String[] items = {UIUtils.getString(R.string.mode1),UIUtils.getString(R.string.mode2)};
    private void selectModel(){
        QMUIDialog.MenuDialogBuilder menuDialogBuilder = new QMUIDialog.MenuDialogBuilder(mContext);
        menuDialogBuilder.addItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mMode = which;
                mContext.nfcDialog();
            }
        });
        menuDialogBuilder.show();
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
            imFragment.mContext.dismiss();
            if (imFragment != null) {
                Bundle bundle = (Bundle) msg.obj;
                boolean status = false;
                String[] data = new String[10];
                if(bundle != null){
                     status = bundle.getBoolean("status");
                     data = bundle.getStringArray("data");
                }
                switch (msg.what) {

                    case 0:
                        if(status){
                            imFragment.mItemView.setDetailText(data[0]);
                            imFragment.mItemView2.setDetailText(data[2]+"V");
                            imFragment.mItemView1.setDetailText(data[1]+"℃");
                            imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_success));

                        }else {
                            imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_error));

                        }

                        break;
                    case 1:
                        if(status){
                            imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_wake_up));
                        } else {
                            imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_no_wake_up));
                        }
                        break;
                    case 2:
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                        if(status){
                            imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_success));
                            HintDialog.successDialog(imFragment.mContext,UIUtils.getString(R.string.text_success));
                        }else {
                            imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_fail));
                            HintDialog.faileDialog(imFragment.mContext,UIUtils.getString(R.string.text_error));
                        }
                        break;
                    case 3:
                        if(status){
                            imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_init));
                        }else {
                            imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_fail));
                        }
                        break;
                    case 4:
                        if(status){
                            imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_led_on));
                        }else {
                            imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_fail));
                        }
                        break;
                    case 5:
                        if(status){
                            imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_led_off));
                        }else {
                            imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_fail));
                        }
                        break;
                    case 6:
                        if(status){
                            imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.hint_text5));
                        }else {
                            imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.hint_text6));
                        }
                        break;
                    case 7:
                        if(status){
                            HintDialog.messageDialog(ActivityUtils.instance.getCurrentActivity(), UIUtils.getString(R.string.hint_text1));
                        }else {
//                            imFragment.inputPasswordDialoag(7,UIUtils.getString(R.string.check_pwd));
                            HintDialog.messageDialog(ActivityUtils.instance.getCurrentActivity(), UIUtils.getString(R.string.hint_text2));
                        }
                        break;
                    case 8:
                        if(status){
                            HintDialog.messageDialog(imFragment.mContext,UIUtils.getString(R.string.hint_text3));
                        }else {
                            imFragment.inputPasswordDialoag(8,UIUtils.getString(R.string.check_pwd));
//                            HintDialog.messageDialog(imFragment.mContext,UIUtils.getString(R.string.hint_text4));
                        }
                        break;
                    default:
                        imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_error));
                        HintDialog.faileDialog(imFragment.mContext,UIUtils.getString(R.string.text_error));
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




    private boolean isUpdate;
    public void showBottomSheet() {
        new QMUIBottomSheet.BottomListSheetBuilder(getContext())
                .addItem(UIUtils.getString(R.string.setting_pwd)+"(Stop RTC)")
                .addItem(UIUtils.getString(R.string.setting_pwd)+"(Lock)")
                .addItem(UIUtils.getString(R.string.update_pwd)+"(Stop RTC)")
                .addItem(UIUtils.getString(R.string.update_pwd)+"(Lock)")
                .addItem(UIUtils.getString(R.string.lock))
                .addItem(UIUtils.getString(R.string.unlock))
                .setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                        switch (position) {
                            case 0:
                                isUpdate = false;
                                inputPasswordDialoag(position,UIUtils.getString(R.string.setting_pwd)+"(Stop RTC)");
                                break;
                            case 1:
                                isUpdate = true;
                                inputPasswordDialoag(position,UIUtils.getString(R.string.setting_pwd)+"(Lock)");
                                break;
                            case 2:
                                showUpdatePassword(position,UIUtils.getString(R.string.update_pwd)+"(Stop RTC)");
                                break;
                            case 3:
                                showUpdatePassword(position,UIUtils.getString(R.string.update_pwd)+"(Lock)");
                                break;
                            case 4:
                                inputPasswordDialoag(position,UIUtils.getString(R.string.check_pwd));
                                lock = true;
                                break;
                            case 5:
                                lock = false;
                                inputPasswordDialoag(position,UIUtils.getString(R.string.check_pwd));
                                break;
                            default:
                                break;
                        }
                        dialog.dismiss();
                    }
                })
                .build().show();
    }


    private boolean lock;

    private void showSingleChoiceDialog(final byte[] pwd) {
        final String[] items = new String[]{"Lock Sector1", "Lock Sector2", "Lock Sector3"};
        final QMUIDialog.CheckableDialogBuilder dialogBuilder = new QMUIDialog.CheckableDialogBuilder(getActivity())
                .addItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        dialogBuilder.addAction(UIUtils.getString(R.string.text_cancel), new QMUIDialogAction.ActionListener() {
            @Override
            public void onClick(QMUIDialog dialog, int index) {
                dialog.cancel();
            }
        }).addAction(UIUtils.getString(R.string.text_confirm), new QMUIDialogAction.ActionListener() {
            @Override
            public void onClick(QMUIDialog dialog, int index) {
                int checkedIndex = dialogBuilder.getCheckedIndex();
                LogUtil.d("" + checkedIndex);
                byte[] address = new byte[2];
                address[0] = (byte) 0xb0;

                switch (checkedIndex) {
                    case 0:
                        address[1] = 0x7C;
                        break;
                    case 1:
                        address[1] = (byte) 0xBC;
                        break;
                    case 2:
                        address[1] = (byte) 0xFC;
                        break;
                    default:
                        break;
                }
                byte[] data = null;
                if (lock) {
                    data = new byte[]{0x5A, 0x5A, 0x5A, 0x5A};
                } else {
                    data = new byte[4];
                }
                dialog.dismiss();
                mPasswordBundle = new Bundle();
                mPasswordBundle.putString("oldPwd",TransUtil.byteToHex(pwd));
                mPasswordBundle.putString("newPwd",TransUtil.byteToHex(data));
                mPasswordBundle.putByteArray("address", address);
                mStatu = 14;
                mContext.nfcDialog();


            }
        });

        if (lock) {
            dialogBuilder.setTitle(UIUtils.getString(R.string.lock));
        } else {
            dialogBuilder.setTitle(UIUtils.getString(R.string.unlock));
        }

        dialogBuilder.create(R.style.DialogTheme2).show();
    }



    private void inputPasswordDialoag(final int position , String title) {
        final QMUIDialog.EditTextDialogBuilder mPwdDialoag = new QMUIDialog.EditTextDialogBuilder(mContext);
        mPwdDialoag.setTitle(title)
                .setPlaceholder(UIUtils.getString(R.string.input_pwd))
                .setInputType(InputType.TYPE_CLASS_NUMBER)
                .addAction(UIUtils.getString(R.string.text_cancel), new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();

                    }
                })
                .addAction(UIUtils.getString(R.string.text_confirm), new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        EditText editText = mPwdDialoag.getEditText();
                        String text = editText.getText().toString();
                        if (text.length() != 8) {
                            HintDialog.messageDialog(mContext, UIUtils.getString(R.string.hint_pwd));
                            return;
                        }
                        dialog.dismiss();
                        mStatu = 13;
                        mPasswordBundle = new Bundle();
                        mPasswordBundle.putString("pwd",text);
                        switch (position){
                            case 0:
                                mPasswordBundle.putByteArray("address", new byte[]{(byte) 0xb1,0x30});
                                mContext.nfcDialog();
                                break;
                            case 1:
                                mPasswordBundle.putByteArray("address", new byte[]{(byte) 0xb1,0x2C});
                                mContext.nfcDialog();
                                break;
                            case 4:
                                lock = true;
                                showSingleChoiceDialog(TransUtil.hexStringToBytes(text));
                                break;
                            case 5:
                                lock = false;
                                showSingleChoiceDialog(TransUtil.hexStringToBytes(text));
                                break;
                            case 8:
                                mStatu = 8;
                                mStopRTC = text;
                                mContext.FLAG = 0;
                                mContext.nfcDialog();
                                break;
                            default:
                                break;
                        }
                    }
                })
                .create(R.style.QMUI_Dialog);
        mPwdDialoag.show();
    }

    public void showUpdatePassword(final int position, String title){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.setting_password, null);
       final EditText etPwd= inflate.findViewById(R.id.et_pwd);
       final EditText newPwd= inflate.findViewById(R.id.new_pwd);
        builder.setView(inflate);
        builder.setTitle(title);
        builder.setNegativeButton( UIUtils.getString(R.string.text_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        builder.setPositiveButton( UIUtils.getString(R.string.text_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String oldPwd = etPwd.getText().toString().trim();
                String newPassword = newPwd.getText().toString().trim();
                if (oldPwd.length() != 8) {
                    HintDialog.messageDialog(mContext, UIUtils.getString(R.string.hint_pwd));
                    return;
                }
                if (newPassword.length() != 8) {
                    HintDialog.messageDialog(mContext, UIUtils.getString(R.string.hint_pwd));
                    return;
                }
                dialog.dismiss();
                mStatu = 14;
                mContext.nfcDialog();
                mPasswordBundle = new Bundle();
                mPasswordBundle.putString("oldPwd",oldPwd);
                mPasswordBundle.putString("newPwd",newPassword);
                if(position == 2){
                    mPasswordBundle.putByteArray("address", new byte[]{(byte) 0xb1,0x30});
                }else {
                    mPasswordBundle.putByteArray("address", new byte[]{(byte) 0xb1,0x2c});
                }

            }
        });
        builder.show();


    }
}
