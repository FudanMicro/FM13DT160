package com.fmsh.temperature.activity;

import android.nfc.Tag;
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
import com.fmsh.temperature.tools.CommThread;
import com.fmsh.temperature.util.MyConstant;
import com.fmsh.temperature.util.UIUtils;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;

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


    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initView() {
        topbar.setTitle(UIUtils.getString(R.string.text_lab1));
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
                MyConstant.FLAG = 0;
                mImFragment.mStatu = 0;
                topbar.setTitle(UIUtils.getString(R.string.text_lab1));
                switchFragment(mSettingFragment, mImFragment);
                cbLab1.setChecked(true);
                cbLab2.setChecked(false);

                break;
            case R.id.cb_lab2:
                if (mSettingFragment == null) {

                    mSettingFragment = new SettingFragment();
                }
                MyConstant.FLAG = 1;
                topbar.setTitle(UIUtils.getString(R.string.text_lab2));
                switchFragment(mImFragment, mSettingFragment);
                cbLab1.setChecked(false);
                cbLab2.setChecked(true);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BroadcastManager.getInstance(mContext).destroy("instruct");
    }
}
