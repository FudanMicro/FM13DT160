package com.fmsh.temperature.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Message;

import com.fmsh.temperature.R;
import com.fmsh.temperature.tools.IncomeBean;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableImage;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 * @author wuyajiang
 * @date 2021/1/7
 */
public class ExcelUtils {

    public static void writeExcel(List<QMUICommonListItemView> itemList, List<IncomeBean> dateList) {
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
                int row = 2;
                os = new FileOutputStream(file);
                wwb = Workbook.createWorkbook(os);
                WritableSheet sheet = wwb.createSheet(UIUtils.getString(R.string.text_data), 0);
                sheet.addImage(new WritableImage(0,0,2,row+1,loadImageFromAsserts()));
                sheet.mergeCells(0,0,1,2);
                WritableFont font2 = new WritableFont(WritableFont.ARIAL,10,WritableFont.NO_BOLD,false, UnderlineStyle.NO_UNDERLINE,Colour.WHITE);
                WritableCellFormat writableCellFormat = new WritableCellFormat(font2);
                writableCellFormat.setBackground(Colour.DARK_BLUE2);
                writableCellFormat.setAlignment(Alignment.CENTRE);
                sheet.addCell(new Label(0,row+1,UIUtils.getString(R.string.report),writableCellFormat));
                sheet.addCell(new Label(0,row+14,UIUtils.getString(R.string.record),writableCellFormat));
                sheet.mergeCells(0,row+1,1,row+1);
                sheet.mergeCells(0,row+14,1,row+14);

                Label label;
                for (int i = 0; i < itemList.size(); i++) {
                    label = new Label(0, i+2+row, itemList.get(i).getText().toString());
                    sheet.addCell(label);
                    label = new Label(1, i+2+row, itemList.get(i).getDetailText().toString());
                    sheet.addCell(label);
                }
                sheet.addCell(new Label(0,15+row,UIUtils.getString(R.string.m_time)));
                sheet.addCell(new Label(1,15+row,UIUtils.getString(R.string.text_main1)+"(\u00B0C)"));
                for (int i = 0; i < dateList.size(); i++) {
                    IncomeBean incomeBean = dateList.get(i);
                    label = new Label(0,  i + 16+row, TimeUitls.formatDateTime(incomeBean.getTradeDate() * 1000));
                    sheet.addCell(label);
//                    label = new Label(4,  i + 3, incomeBean.getValue()+"");
                    Number number = new Number(1, i + 16+row, incomeBean.getValue());
                    sheet.addCell(number);
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

    /**  从assets 文件夹中读取图片  */
    public   static byte[] loadImageFromAsserts() {
        try  {
            InputStream is = UIUtils.getContext().getResources().getAssets().open("fmsh_1.png");

            ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
            int ch;
            while ((ch = is.read()) != -1) {
                bytestream.write(ch);
            }
            byte imgdata[] = bytestream.toByteArray();
            bytestream.close();
            return imgdata;

        }  catch  (IOException e) {
            if  (e !=  null ) {
                e.printStackTrace();
            }
        }
        return   null ;
    }


}
