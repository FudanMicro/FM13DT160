package com.fmsh.temperature.util;

import android.app.Dialog;
import android.content.Context;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcV;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.Html;
import android.util.Log;
import android.util.Size;

import com.fmsh.temperature.R;
import com.fmsh.temperature.listener.OnItemClickListener;

import java.io.IOException;
import java.text.ParseException;

/**
 * Created by wyj on 2018/7/6.
 */
public class NFCUtils {

    private static CommomDialog commomDialog;

    public static String getRfid(Tag tag) {
        byte[] id = null;
        if (tag != null) {
            id = tag.getId();
        }
        return bytesToHexString(id, ':');

    }


    /**
     * 读取芯片15693芯片数据
     *
     * @param tag
     * @return
     */
    public static void readNfcVData(final Tag tag, int type) {
        final NfcV nfcV = NfcV.get(tag);
        final byte[] id = tag.getId();
        StringBuffer sb = new StringBuffer();
        try {
            if (nfcV != null) {
                if (nfcV.isConnected())
                    nfcV.close();
                nfcV.connect();
                if (MyConstant.ISREALTIME && type == 2) {
                    final Handler handler = new Handler();
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                nfcV.connect();
                                byte[] bytes = instructV(11, id);
                                byte[] transceive = nfcV.transceive(bytes);
                                String hexString = getHexString(transceive, transceive.length);
                                LogUtil.d(hexString);
                                SystemClock.sleep(400);
                                if (hexString != null && hexString.contains("FAFF")) {
                                    byte[] transceive1 = nfcV.transceive(instructV(12, id));
                                    double v = strFromat(getHexString(transceive1, transceive1.length));
                                    // TODO: 2018/7/13 温度结果处理
                                    if (UIUtils.getHandler() != null) {
                                        Message message = new Message();
                                        message.what = 1;
                                        message.obj = v;
                                        UIUtils.getHandler().sendMessage(message);
                                    }
                                    handler.postDelayed(this, 1000);
                                } else {
                                    HintDialog.faileDialog(ActivityUtils.instance.getCurrentActivity(), UIUtils.getString(R.string.hint_text11));

                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                Message message = new Message();
                                message.what = -1;
                                UIUtils.getHandler().sendMessage(message);

                            } finally {
                                try {
                                    nfcV.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    };
                    handler.postDelayed(runnable, 1000);

                } else {
                    if (type == 2 && UIUtils.getHandler() != null) {

                        byte[] res = new byte[0];
                        byte[] bytes2 = new byte[]{0x22, (byte) 0xb1, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xb0, 0x54, 0x00, (byte) 0x03};
                        System.arraycopy(id, 0, bytes2, 3, id.length);
                        res = nfcV.transceive(bytes2);
                        if (res != null) {
                            String hexString = getHexString(res, res.length);
                            String substring = hexString.substring(hexString.length() - 2, hexString.length());
                            int size = Integer.parseInt(substring, 16) * 1024; //存储的数据区域大小

                            byte[] transceive = nfcV.transceive(instructV(14, id));
                            String hexString1 = getHexString(transceive, transceive.length);
                            //测温次数
                            int tpCount = Integer.parseInt(hexString1.substring(4, 6) + hexString1.substring(2, 4), 16);

                            //测温间隔时间和开始测温时间
                            byte[] transceive1 = nfcV.transceive(instructV(21, id));
                            String hexString2 = getHexString(transceive1, transceive1.length).substring(2);
                            String startTime = hexString2.substring(4, 12); //测温开始时间
                            String start = hexString2.substring(hexString2.length() - 2);
                            String end = hexString2.substring(hexString2.length() - 4, hexString2.length() - 2);
                            int tpTime = Integer.parseInt(end + start, 16);

                            //获取最大最小温度值
                            String strTemp = TransUtil.byteToHex(nfcV.transceive(instructV(18, id))).substring(2);
                            String[] split = new String[2];
                            split[0] = "00" + strTemp.substring(0, 4);
                            split[1] = "00" + strTemp.substring(4, 8);
                            //读取温度超限次数
                            String toHex = TransUtil.byteToHex(nfcV.transceive(instructV(36, id)));
                            String maxWithMin = "";
                            if (Integer.parseInt(toHex, 16) == 0) {
                                maxWithMin = TransUtil.byteToHex(nfcV.transceive(instructV(37, id))).substring(2);
                                LogUtil.d(maxWithMin);
                            } else {
                                String strMax = TransUtil.byteToHex(nfcV.transceive(instructV(38, id)));
                                String strMin = TransUtil.byteToHex(nfcV.transceive(instructV(39, id)));
                                String tepMin = TransUtil.byteToHex(nfcV.transceive(instructV(42, id)));
                                String tepMax = TransUtil.byteToHex(nfcV.transceive(instructV(43, id)));
                                maxWithMin = strMax + "with" + strMin + "with" + tepMax + tepMin;
                                LogUtil.d(strMax + strMin);
                            }
                            //测温延时
                            String delay = TransUtil.byteToHex(nfcV.transceive(instructV(4, id)));

                            byte[] transceive2 = nfcV.transceive(instructV(9, id));
                            int intFlag = transceive2[2] & 0x10;
                            String hexCount = "000000";
                            String flag = "";
                            int allCount = tpCount;
                            if (intFlag == 16) {
                                hexCount = TransUtil.byteToHex(nfcV.transceive(instructV(32, id)));
                                tpCount = Integer.parseInt(hexCount.substring(4) + hexCount.substring(2, 4), 16);
                                if (Integer.parseInt(delay, 16) == 0) {
                                    tpCount++;
                                }

                                flag = "0";
                            } else {
                                String s1 = TransUtil.byteToHex(nfcV.transceive(instructV(44, id))).substring(2);
                                hexCount = "00" + s1.substring(0, 4);
                                tpCount = Integer.parseInt(s1.substring(2, 4) + s1.substring(0, 2), 16) + 1;
                                flag = "1";
                            }

                            if (tpCount * 4 > size) {

                                addDataV(size, id, nfcV, sb);
                            } else if (tpCount > 0) {

                                addDataV(tpCount * 4, id, nfcV, sb);
                            }

                            sb.append(",").append(allCount).append(",").append(split[0]).append(",")
                                    .append(split[1]).append(",").append(tpTime).append(",").append(startTime)
                                    .append("," + maxWithMin).append("," + hexCount).append("," + flag);
                            if (UIUtils.getHandler() != null && type == 2) {
                                Message message = new Message();
                                message.what = 2;
                                message.obj = sb;
                                UIUtils.getHandler().sendMessage(message);

                            }
                        }
                    } else if (type == 3) {
                        byte[] transceive2 = nfcV.transceive(instructV(9, id));
                        int flag = transceive2[2] & 0x10;
                        if (flag == 16) {
                            HintDialog.messageDialog(ActivityUtils.instance.getCurrentActivity(), UIUtils.getString(R.string.hint_text5));
                        } else {
                            byte[] bytes = instructV(1, id);
                            nfcV.transceive(bytes);
                            byte[] transceive = nfcV.transceive(instructV(2, id));
                            String string = getHexString(transceive, transceive.length);
                            LogUtil.d(string);
                            if (string.contains("5555")) {
                                nfcV.transceive(instructV(3, id));
                                byte[] delay = nfcV.transceive(instructV(4, id));
                                delay[0] = delay[1];
                                delay[1] = delay[2];
                                delay[2] = delay[0];
                                int intDelay = Integer.parseInt(getHexString(delay, delay.length).substring(2), 16);
                                int delayValue = SpUtils.getIntValue(MyConstant.delayTime, 0);
                                if (intDelay == delayValue) {
                                    byte[] transceive3 = nfcV.transceive(instructV(5, id));
                                    String hexString = getHexString(transceive3, transceive3.length);
                                    LogUtil.d(hexString);
                                    byte[] intervalTime = nfcV.transceive(instructV(6, id));
                                    intervalTime[0] = intervalTime[1];
                                    intervalTime[1] = intervalTime[2];
                                    intervalTime[2] = intervalTime[0];
                                    int IntIntervalTime = Integer.parseInt(getHexString(intervalTime, intervalTime.length).substring(2), 16);
                                    int intValue = SpUtils.getIntValue(MyConstant.intervalTime, 1);
                                    if (IntIntervalTime == intValue) {
                                        nfcV.transceive(instructV(5, id));
                                        nfcV.transceive(instructV(13, id));
                                        nfcV.transceive(instructV(40, id));
                                        nfcV.transceive(instructV(41, id));
                                        nfcV.transceive(instructV(16, id));
                                        nfcV.transceive(instructV(17, id));
                                        nfcV.transceive(instructV(19, id));
                                        nfcV.transceive(instructV(20, id));
                                        nfcV.transceive(instructV(22, id));
                                        byte[] transceive1 = nfcV.transceive(instructV(7, id));
                                        String string1 = getHexString(transceive1, transceive1.length);
                                        if (string1.contains("0000")) {

                                            HintDialog.messageDialog(ActivityUtils.instance.getCurrentActivity(), UIUtils.getString(R.string.hint_text1));
                                        } else {

                                            HintDialog.messageDialog(ActivityUtils.instance.getCurrentActivity(), UIUtils.getString(R.string.hint_text2));
                                        }
                                    } else {
                                        HintDialog.messageDialog(ActivityUtils.instance.getCurrentActivity(), UIUtils.getString(R.string.hint_text12));
                                    }
                                } else {
                                    HintDialog.messageDialog(ActivityUtils.instance.getCurrentActivity(), UIUtils.getString(R.string.hint_text12));
                                }
                            } else {
                                HintDialog.messageDialog(ActivityUtils.instance.getCurrentActivity(), UIUtils.getString(R.string.hint_text12));
                            }
                        }
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (type == 3) {
                HintDialog.messageDialog(ActivityUtils.instance.getCurrentActivity(), UIUtils.getString(R.string.hint_text12));
            }
            if (type == 2) {
                Message message = new Message();
                message.what = 2;
                message.obj = sb;
                UIUtils.getHandler().sendMessage(message);
                HintDialog.messageDialog(ActivityUtils.instance.getCurrentActivity(), UIUtils.getString(R.string.hint_text13));
            }
        } finally {
            try {
                nfcV.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private static void addDataV(int size, byte[] id, NfcV nfcV, StringBuffer sb) throws IOException {
        // x 高位 y 地位 ,n 读取大小,count 次数,p 余数
        int x = 0x10, y = 0x00, n = 0xf8, count = 1, p = 0;
        p = size % 248;
        count = size / 248;
        if (p != 0) {
            count = count + 1;
        }
        for (int i = 0; i < count; i++) {
            if (i == 0) {
                y = 0x00;
            } else if (i == 1) {
                y = y + 0xfc;
            } else {
                x = x + 0x01;
                y = y - 0x04;
            }
            if (p != 0 && i == count - 1) {
                n = p - count * 4;
            }
            if (i == 76) {
                n = 44;
            }
            byte[] bytes1 = new byte[]{0x22, (byte) 0xb1, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) x, (byte) y, 0x00, (byte) n};
            System.arraycopy(id, 0, bytes1, 3, id.length);
            byte[] transceive = nfcV.transceive(bytes1);
            String hexString1 = getHexString(transceive, transceive.length);
            if (hexString1.equals("000300")) {
                break;
            }
            sb.append(hexString1.substring(2, hexString1.length()));
            LogUtil.d(hexString1);

        }
    }

    /**
     * 指令类型处理
     *
     * @param type
     * @param id
     * @return
     */
    public static byte[] instructV(int type, byte[] id) {
        byte[] bytes = null;
        switch (type) {
            case 1: //唤醒
                bytes = new byte[]{0x22, (byte) 0xc4, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0x00};
                break;
            case 2://查看唤醒
                bytes = new byte[]{0x22, (byte) 0xc4, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0x80};
                break;
            case 3: //延时
                bytes = new byte[]{0x22, (byte) 0xc5, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xc0, (byte) 0x84, (byte) 0x00, (byte) SpUtils.getIntValue(MyConstant.delayTime, 0)};
                break;
            case 4: //查看延时
                bytes = new byte[]{0x22, (byte) 0xc6, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xc0, (byte) 0x84};
                break;
            case 5: //测温间隔
                bytes = new byte[]{0x22, (byte) 0xc5, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xc0, (byte) 0x85, (byte) getCount(SpUtils.getIntValue(MyConstant.intervalTime, 0))[0], (byte) getCount(SpUtils.getIntValue(MyConstant.intervalTime, 1))[1]};
                break;
            case 6: //查看测温间隔
                bytes = new byte[]{0x22, (byte) 0xc6, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xc0, (byte) 0x85};
                break;
            case 7: //开始rtc
                bytes = new byte[]{0x22, (byte) 0xc2, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0x00, 0, 0, 0, 0};
                break;
            case 8: //结束rtc测温
                bytes = new byte[]{0x22, (byte) 0xc2, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0x80, 0, 0, 0, 0};
                break;
            case 9://查看电池,等状态
                bytes = new byte[]{0x22, (byte) 0xcf, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0x01, (byte) 0x00, 0x00};
                break;
            case 10://读取数据
                bytes = new byte[]{0x22, (byte) 0xb1, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0x10, (byte) 0x00, 0x00, (byte) 0xfa};
                break;
            case 11://开始实时测温
                bytes = new byte[]{0x22, (byte) 0xc0, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0x06, (byte) 0x00};
                break;
            case 12://获取实时测温结果
                bytes = new byte[]{0x22, (byte) 0xc0, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0x84, (byte) 0x00};
                break;
            case 13: //设置测温次数
                bytes = new byte[]{0x22, (byte) 0xb3, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xb0, (byte) 0x94, 0x01, (byte) getCount(SpUtils.getIntValue(MyConstant.tpCount, 0))[1], (byte) getCount(SpUtils.getIntValue(MyConstant.tpCount, 10))[0]};
                break;
            case 14: //查看测温次数
                bytes = new byte[]{0x22, (byte) 0xb1, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xb0, (byte) 0x94, 0x00, (byte) 0x3};
                break;
            case 15:
                bytes = new byte[]{0x22, (byte) 0xb1, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0x00, 0x26, 0x00, 0x07};
                break;
            case 16://设置最小温度值
                int min = SpUtils.getIntValue(MyConstant.tpMin, 0);
                if (min < 0) {
                    bytes = new byte[]{0x22, (byte) 0xb3, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xb0, (byte) 0x80, 0x01, (byte) getCount(min * 4 + 0x400)[1], (byte) getCount(min * 4 + 0x400)[0]};

                } else {
                    bytes = new byte[]{0x22, (byte) 0xb3, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xb0, (byte) 0x80, 0x01, (byte) getCount(min * 4)[1], (byte) getCount(min * 4)[0]};

                }
                break;
            case 17: // 设置最大温度值
                int max = SpUtils.getIntValue(MyConstant.tpMax, 40);
                if (max < 0) {
                    bytes = new byte[]{0x22, (byte) 0xb3, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xb0, (byte) 0x82, 0x01, (byte) getCount(max * 4 + 0x400)[1], (byte) getCount(max * 4 + 0x400)[0]};

                } else {
                    bytes = new byte[]{0x22, (byte) 0xb3, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xb0, (byte) 0x82, 0x01, (byte) getCount(max * 4)[1], (byte) getCount(max * 4)[0]};

                }
                break;
            case 18://获取温度值
                bytes = new byte[]{0x22, (byte) 0xb1, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xb0, (byte) 0x80, 0x00, 0x03};
                break;
            case 19: //写温度大小值
                bytes = new byte[]{0x22, (byte) 0xb3, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0x00, (byte) 0x1e, 0x03, 0x00, (byte) SpUtils.getIntValue(MyConstant.tpMin, 0), 0x00, (byte) SpUtils.getIntValue(MyConstant.tpMax, 40)};
                break;
            case 20: //写测温间隔
                bytes = new byte[]{0x22, (byte) 0xb3, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0x00, (byte) 0x22, 0x01, (byte) getCount(SpUtils.getIntValue(MyConstant.intervalTime, 0))[0], (byte) getCount(SpUtils.getIntValue(MyConstant.intervalTime, 1))[1]};
                break;
            case 21: // 读取测温间隔
                bytes = new byte[]{0x22, (byte) 0xb1, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0x00, (byte) 0x18, 0x00, 0x0b};
                break;
            case 22://写时间
                bytes = new byte[]{0x22, (byte) 0xb3, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, 0x00, 0x1a, 0x03, (byte) getTimeHex()[0], (byte) getTimeHex()[1], (byte) getTimeHex()[2], (byte) getTimeHex()[3]};
                break;
            case 23: //读取时间
                bytes = new byte[]{0x22, (byte) 0xb1, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, 0x00, (byte) 0x18, 0x00, 0x07};
                break;
            case 24: //休眠
                bytes = new byte[]{0x22, (byte) 0xc3, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, 0x01};
                break;
            case 25://Init Regfile
                bytes = new byte[]{0x22, (byte) 0xce, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                break;
            case 26: //led on
                bytes = new byte[]{0x22, (byte) 0xc9, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0x02};
                break;
            case 27: //led off
                bytes = new byte[]{0x22, (byte) 0xc9, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0x00};
                break;
            case 28: //获取电池电压第一步 配置寄存器
                bytes = new byte[]{0x22, (byte) 0xc5, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xc0, 0x12, 0x00, 0x08};
                break;
            case 29: //获取电池电压第二步  启动测量
                bytes = new byte[]{0x22, (byte) 0xc0, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, 0x12, 0x00};
                break;
            case 30: //获取电池电压第三步 读取结果
                bytes = new byte[]{0x22, (byte) 0xc0, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0x92, 0x00};
                break;
            case 31://获取电池电压第四步  恢复寄存器
                bytes = new byte[]{0x22, (byte) 0xc5, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xc0, 0x12, 0x00, 0x00};
                break;
            case 32://读C091寄存器
                bytes = new byte[]{0x22, (byte) 0xc6, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xc0, (byte) 0x91};
                break;
            case 33://读C092寄存器
                bytes = new byte[]{0x22, (byte) 0xc6, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xc0, (byte) 0x92};
                break;
            case 34://获取场强指令
                bytes = new byte[]{0x22, (byte) 0xD0, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, 0x00};
                break;
            case 35: //获取随机数
                bytes = new byte[]{0x22, (byte) 0xB2, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0};
                break;
            case 36: //读取c094寄存器
                bytes = new byte[]{0x22, (byte) 0xc6, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xc0, (byte) 0x94};
                break;
            case 37: //读取B180地址
                bytes = new byte[]{0x22, (byte) 0xb1, (byte) 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xb1, (byte) 0x80, 0x00, 0x04};
                break;
            case 38: //读取c09a寄存器max_limit_cnt
                bytes = new byte[]{0x22, (byte) 0xc6, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xc0, (byte) 0x9a};
                break;
            case 39: //读取c09b寄存器min_limit_cnt
                bytes = new byte[]{0x22, (byte) 0xc6, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xc0, (byte) 0x9b};
                break;
            case 40: //写测温最小值
                bytes = new byte[]{0x22, (byte) 0xc5, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xc0, (byte) 0x99, (byte) getCount(100 * 4)[0], (byte) getCount(100 * 4)[1]};
                break;
            case 41: //写测温最大值
                bytes = new byte[]{0x22, (byte) 0xc5, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xc0, (byte) 0x98, (byte) getCount(-100 * 4 + 0x400)[0], (byte) getCount(-100 * 4 + 0x400)[1]};
                break;
            case 42: //读测温最小值
                bytes = new byte[]{0x22, (byte) 0xc6, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xc0, (byte) 0x99};
                break;
            case 43: //读测温最大值
                bytes = new byte[]{0x22, (byte) 0xc6, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xc0, (byte) 0x98};
                break;
            case 44: //读测温次数
                bytes = new byte[]{0x22, (byte) 0xb1, (byte) 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xb1, (byte) 0x88, 0x00, 0x00};
                break;
        }
        System.arraycopy(id, 0, bytes, 3, id.length);
        return bytes;
    }

    private static int[] getTimeHex() {
        int[] ints = new int[4];
        long aLong = System.currentTimeMillis() / 1000;
        String string = Long.toHexString(aLong);
        int a = Integer.parseInt(string.substring(0, 2), 16),
                b = Integer.parseInt(string.substring(2, 4), 16),
                c = Integer.parseInt(string.substring(4, 6), 16),
                d = Integer.parseInt(string.substring(6, 8), 16);
        LogUtil.d(string);
        ints[0] = a;
        ints[1] = b;
        ints[2] = c;
        ints[3] = d;
        return ints;
    }

    /**
     * 读取14443芯片数据
     *
     * @param tag
     * @return
     */
    public static void readNfcAData(final Tag tag, int type) {
        final NfcA nfcA = NfcA.get(tag);
        StringBuffer sb = new StringBuffer();
        try {
            if (nfcA != null) {
                if (nfcA.isConnected()) {

                    nfcA.close();
                }
                nfcA.connect();
                if (MyConstant.ISREALTIME && type == 2) {
                    final Handler handler = new Handler();
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                nfcA.connect();
                                byte[] bytes = instructA(11);
                                byte[] transceive = nfcA.transceive(bytes);
                                String hexString = getHexString(transceive, transceive.length);
                                LogUtil.d(hexString);
                                SystemClock.sleep(400);
                                if (hexString != null && hexString.contains("FAFF")) {
                                    byte[] transceive1 = nfcA.transceive(instructA(12));
                                    double v = strFromat(getHexString(transceive1, transceive1.length));
                                    // TODO: 2018/7/13 温度结果处理
                                    if (UIUtils.getHandler() != null) {
                                        Message message = new Message();
                                        message.what = 1;
                                        message.obj = v;
                                        UIUtils.getHandler().sendMessage(message);
                                    }
                                    handler.postDelayed(this, 1000);
                                } else {
                                    HintDialog.messageDialog(ActivityUtils.instance.getCurrentActivity(), "Error");
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                Message message = new Message();
                                message.what = -1;
                                UIUtils.getHandler().sendMessage(message);

                            } finally {
                                try {
                                    nfcA.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    };
                    handler.postDelayed(runnable, 1000);

                } else {
                    if (type == 2 && UIUtils.getHandler() != null) {

                        byte[] res = new byte[0];
                        byte[] bytes = new byte[]{0x40, (byte) 0xb1, (byte) 0xb0, 0x54, 0x00, (byte) 0x03, 0x00};
                        res = nfcA.transceive(bytes);
                        if (res != null) {
                            String hexString = getHexString(res, res.length);
                            String substring = hexString.substring(hexString.length() - 2, hexString.length());
                            int size = Integer.parseInt(substring, 16) * 1024; //存储的数据区域大小

                            byte[] transceive = nfcA.transceive(instructA(14));
                            String hexString1 = getHexString(transceive, transceive.length);
                            //测温次数
                            int tpCount = Integer.parseInt(hexString1.substring(2, 4) + hexString1.substring(0, 2), 16);

                            //测温间隔时间和开始测温时间
                            byte[] transceive1 = nfcA.transceive(instructA(21));
                            String hexString2 = getHexString(transceive1, transceive1.length);
                            String startTime = hexString2.substring(0, 8); //测温开始时间
                            int tpTime = Integer.parseInt(hexString2.substring(hexString2.length() - 8, hexString2.length() - 4), 16);


                            //获取最大最小温度值
                            String[] split = new String[2];
                            String setTemp = TransUtil.byteToHex(nfcA.transceive(instructA(18)));
                            split[0] = "00" + setTemp.substring(0, 4);
                            split[1] = "00" + setTemp.substring(4, 8);
                            //                            split[0] =TransUtil.byteToHex(nfcA.transceive(instructA(43)));
                            //                            split[1] =TransUtil.byteToHex(nfcA.transceive(instructA(42)));
                            //读取温度超限次数
                            String toHex = TransUtil.byteToHex(nfcA.transceive(instructA(36)));
                            String maxWithMin = "";
                            if (Integer.parseInt(toHex, 16) == 0) {
                                maxWithMin = TransUtil.byteToHex(nfcA.transceive(instructA(37)));
                                LogUtil.d(maxWithMin);
                            } else {
                                String strMax = TransUtil.byteToHex(nfcA.transceive(instructA(38)));
                                String strMin = TransUtil.byteToHex(nfcA.transceive(instructA(39)));
                                String tepMin = TransUtil.byteToHex(nfcA.transceive(instructA(42)));
                                String tepMax = TransUtil.byteToHex(nfcA.transceive(instructA(43)));
                                maxWithMin = strMax + "with" + strMin + "with" + tepMax + tepMin;
                                LogUtil.d(strMax + strMin);
                            }
                            //测温延时
                            String delay = TransUtil.byteToHex(nfcA.transceive(instructA(4)));

                            byte[] transceive2 = nfcA.transceive(instructA(9));
                            int intFlag = transceive2[2] & 0x10;
                            String hexCount = "000000";
                            int allCount = tpCount;
                            String flag = "";
                            if (intFlag == 16) {
                                hexCount = TransUtil.byteToHex(nfcA.transceive(instructA(32)));
                                tpCount = Integer.parseInt(hexCount.substring(4) + hexCount.substring(2, 4), 16);
                                if (Integer.parseInt(delay, 16) == 0) {
                                    tpCount++;
                                }
                                flag = "0";
                            } else {
                                String s1 = TransUtil.byteToHex(nfcA.transceive(instructA(44)));
                                hexCount = "00" + s1.substring(0, 4);
                                tpCount = Integer.parseInt(s1.substring(2, 4) + s1.substring(0, 2), 16) + 1;
                                flag = "1";
                            }

                            if (tpCount * 4 > size) {

                                getTpDataA(nfcA, size, sb);
                            } else if (tpCount > 0) {

                                getTpDataA(nfcA, tpCount * 4, sb);
                            }
                            sb.append(",").append(allCount).append(",").append(split[0]).append(",")
                                    .append(split[1]).append(",").append(tpTime).append(",").append(startTime)
                                    .append("," + maxWithMin).append("," + hexCount).append("," + flag);
                            if (UIUtils.getHandler() != null && type == 2) {
                                Message message = new Message();
                                message.what = 2;
                                message.obj = sb;
                                UIUtils.getHandler().sendMessage(message);

                            }
                        }
                    } else if (type == 3) {
                        byte[] transceive2 = nfcA.transceive(instructA(9));
                        int flag = transceive2[2] & 0x10;
                        if (flag == 16) {
                            HintDialog.messageDialog(ActivityUtils.instance.getCurrentActivity(), UIUtils.getString(R.string.hint_text5));
                        } else {
                            nfcA.transceive(instructA(1));
                            byte[] transceive = nfcA.transceive(instructA(2));
                            String string = getHexString(transceive, transceive.length);
                            LogUtil.d(string);
                            if (string.contains("5555")) {
                                nfcA.transceive(instructA(3));
                                byte[] delay = nfcA.transceive(instructA(4));
                                delay[0] = delay[1];
                                delay[1] = delay[2];
                                delay[2] = delay[0];
                                int intDelay = Integer.parseInt(getHexString(delay, delay.length).substring(2), 16);
                                int delayValue = SpUtils.getIntValue(MyConstant.delayTime, 0);
                                if (intDelay == delayValue) {
                                    byte[] transceive3 = nfcA.transceive(instructA(5));
                                    String hexString = getHexString(transceive3, transceive3.length);
                                    LogUtil.d(hexString);
                                    byte[] intervalTime = nfcA.transceive(instructA(6));
                                    intervalTime[0] = intervalTime[1];
                                    intervalTime[1] = intervalTime[2];
                                    intervalTime[2] = intervalTime[0];
                                    int IntIntervalTime = Integer.parseInt(getHexString(intervalTime, intervalTime.length).substring(2), 16);
                                    int intValue = SpUtils.getIntValue(MyConstant.intervalTime, 1);
                                    if (IntIntervalTime == intValue) {
                                        nfcA.transceive(instructA(13));
                                        nfcA.transceive(instructA(40));
                                        nfcA.transceive(instructA(41));
                                        nfcA.transceive(instructA(16));
                                        nfcA.transceive(instructA(17));
                                        nfcA.transceive(instructA(19));
                                        nfcA.transceive(instructA(20));
                                        nfcA.transceive(instructA(22));
                                        byte[] transceive1 = nfcA.transceive(instructA(7));
                                        String string1 = getHexString(transceive1, transceive1.length);
                                        if (string1.contains("0000")) {

                                            HintDialog.messageDialog(ActivityUtils.instance.getCurrentActivity(), UIUtils.getString(R.string.hint_text1));
                                        } else {

                                            HintDialog.messageDialog(ActivityUtils.instance.getCurrentActivity(), UIUtils.getString(R.string.hint_text2));
                                        }
                                    } else {
                                        HintDialog.messageDialog(ActivityUtils.instance.getCurrentActivity(), UIUtils.getString(R.string.hint_text12));
                                    }
                                } else {
                                    HintDialog.messageDialog(ActivityUtils.instance.getCurrentActivity(), UIUtils.getString(R.string.hint_text12));
                                }

                            } else {
                                HintDialog.messageDialog(ActivityUtils.instance.getCurrentActivity(), UIUtils.getString(R.string.hint_text12));
                            }
                        }

                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (type == 3) {
                HintDialog.messageDialog(ActivityUtils.instance.getCurrentActivity(), UIUtils.getString(R.string.hint_text12));
            }
            if (type == 2) {
                Message message = new Message();
                message.what = 2;
                message.obj = sb;
                UIUtils.getHandler().sendMessage(message);
                HintDialog.messageDialog(ActivityUtils.instance.getCurrentActivity(), UIUtils.getString(R.string.hint_text13));
            }
        } finally {

            try {
                nfcA.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void getTpDataA(NfcA nfcA, int size, StringBuffer sb) throws IOException {
        // x 高位 y 地位 ,n 读取大小,count 次数,p 余数
        int x = 0x10, y = 0x00, n = 0xf8, count = 1, p = 0;

        p = size % 252;
        count = size / 252;
        if (p != 0) {
            count = count + 1;
        }
        for (int i = 0; i < count; i++) {
            if (i == 0) {
                y = 0x00;
            } else if (i == 1) {
                y = y + 0xfc;
            } else {
                x = x + 0x01;
                y = y - 0x04;
            }
            if (p != 0 && i == count - 1) {
                n = p - count * 4;
            }
            if (i == 76) {
                n = 44;
            }
            byte[] bytes = new byte[]{0x40, (byte) 0xb1, (byte) x, (byte) y, (byte) 0x00, (byte) n, 0x00};
            byte[] transceive = nfcA.transceive(bytes);
            if (transceive != null) {
                String hexString1 = getHexString(transceive, transceive.length);
                if (hexString1.equals("000300")) {
                    break;
                }
                LogUtil.d(hexString1);
                sb.append(hexString1);
            }

        }


    }


    public static void startV(Tag tag, int type) {
        NfcV nfcV = NfcV.get(tag);
        byte[] id = tag.getId();
        try {
            nfcV.connect();
            if (type == 28) {

                byte[] bytes1 = nfcV.transceive(instructV(28, id));
                byte[] bytes2 = nfcV.transceive(instructV(29, id));
                SystemClock.sleep(500);
                byte[] bytes3 = nfcV.transceive(instructV(30, id));
                byte[] bytes4 = nfcV.transceive(instructV(31, id));
                LogUtil.d(getHexString(bytes1, bytes1.length));
                LogUtil.d(getHexString(bytes2, bytes2.length));
                LogUtil.d(getHexString(bytes3, bytes3.length));
                LogUtil.d(getHexString(bytes4, bytes4.length));
                String hexString = getHexString(bytes3, bytes3.length);
                Message message = new Message();
                message.what = type;
                message.obj = hexString;
                if (UIUtils.getHandler() != null) {

                    UIUtils.getHandler().sendMessage(message);
                }


            } else if (type == 11) {
                byte[] bytes = instructV(11, id);
                byte[] transceive = nfcV.transceive(bytes);
                String hexString = getHexString(transceive, transceive.length);
                LogUtil.d(hexString);
                SystemClock.sleep(400);
                if (hexString != null && hexString.contains("FAFF")) {
                    byte[] transceive1 = nfcV.transceive(instructV(12, id));
                    double v = strFromat(getHexString(transceive1, transceive1.length));
                    // TODO: 2018/7/13 温度结果处理
                    if (UIUtils.getHandler() != null) {
                        Message message = new Message();
                        message.what = type;
                        message.obj = v + "℃";
                        UIUtils.getHandler().sendMessage(message);
                    }
                } else {
                    HintDialog.faileDialog(ActivityUtils.instance.getCurrentActivity(), "Error");

                }
            } else if (type == 35) {
                byte[] transceive = nfcV.transceive(instructV(type, id));
                byte[] randomByte = new byte[4];
                randomByte[0] = transceive[2];
                randomByte[1] = transceive[4];
                randomByte[2] = transceive[1];
                randomByte[3] = transceive[3];
                String random = getHexString(randomByte, randomByte.length);
                random = TransUtil.hexStringToBinary(random);
                random = random.substring(random.length() - 3) + random.substring(0, random.length() - 3);
                String randomHex = TransUtil.binaryString2hexString(random);
                byte[] bytesRandom = TransUtil.hexStringToBytes(randomHex);
                byte[] instruct = new byte[]{0x22, (byte) 0xc2, (byte) 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0x80, bytesRandom[3], bytesRandom[2], bytesRandom[1], bytesRandom[0]};
                System.arraycopy(id, 0, instruct, 3, id.length);
                byte[] stopResult = nfcV.transceive(instruct);
                Message message = new Message();
                message.what = type;
                message.obj = getHexString(stopResult, stopResult.length);
                if (UIUtils.getHandler() != null)
                    UIUtils.getHandler().sendMessage(message);
                SystemClock.sleep(100);
            } else {
                byte[] bytes = instructV(type, id);
                byte[] transceive = nfcV.transceive(bytes);
                String hexString = getHexString(transceive, transceive.length);
                LogUtil.d(hexString);
                Message message = new Message();
                message.what = type;
                message.obj = hexString;
                if (UIUtils.getHandler() != null)
                    UIUtils.getHandler().sendMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
            UIUtils.getHandler().sendEmptyMessage(-1);
        } finally {
            try {
                if (nfcV != null) {

                    nfcV.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void startA(Tag tag, int type) {
        NfcA nfcA = NfcA.get(tag);
        try {
            nfcA.connect();
            if (type == 28) {
                byte[] bytes1 = nfcA.transceive(instructA(28));
                byte[] bytes2 = nfcA.transceive(instructA(29));
                SystemClock.sleep(500);
                byte[] bytes3 = nfcA.transceive(instructA(30));
                byte[] bytes4 = nfcA.transceive(instructA(31));

                LogUtil.d(getHexString(bytes1, bytes1.length));
                LogUtil.d(getHexString(bytes2, bytes2.length));
                LogUtil.d(getHexString(bytes3, bytes3.length));
                LogUtil.d(getHexString(bytes4, bytes4.length));
                String hexString = getHexString(bytes3, bytes3.length);
                Message message = new Message();
                message.what = type;
                message.obj = hexString;
                if (UIUtils.getHandler() != null) {
                    UIUtils.getHandler().sendMessage(message);
                }


            } else if (type == 11) {
                byte[] bytes = instructA(11);
                byte[] transceive = nfcA.transceive(bytes);
                String hexString = getHexString(transceive, transceive.length);
                LogUtil.d(hexString);
                SystemClock.sleep(400);
                if (hexString != null && hexString.contains("FAFF")) {
                    byte[] transceive1 = nfcA.transceive(instructA(12));
                    double v = strFromat(getHexString(transceive1, transceive1.length));
                    // TODO: 2018/7/13 温度结果处理
                    if (UIUtils.getHandler() != null) {
                        Message message = new Message();
                        message.what = type;
                        message.obj = v + "℃";
                        UIUtils.getHandler().sendMessage(message);
                    }
                } else {
                    HintDialog.faileDialog(ActivityUtils.instance.getCurrentActivity(), "Error");
                }
            } else if (type == 35) {
                byte[] transceive = nfcA.transceive(instructA(type));
                byte[] randomByte = new byte[4];
                randomByte[0] = transceive[2];
                randomByte[1] = transceive[4];
                randomByte[2] = transceive[1];
                randomByte[3] = transceive[3];
                String random = getHexString(randomByte, randomByte.length);
                random = TransUtil.hexStringToBinary(random);
                random = random.substring(random.length() - 3) + random.substring(0, random.length() - 3);
                String randomHex = TransUtil.binaryString2hexString(random);
                byte[] bytesRandom = TransUtil.hexStringToBytes(randomHex);
                byte[] stopResult = nfcA.transceive(new byte[]{0x40, (byte) 0xc2, (byte) 0x80, bytesRandom[3], bytesRandom[2], bytesRandom[1], bytesRandom[0]});

                Message message = new Message();
                message.what = type;
                message.obj = getHexString(stopResult, stopResult.length);
                if (UIUtils.getHandler() != null)
                    UIUtils.getHandler().sendMessage(message);
                SystemClock.sleep(100);
            } else {
                byte[] bytes = instructA(type);
                byte[] transceive = nfcA.transceive(bytes);
                String hexString = getHexString(transceive, transceive.length);
                LogUtil.d(hexString);
                Message message = new Message();
                message.obj = hexString;
                message.what = type;
                if (UIUtils.getHandler() != null) {
                    UIUtils.getHandler().sendMessage(message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            UIUtils.getHandler().sendEmptyMessage(-1);
        } finally {
            try {
                if (nfcA != null) {
                    nfcA.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static byte[] instructA(int type) {
        byte[] bytes = null;
        switch (type) {
            case 1://唤醒指令
                bytes = new byte[]{0x40, (byte) 0xc4, 0x00, 0x00, 0x00, 0x00, 0x00};
                break;
            case 2://查看唤醒状态
                bytes = new byte[]{0x40, (byte) 0xc4, (byte) 0x80, 0x00, 0x00, 0x00, 0x00};
                break;
            case 3://配置延时测温时间 m
                bytes = new byte[]{0x40, (byte) 0xc5, (byte) 0xc0, (byte) 0x84, 0x00, (byte) SpUtils.getIntValue(MyConstant.delayTime, 0), 0x00};
                break;
            case 4://查看延时测温时间
                bytes = new byte[]{0x40, (byte) 0xc6, (byte) 0xc0, (byte) 0x84, 0x00, 0x00, 0x00};
                //                System.arraycopy(id,0,bytes,2,id.length);
                break;
            case 5://配置测温间隔时间 s
                bytes = new byte[]{0x40, (byte) 0xc5, (byte) 0xc0, (byte) 0x85, (byte) getCount(SpUtils.getIntValue(MyConstant.intervalTime, 0))[0], (byte) getCount(SpUtils.getIntValue(MyConstant.intervalTime, 1))[1], 0x00};
                break;
            case 6://查看测温间隔时间
                bytes = new byte[]{0x40, (byte) 0xc6, (byte) 0xc0, (byte) 0x85, 0x00, 0x00, 0x00};
                break;
            case 7: //开启rtc测温
                bytes = new byte[]{0x40, (byte) 0xc2, (byte) 0x00, (byte) 0x00, 0x00, 0x00, 0x00};
                break;
            case 8:// 结束rtc测温
                bytes = new byte[]{0x40, (byte) 0xc2, (byte) 0x80, (byte) 0x00, 0x00, 0x00, 0x00};
                break;
            case 9://查看状态结果
                bytes = new byte[]{0x40, (byte) 0xcf, (byte) 0x01, (byte) 0x00, 0x00, 0x00, 0x00};
                break;
            case 10: //读取rtc测温数据
                bytes = new byte[]{0x40, (byte) 0xb1, (byte) 0x10, 0x00, 0x00, (byte) 0xfa, 0x00};
                break;
            case 11: //启动实时测温
                bytes = new byte[]{0x40, (byte) 0xc0, 0x06, 0x00, 0x00, 0x00, 0x00};
                break;
            case 12: // 获取实时测温数据
                bytes = new byte[]{0x40, (byte) 0xc0, (byte) 0x84, 0x00, 0x00, 0x00, 0x00};
                break;
            case 13://设置测温次数
                bytes = new byte[]{0x40, (byte) 0xb3, (byte) 0xb0, (byte) 0x94, 0x01, 0x00, 0x00, (byte) getCount(SpUtils.getIntValue(MyConstant.tpCount, 10))[1], (byte) getCount(SpUtils.getIntValue(MyConstant.tpCount, 10))[0]};
                break;
            case 14: //查看测温次数
                bytes = new byte[]{0x40, (byte) 0xb1, (byte) 0xb0, (byte) 0x94, 0x00, (byte) 0x3, 0x00};
                break;
            case 15: //获取测温时间
                bytes = new byte[]{0x40, (byte) 0xb1, 0x00, 0x26, 0x00, 0x07, 0x00};
                break;
            case 16: // 设置最小温度值
                int min = SpUtils.getIntValue(MyConstant.tpMin, 0);
                if (min < 0) {

                    bytes = new byte[]{0x40, (byte) 0xb3, (byte) 0xb0, (byte) 0x80, 0x01, 0x00, 0x00, (byte) getCount(min * 4 + 0x400)[1], (byte) getCount(min * 4 + 0x400)[0]};

                } else {

                    bytes = new byte[]{0x40, (byte) 0xb3, (byte) 0xb0, (byte) 0x80, 0x01, 0x00, 0x00, (byte) getCount(min * 4)[1], (byte) getCount(SpUtils.getIntValue(MyConstant.tpMin, 0) * 4)[0]};

                }
                break;
            case 17: // 设置最大温度值
                int max = SpUtils.getIntValue(MyConstant.tpMax, 40);
                if (max < 0) {
                    bytes = new byte[]{0x40, (byte) 0xb3, (byte) 0xb0, (byte) 0x82, 0x01, 0x00, 0x00, (byte) getCount(max * 4 + 0x400)[1], (byte) getCount(max * 4 + 0x400)[0]};

                } else {
                    bytes = new byte[]{0x40, (byte) 0xb3, (byte) 0xb0, (byte) 0x82, 0x01, 0x00, 0x00, (byte) getCount(max * 4)[1], (byte) getCount(max * 4)[0]};

                }

                break;
            case 18: //获取最大最小温度值
                bytes = new byte[]{0x40, (byte) 0xb1, (byte) 0xb0, (byte) 0x80, 0x00, 0x03, 0x00};
                break;
            case 19: //写到block9温度大小值
                bytes = new byte[]{0x40, (byte) 0xb3, (byte) 0x00, (byte) 0x24, 0x03, 0x00, 0x00, 0x00, (byte) SpUtils.getIntValue(MyConstant.tpMin, 0), 0x00, (byte) SpUtils.getIntValue(MyConstant.tpMax, 40)};
                break;
            case 20: //写测温block10间隔
                bytes = new byte[]{0x40, (byte) 0xb3, (byte) 0x00, (byte) 0x28, 0x03, 0x00, 0x00, (byte) getCount(SpUtils.getIntValue(MyConstant.intervalTime, 0))[0], (byte) getCount(SpUtils.getIntValue(MyConstant.intervalTime, 1))[1], 0x12, 0x03};
                break;
            case 21: //读取测温间隔
                bytes = new byte[]{0x40, (byte) 0xb1, 0x00, 0x20, 0x00, 0x0b, 0x00};
                break;
            case 22: //写时间
                bytes = new byte[]{0x40, (byte) 0xb3, (byte) 0x00, (byte) 0x20, 0x03, 0x00, 0x00, (byte) getTimeHex()[0], (byte) getTimeHex()[1], (byte) getTimeHex()[2], (byte) getTimeHex()[3]};
                break;
            case 23: //读测温时间
                bytes = new byte[]{0x40, (byte) 0xb1, 0x00, 0x20, 0x00, 0x03, 0x00};
                break;
            case 24: //休眠
                bytes = new byte[]{0x40, (byte) 0xc3, 0x01, 0x00, 0x00, 0x00, 0x00};
                break;
            case 25://Init Regfile
                bytes = new byte[]{0x40, (byte) 0xce, 0x00, 0x00, 0x00, 0x00, 0x00};
                break;
            case 26: //led on
                bytes = new byte[]{0x40, (byte) 0xc9, 0x02, 0x00, 0x00, 0x00, 0x00};
                break;
            case 27: //led off
                bytes = new byte[]{0x40, (byte) 0xc9, 0x01, 0x00, 0x00, 0x00, 0x00};
                break;
            case 28: //获取电池电压第一步 配置寄存器
                bytes = new byte[]{0x40, (byte) 0xc5, (byte) 0xc0, 0x12, 0x00, 0x08, 0x00};
                break;
            case 29: //获取电池电压第二步  启动测量
                bytes = new byte[]{0x40, (byte) 0xc0, (byte) 0x12, 0x00, 0x00, 0x00, 0x00};
                break;
            case 30: //获取电池电压第三步 读取结果
                bytes = new byte[]{0x40, (byte) 0xc0, (byte) 0x92, 0x00, 0x00, 0x00, 0x00};
                break;
            case 31://获取电池电压第四步  恢复寄存器
                bytes = new byte[]{0x40, (byte) 0xc5, (byte) 0xc0, 0x12, 0x00, 0x00, 0x00};
                break;
            case 32://读C091寄存器
                bytes = new byte[]{0x40, (byte) 0xc6, (byte) 0xc0, (byte) 0x91, 0x00, 0x00, 0x00};
                break;
            case 33://读C092寄存器
                bytes = new byte[]{0x40, (byte) 0xc6, (byte) 0xc0, (byte) 0x92, 0x00, 0x00, 0x00};
                break;
            case 34://获取场强指令
                bytes = new byte[]{0x40, (byte) 0xD0, (byte) 0x00, (byte) 0x00, 0x00, 0x00, 0x00};
                break;
            case 35: //获取随机数
                bytes = new byte[]{0x40, (byte) 0xB2, (byte) 0x00, (byte) 0x00, 0x00, 0x00, 0x00};
                break;
            case 36: //读取c094寄存器
                bytes = new byte[]{0x40, (byte) 0xc6, (byte) 0xc0, (byte) 0x94, 0x00, 0x00, 0x00};
                break;
            case 37: //读取B180地址
                bytes = new byte[]{0x40, (byte) 0xb1, (byte) 0xb1, (byte) 0x80, 0x00, 0x04, 0x00};
                break;
            case 38: //读取c09a寄存器max_limit_cnt
                bytes = new byte[]{0x40, (byte) 0xc6, (byte) 0xc0, (byte) 0x9a, 0x00, 0x00, 0x00};
                break;
            case 39: //读取c09b寄存器min_limit_cnt
                bytes = new byte[]{0x40, (byte) 0xc6, (byte) 0xc0, (byte) 0x9b, 0x00, 0x00, 0x00};
                break;
            case 40: //写测温最小值
                bytes = new byte[]{0x40, (byte) 0xc5, (byte) 0xc0, (byte) 0x99, (byte) getCount(100 * 4)[0], (byte) getCount(100 * 4)[1], 0x00};
                break;
            case 41: //写测温最大值
                bytes = new byte[]{0x40, (byte) 0xc5, (byte) 0xc0, (byte) 0x98, (byte) getCount(-100 * 4 + 0x400)[0], (byte) getCount(-100 * 4 + 0x400)[1], 0x00};
                break;
            case 42: //读测温最小值
                bytes = new byte[]{0x40, (byte) 0xc6, (byte) 0xc0, (byte) 0x99, (byte) 0x00, 0x00, 0x00};
                break;
            case 43: //读测温最大值
                bytes = new byte[]{0x40, (byte) 0xc6, (byte) 0xc0, (byte) 0x98, (byte) 0x00, 0x00, 0x00};
                break;
            case 44: //读测温次数
                bytes = new byte[]{0x40, (byte) 0xb1, (byte) 0xb1, (byte) 0x88, (byte) 0x00, 0x00, 0x00};
                break;
            default:
                break;
        }
        return bytes;
    }

    private static int[] getCount(int count) {
        int[] ints = {0, 0};
        if (count <= 255) {
            ints[1] = count;
        } else {
            ints[0] = count / 256;
            ints[1] = count % 256;
        }
        return ints;
    }


    public static float strFromat(String str) {
        float resultTem = 0;
        LogUtil.d(str);
        String substring = str.substring(str.length() - 4, str.length() - 2);
        String substring1 = str.substring(str.length() - 2, str.length());
        String newstr = substring1 + substring;

        String stringToBinary = TransUtil.hexStringToBinary(newstr);
        String tempData = stringToBinary.substring(stringToBinary.length() - 10);

        String bStr = tempData.substring(1);
        String hexString = TransUtil.binaryString2hexString("0000000" + bStr);


        char[] chars = substring1.toCharArray();
        char nu = '2';
        if (chars[1] >= nu) {

            int i = -((0xffff - Integer.parseInt(newstr, 16)) & 0x03ff) - 1;
            resultTem = (float) (i / 4.00);
        } else {
            int a = Integer.parseInt(hexString, 16);
            resultTem = (float) (a / 4.00);
        }
        LogUtil.d(resultTem + "");
        return resultTem;
    }

    /**
     * byte数组转十六进制字符串
     *
     * @param bytes     数组
     * @param separator 分隔符
     * @return
     */
    public static String bytesToHexString(byte[] bytes, char separator) {
        String s = "0";
        StringBuilder hexString = new StringBuilder();
        if ((bytes != null) && (bytes.length > 0)) {
            for (byte b : bytes) {
                int n = b & 0xff;
                if (n < 0x10) {
                    hexString.append("0");
                }
                hexString.append(Integer.toHexString(n));
                if (separator != 0) {
                    hexString.append(separator);
                }
            }
            s = hexString.substring(0, hexString.length() - 1);
        }
        return s;
    }

    // Hex help
    private static final byte[] HEX_CHAR_TABLE = {(byte) '0', (byte) '1',
            (byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6',
            (byte) '7', (byte) '8', (byte) '9', (byte) 'A', (byte) 'B',
            (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F'};

    public static String getHexString(byte[] raw, int len) {
        byte[] hex = new byte[2 * len];
        int index = 0;
        int pos = 0;

        for (byte b : raw) {
            if (pos >= len)
                break;

            pos++;
            int v = b & 0xFF;
            hex[index++] = HEX_CHAR_TABLE[v >>> 4];
            hex[index++] = HEX_CHAR_TABLE[v & 0xF];
        }

        return new String(hex);
    }

    public static double getRTC(String tp) {
        double result = 0;
        if (tp != null && tp.length() == 4) {


        }
        return result;

    }

    /**
     * 将十六进制的字符串转换成二进制的字符串
     *
     * @param hexString
     * @return
     */
    public static String hexStrToBinaryStr(String hexString) {

        if (hexString == null || hexString.equals("")) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        // 将每一个十六进制字符分别转换成一个四位的二进制字符
        for (int i = 0; i < hexString.length(); i++) {
            String indexStr = hexString.substring(i, i + 1);
            String binaryStr = Integer.toBinaryString(Integer.parseInt(indexStr, 16));
            while (binaryStr.length() < 4) {
                binaryStr = "0" + binaryStr;
            }
            sb.append(binaryStr);
        }

        return sb.toString();
    }


}
