package com.fmsh.temperature.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.fmsh.temperature.R;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;


/**
 * @author wuyajiang
 * @date 2019/9/12
 */
public class HintDialog {

    /**
     * 显示加载框
     *
     */
    public static QMUITipDialog loadingDialog(Context context) {
        QMUITipDialog tipDialog = new QMUITipDialog.Builder(context)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(UIUtils.getString(R.string.text_loading))
                .create();
        return  tipDialog;
    }


    /**
     * 消息提示
     * @param msg
     */
    public static void messageDialog(Context context,String msg) {
        QMUITipDialog tipDialog = new QMUITipDialog.Builder(context)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_INFO)
                .setTipWord(msg)
                .create();
        tipDialog.show();
        dialogDismiss(tipDialog);
    }

    /**
     * 失败消息提示
     * @param msg
     */
    public static  void faileDialog(Context context,String msg){
        QMUITipDialog tipDialog = new QMUITipDialog.Builder(context)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                .setTipWord(msg)
                .create();
        tipDialog.show();
        dialogDismiss(tipDialog);
    }

    /**
     * 成功消息提示
     * @param msg
     */
    public static  void  successDialog(Context context,String msg){
        QMUITipDialog tipDialog = new QMUITipDialog.Builder(context)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                .setTipWord(msg)
                .create();
        tipDialog.show();
        dialogDismiss(tipDialog);
    }


    /**
     * 关闭dialog
     * @param qmuiTipDialog
     */
    private static void dialogDismiss(final QMUITipDialog qmuiTipDialog) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (qmuiTipDialog != null) {

                    qmuiTipDialog.dismiss();
                }
            }
        }, 1500);
    }

}
