package com.fmsh.temperature.activity;

import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;

import com.fmsh.temperature.R;
import com.fmsh.temperature.fragment.BaseFragment;
import com.fmsh.temperature.fragment.IMFragment;
import com.fmsh.temperature.fragment.SettingFragment;
import com.fmsh.temperature.tools.BroadcastManager;
import com.fmsh.temperature.util.ActivityUtils;
import com.fmsh.temperature.util.LogUtil;
import com.fmsh.temperature.util.UIUtils;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    @BindView(R.id.topbar)
    QMUITopBarLayout topbar;
    @BindView(R.id.fl_fragment)
    FrameLayout flFragment;
    @BindView(R.id.cb_lab1)
    CheckBox cbLab1;
    @BindView(R.id.cb_lab2)
    CheckBox cbLab2;
    public IMFragment mImFragment;
    private SettingFragment mSettingFragment;
    /**
     * 0 为即使测温页面,1 为RTC 测温页面
     */
    public int FLAG = 0 ;
    private QMUIDialog mQmuiDialog;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initView() {
        String version = "1.0.0";
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = packageInfo.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();


        }

        topbar.setTitle(UIUtils.getString(R.string.text_lab1));
        topbar.addLeftTextButton(UIUtils.getString(R.string.text_version)+version,0x124);

        topbar.addRightImageButton(R.mipmap.more,0x111).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mImFragment != null) {
                    mImFragment.showBottomSheet();
                }
            }
        });

        if (mImFragment == null) {
            mImFragment = new IMFragment();
        }
        if (!mImFragment.isAdded()) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.fl_fragment, mImFragment).commit();
        }

    }

    public void switchFragment(Fragment fromFragment, BaseFragment nextFragment) {
        if (nextFragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            //判断nextFragment是否添加
            if (!nextFragment.isAdded()) {
                //隐藏当前Fragment
                if (fromFragment != null) {
                    transaction.hide(fromFragment);
                }
                transaction.add(R.id.fl_fragment, nextFragment).commit();
            } else {
                //隐藏当前Fragment
                if (fromFragment != null) {
                    transaction.hide(fromFragment);
                }
                transaction.show(nextFragment).commit();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
        //不保留activity状态
    }

    @Override
    protected void onStart() {
        super.onStart();
    }



    @OnClick({R.id.cb_lab1, R.id.cb_lab2})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.cb_lab1:
                if (mImFragment == null) {
                    mImFragment = new IMFragment();
                }
                FLAG = 0;
                mImFragment.mStatu = 0;
                topbar.setTitle(UIUtils.getString(R.string.text_lab1));
                switchFragment(mSettingFragment, mImFragment);
                cbLab1.setChecked(true);
                cbLab2.setChecked(false);
                topbar.addRightImageButton(R.mipmap.more,0x111).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mImFragment != null) {
                            mImFragment.showBottomSheet();
                        }
                    }
                });
                break;
            case R.id.cb_lab2:
                if (mSettingFragment == null) {

                    mSettingFragment = new SettingFragment();
                }
                FLAG = 1;
                topbar.setTitle(UIUtils.getString(R.string.text_lab2));
                switchFragment(mImFragment, mSettingFragment);
                cbLab1.setChecked(false);
                cbLab2.setChecked(true);
                topbar.removeAllRightViews();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.d("destory");
        BroadcastManager.getInstance(mContext).destroy("instruct");
    }


    public void nfcDialog(){
        if(mQmuiDialog != null){
            mQmuiDialog.dismiss();
            mQmuiDialog =null;
        }
        mQmuiDialog = new QMUIDialog.CustomDialogBuilder(ActivityUtils.instance.getCurrentActivity())
                .setLayout(R.layout.dialog_nfc_hint).addAction(UIUtils.getString(R.string.text_cancel), new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                }).create(R.style.DialogTheme2);

        mQmuiDialog.show();
        mQmuiDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(mImFragment.mStatu == 7 || mImFragment.mStatu == 8 || mImFragment.mStatu == 9){
                    FLAG = 1;
                    //防止在定时测温界面响应即使测温结果
                }
                mImFragment.mStatu = 0;

            }
        });
    }
    public void disNFCDialog(){
        if(mQmuiDialog != null){

            mQmuiDialog.dismiss();
        }
    }
}
