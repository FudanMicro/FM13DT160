package com.fmsh.temperature.fragment;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.fmsh.temperature.tools.FullScreenDialog;
import com.fmsh.temperature.tools.IncomeBean;
import com.fmsh.temperature.tools.LineChartManager;
import com.fmsh.temperature.util.ActivityUtils;
import com.fmsh.temperature.util.BitmapUtils;
import com.fmsh.temperature.util.HintDialog;
import com.fmsh.temperature.util.LogUtil;
import com.fmsh.temperature.util.MyConstant;
import com.fmsh.temperature.util.SpUtils;
import com.fmsh.temperature.util.TimeUitls;
import com.fmsh.temperature.util.TransUtil;
import com.fmsh.temperature.util.UIUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.QMUILoadingView;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogBuilder;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;

import java.io.File;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
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
            UIUtils.getString(R.string.text_measure_mode),
            UIUtils.getString(R.string.nfc_instruct),

    };
    private QMUIGroupListView mGroupListView;
    private RecyclerAdapter mRecyclerAdapter;
    public int mStatu = 0;

    private int mMode = 0;
    private Bundle mPasswordBundle;
    private boolean isVisibleToUser;


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
    public void onPause() {
        super.onPause();
        this.isVisibleToUser = false;
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
        TextView textView = mItemView5.getDetailTextView();
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
                if (mContext.mTag != null && mContext.FLAG == 0 && isVisibleToUser) {
                    mItemView3.setDetailText(mContext.mStrId.toUpperCase());
                    String[] techList = mContext.mTag.getTechList();
                    UIUtils.setHandler(mUiHandler);
                    mContext.loading();
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
        this.isVisibleToUser = true;
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
                bundle.putString("pwd", mStopRTC);
                message.setData(bundle);
                //停止测温
                break;
            case 12:

                bundle.putInt("mode", mMode);
                message.setData(bundle);
                break;
            case 13:
            case 14:
                message.setData(mPasswordBundle);
                break;
            case 15:
                bundle.putInt("mode", mWhich);
                message.setData(bundle);
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
                        showSingleChoiceDialog();
                        break;
                    case 9:
                        startActivity(new Intent(mContext, NfcCommandActivity.class));
                        break;
                    default:
                        break;
                }
                if (position != 8 && position != 7 && position != 9) {
                    mContext.nfcDialog();
                }

            }
        });
    }

    int mWhich = 0;
    private void showSingleChoiceDialog() {
        final String[] items = new String[]{UIUtils.getString(R.string.text_measure_normal_mode), UIUtils.getString(R.string.text_measure_compression_mode)};
        final QMUIDialog.CheckableDialogBuilder dialogBuilder = new QMUIDialog.CheckableDialogBuilder(getActivity())
                .addItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mWhich = which;
                        mStatu = 15;
                        mContext.nfcDialog();
                    }
                });

        dialogBuilder.create(R.style.DialogTheme2).show();
    }

    private String[] items = {UIUtils.getString(R.string.mode1), UIUtils.getString(R.string.mode2)};

    private void selectModel() {
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
                if(msg.what == -1){
                    HintDialog.faileDialog(imFragment.mContext, UIUtils.getString(R.string.text_error));
                    return;
                }
                if (msg.obj instanceof Bundle) {
                    Bundle bundle = (Bundle) msg.obj;
                    boolean status = false;
                    String[] data = new String[10];
                    if (bundle != null) {
                        status = bundle.getBoolean("status");
                        data = bundle.getStringArray("data");
                    }
                    switch (msg.what) {

                        case 0:
                            if (status) {
                                imFragment.mItemView.setDetailText(data[0]);
                                imFragment.mItemView2.setDetailText(data[2] + "V");
                                imFragment.mItemView1.setDetailText(data[1] + "℃");
                                imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_success));

                            } else {
                                imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_fail));

                            }

                            break;
                        case 1:
                            if (status) {
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
                            if (status) {
                                imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_success));
                                HintDialog.successDialog(imFragment.mContext, UIUtils.getString(R.string.text_success));
                            } else {
                                imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_fail));
                                HintDialog.faileDialog(imFragment.mContext, UIUtils.getString(R.string.text_fail));
                            }
                            break;
                        case 15:
                            if(status){
                                SpUtils.putIntValue(MyConstant.tpMode, imFragment.mWhich);
                                imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_success));
                                HintDialog.successDialog(imFragment.mContext, UIUtils.getString(R.string.text_success));;
                            }else {
                                imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_fail));
                                HintDialog.faileDialog(imFragment.mContext, UIUtils.getString(R.string.text_fail));
                            }
                            break;
                        case 3:
                            if (status) {
                                imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_init));
                            } else {
                                imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_fail));
                            }
                            break;
                        case 4:
                            if (status) {
                                imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_led_on));
                            } else {
                                imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_fail));
                            }
                            break;
                        case 5:
                            if (status) {
                                imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_led_off));
                            } else {
                                imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_fail));
                            }
                            break;
                        case 6:
                            if (status) {
                                imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.hint_text5));
                            } else {
                                imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.hint_text6));
                            }
                            break;
                        case 7:
                            if (status) {
                                HintDialog.successDialog(ActivityUtils.instance.getCurrentActivity(), UIUtils.getString(R.string.hint_text1));
                            } else {
                                //                            imFragment.inputPasswordDialoag(7,UIUtils.getString(R.string.check_pwd));
                                if(data != null && data[0].equals("0")){
                                    HintDialog.faileDialog(ActivityUtils.instance.getCurrentActivity(), UIUtils.getString(R.string.mode_error));
                                }else {
                                    HintDialog.faileDialog(ActivityUtils.instance.getCurrentActivity(), UIUtils.getString(R.string.hint_text2));
                                }
                            }
                            break;
                        case 8:
                            if (status) {
                                HintDialog.successDialog(imFragment.mContext, UIUtils.getString(R.string.hint_text3));
                            } else {
                                imFragment.inputPasswordDialoag(8, UIUtils.getString(R.string.check_pwd));
                                //                            HintDialog.messageDialog(imFragment.mContext,UIUtils.getString(R.string.hint_text4));
                            }
                            break;
                        case 9:
                            imFragment.getResult((Bundle) msg.obj);
                            break;
                        default:
                            imFragment.mItemView5.setDetailText(UIUtils.getString(R.string.text_error));
                            HintDialog.faileDialog(imFragment.mContext, UIUtils.getString(R.string.text_error));
                            break;
                    }
                } else {
                    switch (msg.what) {
                        case 15:
                            //pdf 创建成功
                        case 16:
                            // excel 创建成功
                            imFragment.sendEmail((String) msg.obj);
                            break;
                        case 17:
                            // 文件创建失败
                            HintDialog.messageDialog(ActivityUtils.instance.getCurrentActivity(), UIUtils.getString(R.string.create_file));
                            break;
                        default:
                            break;
                    }
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
                .addItem(UIUtils.getString(R.string.setting_pwd) + "(Stop RTC)")
                .addItem(UIUtils.getString(R.string.setting_pwd) + "(Lock)")
                .addItem(UIUtils.getString(R.string.update_pwd) + "(Stop RTC)")
                .addItem(UIUtils.getString(R.string.update_pwd) + "(Lock)")
                .addItem(UIUtils.getString(R.string.lock))
                .addItem(UIUtils.getString(R.string.unlock))
                .setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                        switch (position) {
                            case 0:
                                isUpdate = false;
                                inputPasswordDialoag(position, UIUtils.getString(R.string.setting_pwd) + "(Stop RTC)");
                                break;
                            case 1:
                                isUpdate = true;
                                inputPasswordDialoag(position, UIUtils.getString(R.string.setting_pwd) + "(Lock)");
                                break;
                            case 2:
                                showUpdatePassword(position, UIUtils.getString(R.string.update_pwd) + "(Stop RTC)");
                                break;
                            case 3:
                                showUpdatePassword(position, UIUtils.getString(R.string.update_pwd) + "(Lock)");
                                break;
                            case 4:
                                inputPasswordDialoag(position, UIUtils.getString(R.string.check_pwd));
                                lock = true;
                                break;
                            case 5:
                                lock = false;
                                inputPasswordDialoag(position, UIUtils.getString(R.string.check_pwd));
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
                mPasswordBundle.putString("oldPwd", TransUtil.byteToHex(pwd));
                mPasswordBundle.putString("newPwd", TransUtil.byteToHex(data));
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


    private void inputPasswordDialoag(final int position, String title) {
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
                        mPasswordBundle.putString("pwd", text);
                        switch (position) {
                            case 0:
                                mPasswordBundle.putByteArray("address", new byte[]{(byte) 0xb1, 0x30});
                                mContext.nfcDialog();
                                break;
                            case 1:
                                mPasswordBundle.putByteArray("address", new byte[]{(byte) 0xb1, 0x2C});
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

    public void showUpdatePassword(final int position, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.setting_password, null);
        final EditText etPwd = inflate.findViewById(R.id.et_pwd);
        final EditText newPwd = inflate.findViewById(R.id.new_pwd);
        builder.setView(inflate);
        builder.setTitle(title);
        builder.setNegativeButton(UIUtils.getString(R.string.text_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        builder.setPositiveButton(UIUtils.getString(R.string.text_confirm), new DialogInterface.OnClickListener() {
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
                mPasswordBundle.putString("oldPwd", oldPwd);
                mPasswordBundle.putString("newPwd", newPassword);
                if (position == 2) {
                    mPasswordBundle.putByteArray("address", new byte[]{(byte) 0xb1, 0x30});
                } else {
                    mPasswordBundle.putByteArray("address", new byte[]{(byte) 0xb1, 0x2c});
                }

            }
        });
        builder.show();


    }

    private List<IncomeBean> mResult = new ArrayList<>();

    public List<IncomeBean> getResult(Bundle bundle) {
        mResult.clear();
        if (bundle == null) {
            HintDialog.messageDialog(ActivityUtils.instance.getCurrentActivity(), UIUtils.getString(R.string.hint_text13));
            return null;
        }
        boolean status = bundle.getBoolean("status");
        String[] data = bundle.getStringArray("data");
        if (data == null) {
            HintDialog.messageDialog(ActivityUtils.instance.getCurrentActivity(), UIUtils.getString(R.string.hint_text13));
            return null;
        }
        if (status) {
            if ("0".equals(data[0])) {
                HintDialog.messageDialog(mContext, UIUtils.getString(R.string.hint_text26));
            } else{
                showRecord(bundle);
            }
        } else {
            if ("0".equals(data[0])) {
                HintDialog.messageDialog(mContext, UIUtils.getString(R.string.hint_text26));
            } else if ("2".equals(data[0])) {
                HintDialog.messageDialog(ActivityUtils.instance.getCurrentActivity(), UIUtils.getString(R.string.hint_text25));
            } else {
                HintDialog.messageDialog(ActivityUtils.instance.getCurrentActivity(), UIUtils.getString(R.string.hint_text13));
            }
        }
        return mResult;
    }

    private LinearLayout mLinearHint;
    private LineChart mLineChart;

    private void showRecord(Bundle bundle) {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.dialog_record, null);
        mLinearHint = inflate.findViewById(R.id.linearHint);
        QMUITopBarLayout topbar = inflate.findViewById(R.id.topbar);
        mLineChart = inflate.findViewById(R.id.lineChart);


        topbar.setTitle(R.string.text_data);
        String[] data = bundle.getStringArray("data");
        int tpTime = Integer.parseInt(data[5]);
        long startTime = Long.parseLong(data[1]);
        int delayTime = 60 * Integer.parseInt(data[4]);
        //计算平均时间,当正在测温中时,根据开始时间和当前时间计算每次测温间隔
        long currentTimeMillis = System.currentTimeMillis() / 1000;
        long countTime = currentTimeMillis - startTime;
        long value = countTime / (data.length - 12);
        if (!data[0].equals("1")) {
            value = tpTime;
        }
        for (int i = 0; i < data.length - 12; i++) {
            IncomeBean incomeBean = new IncomeBean();
            incomeBean.setTradeDate(startTime + delayTime + value * i);
            if (data[12 + i].contains(":")) {
                String[] split = data[12 + i].split(":");
                float v = Float.parseFloat(split[0]);
                float filed = Float.parseFloat(split[1]);
                LogUtil.d(filed + "");
                incomeBean.setValue(v);
                incomeBean.setFiled(filed * 10);
            } else {
                incomeBean.setValue(Float.parseFloat(data[12 + i]));
            }
            mResult.add(incomeBean);
        }
        addSeriesData(data, mResult, mLineChart);


        final Dialog dialog = new FullScreenDialog(mContext,
                android.R.style.Theme_DeviceDefault_Light_NoActionBar, inflate);
        dialog.setCancelable(true);


        topbar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    public boolean isShowFiled = false;

    private void initChart(List<IncomeBean> list, String[] data, LineChart mLineChart) {
        LineChartManager lineChartManager = new LineChartManager(mLineChart);
        float min = Float.parseFloat(data[8]);
        float max = Float.parseFloat(data[9]);

        lineChartManager.setLowLimitLine(min, data[8], UIUtils.getColor(R.color.blue));
        lineChartManager.setHighLimitLine(max, data[9], UIUtils.getColor(R.color.red));
        float height = Float.parseFloat(data[7]);
        float low = Float.parseFloat(data[6]);
        if (height > max) {
            max = height + 5;
        } else {
            max = max + 5;
        }
        if (low < min) {
            min = low - 5;
        } else {
            min = min - 5;
        }
        int labelCount = 2;
        int value = (int) (max - min);
        if ((value % 5) == 0) {
            labelCount = value / 5;
        } else {
            labelCount = value / 5 + 1;
        }

        List<String> xAxisStr = new ArrayList<>();
        for (int i = 0; i < 10; i++) {

            xAxisStr.add(i + "");
        }

        lineChartManager.showLineChart(list, "", UIUtils.getColor(R.color.app_color_blue), isShowFiled);
        lineChartManager.setYAxisData(max, min, labelCount);
        //        lineChartManager.setXAxisData(xAxisStr,10);
    }

    private QMUICommonListItemView createItem(QMUIGroupListView groupListView, String title, String detailText) {
        QMUICommonListItemView itemView = groupListView.createItemView(null, title,
                detailText, QMUICommonListItemView.HORIZONTAL,
                QMUICommonListItemView.ACCESSORY_TYPE_NONE, QMUIDisplayHelper.dpToPx(35));
        TextView textView = itemView.getTextView();
        textView.setTextSize(14);
        return itemView;
    }

    private List<QMUICommonListItemView> mItemList = new ArrayList<>();

    private void addSeriesData(String[] data, List<IncomeBean> result, LineChart lineChart) {
        if (result == null) {
            return;
        }

        initChart(result, data, lineChart);


        String hint = "";
        switch (data[0]) {
            case "0":
                hint = UIUtils.getString(R.string.hint_text26);
                break;
            case "1":
                hint = UIUtils.getString(R.string.hint_text23);
                break;
            case "2":
                hint = UIUtils.getString(R.string.hint_text25);
                break;
            case "3":
                hint = UIUtils.getString(R.string.hint_text24);
                break;
            default:
                break;
        }

        View inflate = LayoutInflater.from(mContext).inflate(R.layout.dialog_hint, null);
        Button sendPdf = inflate.findViewById(R.id.sendPdf);
        Button sendExcel = inflate.findViewById(R.id.sendExcel);
        QMUIGroupListView groupListView = inflate.findViewById(R.id.groupListView);
        QMUICommonListItemView itemView = createItem(groupListView, UIUtils.getString(R.string.hint_text22), hint);
        QMUICommonListItemView itemView1 = createItem(groupListView, UIUtils.getString(R.string.hint_text20), String.format("%s\u00B0C", data[7]));
        QMUICommonListItemView itemView2 = createItem(groupListView, UIUtils.getString(R.string.hint_text21), String.format("%s\u00B0C", data[6]));
        QMUICommonListItemView itemView3 = createItem(groupListView, UIUtils.getString(R.string.hint_text14), String.format("%s / %s", data[3], data[2]));
        QMUICommonListItemView itemView4 = createItem(groupListView, UIUtils.getString(R.string.hint_text17), String.format(" [%s\u00B0C,%s\u00B0C]", data[8], data[9]));
        QMUICommonListItemView itemView5 = createItem(groupListView, UIUtils.getString(R.string.hint_text18), String.format("%ss", data[5]));
        QMUICommonListItemView itemView6 = createItem(groupListView, UIUtils.getString(R.string.hint_text15), String.format("%s", TimeUitls.formatDateTime(Long.parseLong(data[1]) * 1000)));
        QMUICommonListItemView itemView7 = createItem(groupListView, UIUtils.getString(R.string.hint_text16), String.format("%s", data[10]));
        QMUICommonListItemView itemView8 = createItem(groupListView, UIUtils.getString(R.string.hint_text19), String.format("%s", data[11]));
        QMUICommonListItemView itemView9 = createItem(groupListView, UIUtils.getString(R.string.hint_text27), String.format("%sm", data[4]));
        QMUIGroupListView.newSection(mContext)
                .addItemView(itemView, null)
                .addItemView(itemView1, null)
                .addItemView(itemView2, null)
                .addItemView(itemView3, null)
                .addItemView(itemView4, null)
                .addItemView(itemView9, null)
                .addItemView(itemView5, null)
                .addItemView(itemView6, null)
                .addItemView(itemView7, null)
                .addItemView(itemView8, null)
                .addTo(groupListView);
        mItemList.clear();
        mItemList.add(createItem(groupListView, "UID", String.format("%s", mContext.mStrId).toUpperCase()));
        mItemList.add(itemView);
        mItemList.add(itemView1);
        mItemList.add(itemView2);
        mItemList.add(itemView3);
        mItemList.add(itemView4);
        mItemList.add(itemView9);
        mItemList.add(itemView5);
        mItemList.add(itemView6);
        mItemList.add(itemView7);
        mItemList.add(itemView8);
        delayTime = data[4];

        sendPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermissions(0);
            }
        });
        sendExcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermissions(1);

            }
        });

        showDialog(inflate);

    }


    private String delayTime = "0";

    private void showDialog(View view) {
        mLinearHint.removeAllViews();
        mLinearHint.addView(view);

    }

    private void writeExcel() {
        mContext.loading();
        Bundle bundle = new Bundle();
        bundle.putSerializable("item", (Serializable) mItemList);
        bundle.putParcelableArrayList("result", (ArrayList<? extends Parcelable>) mResult);

        UIUtils.setHandler(mUiHandler);
        Message message = new Message();
        message.what = 16;
        message.setData(bundle);
        mContext.mCommThread.getWorkerThreadHan().sendMessage(message);
    }

    public void sendEmail(String address) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        File file = new File(mContext.getExternalFilesDir(null).getPath(), address);

        intent.putExtra(Intent.EXTRA_SUBJECT, "TemperatureData");
        intent.setType("application/octet-stream");
        Uri photoURI = FileProvider.getUriForFile(mContext.getApplicationContext(),
                "com.fmsh.DT160.fileprovider",
                file);
        intent.putExtra(Intent.EXTRA_STREAM, photoURI);
        startActivity(Intent.createChooser(intent,
                "Select email application."));

    }

    public void writePdf() {
        final List<String> info = new ArrayList<>();
        for (int i = 0; i < mItemList.size(); i++) {
            info.add(mItemList.get(i).getDetailText().toString());
        }
        mContext.loading();
        Bitmap bitmap = BitmapUtils.viewConversionBitmap(mLineChart);
        Bitmap scaleBitmap = BitmapUtils.scaleBitmap(bitmap, (float) 0.4);

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("pdf", (ArrayList<? extends Parcelable>) mResult);
        bundle.putStringArrayList("info", (ArrayList<String>) info);
        bundle.putParcelable("img", scaleBitmap);
        UIUtils.setHandler(mUiHandler);
        Message message = new Message();
        message.what = 15;
        message.setData(bundle);
        mContext.mCommThread.getWorkerThreadHan().sendMessage(message);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                writePdf();
            }
        } else if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                writeExcel();
            }
        }
    }


    private void requestPermissions(int type) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                if (type == 0) {
                    writePdf();
                } else {
                    writeExcel();
                }
            } else {
                if (type == 0) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                } else {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);

                }

            }
        }
    }
}
