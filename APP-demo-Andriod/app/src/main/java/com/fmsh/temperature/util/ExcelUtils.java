package com.fmsh.temperature.util;

import android.os.Message;

import com.fmsh.temperature.R;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 * @author wuyajiang
 * @date 2021/1/7
 */
public class ExcelUtils {

    public static void writeExcel(List<QMUICommonListItemView> itemList, List<Date> dateList, List<Float> floatList) {
        boolean sdcardReady = QMUIDisplayHelper.isSdcardReady();
        if (sdcardReady) {
            String path = UIUtils.getContext().getExternalFilesDir(null).getPath();
            long time = System.currentTimeMillis();
            Date date = new Date(time);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String address = "TemperatureData_"+sdf.format(date)+".xls";
            File file = new File(path,address);
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
                for (int i = 0; i < itemList.size(); i++) {
                    label = new Label(0, i, itemList.get(i).getText().toString());
                    sheet.addCell(label);
                    label = new Label(1, i, itemList.get(i).getDetailText().toString());
                    sheet.addCell(label);
                }
                for (int i = 0; i < dateList.size(); i++) {
                    label = new Label(0, itemList.size() + 1 + i, TimeUitls.formatDateTime(dateList.get(i).getTime() * 1000));
                    sheet.addCell(label);
                    label = new Label(1, itemList.size() + 1 + i, floatList.get(i) + "\u00B0C");
                    sheet.addCell(label);
                }
                wwb.write();
                wwb.close();
                Message message = new Message();
                message.what = 16;
                message.obj = address;
                UIUtils.getHandler().sendMessage(message);


            } catch (Exception e) {
                e.printStackTrace();
                UIUtils.getHandler().sendEmptyMessage(17);
            }

        }
    }


}
