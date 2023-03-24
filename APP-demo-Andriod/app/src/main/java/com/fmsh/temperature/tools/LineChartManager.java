package com.fmsh.temperature.tools;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.fmsh.temperature.R;
import com.fmsh.temperature.util.LogUtil;
import com.fmsh.temperature.util.TimeUitls;
import com.fmsh.temperature.util.UIUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

public class LineChartManager {

    private LineChart lineChart;
    private XAxis xAxis;                //X轴
    private YAxis leftYAxis;            //左侧Y轴
    private YAxis rightYAxis;           //右侧Y轴 自定义XY轴值
    private Legend legend;              //图例
    private LimitLine limitLine;        //限制线

    public LineChartManager(LineChart lineChart) {
        this.lineChart = lineChart;
        leftYAxis = lineChart.getAxisLeft();
        rightYAxis = lineChart.getAxisRight();
        xAxis = lineChart.getXAxis();

        initChart(lineChart);
    }

    /**
     * 初始化图表
     */
    private void initChart(LineChart lineChart) {
        /***图表设置***/
        //是否展示网格线
        lineChart.setDrawGridBackground(false);
        lineChart.setBackgroundColor(Color.WHITE);
        //是否显示边界
        lineChart.setDrawBorders(false);
        //是否可以拖动
        //        lineChart.setDragEnabled(false);
        lineChart.setDoubleTapToZoomEnabled(false);

        //是否有触摸事件
        lineChart.setTouchEnabled(false);

        //设置XY轴动画效果
        lineChart.animateY(500);
        lineChart.animateX(500);
        Description description = new Description();
        //        description.setText("需要展示的内容");
        description.setEnabled(false);
        lineChart.setDescription(description);


        /***XY轴的设置***/
        xAxis = lineChart.getXAxis();
        leftYAxis = lineChart.getAxisLeft();
        rightYAxis = lineChart.getAxisRight();

        xAxis.setDrawGridLines(false);
        rightYAxis.setDrawGridLines(false);
        leftYAxis.setDrawGridLines(true);
        //设置Y轴网格线为虚线
        leftYAxis.enableGridDashedLine(10f, 10f, 0f);
        leftYAxis.setGridLineWidth(0.8f);
        rightYAxis.setEnabled(false);

        //X轴设置显示位置在底部
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(0f);
        xAxis.setGranularity(1f);
        //保证Y轴从0开始，不然会上移一点
        leftYAxis.setAxisMinimum(0f);
        rightYAxis.setAxisMinimum(0f);

        /***折线图例 标签 设置***/
        legend = lineChart.getLegend();
        //设置显示类型，LINE CIRCLE SQUARE EMPTY 等等 多种方式，查看LegendForm 即可
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextSize(12f);
        //显示位置 左下方
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        //是否绘制在图表里面
        legend.setDrawInside(false);
        //是否显示
        legend.setEnabled(false);
    }

    /**
     * 曲线初始化设置 一个LineDataSet 代表一条曲线
     *
     * @param lineDataSet 线条
     * @param color       线条颜色
     * @param mode
     */
    private void initLineDataSet(LineDataSet lineDataSet, int color, LineDataSet.Mode mode) {
        lineDataSet.setColor(color);
        lineDataSet.setCircleColor(color);
        lineDataSet.setLineWidth(1f);
        lineDataSet.setCircleRadius(1f);

        lineDataSet.setDrawCircles(true);
        lineDataSet.setDrawValues(false);
        //设置曲线值的圆点是实心还是空心
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setValueTextSize(10f);
        //设置折线图填充
        lineDataSet.setDrawFilled(false);
        lineDataSet.setFormLineWidth(1f);
        lineDataSet.setFormSize(15.f);
        if (mode == null) {
            //设置曲线展示为圆滑曲线（如果不设置则默认折线）
            lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        } else {
            lineDataSet.setMode(mode);
        }
    }


    /**
     * 展示一条曲线 默认x轴
     *
     * @param yData    y轴的数据
     * @param lineName 曲线名称
     * @param color    曲线颜色
     */
    public void showOneLineChart(List<Float> yData, String lineName, int color) {
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < yData.size(); i++) {
            entries.add(new Entry(yData.get(i), yData.get(i)));
        }

        // 每一个LineDataSet代表一条线
        LineDataSet lineDataSet = new LineDataSet(entries, lineName);
        // CUBIC_BEZIER 圆滑曲线
        initLineDataSet(lineDataSet, color, LineDataSet.Mode.CUBIC_BEZIER);

