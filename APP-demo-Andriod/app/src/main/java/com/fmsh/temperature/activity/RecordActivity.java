package com.fmsh.temperature.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.fmsh.temperature.R;
import com.fmsh.temperature.tools.BroadcastManager;
import com.fmsh.temperature.util.ActivityUtils;
import com.fmsh.temperature.util.HintDialog;
import com.fmsh.temperature.util.TimeUitls;
import com.fmsh.temperature.util.ToastUtil;
import com.fmsh.temperature.util.UIUtils;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.SecondScale;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 * @author wuyajiang
 * @date 2019/9/18
 */
public class RecordActivity extends BaseActivity {
    @BindView(R.id.graphView)
    GraphView graphView;
    @BindView(R.id.realGraphView)
    GraphView realGraphView;
    @BindView(R.id.topbar)
    QMUITopBarLayout topbar;

    private Handler mHandler = new MyHandler(this);

    private AlertDialog.Builder mBuilder;
    private AlertDialog mAlertDialog;
    private QMUIDialog mQmuiDialog;
    private boolean mFiled;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_record;
    }


    @Override
    protected void initView() {
        mFiled = getIntent().getBooleanExtra("filed", false);
        initGraph(graphView, true);
        isSendBroadCast = true;
        topbar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        topbar.setTitle(R.string.text_data);

    }

    @Override
    protected void initData() {
        BroadcastManager.getInstance(mContext).addAction("record", new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                disNFCDialog();
                Message message = new Message();
                message.obj = mTag;
                message.what = 9;
                Bundle bundle = new Bundle();
                bundle.putBoolean("filed",mFiled);
                message.setData(bundle);
                UIUtils.setHandler(mHandler);
                mCommThread.getWorkerThreadHan().sendMessage(message);
                loading();
            }
        });

    }

    private void initGraph(GraphView graphView, boolean show) {

        Viewport mViewport = graphView.getViewport();
        mViewport.setYAxisBoundsManual(true);
        mViewport.setMinY(-40);
        mViewport.setMaxY(80);
        mViewport.setXAxisBoundsManual(true);
        if (!show) {
            mViewport.setMinX(0);
            mViewport.setMaxX(4);
        }

        if(mFiled){
            SecondScale secondScale = graphView.getSecondScale();
            secondScale.setMinY(-40);
            secondScale.setMaxY(80);
            StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graphView);
            staticLabelsFormatter.setVerticalLabels(new String[] {"", ""});
            secondScale.setLabelFormatter(staticLabelsFormatter);
        }


        GridLabelRenderer gridLabelRenderer = graphView.getGridLabelRenderer();
        gridLabelRenderer.setLabelVerticalWidth(80);
        gridLabelRenderer.setGridColor(R.color.nxp_blue);
        gridLabelRenderer.setHighlightZeroLines(false);
        gridLabelRenderer.setHorizontalLabelsColor(R.color.nxp_blue);
        gridLabelRenderer.setVerticalLabelsColor(R.color.nxp_blue);
        gridLabelRenderer.setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        if (show) {
            gridLabelRenderer.setLabelFormatter(new DateAsXAxisLabelFormatter(mContext, new SimpleDateFormat("HH:mm:ss")));
        }
        gridLabelRenderer.setNumVerticalLabels(25);
        if (show) {
            gridLabelRenderer.setNumHorizontalLabels(5);
        }
        gridLabelRenderer.setHorizontalLabelsAngle(30);
        gridLabelRenderer.setHumanRounding(true, false);
        if (show) {

            gridLabelRenderer.setHorizontalLabelsVisible(false);
        }
        gridLabelRenderer.reloadStyles();

    }


    @Override
    public void onResume() {
        super.onResume();
        nfcDialog();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isSendBroadCast = false;
        BroadcastManager.getInstance(mContext).destroy("record");

    }


    public void nfcDialog() {
        if (mQmuiDialog != null) {
            mQmuiDialog.dismiss();
            mQmuiDialog = null;
        }
        mQmuiDialog = new QMUIDialog.CustomDialogBuilder(ActivityUtils.instance.getCurrentActivity())
                .setLayout(R.layout.dialog_nfc_hint).addAction(UIUtils.getString(R.string.text_cancel), new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                }).create(R.style.DialogTheme2);

        mQmuiDialog.show();
    }

    public void disNFCDialog() {
        if (mQmuiDialog != null) {

            mQmuiDialog.dismiss();
        }
    }

    private List<Date> mDateList = new ArrayList<>();
    private List<Float> mTpList = new ArrayList<>();
    private List<Float> filedList = new ArrayList<>();

    private static class MyHandler extends Handler {

        private final WeakReference<RecordActivity> mRecordFragmentWeakReference;

        public MyHandler(RecordActivity recordActivity) {
            mRecordFragmentWeakReference = new WeakReference<>(recordActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            RecordActivity recordActivity = mRecordFragmentWeakReference.get();
            if (recordActivity != null) {
                switch (msg.what) {
                    case 9:
                        recordActivity.dismiss();
                        if (recordActivity.mAlertDialog != null) {

                            recordActivity.mAlertDialog.dismiss();
                        }
                        recordActivity.mTpList.clear();
                        recordActivity.mDateList.clear();
                        recordActivity.filedList.clear();
                        Bundle bundle = (Bundle) msg.obj;
                        boolean status = bundle.getBoolean("status");
                        String[] data = bundle.getStringArray("data");
                        if (data == null) {
                            HintDialog.messageDialog(ActivityUtils.instance.getCurrentActivity(), UIUtils.getString(R.string.hint_text13));
                            return;
                        }
                        if (status) {
                            if ("0".equals(data[0])) {
                                HintDialog.messageDialog(recordActivity.mContext, UIUtils.getString(R.string.hint_text26));
                                return;
                            }


                            int tpTime = Integer.parseInt(data[5]);
                            long startTime = Long.parseLong(data[1]);
                            int delayTime = 60 * Integer.parseInt(data[4]);

                            for (int i = 0; i < data.length - 12; i++) {
                                recordActivity.mDateList.add(new Date(startTime + delayTime + tpTime * i));
                                if(data[12+i].contains(":")){
                                    String[] split = data[12 + i].split(":");
                                    float v =  Float.parseFloat(split[0]);
                                    float filed = Float.parseFloat(split[1]);
                                    recordActivity.mTpList.add(v);
                                    recordActivity.filedList.add(filed*10);
                                }else {
                                    recordActivity.mTpList.add(Float.parseFloat(data[12 + i]));
                                }
                            }
                            if (recordActivity.mTpList.size() > 0) {
                                recordActivity.addSeriesData(data);
                            } else {
                                HintDialog.messageDialog(recordActivity.mContext, UIUtils.getString(R.string.hint_text26));
                            }

                        } else {
                            HintDialog.messageDialog(ActivityUtils.instance.getCurrentActivity(), UIUtils.getString(R.string.hint_text13));
                        }
                        break;
                    default:
                        recordActivity.dismiss();
                        HintDialog.messageDialog(ActivityUtils.instance.getCurrentActivity(), UIUtils.getString(R.string.hint_text13));
                        break;
                }

            }

        }
    }

    private void addSeriesData(String[] data) {

        DataPoint[] dataPoints = new DataPoint[mTpList.size()];
        DataPoint[] dataPointsFiled = new DataPoint[mTpList.size()];
        for (int i = 0; i < mTpList.size(); i++) {
            dataPoints[i] = new DataPoint(mDateList.get(i), mTpList.get(i));
            if(filedList.size() > 0){
                dataPointsFiled[i] = new DataPoint(mDateList.get(i), filedList.get(i));
            }
        }
        if (mTpList.size() > 1) {
            graphView.removeAllSeries();
            graphView.getSecondScale().removeAllSeries();
            LineGraphSeries lineGraphSeries = new LineGraphSeries(dataPoints);
            lineGraphSeries.setDrawDataPoints(true);
            lineGraphSeries.setDataPointsRadius(3.5f);
            lineGraphSeries.setDrawBackground(false);
            lineGraphSeries.setColor(0xFFFF0000);
            lineGraphSeries.setAnimated(true);
            graphView.addSeries(lineGraphSeries);
            graphView.getViewport().setMinX(mDateList.get(0).getTime());
            graphView.getViewport().setMaxX(mDateList.get(mDateList.size() - 1).getTime());
            if(filedList.size() > 0 && mFiled){
                LineGraphSeries lineGraphSeries1 = new LineGraphSeries(dataPointsFiled);
                lineGraphSeries1.setDrawDataPoints(true);
                lineGraphSeries1.setDataPointsRadius(3.5f);
                lineGraphSeries1.setAnimated(true);
                graphView.getSecondScale().addSeries(lineGraphSeries1);
            }
            lineGraphSeries.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {
                    double y = dataPoint.getY();
                    ToastUtil.sToastUtil.shortDuration(new BigDecimal(y).setScale(3,BigDecimal.ROUND_HALF_UP).floatValue() + "℃");
                }
            });
        }

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

        if (mTpList.size() > 0) {

            View inflate = LayoutInflater.from(mContext).inflate(R.layout.dialog_hint, null);
            Button sendEmail = inflate.findViewById(R.id.sendMail);
            QMUIGroupListView groupListView = inflate.findViewById(R.id.groupListView);
            QMUICommonListItemView itemView = createItem(groupListView, UIUtils.getString(R.string.hint_text22), hint);
            QMUICommonListItemView itemView1 = createItem(groupListView, UIUtils.getString(R.string.hint_text20), String.format("%s℃", data[7]));
            QMUICommonListItemView itemView2 = createItem(groupListView, UIUtils.getString(R.string.hint_text21), String.format("%s℃", data[6]));
            QMUICommonListItemView itemView3 = createItem(groupListView, UIUtils.getString(R.string.hint_text14), String.format("%s / %s", data[3], data[2]));
            QMUICommonListItemView itemView4 = createItem(groupListView, UIUtils.getString(R.string.hint_text17), String.format(" [%s℃,%s℃]", data[8], data[9]));
            QMUICommonListItemView itemView5 = createItem(groupListView, UIUtils.getString(R.string.hint_text18), String.format("%ss", data[5]));
            QMUICommonListItemView itemView6 = createItem(groupListView, UIUtils.getString(R.string.hint_text15), String.format("%s", TimeUitls.formatDateTime(Long.parseLong(data[1]) * 1000)));
            QMUICommonListItemView itemView7 = createItem(groupListView, UIUtils.getString(R.string.hint_text16), String.format("%s", data[10]));
            QMUICommonListItemView itemView8 = createItem(groupListView, UIUtils.getString(R.string.hint_text19), String.format("%s", data[11]));
            QMUIGroupListView.newSection(mContext)
                    .addItemView(itemView, null)
                    .addItemView(itemView1, null)
                    .addItemView(itemView2, null)
                    .addItemView(itemView3, null)
                    .addItemView(itemView4, null)
                    .addItemView(itemView5, null)
                    .addItemView(itemView6, null)
                    .addItemView(itemView7, null)
                    .addItemView(itemView8, null)
                    .addTo(groupListView);
            mItemList.clear();
            mItemList.add(itemView);
            mItemList.add(itemView1);
            mItemList.add(itemView2);
            mItemList.add(itemView3);
            mItemList.add(itemView4);
            mItemList.add(itemView5);
            mItemList.add(itemView6);
            mItemList.add(itemView7);
            mItemList.add(itemView8);
            sendEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestPermissions();

                }
            });


            showDialog(inflate);

        }

    }

    private void showDialog(View view) {
        if (mAlertDialog != null) {
            mBuilder = null;
            mAlertDialog = null;
        }
        mBuilder = new AlertDialog.Builder(mContext, R.style.dialog);
        mAlertDialog = mBuilder.create();

        mAlertDialog.show();
        Window window = mAlertDialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setContentView(view);
        window.setWindowAnimations(R.style.dialogAnimation);
        WindowManager windowManager = mContext.getWindowManager();
        Display defaultDisplay = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.width = defaultDisplay.getWidth();
        attributes.height = (int) (defaultDisplay.getHeight() * 0.55);
        window.setAttributes(attributes);

    }

    private QMUICommonListItemView createItem(QMUIGroupListView groupListView, String title, String detailText) {
        QMUICommonListItemView itemView = groupListView.createItemView(null, title,
                detailText, QMUICommonListItemView.HORIZONTAL,
                QMUICommonListItemView.ACCESSORY_TYPE_NONE, QMUIDisplayHelper.dpToPx(35));
        TextView textView = itemView.getTextView();
        textView.setTextSize(14);
        return itemView;
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= 21) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                writeExcel();
            } else {
                ActivityCompat.requestPermissions(mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                writeExcel();
            }
        }
    }

    private List<QMUICommonListItemView> mItemList = new ArrayList<>();

    private void writeExcel() {
        boolean sdcardReady = QMUIDisplayHelper.isSdcardReady();
        if (sdcardReady) {
            String path = mContext.getExternalFilesDir(null).getPath();
            File file = new File(path, "TemperatureData.xls");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            WritableWorkbook wwb;
            OutputStream os = null;
            try {
                os = new FileOutputStream(file);
                wwb = Workbook.createWorkbook(os);
                WritableSheet sheet = wwb.createSheet(UIUtils.getString(R.string.text_data), 0);
                Label label;
                for (int i = 0; i < mItemList.size(); i++) {
                    label = new Label(0, i, mItemList.get(i).getText().toString());
                    sheet.addCell(label);
                    label = new Label(1, i, mItemList.get(i).getDetailText().toString());
                    sheet.addCell(label);
                }
                for (int i = 0; i < mDateList.size(); i++) {
                    label = new Label(0, mItemList.size() + 1 + i, TimeUitls.formatDateTime(mDateList.get(i).getTime() * 1000));
                    sheet.addCell(label);
                    label = new Label(1, mItemList.size() + 1 + i, mTpList.get(i) + "℃");
                    sheet.addCell(label);
                }
                wwb.write();
                wwb.close();

                sendEmail();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void sendEmail() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        File file = new File(mContext.getExternalFilesDir(null).getPath(), "TemperatureData.xls");

        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "TemperatureData");
        intent.setType("application/octet-stream");
        Uri photoURI = FileProvider.getUriForFile(getApplicationContext(),
                "com.fmsh.DT160.fileprovider",
                file);
        intent.putExtra(Intent.EXTRA_STREAM, photoURI);
        startActivity(Intent.createChooser(intent,
                "Select email application."));

    }


}
