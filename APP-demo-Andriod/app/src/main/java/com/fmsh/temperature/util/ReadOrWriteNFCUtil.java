package com.fmsh.temperature.util;

import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;

/**
 * Created by wuyajiang on 2018/3/7.
 */
public class ReadOrWriteNFCUtil {

    /**
     * 读取NFC标签文本数据
     */
    public static  String readNfcTag(Intent intent) {
        String textContent = "";
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage msgs[] = null;
            int contentSize = 0;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                    contentSize += msgs[i].toByteArray().length;
                }
            }
            try {
                if (msgs != null) {
                    NdefRecord record = msgs[0].getRecords()[0];

                    textContent = parseTextRecord(record);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally {

            }
        }
        return textContent;
    }
    /**
     * 解析NDEF文本数据，从第三个字节开始，后面的文本数据
     * @param ndefRecord
     * @return
     */
    public static String parseTextRecord(NdefRecord ndefRecord) {
        /**
         * 判断数据是否为NDEF格式
         */
        //判断TNF
        if (ndefRecord.getTnf() != NdefRecord.TNF_WELL_KNOWN) {
            return null;
        }
        //判断可变的长度的类型
        if (!Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
            return null;
        }
        try {
            //获得字节数组，然后进行分析
            byte[] payload = ndefRecord.getPayload();
            //下面开始NDEF文本数据第一个字节，状态字节
            //判断文本是基于UTF-8还是UTF-16的，取第一个字节"位与"上16进制的80，16进制的80也就是最高位是1，
            //其他位都是0，所以进行"位与"运算后就会保留最高位
            String textEncoding = ((payload[0] & 0x80) == 0) ? "UTF-8" : "UTF-16";
            //3f最高两位是0，第六位是1，所以进行"位与"运算后获得第六位
            int languageCodeLength = payload[0] & 0x3f;
            //下面开始NDEF文本数据第二个字节，语言编码
            //获得语言编码
            String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            //下面开始NDEF文本数据后面的字节，解析出文本
            String textRecord = new String(payload, languageCodeLength + 1,
                    payload.length - languageCodeLength - 1, textEncoding);
            return textRecord;
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }

    public static void writeTextNfc( Intent intent,String text){
        Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        NdefMessage ndefMessage = new NdefMessage(
                new NdefRecord[] { createTextRecord(text) });
        boolean result = writeTag(ndefMessage, detectedTag);
        if(result){
            ToastUtil.sToastUtil.shortDuration("write success");
        }else {
            ToastUtil.sToastUtil.shortDuration("write fail");
        }
    }

    /**
     * 创建NDEF文本数据
     * @param text
     * @return
     */
    private static NdefRecord createTextRecord(String text) {
        byte[] langBytes = Locale.CHINA.getLanguage().getBytes(Charset.forName("US-ASCII"));
        Charset utfEncoding = Charset.forName("UTF-8");
        //将文本转换为UTF-8格式
        byte[] textBytes = text.getBytes(utfEncoding);
        //设置状态字节编码最高位数为0
        int utfBit = 0;
        //定义状态字节
        char status = (char) (utfBit + langBytes.length);
        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        //设置第一个状态字节，先将状态码转换成字节
        data[0] = (byte) status;
        //设置语言编码，使用数组拷贝方法，从0开始拷贝到data中，拷贝到data的1到langBytes.length的位置
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        //设置文本字节，使用数组拷贝方法，从0开始拷贝到data中，拷贝到data的1 + langBytes.length
        //到textBytes.length的位置
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);
        //通过字节传入NdefRecord对象
        //NdefRecord.RTD_TEXT：传入类型 读写
        NdefRecord ndefRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_TEXT, new byte[0], data);
        return ndefRecord;
    }
    /**
     * 写数据
     * @param ndefMessage 创建好的NDEF文本数据
     * @param tag 标签
     * @return
     */
    private static boolean writeTag(NdefMessage ndefMessage, Tag tag) {
        try {
            Ndef ndef = Ndef.get(tag);
            ndef.connect();
            ndef.writeNdefMessage(ndefMessage);
            return true;
        } catch (Exception e) {
        }
        return false;
    }


    /**
     * 读取NFC标签Uri
     */
    public  static  String readNfcUriTag(Intent intent) {
        String uriText = "";
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage ndefMessage = null;
            int contentSize = 0;
            if (rawMsgs != null) {
                if (rawMsgs.length > 0) {
                    ndefMessage = (NdefMessage) rawMsgs[0];
                    contentSize = ndefMessage.toByteArray().length;
                } else {
                    return "";
                }
            }
            try {
                NdefRecord ndefRecord = ndefMessage.getRecords()[0];
                Log.i("JAVA",ndefRecord.toString());
                Uri uri = parse(ndefRecord);
                Log.i("JAVA","uri:"+uri.toString());
                uriText = uri.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return uriText;
    }
    /**
     * 解析NdefRecord中Uri数据
     * @param record
     * @return
     */
    public static Uri parse(NdefRecord record) {
        short tnf = record.getTnf();
        if (tnf == NdefRecord.TNF_WELL_KNOWN) {
            return parseWellKnown(record);
        } else if (tnf == NdefRecord.TNF_ABSOLUTE_URI) {
            return parseAbsolute(record);
        }
        throw new IllegalArgumentException("Unknown TNF " + tnf);
    }
    /**
     * 处理绝对的Uri
     * 没有Uri识别码，也就是没有Uri前缀，存储的全部是字符串
     * @param ndefRecord 描述NDEF信息的一个信息段，一个NdefMessage可能包含一个或者多个NdefRecord
     * @return
     */
    private static Uri parseAbsolute(NdefRecord ndefRecord) {
        //获取所有的字节数据
        byte[] payload = ndefRecord.getPayload();
        Uri uri = Uri.parse(new String(payload, Charset.forName("UTF-8")));
        return uri;
    }
    /**
     * 处理已知类型的Uri
     * @param ndefRecord
     * @return
     */
    private static Uri parseWellKnown(NdefRecord ndefRecord) {
        //判断数据是否是Uri类型的
//        if (!Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_URI))
//            return null;
//        //获取所有的字节数据
//        byte[] payload = ndefRecord.getPayload();
//        String prefix = UriPrefix.URI_PREFIX_MAP.get(payload[0]);
//        byte[] prefixBytes = prefix.getBytes(Charset.forName("UTF-8"));
//        byte[] fullUri = new byte[prefixBytes.length + payload.length - 1];
//        System.arraycopy(prefixBytes, 0, fullUri, 0, prefixBytes.length);
//        System.arraycopy(payload, 1, fullUri, prefixBytes.length, payload.length - 1);
//        Uri uri = Uri.parse(new String(fullUri, Charset.forName("UTF-8")));
        return null;
    }

    public static void writUriNfc(Intent intent,String uri){
        Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        NdefMessage ndefMessage = new NdefMessage(new NdefRecord[]{createUriRecord(uri)});
        boolean result = writeTag(ndefMessage, detectedTag);
        if(result){
            ToastUtil.sToastUtil.shortDuration("write success");
        }else {
            ToastUtil.sToastUtil.shortDuration("write fail");
        }
    }
    /**
     * 将Uri转成NdefRecord
     * @param uriStr
     * @return
     */
    private static NdefRecord createUriRecord(String uriStr) {
//        byte prefix = 0;
//        for (Byte b : UriPrefix.URI_PREFIX_MAP.keySet()) {
//            String prefixStr = UriPrefix.URI_PREFIX_MAP.get(b).toLowerCase();
//            if ("".equals(prefixStr))
//                continue;
//            if (uriStr.toLowerCase().startsWith(prefixStr)) {
//                prefix = b;
//                uriStr = uriStr.substring(prefixStr.length());
//                break;
//            }
//        }
//        byte[] data = new byte[1 + uriStr.length()];
//        data[0] = prefix;
//        System.arraycopy(uriStr.getBytes(), 0, data, 1, uriStr.length());
//        NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_URI, new byte[0], data);
        return null;
    }
    /**
     * 写入标签
     * @param message
     * @param tag
     * @return
     */
    private static boolean writeUriTag(NdefMessage message, Tag tag) {
        int size = message.toByteArray().length;
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable()) {
                    return false;
                }
                if (ndef.getMaxSize() < size) {
                    return false;
                }
                ndef.writeNdefMessage(message);
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    /**非NDEF格式
     * 将NFC标签的存储区域分为16个页，每一个页可以存储4个字节，一个可存储64个字节（512位）。
     * 页码从0开始（0至15）。前4页（0至3）存储了NFC标签相关的信息（如NFC标签的序列号、控制位等）。
     * 从第5页开始存储实际的数据（4至15页）。使用MifareUltralight.get方法获取MifareUltralight对象，
     * 然后调用MifareUltralight.connect方法进行连接，
     * 并使用MifareUltralight.writePage方法每次写入1页（4个字节）。
     * 也可以使用MifareUltralight.readPages方法每次连续读取4页。
     * 如果读取的页的序号超过15，则从头开始读。
     * 例如，从第15页（序号为14）开始读。readPages方法会读取14、15、0、1页的数据。
     * 读取MifareUltralight格式数据
     * @param intent
     */
    public static void readMul(Intent intent){
    Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
    String[] techList = tag.getTechList();
    boolean haveMifareUltralight = false;
    for (String tech : techList) {
        if (tech.indexOf("MifareUltralight") >= 0) {
            haveMifareUltralight = true;
            break;
        }
    }
    if (!haveMifareUltralight) {
        ToastUtil.sToastUtil.shortDuration("不支持MifareUltralight数据格式");
        return;
    }
    String data = readMifareUltralightTag(tag);
    if (data != null){
        ToastUtil.sToastUtil.shortDuration(data);
    }

}

    private static String readMifareUltralightTag(Tag tag) {
        MifareUltralight ultralight = MifareUltralight.get(tag);
        try {
            ultralight.connect();
            byte[] data = ultralight.readPages(4);
            return new String(data, Charset.forName("GB2312"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                ultralight.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 写入MifareUltralight格式数据
     * @param intent
     * @param text
     */
    public static void writeMul(Intent intent,String text){
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String[] techList = tag.getTechList();
        boolean haveMifareUltralight = false;
        for (String tech : techList) {
            if (tech.indexOf("MifareUltralight") >= 0) {
                haveMifareUltralight = true;
                break;
            }
        }
        if (!haveMifareUltralight) {
            ToastUtil.sToastUtil.shortDuration("不支持MifareUltralight数据格式");
            return;
        }
        writeMifareUltralightTag(tag,text);
    }

    public static void writeMifareUltralightTag(Tag tag,String text) {
        MifareUltralight ultralight = MifareUltralight.get(tag);
        try {
            ultralight.connect();
            //写入八个汉字，从第五页开始写，中文需要转换成GB2312格式
            ultralight.writePage(4, "北京".getBytes(Charset.forName("GB2312")));
            ultralight.writePage(5, "上海".getBytes(Charset.forName("GB2312")));
            ultralight.writePage(6, "广州".getBytes(Charset.forName("GB2312")));
            ultralight.writePage(7, "天津".getBytes(Charset.forName("GB2312")));
            ToastUtil.sToastUtil.shortDuration("写入成功");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                ultralight.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
