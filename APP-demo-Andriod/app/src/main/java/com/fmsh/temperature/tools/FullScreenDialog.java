package com.fmsh.temperature.tools;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.fmsh.temperature.R;


/**
 * @author wuyajiang
 * @date 2021/2/4
 */
public class FullScreenDialog extends Dialog {
    private View view;

    public FullScreenDialog(@NonNull Context context, View view) {
        super(context);
        this.view = view;
    }

    public FullScreenDialog(@NonNull Context context, int themeResId, View view) {
        super(context, themeResId);
        this.view = view;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(view);

        //按空白处不能取消
        setCanceledOnTouchOutside(false);
        Window window = getWindow();
        View decorView = window.getDecorView();
        //设置window背景，默认的背景会有Padding值，不能全屏。当然不一定要是透明，你可以设置其他背景，替换默认的背景即可。
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setWindowAnimations(R.style.dialogAnimation);
        //两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
        int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(option);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        //设置导航栏颜
        window.setNavigationBarColor(Color.TRANSPARENT);
        //内容扩展到导航栏
        window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_PANEL);


    }
}