package com.fmsh.temperature.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Environment;
import android.os.Message;

import com.fmsh.temperature.R;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.FontSelector;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class PdfUtils {
	
	public static final String ADDRESS = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"DT160";

	public static class Config {
		public static final boolean DEVELOPER_MODE = false;
	}

	public static String[] getString() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_UNMOUNTED)) {
			return null;
		}
		List<File> fileList = new ArrayList<File>();
		String[] string = null;
		String path = ADDRESS;
		File file = new File(path);
		if (file.exists() && file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].getAbsolutePath().endsWith(".pdf")) {
					fileList.add(files[i]);
				}
			}
			Collections.sort(fileList, new FileComparator());
			string = new String[fileList.size()];
			for (int i = 0; i < string.length; i++) {
				string[i] = fileList.get(i).getAbsolutePath().toString();
			}
		}
		return string;
	}

	/**
	 * 将文件按时间升序排列
	 */
	static class FileComparator implements Comparator<File> {

		@Override
		public int compare(File lhs, File rhs) {
			if (lhs.lastModified() < rhs.lastModified()) {
				return 1;// 最后修改的照片在前
			} else {
				return -1;
			}
		}
	}

	/**
	 * 创建pdf文件,按照list存储文件顺序排列
	 * @param elementList 文件数据类型存储
	 */
	public static void createPdfFile(List<Element> elementList){
		if(elementList == null || elementList.isEmpty()){
			return;
		}

		String path = UIUtils.getContext().getExternalFilesDir(null).getPath();
		long time = System.currentTimeMillis();
		Date date = new Date(time);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String address = "TemperatureData_"+sdf.format(date)+".pdf";
		File file = new File(path,address );
//		if (!file.exists()) {
//			try {
//				file.createNewFile();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
		// 创建一个document对象
		Document doc = new Document();
		FileOutputStream fos;
		try {
			// pdf_address为Pdf文件保存到sd卡的路径
			fos = new FileOutputStream(file);
			PdfWriter.getInstance(doc, fos);
			doc.open();
			doc.setPageCount(1);

			for (int i = 0; i < elementList.size(); i++) {
				if(elementList.get(i) == null){
					doc.newPage();
				}else {
					doc.add(elementList.get(i));
				}
			}
			// 一定要记得关闭document对象
			doc.close();
			fos.flush();
			fos.close();
			Message message = new Message();
			message.what = 15;
			message.obj = address;
			UIUtils.getHandler().sendMessage(message);

		} catch (Exception e) {
			e.printStackTrace();
			UIUtils.getHandler().sendEmptyMessage(17);
		}

	}


	public static List<Element> createPdfData(List<String> baseInfo,Bitmap bitmap,List<Date> dateList,List<Float> tpList){
		List<Element> elementList = new ArrayList<>();
		try {
			BaseFont bfChinese;
			if(UIUtils.getCurrentLanguage().equals("en")){
				bfChinese = BaseFont.createFont();
			}else {
				bfChinese= BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
			}


			Font font20 = new Font(bfChinese, 20, Font.BOLD);
			Font font30 = new Font(bfChinese, 30, Font.BOLD);
			Font font50 = new Font(bfChinese, 50, Font.BOLD);
			Font noBoldFont = new Font(bfChinese, 12);
			Font redNoBoldFont = new Font(bfChinese, 12,Font.NORMAL, BaseColor.RED);
			Font blueNoBoldFont = new Font(bfChinese, 12,Font.NORMAL, BaseColor.BLUE);
			Paragraph p1 = new Paragraph(UIUtils.getString(R.string.temperature_tag_report), font30);
			p1.setAlignment(1);
			elementList.add(p1);
			Paragraph p2 = new Paragraph();
			p2.add(new com.itextpdf.text.Chunk(new LineSeparator(font50)));
			p2.setLeading(0);
			elementList.add(p2);
			Paragraph p3 = new Paragraph(String.format("%s    %s",UIUtils.getString(R.string.status_normal),baseInfo.get(1)), font20);
			p3.setSpacingBefore(15f);
			elementList.add(p3);
			elementList.add(p2);
			PdfPTable table = createTable(2, 5);
			table.addCell(createCell(UIUtils.getString(R.string.tag_info),font20,Element.ALIGN_LEFT,2,false));
			table.addCell(createCell(String.format("UID: %s",baseInfo.get(0)),noBoldFont,Element.ALIGN_LEFT,false));
			table.addCell(createCell(String.format("%s %s",UIUtils.getString(R.string.hint_text14),baseInfo.get(4)),noBoldFont,Element.ALIGN_LEFT,false));
			table.addCell(createCell(String.format("%s %s",UIUtils.getString(R.string.hint_text21),baseInfo.get(3)),noBoldFont,Element.ALIGN_LEFT,false));
			table.addCell(createCell(String.format("%s %s",UIUtils.getString(R.string.hint_text20),baseInfo.get(2)),noBoldFont,Element.ALIGN_LEFT,false));
			table.addCell(createCell(String.format("%s %s",UIUtils.getString(R.string.hint_text17),baseInfo.get(5)),noBoldFont,Element.ALIGN_LEFT,false));
			table.addCell(createCell(String.format("%s %s",UIUtils.getString(R.string.delay_time),baseInfo.get(6)),noBoldFont,Element.ALIGN_LEFT,false));
			table.addCell(createCell(String.format("%s %s",UIUtils.getString(R.string.hint_text18),baseInfo.get(7)),noBoldFont,Element.ALIGN_LEFT,false));
			table.addCell(createCell(String.format("%s %s",UIUtils.getString(R.string.hint_text15),baseInfo.get(8)),noBoldFont,Element.ALIGN_LEFT,false));
			table.addCell(createCell(String.format("%s %s",UIUtils.getString(R.string.hint_text16),baseInfo.get(9)),noBoldFont,Element.ALIGN_LEFT,false));
			table.addCell(createCell(String.format("%s %s",UIUtils.getString(R.string.hint_text19),baseInfo.get(10)),noBoldFont,Element.ALIGN_LEFT,false));
			table.setSpacingBefore(15f);
			elementList.add(table);
			elementList.add(p2);
			Paragraph p4 = new Paragraph(UIUtils.getString(R.string.temperature_curve), font20);
			p4.setSpacingBefore(15f);
			elementList.add(p4);

			if(bitmap != null){
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.PNG, 100 , stream);
				Image myImg = Image.getInstance(stream.toByteArray());
				myImg.setAlignment(Image.MIDDLE);
				elementList.add(myImg);
			}
			int size = dateList.size();
			int count = size /120;
			if(size % 120 != 0){
				count++;
			}
			String range = baseInfo.get(5);
			String[] split = range.split(",");
			float low = Float.parseFloat(split[0].replace("[","").replace("\u00B0C",""));
			float height = Float.parseFloat(split[1].replace("]","").replace("\u00B0C",""));


			int j = 0;
			for (int i = 0; i < count; i++) {
				//添加页面
				elementList.add(null);
				Paragraph p5 = new Paragraph(UIUtils.getString(R.string.temperature_tag_report), font20);
				p5.setAlignment(1);
				elementList.add(p5);
				elementList.add(p2);
				PdfPTable tableFather = createTable(3, 1);
				tableFather.setSpacingBefore(15f);
				tableFather.addCell(createCell(UIUtils.getString(R.string.detail_datas),font20,Element.ALIGN_LEFT,3,false));
				PdfPTable table1 = createTable(new float[]{120,50});
				table1.setWidthPercentage(30);
				table1.addCell(createCell(UIUtils.getString(R.string.time),noBoldFont,Element.ALIGN_LEFT,false));
				table1.addCell(createCell(UIUtils.getString(R.string.value),noBoldFont,Element.ALIGN_LEFT,false));
				int counts = j+40;
				if(dateList.size() < counts){
					counts = dateList.size();
				}
				for ( ; j < counts; j++) {
					Float aFloat = tpList.get(j);
					if(aFloat > height ){
						table1.addCell(createCell(TimeUitls.formatDateTime(dateList.get(j).getTime() * 1000),redNoBoldFont,Element.ALIGN_LEFT,BaseColor.RED,false));
						table1.addCell(createCell(String.format("%.2f\u00B0C",aFloat),redNoBoldFont,Element.ALIGN_LEFT,BaseColor.RED,false));

					}
					if(aFloat < low){
						table1.addCell(createCell(TimeUitls.formatDateTime(dateList.get(j).getTime() * 1000),blueNoBoldFont,Element.ALIGN_LEFT,BaseColor.BLUE,false));
						table1.addCell(createCell(String.format("%.2f\u00B0C",aFloat),blueNoBoldFont,Element.ALIGN_LEFT,BaseColor.BLUE,false));
					}
					if(aFloat <= height && aFloat >= low){
						table1.addCell(createCell(TimeUitls.formatDateTime(dateList.get(j).getTime() * 1000),noBoldFont,Element.ALIGN_LEFT,false));
						table1.addCell(createCell(String.format("%.2f\u00B0C",aFloat),noBoldFont,Element.ALIGN_LEFT,false));
					}
				}
				PdfPTable table2 = createTable(new float[]{120,50});
				table2.setWidthPercentage(30);
				table2.addCell(createCell(UIUtils.getString(R.string.time),noBoldFont,Element.ALIGN_LEFT,false));
				table2.addCell(createCell(UIUtils.getString(R.string.value),noBoldFont,Element.ALIGN_LEFT,false));
				counts = j+40;
				if(dateList.size() < counts){
					counts = dateList.size();
				}
				for (; j < counts; j++) {
					Float aFloat = tpList.get(j);
					if(aFloat > height ){
						table2.addCell(createCell(TimeUitls.formatDateTime(dateList.get(j).getTime() * 1000),redNoBoldFont,Element.ALIGN_LEFT,BaseColor.RED,false));
						table2.addCell(createCell(String.format("%.2f\u00B0C",aFloat),redNoBoldFont,Element.ALIGN_LEFT,BaseColor.RED,false));

					}
					if(aFloat < low){
						table2.addCell(createCell(TimeUitls.formatDateTime(dateList.get(j).getTime() * 1000),blueNoBoldFont,Element.ALIGN_LEFT,BaseColor.BLUE,false));
						table2.addCell(createCell(String.format("%.2f\u00B0C",aFloat),blueNoBoldFont,Element.ALIGN_LEFT,BaseColor.BLUE,false));
					}
					if(aFloat <= height && aFloat >= low){
						table2.addCell(createCell(TimeUitls.formatDateTime(dateList.get(j).getTime() * 1000),noBoldFont,Element.ALIGN_LEFT,false));
						table2.addCell(createCell(String.format("%.2f\u00B0C",aFloat),noBoldFont,Element.ALIGN_LEFT,false));
					}
				}
				PdfPTable table3 = createTable(new float[]{120,50});
				table3.setWidthPercentage(30);
				table3.addCell(createCell(UIUtils.getString(R.string.time),noBoldFont,Element.ALIGN_LEFT,false));
				table3.addCell(createCell(UIUtils.getString(R.string.value),noBoldFont,Element.ALIGN_LEFT,false));
				counts = j+40;
				if(dateList.size() < counts){
					counts = dateList.size();
				}
				for (; j < counts; j++) {
					Float aFloat = tpList.get(j);
					if(aFloat > height ){
						table3.addCell(createCell(TimeUitls.formatDateTime(dateList.get(j).getTime() * 1000),redNoBoldFont,Element.ALIGN_LEFT,BaseColor.RED,false));
						table3.addCell(createCell(String.format("%.2f\u00B0C",aFloat),redNoBoldFont,Element.ALIGN_LEFT,BaseColor.RED,false));

					}
					if(aFloat < low){
						table3.addCell(createCell(TimeUitls.formatDateTime(dateList.get(j).getTime() * 1000),blueNoBoldFont,Element.ALIGN_LEFT,BaseColor.BLUE,false));
						table3.addCell(createCell(String.format("%.2f\u00B0C",aFloat),blueNoBoldFont,Element.ALIGN_LEFT,BaseColor.BLUE,false));
					}
					if(aFloat <= height && aFloat >= low){
						table3.addCell(createCell(TimeUitls.formatDateTime(dateList.get(j).getTime() * 1000),noBoldFont,Element.ALIGN_LEFT,false));
						table3.addCell(createCell(String.format("%.2f\u00B0C",aFloat),noBoldFont,Element.ALIGN_LEFT,false));
					}
				}
				tableFather.addCell(new PdfPCell(table1));
				tableFather.addCell(new PdfPCell(table2));
				tableFather.addCell(new PdfPCell(table3));
				elementList.add(tableFather);
			}


		}catch (Exception e){
			e.printStackTrace();
		}

		return elementList;
	}

	public static PdfPTable createTable(int colNumber, int align) {
		PdfPTable table = new PdfPTable(colNumber);
		try {
			table.setWidthPercentage(100);
			table.setHorizontalAlignment(align);
			table.getDefaultCell().setBorder(20);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return table;
	}
	public static PdfPTable createTable(float[] relativeWidths) {
		PdfPTable table = new PdfPTable(relativeWidths);
		try {
			table.setWidthPercentage(100);
			table.getDefaultCell().setBorder(20);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return table;
	}


	public static PdfPCell createCell(String value, Font font, int align,boolean isBorder) throws IOException, DocumentException {
		PdfPCell cell = new PdfPCell();
		cell.setVerticalAlignment(Element.ALIGN_TOP);
		cell.setHorizontalAlignment(align);
		FontSelector selector = new FontSelector();
		selector.addFont(new Font(BaseFont.createFont(), 12));
		selector.addFont(font);
		cell.setPhrase(selector.process(value));
		if(!isBorder){
			cell.setBorder(0);
		}
		return cell;
	}
	public static PdfPCell createCell(String value, Font font, int align,BaseColor color,boolean isBorder) throws IOException, DocumentException {
		PdfPCell cell = new PdfPCell();
		cell.setVerticalAlignment(Element.ALIGN_TOP);
		cell.setHorizontalAlignment(align);
		FontSelector selector = new FontSelector();
		selector.addFont(new Font(BaseFont.createFont(), 12,Font.NORMAL,color));
		selector.addFont(font);
		cell.setPhrase(selector.process(value));
		if(!isBorder){
			cell.setBorder(0);
		}
		return cell;
	}
	public static PdfPCell createCell(String value, Font font, int align, int colspan, boolean borderFlag)  {
		PdfPCell cell = new PdfPCell();
		cell.setVerticalAlignment(Element.ALIGN_TOP);
		cell.setHorizontalAlignment(align);
		cell.setColspan(colspan);
		cell.setPhrase(new Paragraph(value,font));
		cell.setPadding(3.0f);
		if (!borderFlag) {
			cell.setBorder(0);
			cell.setPaddingTop(15.0f);
			cell.setPaddingBottom(8.0f);
		} else if (borderFlag) {
			cell.setBorder(0);
			cell.setPaddingTop(0.0f);
			cell.setPaddingBottom(15.0f);
		}
		return cell;
	}
	public static PdfPCell createCell(String value, Font font,boolean isBorder) {
		PdfPCell cell = new PdfPCell();
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
		cell.setPhrase(new Phrase(value, font));
		if(!isBorder){
			cell.setBorder(0);
		}
		return cell;
	}

}
