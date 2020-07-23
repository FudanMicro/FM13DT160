package com.fmsh.temperature.util;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by wuyajiang on 2018/3/7.
 */

public class ToastUtil {
    private Toast mToast;
    public static ToastUtil sToastUtil = new ToastUtil();

    public ToastUtil() {

    }

    /**
     * 短时间显示Toast
     */
    public ToastUtil shortDuration(CharSequence message) {
        if (mToast == null) {
            mToast = Toast.makeText(UIUtils.getContext(), message, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(message);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        show();
        return this;
    }

    /**
     * 长时间显示Toast
     */
    public ToastUtil longDuration(CharSequence message) {
        if (mToast == null) {
            mToast = Toast.makeText(UIUtils.getContext(), message, Toast.LENGTH_LONG);
        } else {
            mToast.setText(message);
            mToast.setDuration(Toast.LENGTH_LONG);
        }
        show();
        return this;
    }

    /**
     * 自定义显示Toast时间
     *
     * @param duration
     */
    public ToastUtil indefinite(int duration) {
        if (mToast != null) {
            mToast.setDuration(duration);
        }
        return this;
    }

    /**
     * 设置显示位置
     *
     * @param gravity
     * @return
     */
    public ToastUtil setGravity(int gravity) {
        if (mToast != null) {
            mToast.setGravity(gravity, 0, 0);
        }
        return this;
    }

    /**
     * 设置Toast字体及背景颜色
     *
     * @param messageColor
     * @param backgroundColor
     * @return
     */
    public ToastUtil setToastColor(int messageColor, int backgroundColor) {
        if (mToast != null) {
            View view = mToast.getView();
            TextView message = ((TextView) view.findViewById(android.R.id.message));
            message.setBackgroundColor(backgroundColor);
            message.setTextColor(messageColor);
        }
        return this;
    }

    /**
     * 设置Toast字体及背景
     *
     * @param messageColor
     * @param background
     * @return
     */
    public ToastUtil setToastBackground(int messageColor, int background) {
        if (mToast != null) {
            View view = mToast.getView();
            TextView message = ((TextView) view.findViewById(android.R.id.message));
            message.setBackgroundResource(background);
            view.setBackgroundColor(Color.TRANSPARENT);
            message.setTextColor(messageColor);
        }
        return this;
    }


    /**
     * 显示Toast
     *
     * @return
     */
    public ToastUtil show() {
        mToast.setGravity(Gravity.CENTER,0,0);
        mToast.show();
        return this;
    }

    /**
     * 完全自定义布局Toast
     *
     * @param view
     */
    public ToastUtil(View view) {
        mToast = new Toast(UIUtils.getContext());
        mToast.setView(view);
        mToast.setDuration(Toast.LENGTH_SHORT);
        mToast.show();
    }
}
