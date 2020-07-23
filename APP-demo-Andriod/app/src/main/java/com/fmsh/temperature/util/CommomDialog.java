package com.fmsh.temperature.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.fmsh.temperature.R;


/**
 * Created by pc on 2017/11/21.
 */

public class CommomDialog extends AlertDialog implements View.OnClickListener {
    public TextView contentTxt;
    private Button btnCancel;
    private Button btnConfirm;

    private Context mContext;
    private String content;
    private OnCloseListener listener;
    private String positiveName;
    private String negativeName;
    private String title;
    private int layoutId;
    private int type = 0;
    private String positiveNameColor;
    private String backGroundPositiveNameColor;
    private String negativeNameColor;
    private String backGroundNegativeNameColor;
    public EditText mEtPwd;


    public CommomDialog(Context context) {
        super(context);
        this.mContext = context;
    }

    public CommomDialog(Context context, int themeResId, int layoutId, String content) {
        super(context, themeResId);
        this.mContext = context;
        this.content = content;
        this.layoutId = layoutId;
    }

    public CommomDialog(Context context, int themeResId, int layoutId, String content, OnCloseListener listener) {
        super(context, themeResId);
        this.mContext = context;
        this.content = content;
        this.layoutId = layoutId;
        this.listener = listener;

    }

    public CommomDialog setColorPositiveButton(String color) {
        this.positiveNameColor = color;
        return this;

    }

    public CommomDialog setColorNegativeButton(String color) {
        this.negativeNameColor = color;
        return this;

    }

    public void setOnCloseListener(OnCloseListener listener) {
        this.listener = listener;
    }

    protected CommomDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.mContext = context;
    }

    public CommomDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public CommomDialog setPositiveButton(String name) {
        this.positiveName = name;
        return this;
    }

    public CommomDialog setNegativeButton(String name) {
        this.negativeName = name;
        return this;
    }

    public CommomDialog setBackgroundNegativeButtonColor(String color) {
        this.backGroundNegativeNameColor = color;
        return this;
    }

    public CommomDialog setBackgroundPositiveButton(String color) {

        this.backGroundPositiveNameColor = color;
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutId);
        setCanceledOnTouchOutside(false);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
//        getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
//        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        initView();
    }


    private void initView() {

        btnCancel = findViewById(R.id.btnCancel);
        btnConfirm = findViewById(R.id.btnConfirm);
        contentTxt = findViewById(R.id.context);
//        mEtPwd = findViewById(R.id.etPwd);

        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(this);
            if (negativeName != null)
                btnConfirm.setText(negativeName);
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(this);
            if (positiveName != null)
                btnCancel.setText(positiveName);
        }
        if (contentTxt != null && content != null) {
            contentTxt.setText(content);
        }


        setCancelable(false);
        setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_SEARCH) {
                    return true;
                } else {
                    return false; //默认返回 false
                }
            }
        });
        //        if(negativeNameColor !=null){
        //            confirm.setTextColor(Color.parseColor(negativeNameColor));
        //        }
        //        if(positiveNameColor !=null){
        //            cancelTxt.setTextColor(Color.parseColor(positiveNameColor));
        //        }
        //        if(backGroundNegativeNameColor != null){
        //            confirm.setBackgroundColor(Color.parseColor(backGroundNegativeNameColor));
        //        }
        //        if(backGroundPositiveNameColor != null){
        //            cancelTxt.setBackgroundColor(Color.parseColor(backGroundPositiveNameColor));
        //        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCancel:
                if (listener != null) {
                    listener.onClick(this, false);
                }
                break;
            case R.id.btnConfirm:
                if (listener != null) {
                    listener.onClick(this, true);
                }
                break;
        }
    }

    public interface OnCloseListener {
        void onClick(Dialog dialog, boolean confirm);
    }
}