        LineData data = new LineData();
        data.addDataSet(lineDataSet);
        lineChart.setData(data);
    }

    /**
     * 注意 集合的长度一致，在此未做处理
     *
     * @param yDataList List<Integer> 代表一条曲线的数据  yDataList.size 代表曲线的条数
     * @param lineNames 曲线名称
     * @param colors    曲线颜色
     */
    public void showMultiNormalLineChart(List<List<Float>> yDataList, List<String> lineNames, List<Integer> colors) {
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        for (int i = 0; i < yDataList.size(); i++) {
            ArrayList<Entry> entries = new ArrayList<>();

            for (int j = 0; j < yDataList.get(i).size(); j++) {
                entries.add(new Entry(yDataList.get(i).get(j), yDataList.get(i).get(j)));
            }
            LineDataSet lineDataSet = new LineDataSet(entries, lineNames.get(i));
            initLineDataSet(lineDataSet, colors.get(i), LineDataSet.Mode.CUBIC_BEZIER);
            dataSets.add(lineDataSet);
        }
        LineData lineData = new LineData(dataSets);
        lineChart.setData(lineData);
    }

    /**
     * 设置X轴的显示值
     *
     * @param min        x轴最小值
     * @param max        x轴最大值
     * @param labelCount x轴的分割数量
     */
    public void setXAxisData(float min, float max, int labelCount) {
        xAxis.setAxisMinimum(min);
        xAxis.setAxisMaximum(max);
        xAxis.setLabelCount(labelCount, false);
        lineChart.invalidate();
    }

    /**
     * 自定义的 X轴显示内容
     *
     * @param xAxisStr
     * @param labelCount x轴的分割数量
     */
    public void setXAxisData(final List<String> xAxisStr, int labelCount) {
        xAxis.setLabelCount(labelCount, false);
        xAxis.setEnabled(true);
        xAxis.setValueFormatter(new ValueFormatter() {
                                    @Override
                                    public String getFormattedValue(float value) {
                                        LogUtil.d(value+"");
                                        return super.getFormattedValue(value);
                                    }
                                }
        );
        lineChart.invalidate();
    }

    /**
     * 设置Y轴值
     *
     * @param max
     * @param min
     * @param labelCount
     */
    public void setYAxisData(float max, float min, int labelCount) {
        leftYAxis.setAxisMaximum(max);
        leftYAxis.setAxisMinimum(min);
        leftYAxis.setLabelCount(labelCount, false);

        rightYAxis.setAxisMaximum(max);
        rightYAxis.setAxisMinimum(min);
        rightYAxis.setLabelCount(labelCount, false);
        lineChart.invalidate();
    }

    /**
     * 自定义的 y轴显示内容
     *
     * @param yAxisStr
     * @param labelCount y轴的分割数量
     */
    public void setYAxisData(final List<String> yAxisStr, int labelCount) {
        xAxis.setLabelCount(labelCount, false);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return yAxisStr.get((int) value % yAxisStr.size());
            }
        });
        lineChart.invalidate();
    }


    /**
     * 设置高限制线
     *
     * @param high
     * @param name
     */
    public void setHighLimitLine(float high, String name, int color) {
        if (name == null) {
            name = "高限制线";
        }
        LimitLine highLimit = new LimitLine(high, name);
        highLimit.setLineWidth(1f);
        highLimit.setTextSize(10f);
        highLimit.setLineColor(color);
        highLimit.setTextColor(color);
        highLimit.enableDashedLine(10f, 10f, 0f);
        leftYAxis.addLimitLine(highLimit);
        lineChart.invalidate();
    }

    /**
     * 设置低限制线
     *
     * @param low
     * @param name
     */
    public void setLowLimitLine(float low, String name, int color) {
        if (name == null) {
            name = "高限制线";
        }
        LimitLine lowLimit = new LimitLine(low, name);
        LimitLine lowLimit1 = new LimitLine(10, name);
        lowLimit.setLineWidth(1f);
        lowLimit.setTextSize(10f);
        lowLimit.setLineColor(color);
        lowLimit.setTextColor(color);
        lowLimit.enableDashedLine(10f, 10f, 0f);
        leftYAxis.addLimitLine(lowLimit);
        lineChart.invalidate();
    }

    /**
     * 设置描述信息
     *
     * @param str
     */
    public void setDescription(String str) {
        Description description = new Description();
        description.setText(str);
        lineChart.setDescription(description);
        lineChart.invalidate();
    }

    /**
     * 设置线条填充背景颜色
     *
     * @param drawable
     */
    public void setChartFillDrawable(Drawable drawable) {
        if (lineChart.getData() != null && lineChart.getData().getDataSetCount() > 0) {
            LineDataSet lineDataSet = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
            //避免在 initLineDataSet()方法中 设置了 lineDataSet.setDrawFilled(false); 而无法实现效果
            lineDataSet.setDrawFilled(true);
            lineDataSet.setFillDrawable(drawable);
            lineChart.invalidate();
        }
    }


    /*****************以下方法无法通用，根据自己数据的不同进行相应的处理********************/
    /**
     * 展示曲线
     *
     * @param dataList 数据集合
     * @param name     曲线名称
     * @param color    曲线颜色
     */
    public void showLineChart(final List<IncomeBean> dataList, String name, int color,boolean showFiled) {
        List<Entry> entries = new ArrayList<>();
        List<Entry> entries1 = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            IncomeBean data = dataList.get(i);
            /**
             * 在此可查看 Entry构造方法，可发现 可传入数值 Entry(float x, float y)
             * 也可传入Drawable， Entry(float x, float y, Drawable icon) 可在XY轴交点 设置Drawable图像展示
             */
            Entry entry = new Entry(i, (float) data.getValue());
            Entry entry1 = new Entry(i, (float) data.getFiled());
            entries.add(entry);
            entries1.add(entry1);
        }

        //        /******根据需求的不同 在此在次设置X Y轴的显示内容******/
        //        xAxis.setLabelCount(6, false);
        //        //设置是否绘制刻度
        xAxis.setDrawLabels(true);
        //是否绘制X轴线
        xAxis.setDrawAxisLine(true);
        xAxis.setLabelRotationAngle(-75);
        if(dataList.size() > 10){
            final float result = (float) (dataList.size() /10.0);
            Log.d("result",result+"");
            //强制设置横坐标个数为10个
            xAxis.setLabelCount(10,true);
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    Log.d("count",value+"");
                    int index = (int) value % dataList.size();
                    Log.d("count",index+"");
                    if((dataList.size()-index) <= result){
                        index = dataList.size()-1;
                    }
                    long tradeDate = dataList.get(index).getTradeDate()*1000;
                    return TimeUitls.formatDateTime(tradeDate);
                }
            });
        }else {
            xAxis.setLabelCount(dataList.size(),false);
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    Log.d("count",value+"");
                    int index = (int) value % dataList.size();
                    long tradeDate = dataList.get(index).getTradeDate()*1000;
                    return TimeUitls.formatDateTime(tradeDate);
                }
            });
        }
        // 每一个LineDataSet代表一条线
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        LineDataSet lineDataSet = new LineDataSet(entries, name);
        //LINEAR 折线图  CUBIC_BEZIER 圆滑曲线
        initLineDataSet(lineDataSet, color, LineDataSet.Mode.LINEAR);
        dataSets.add(lineDataSet);
        if(showFiled){
            LineDataSet lineDataSet1 = new LineDataSet(entries1, UIUtils.getString(R.string.text_main10));
            initLineDataSet(lineDataSet1, UIUtils.getColor(R.color.app_color_theme_3), LineDataSet.Mode.LINEAR);
            dataSets.add(lineDataSet1);
        }
        LineData lineData = new LineData(dataSets);
        lineChart.setData(lineData);

        //线条自定义内容 放在这里
        //        lineDataSet.setValueFormatter(new ValueFormatter() {
        //            @Override
        //            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        //                DecimalFormat df = new DecimalFormat(".00");
        //                return df.format(value * 100) + "%";
        //            }
        //        });


    }

    //    /**
    //     * 添加曲线
    //     */
    //    public void addLine(List<CompositeIndexBean> dataList, String name, int color) {
    //        List<Entry> entries = new ArrayList<>();
    //        for (int i = 0; i < dataList.size(); i++) {
    //            CompositeIndexBean data = dataList.get(i);
    //            Entry entry = new Entry(i, (float) data.getRate());
    //            entries.add(entry);
    //        }
    //        // 每一个LineDataSet代表一条线
    //        LineDataSet lineDataSet = new LineDataSet(entries, name);
    //        initLineDataSet(lineDataSet, color, LineDataSet.Mode.LINEAR);
    //        lineChart.getLineData().addDataSet(lineDataSet);
    //        lineChart.invalidate();
    //    }

    //    /**
    //     * 重置某条曲线 position 从 0 开始
    //     */
    //    public void resetLine(int position, List<CompositeIndexBean> dataList, String name, int color) {
    //        LineData lineData = lineChart.getData();
    //        List<ILineDataSet> list = lineData.getDataSets();
    //        if (list.size() <= position) {
    //            return;
    //        }
    //
    //        List<Entry> entries = new ArrayList<>();
    //        for (int i = 0; i < dataList.size(); i++) {
    //            CompositeIndexBean data = dataList.get(i);
    //            Entry entry = new Entry(i, (float) data.getRate());
    //            entries.add(entry);
    //        }
    //
    //        LineDataSet lineDataSet = new LineDataSet(entries, name);
    //        initLineDataSet(lineDataSet, color, LineDataSet.Mode.LINEAR);
    //
    //        lineData.getDataSets().set(position, lineDataSet);
    //        lineChart.invalidate();
    //    }

    //    /**
    //     * 设置 可以显示X Y 轴自定义值的 MarkerView
    //     */
    //    public void setMarkerView(Context context) {
    //        LineChartMarkView mv = new LineChartMarkView(context, xAxis.getValueFormatter());
    //        mv.setChartView(lineChart);
    //        lineChart.setMarker(mv);
    //        lineChart.invalidate();
    //    }
}
