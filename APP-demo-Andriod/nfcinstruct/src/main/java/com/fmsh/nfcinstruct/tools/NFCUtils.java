package com.fmsh.nfcinstruct.tools;

import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcV;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;

import com.fmsh.nfcinstruct.BuildConfig;
import com.fmsh.nfcinstruct.callback.OnResultCallback;
import com.fmsh.nfcinstruct.utils.LogUtil;
import com.fmsh.nfcinstruct.utils.TransUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by wyj on 2018/7/6.
 */
public class NFCUtils {


    /**
     * 解析tag包含的芯片类型
     *
     * @param tag
     */
    public static void parseTag(Tag tag, Bundle bundle, OnResultCallback callback) {
        if (tag == null) {
            callback.onFailed("this tag is null");
            return;
        }
        String[] techList = tag.getTechList();
        for (String taType : techList) {
            if (taType.endsWith("NfcA")) {
                startA(tag, bundle, callback);
                break;
            }
            if (taType.endsWith("NfcV")) {
                startV(tag, bundle, callback);
                break;
            }
        }
    }

    /**
     * 14443芯片指令发送接收方法
     *
     * @param tag
     */
    public static void startA(Tag tag, Bundle bundle, OnResultCallback callback) {
        NfcA nfcA = NfcA.get(tag);
        if (nfcA != null) {
            if (!nfcA.isConnected()) {
                try {
                    nfcA.connect();
                    INfcA iNfcA = new INfcA(nfcA);
                    int position = bundle.getInt("position");
                    switch (position) {
                        case 0:
                            checkChipType(InstructMap.instructA(48), InstructMap.instructA(49), iNfcA);
                            measureBaseData(iNfcA, InstructMap.getBaseInstructList(true), callback);
                            break;
                        case 1:
                            //检测唤醒状态
                            checkWakeUp(iNfcA, InstructMap.getInstruct(true, 2), callback);
                            break;
                        case 2:
                            //休眠
                            singleInstruct(iNfcA, InstructMap.getInstruct(true, 24), callback);
                            break;
                        case 3:
                            //超高频初始化
                            singleInstruct(iNfcA, InstructMap.getInstruct(true, 25), callback);
                            break;
                        case 4:
                            //打开led
                            singleInstruct(iNfcA, InstructMap.getInstruct(true, 26), callback);
                            break;
                        case 5:
                            //关闭led
                            singleInstruct(iNfcA, InstructMap.getInstruct(true, 27), callback);
                            break;
                        case 6:
                            //查看测温状态
                            checkStatus(iNfcA, InstructMap.getInstruct(true, 9), callback);
                            break;
                        case 7:
                            //开启测温
                            startLogging(iNfcA, InstructMap.getParameterInstructList(true), bundle.getInt("loggingCount"), callback);
                            break;
                        case 8:
                            //停止测温
                            stopLogging(iNfcA, TransUtil.hexToByte(bundle.getString("pwd")), InstructMap.getStopInstruct(true), callback);
                            break;
                        case 9:
                            checkChipType(InstructMap.instructA(48), InstructMap.instructA(49), iNfcA);
                            //读取测温数据
                            boolean filed = bundle.getBoolean("filed");
                            getLoggingResult(filed, iNfcA, InstructMap.getResultInstruct(true), callback);
                            break;
                        case 10:
                            //通用指令发送接口
                            byte[] instructs = bundle.getByteArray("instruct");
                            String transceive = iNfcA.transceive(instructs);
                            callback.onResult(true, transceive);
                            break;
                        case 11:
                            //配置原始模式
                            setConfig(iNfcA, InstructMap.getConfig(true), callback);
                            break;
                        case 12:
                            //配置标准模式
                            int mode = bundle.getInt("mode");
                            configStandardMode(iNfcA, mode, InstructMap.getStandard(true), callback);
                            break;
                        case 13:
                            // 设置密码
                            String pwd = bundle.getString("pwd");
                            byte[] address = bundle.getByteArray("address");
                            settingPassword(iNfcA, TransUtil.hexToByte(pwd), address, InstructMap.instructA(54), callback);
                            break;
                        case 14:
                            //更新密码
                            String oldPwd = bundle.getString("oldPwd");
                            String newPwd = bundle.getString("newPwd");
                            updatePassword(iNfcA, TransUtil.hexToByte(oldPwd), TransUtil.hexToByte(newPwd), bundle.getByteArray("address"), InstructMap.getUpdatePassword(true), callback);
                            break;
                        case 15:
                            switchStorageMode(iNfcA, bundle, callback);
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(e.getMessage());
                } finally {
                    try {
                        nfcA.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 15693芯片指令发送接收方法
     *
     * @param tag
     */
    public static void startV(Tag tag, Bundle bundle, OnResultCallback callback) {
        NfcV nfcV = NfcV.get(tag);
        if (nfcV != null) {
            if (!nfcV.isConnected()) {
                try {
                    nfcV.connect();
                    INfcV iNfcV = new INfcV(nfcV);
                    int position = bundle.getInt("position");
                    switch (position) {
                        case 0:
                            checkChipType(InstructMap.instructV(48), InstructMap.instructV(49), iNfcV);
                            measureBaseData(iNfcV, InstructMap.getBaseInstructList(false), callback);
                            break;
                        case 1:
                            checkWakeUp(iNfcV, InstructMap.getInstruct(false, 2), callback);
                            break;
                        case 2:
                            singleInstruct(iNfcV, InstructMap.getInstruct(false, 24), callback);
                            break;
                        case 3:
                            singleInstruct(iNfcV, InstructMap.getInstruct(false, 25), callback);
                            break;
                        case 4:
                            //打开led
                            singleInstruct(iNfcV, InstructMap.getInstruct(false, 26), callback);
                            break;
                        case 5:
                            //关闭led
                            singleInstruct(iNfcV, InstructMap.getInstruct(false, 27), callback);
                            break;
                        case 6:
                            //查看测温状态
                            checkStatus(iNfcV, InstructMap.getInstruct(false, 9), callback);
                            break;
                        case 7:
                            //开启测温
                            startLogging(iNfcV, InstructMap.getParameterInstructList(false), bundle.getInt("loggingCount"), callback);
                            break;
                        case 8:
                            //停止测温
                            stopLogging(iNfcV, TransUtil.hexToByte(bundle.getString("pwd")), InstructMap.getStopInstruct(false), callback);
                            break;
                        case 9:
                            //读取测温数据
                            checkChipType(InstructMap.instructV(48), InstructMap.instructV(49), iNfcV);
                            boolean filed = bundle.getBoolean("filed");
                            getLoggingResult(filed, iNfcV, InstructMap.getResultInstruct(false), callback);
                            break;
                        case 10:
                            //通用指令发送接口
                            byte[] instructs = bundle.getByteArray("instruct");
                            String transceive = iNfcV.transceive(instructs);
                            callback.onResult(true, transceive);
                            break;
                        case 11:
                            //配置原始模式
                            setConfig(iNfcV, InstructMap.getConfig(false), callback);
                            break;
                        case 12:
                            //配置标准模式
                            int mode = bundle.getInt("mode");
                            configStandardMode(iNfcV, mode, InstructMap.getStandard(false), callback);
                            break;
                        case 13:
                            // 设置密码
                            String pwd = bundle.getString("pwd");
                            byte[] address = bundle.getByteArray("address");
                            settingPassword(iNfcV, TransUtil.hexToByte(pwd), address, InstructMap.instructV(54), callback);
                            break;
                        case 14:
                            //更新密码
                            String oldPwd = bundle.getString("oldPwd");
                            String newPwd = bundle.getString("newPwd");
                            updatePassword(iNfcV, TransUtil.hexToByte(oldPwd), TransUtil.hexToByte(newPwd), bundle.getByteArray("address"), InstructMap.getUpdatePassword(false), callback);
                            break;
                        case 15:
                            switchStorageMode(iNfcV, bundle, callback);
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(e.getMessage());
                } finally {
                    try {
                        nfcV.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }


    /**
     * 切换温度数据存储模式
     * 0 正常数据存储模式 1压缩存储模式 ,2原始数据存储模式,3 limit2模式
     * limit2 模式下的 limit0,1,2模式
     *
     * @param baseNfc
     * @param bundle
     * @param callback
     * @throws Exception
     */
    private static void switchStorageMode(BaseNfc baseNfc, Bundle bundle, OnResultCallback callback) throws Exception {
        int mode = bundle.getInt("mode");
        byte[] rev;
        if (baseNfc instanceof INfcV) {
            rev = new byte[]{(byte) 0x01, 0, 0};
        } else {
            rev = new byte[]{(byte) 0x01, 0, 0, 0, 0};
        }
        byte[] bytesFlag = baseNfc.send((byte) 0xcf, rev);
        int flag = bytesFlag[2] & 0x10;
        if (flag == 16) {
            callback.onResult(false);
            return;
        }
        byte[] sendCommand = new byte[5];
        byte[] bytes = baseNfc.readMemory(new byte[]{(byte) 0xb0, 0x40}, 3);
        System.arraycopy(bytes, 0, sendCommand, sendCommand.length - bytes.length, bytes.length);
        switch (mode) {
            case 0:
                sendCommand[1] = (byte) ((sendCommand[1] & (byte) 0xE3) | (byte) 0xC);
                break;
            case 1:
                sendCommand[1] = (byte) ((sendCommand[1] & (byte) 0xE3) | (byte) 0x04);
                break;
            case 2:
                sendCommand[1] = (byte) ((sendCommand[1] & (byte) 0xE3) | (byte) 0x1C);
                break;
            case 3:
                sendCommand[1] = (byte) ((sendCommand[1] & (byte) 0xE3) | (byte) 0x18);
                break;
            default:
                break;
        }
        byte[] writeMemory = baseNfc.writeMemory(new byte[]{(byte) 0xb0, 0x40}, new byte[]{sendCommand[1], (byte) ~sendCommand[1], sendCommand[3], (byte) ~sendCommand[3]});
        String toHex = TransUtil.byteToHex(writeMemory);
        if ("000000".equals(toHex)) {
            // 成功

            if (mode == 3) {
                updateLimitMode(baseNfc, bundle.getIntegerArrayList("limit"), callback);
            } else {
                callback.onResult(true);
            }
        } else {
            //失败
            callback.onResult(false, TransUtil.byteToHex(new byte[]{(byte) 0xb0, 0x40, 0x03, sendCommand[1], (byte) ~sendCommand[1], sendCommand[3], (byte) ~sendCommand[3]}), toHex);
        }
    }

    /**
     * 修改limit模式数据
     */
    private static void updateLimitMode(BaseNfc baseNfc, ArrayList<Integer> limitArray, OnResultCallback callback) throws IOException {
        if (limitArray != null && !limitArray.isEmpty()) {
            byte[] address = new byte[]{(byte) 0x80, (byte) 0x84, (byte) 0x88};
            for (int i = 0; i < limitArray.size() / 2; i++) {
                byte byte1, byte2, byte3, byte4;
                if (limitArray.get(i * 2) < 0) {
                    byte1 = (byte) ((limitArray.get(i * 2) * 4 + 0x400) & 0xff);
                    byte2 = (byte) ((limitArray.get(i * 2) * 4 + 0x400) >> 8 & 0xff);
                } else {
                    byte1 = (byte) ((limitArray.get(i * 2) * 4) & 0xff);
                    byte2 = (byte) ((limitArray.get(i * 2) * 4) >> 8 & 0xff);
                }
                if (limitArray.get(i * 2 + 1) < 0) {
                    byte3 = (byte) ((limitArray.get(i * 2 + 1) * 4 + 0x400) & 0xff);
                    byte4 = (byte) ((limitArray.get(i * 2 + 1) * 4 + 0x400) >> 8 & 0xff);
                } else {
                    byte3 = (byte) ((limitArray.get(i * 2 + 1) * 4) & 0xff);
                    byte4 = (byte) ((limitArray.get(i * 2 + 1) * 4) >> 8 & 0xff);
                }

                byte[] data = new byte[]{byte1, byte2, byte3, byte4};
                byte[] bytes = baseNfc.writeMemory(new byte[]{(byte) 0xb0, address[i]}, data);
                String toHex = TransUtil.byteToHex(bytes);
                LogUtil.d(toHex);
                if (Integer.parseInt(toHex, 16) != 0) {
                    callback.onResult(false);
                    return;
                }
            }
            callback.onResult(true);

        } else {
            callback.onResult(false);
        }

    }


    /**
     * 获取场强,温度,电压
     *
     * @param baseNfc
     * @param callback
     * @throws IOException
     */
    private static void measureBaseData(BaseNfc baseNfc, List<byte[]> byteList, OnResultCallback callback) throws Exception {
        String[] response = new String[3];
        //温度
        byte[] bytes = byteList.get(1);
        String byteToHex = baseNfc.transceive(bytes);
        SystemClock.sleep(400);
        if (byteToHex.contains("FAFF")) {
            double temp = singleTemp(baseNfc.transceive(byteList.get(2)).substring(2));
            response[1] = String.valueOf(temp);
        } else {
            callback.onResult(false);
            return;
        }

        //场强
        String fieldStrength = baseNfc.transceive(byteList.get(0));
        fieldStrength = fieldStrength.substring(fieldStrength.length() - 3, fieldStrength.length() - 2);
        int fieldStrengthNumber = Integer.parseInt(fieldStrength, 16);

        response[0] = String.valueOf(fieldStrengthNumber);


        //电压
        String transceive = baseNfc.transceive(byteList.get(3));
        byte[] toByte = TransUtil.hexToByte(transceive);
        if ((toByte[2] & (byte) 0x01) == 1) {

            baseNfc.transceive(byteList.get(4));
            baseNfc.transceive(byteList.get(5));
            SystemClock.sleep(500);
            String voltage = baseNfc.transceive(byteList.get(6));
            baseNfc.transceive(byteList.get(7));
            voltage = voltage.substring(voltage.length() - 2) + voltage.substring(voltage.length() - 4, voltage.length() - 2);
            double i = Integer.parseInt(voltage, 16) / 8192.00 * 2.5;
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            response[2] = decimalFormat.format(i);
        } else {
            response[2] = "0.00";
        }
        callback.onResult(true, response);

    }

    /**
     * 检测唤醒转态
     *
     * @param baseNfc
     * @param bytes
     * @param callback
     * @throws IOException
     */
    private static void checkWakeUp(BaseNfc baseNfc, byte[] bytes, OnResultCallback callback) throws Exception {
        String transceive = baseNfc.transceive(bytes);
        if ("005555".equals(transceive)) {
            //已唤醒状态
            callback.onResult(true);
        } else {
            //未唤醒状态
            callback.onResult(false);
        }

    }

    /**
     * 单条指令
     *
     * @param baseNfc
     * @param bytes
     * @param callback
     * @throws IOException
     */
    private static void singleInstruct(BaseNfc baseNfc, byte[] bytes, OnResultCallback callback) throws Exception {
        String transceive = baseNfc.transceive(bytes);
        if ("000000".equals(transceive)) {
            // 成功
            callback.onResult(true);
        } else {
            //失败
            callback.onResult(false, TransUtil.byteToHex(bytes), transceive);
        }
    }

    private static void setConfig(BaseNfc baseNfc, List<byte[]> byteList, OnResultCallback callback) throws IOException {
        if (baseNfc instanceof INfcV) {

            byte[] bytesFlag = baseNfc.sendCommand(new byte[]{0x02, (byte) 0xcf, (byte) 0x1d, (byte) 0x01, 0, 0});
            int flag = bytesFlag[2] & 0x10;
            if (flag == 16) {
                callback.onResult(false);
                return;
            }
        } else {
            //测温流程中不能改变模式
            byte[] bytesFlag = baseNfc.sendCommand(new byte[]{0x40, (byte) 0xcf, (byte) 0x01, (byte) 0x00, 0x00, 0x00, 0x00});
            int flag = bytesFlag[2] & 0x10;
            if (flag == 16) {
                callback.onResult(false);
                return;
            }
        }
        byte[] config = baseNfc.sendCommand(byteList.get(0));
        byte value = (byte) (config[config.length - 4] | 0x1C);
        byte[] send = byteList.get(1);
        if (send.length > 11) {
            send[send.length - 4] = value;
            send[send.length - 3] = (byte) ~value;
            send[send.length - 2] = config[config.length - 2];
            send[send.length - 1] = (byte) ~config[config.length - 2];

        } else {
            send[7] = value;
            send[8] = (byte) ~value;
            send[9] = config[config.length - 2];
            send[10] = (byte) ~config[config.length - 2];
        }
        String transceive = baseNfc.transceive(send);

        if ("000000".equals(transceive)) {
            // 成功
            callback.onResult(true);
        } else {
            //失败
            callback.onResult(false, TransUtil.byteToHex(send), transceive);
        }


    }

    /**
     * 查看测温状态
     *
     * @param baseNfc
     * @param bytes
     * @param callback
     * @throws IOException
     */
    private static void checkStatus(BaseNfc baseNfc, byte[] bytes, OnResultCallback callback) throws Exception {
        String transceive = baseNfc.transceive(bytes);
        byte[] bytesFlag = TransUtil.hexToByte(transceive);
        int flag = bytesFlag[2] & 0x10;
        if (flag == 16) {
            //处于测温中
            callback.onResult(true);
        } else {
            //非测温中
            callback.onResult(false);
        }
    }


    /**
     * 开启测温
     *
     * @param baseNfc
     * @param byteList
     * @param loggingCount 测温次数
     * @param callback
     * @throws IOException
     */
    private static void startLogging(BaseNfc baseNfc, List<byte[]> byteList, int loggingCount, OnResultCallback callback) throws Exception {

        String transceive = baseNfc.transceive(byteList.get(0));
        byte[] bytesFlag = TransUtil.hexToByte(transceive);
        int flag = bytesFlag[2] & 0x10;
        if (flag == 16) {
            callback.onResult(false, "IN RTC Flow Status");
        } else {

            // mode = 3 正常模式 4byte一个温度数据 mode=1 压缩模式 4byte 3个温度数据 mode =6 limit2模式 4byte 8个温度数据 mode=7 原始模式4byte2个温度数据
            int mode;
            if (baseNfc instanceof INfcV) {
                byte[] sendCommand = baseNfc.sendCommand(new byte[]{0x02, (byte) 0xb1, (byte) 0x1d, (byte) 0xb0, 0x40, 0, 0});
                mode = (sendCommand[sendCommand.length - 4] >> 2) & (byte) 0x07;
            } else {
                byte[] sendCommand = baseNfc.sendCommand(new byte[]{0x40, (byte) 0xb1, (byte) 0xb0, 0x40, 0, 0, 0});
                mode = (sendCommand[sendCommand.length - 4] >> 2) & (byte) 0x07;
            }
            if (mode == 3 && loggingCount > 4864) {
                callback.onResult(false, "0");
                return;
            }
            if (mode == 7 && loggingCount > 9728) {
                callback.onResult(false, "0");
                return;
            }
            if (mode == 1 && loggingCount > 14592) {
                callback.onResult(false, "0");
                return;
            }


            baseNfc.transceive(byteList.get(1));
            String wakeStatus = baseNfc.transceive(byteList.get(2));
            if ("005555".equals(wakeStatus)) {
                for (int i = 3; i < 14; i++) {

                    String data = baseNfc.transceive(byteList.get(i));
                    int parseInt = Integer.parseInt(data, 16);
                    if (parseInt != 0) {
                        callback.onResult(false, TransUtil.byteToHex(byteList.get(i)), data);
                        return;
                    }
                    if (i == 3 || i == 4) {
                        //查看延时时间配置
                        if (!check(baseNfc, byteList.get(i))) {
                            callback.onResult(false, TransUtil.byteToHex(byteList.get(i)), data);
                            return;
                        }
                    }
                }
                callback.onResult(true);
            } else {
                callback.onResult(false, "Wakeup fail");
            }
        }

    }

    /**
     * 检测延迟时间和时间间隔是否写入
     *
     * @param baseNfc
     * @param data
     * @return
     * @throws IOException
     */
    private static boolean check(BaseNfc baseNfc, byte[] data) throws IOException {
        if (data.length > 7) {
            byte[] readReg = baseNfc.readReg(new byte[]{data[data.length - 4], data[data.length - 3]});
            if (readReg[readReg.length - 1] == data[data.length - 2] && readReg[readReg.length - 2] == data[data.length - 1]) {
                return true;
            }
            for (int i = 0; i < 3; i++) {
                baseNfc.sendCommand(data);
            }
            readReg = baseNfc.readReg(new byte[]{data[data.length - 4], data[data.length - 3]});
            if (readReg[readReg.length - 1] == data[data.length - 2] && readReg[readReg.length - 2] == data[data.length - 1]) {
                return true;
            }
            return false;
        } else {
            byte[] readReg = baseNfc.readReg(new byte[]{data[2], data[3]});
            if (readReg[readReg.length - 1] == data[data.length - 3] && readReg[readReg.length - 2] == data[data.length - 2]) {
                return true;
            }
            for (int i = 0; i < 3; i++) {
                baseNfc.sendCommand(data);
            }
            readReg = baseNfc.readReg(new byte[]{data[2], data[3]});
            if (readReg[readReg.length - 1] == data[data.length - 3] && readReg[readReg.length - 2] == data[data.length - 2]) {
                return true;
            }
            return false;
        }

    }

    /**
     * 停止测温
     *
     * @param baseNfc
     * @param byteList
     * @param callback
     * @throws IOException
     */
    private static void stopLogging(BaseNfc baseNfc, byte[] pwd, List<byte[]> byteList, OnResultCallback callback) throws Exception {
        //        baseNfc.transceive(new byte[]{0x22, (byte) 0xc5, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xc0, 0x0a, 0x00, 0x07});

        byte[] bytes1 = encryptionPwd(baseNfc, pwd, byteList);
        boolean authPwd = authPwd(baseNfc, bytes1, byteList);
        if (authPwd) {
            byte[] bytes = byteList.get(3);
            for (int i = 3; i >= 0; i--) {
                bytes[bytes.length - i - 1] = bytes1[i];
            }
            byte[] transceive1 = baseNfc.sendCommand(bytes);
            SystemClock.sleep(100);
            if ((transceive1[1] & 0x02) != 2) {
                callback.onResult(true);
            } else {
                callback.onResult(false);
            }
        } else {
            callback.onResult(false);
        }


    }

    /**
     * 获取测温数据和各种参数
     *
     * @param baseNfc
     * @param byteList
     * @param callback
     * @throws Exception
     */
    private static void getLoggingResult(boolean isFiled, BaseNfc baseNfc, List<byte[]> byteList, OnResultCallback callback) throws Exception {
        //电池状态异常
        byte[] toByte = TransUtil.hexToByte(baseNfc.transceive(byteList.get(12)));
        if ((toByte[2] & (byte) 0x01) != 1) {
            callback.onResult(false, new String[]{"2"});
            return;
        }
        String sizeString = baseNfc.transceive(byteList.get(0));
        //存储的数据区域大小
        int size = Integer.parseInt(sizeString.substring(sizeString.length() - 2), 16) * 1024;

        //测温次数
        String strCount = baseNfc.transceive(byteList.get(1));
        int tpCount = Integer.parseInt(strCount.substring(strCount.length() - 6, strCount.length() - 4) + strCount.substring(strCount.length() - 8, strCount.length() - 6), 16);

        //测温间隔时间和测温延时时间
        String times = baseNfc.transceive(byteList.get(2));
        if (times.length() == 24) {
            times = "00" + times;
        }
        //延时时间
        String delayTime = String.valueOf(Integer.parseInt(times.substring(2, 6), 16));
        //测温间隔时间
        String intervalTime = String.valueOf(Integer.parseInt(times.substring(10, 14), 16));

        String startTimeHex = baseNfc.transceive(byteList.get(3));
        if (startTimeHex.length() == 8) {
            startTimeHex = "00" + startTimeHex;
        }
        String startTime = String.valueOf(Long.parseLong(startTimeHex.substring(2), 16));

        //查看温度数据配置
        byte[] sendCommand = baseNfc.sendCommand(byteList.get(byteList.size() - 1));
        // mode = 3 正常模式 4byte一个温度数据 mode=1 压缩模式 4byte 3个温度数据 mode = 7 原始数据模式 一个block 2个温度数据
        int mode = (sendCommand[sendCommand.length - 4] >> 2) & (byte) 0x07;
        if (mode == 6) {
            parseLimit2ModeData(baseNfc, size, tpCount, delayTime, intervalTime, startTime, callback);
            return;
        }

        //最大,最小温度值
        String temp = baseNfc.transceive(byteList.get(4));
        float currentSettingMinTemp = NFCUtils.strFromat(true, temp.substring(temp.length() - 8, temp.length() - 4));
        float currentSettingMaxTemp = NFCUtils.strFromat(true, temp.substring(temp.length() - 4));

        //读取温度超限次数
        String limitCount = baseNfc.transceive(byteList.get(5));
        String maxWithMin = "";
        if (Integer.parseInt(limitCount, 16) == 0) {
            maxWithMin = baseNfc.transceive(byteList.get(6));
        } else {
            String strMax = baseNfc.transceive(byteList.get(7));
            String strMin = baseNfc.transceive(byteList.get(8));
            String tepMin = baseNfc.transceive(byteList.get(9));
            String tepMax = baseNfc.transceive(byteList.get(10));
            maxWithMin = strMax + "with" + strMin + "with" + tepMax + tepMin;
        }
        //超过最高温度次数
        int overMaxLimit;
        // 超过最低温度次数
        int overMinLimit;
        //最高温度
        float maxTep = 0;
        //最低温度
        float minTep = 0;
        //        if (maxWithMin.contains("with")) {
        //            String[] withs = maxWithMin.split("with");
        //            overMaxLimit = Integer.parseInt(withs[0].substring(4, 6) + withs[0].substring(2, 4), 16);
        //            overMinLimit = Integer.parseInt(withs[1].substring(4, 6) + withs[1].substring(2, 4), 16);
        //            maxTep = NFCUtils.strFromat(withs[2].substring(0, 6));
        //            minTep = NFCUtils.strFromat(withs[2].substring(6));
        //        } else {
        //            String strMax = maxWithMin.substring(maxWithMin.length() - 8, maxWithMin.length() - 4);
        //            String strMin = maxWithMin.substring(maxWithMin.length() - 4);
        //            overMaxLimit = Integer.parseInt(strMax.substring(2) + strMax.substring(0, 2), 16);
        //            overMinLimit = Integer.parseInt(strMin.substring(2) + strMin.substring(0, 2), 16);
        //            String strMaxTep = "00" + maxWithMin.substring(maxWithMin.length() - 16, maxWithMin.length() - 12);
        //            String strMinTep = "00" + maxWithMin.substring(maxWithMin.length() - 12, maxWithMin.length() - 8);
        //            maxTep = NFCUtils.strFromat(strMaxTep);
        //            minTep = NFCUtils.strFromat(strMinTep);
        //
        //        }


        byte[] toByte1 = TransUtil.hexToByte(baseNfc.transceive(byteList.get(12)));
        int intFlag = toByte1[2] & 0x10;
        int allCount = tpCount;
        String hexCount = "";
        int currentCount = 0;
        // 测温状态
        String tempStatus = "0";


        //正在测量中
        if (intFlag == 16) {
            hexCount = baseNfc.transceive(byteList.get(13));
            tpCount = Integer.parseInt(hexCount.substring(4) + hexCount.substring(2, 4), 16);
            tempStatus = "1";
            currentCount = tpCount;

        } else {
            //测量停止
            hexCount = baseNfc.transceive(byteList.get(14));
            tpCount = Integer.parseInt(hexCount.substring(hexCount.length() - 6, hexCount.length() - 4) + hexCount.substring(hexCount.length() - 8, hexCount.length() - 6), 16);
            tempStatus = "3";
            int pointer = Integer.parseInt(hexCount.substring(hexCount.length() - 4, hexCount.length() - 2), 16);
            if (chipType) {
                if (pointer == 0) {
                    currentCount = (tpCount + 1) * 2 - 1;
                } else {
                    currentCount = (tpCount + 1) * 2;
                }
            } else {
                if (mode == 3) {
                    currentCount = tpCount + 1;
                } else {
                    currentCount = tpCount * 3 + pointer + 1;
                }
            }
        }
        long currentTimeMillis = System.currentTimeMillis();
        long timeDifference = currentTimeMillis - Long.parseLong(startTime) * 1000;
        long valueTime = Integer.parseInt(delayTime) * 60 * 1000 + Integer.parseInt(intervalTime) * 1000;
        if (timeDifference > valueTime && currentCount == 0) {
            callback.onResult(false, new String[]{"2"});
            return;
        }
        int readSize;
        if (chipType) {
            readSize = currentCount * 2;
        } else {
            if (mode == 3) {
                readSize = currentCount * 4;
            } else {
                if (currentCount % 3 == 0) {
                    readSize = currentCount / 3 * 4;
                } else {
                    readSize = (currentCount / 3 + 1) * 4;
                }
            }
        }

        String tempData = "";
        if (readSize > size) {

            tempData = readData(baseNfc, size);
        } else if (readSize > 0) {

            tempData = readData(baseNfc, readSize);
        }
        if (tempData.length() > readSize * 2) {
            tempData = tempData.substring(0, readSize * 2);
        }
        List<Float> list = new ArrayList<>();
        List<String> strList = new ArrayList<>();
        int tempSize = 8;
        if (chipType) {
            tempSize = 4;
        } else {
            tempSize = 8;
        }
        for (int i = 0; i < tempData.length(); i += tempSize) {

            if (mode == 3 || chipType) {
                String data = tempData.substring(i, i + 4);
                float tp = NFCUtils.formatTemp(data);
                list.add(tp);
                if (!chipType && isFiled) {
                    int filed = parseField(data);
                    strList.add(tp + ":" + filed);
                } else {
                    strList.add(String.valueOf(tp));
                }
            } else {
                String data = tempData.substring(i, i + 8);
                formatCompressTemp(list, strList, data);
            }

        }
        if (mode == 1) {
            int size1 = list.size();
            // 移除多余的数据
            for (int i = 0; i < size1 - currentCount; i++) {
                list.remove(list.size() - 1);
                strList.remove(strList.size() - 1);
            }
        }

        if (currentCount != allCount && !"1".equals(tempStatus)) {
            tempStatus = "2";
            //异常停止
            if (currentCount > 1) {
                currentCount--;
                list.remove(list.size() - 1);
                strList.remove(strList.size() - 1);
            }
        }
        if (list.size() == 0 && !"0".equals(delayTime)) {
            tempStatus = "0";
        }
        overMaxLimit = 0;
        overMinLimit = 0;

        if (list.size() != 0) {
            maxTep = Collections.max(list);
            minTep = Collections.min(list);
        }
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) > currentSettingMaxTemp) {
                overMaxLimit++;
            }
            if (list.get(i) < currentSettingMinTemp) {
                overMinLimit++;
            }
        }

        Object[] tempArr = strList.toArray();
        String[] response = new String[12 + tempArr.length];
        System.arraycopy(tempArr, 0, response, 12, tempArr.length);

        response[0] = tempStatus;
        response[1] = startTime;
        response[2] = String.valueOf(allCount);
        response[3] = String.valueOf(currentCount);
        response[4] = delayTime;
        response[5] = intervalTime;
        response[6] = String.valueOf(minTep);
        response[7] = String.valueOf(maxTep);
        response[8] = String.valueOf(currentSettingMinTemp);
        response[9] = String.valueOf(currentSettingMaxTemp);
        response[10] = String.valueOf(overMinLimit);
        response[11] = String.valueOf(overMaxLimit);
        callback.onResult(true, response);

    }

    private static void parseLimit2ModeData(BaseNfc baseNfc, int size, int tpCount, String delayTime, String intervalTime, String startTime, OnResultCallback callback) throws Exception {
        String limit0 = TransUtil.byteToHex(baseNfc.readMemory(new byte[]{(byte) 0xb0, (byte) 0x80}, 3));
        String limit1 = TransUtil.byteToHex(baseNfc.readMemory(new byte[]{(byte) 0xb0, (byte) 0x84}, 3));
        String limit2 = TransUtil.byteToHex(baseNfc.readMemory(new byte[]{(byte) 0xb0, (byte) 0x88}, 3));
        float minLimit0 = NFCUtils.strFromat(true, limit0.substring(limit0.length() - 8, limit0.length() - 4));
        float minLimit1 = NFCUtils.strFromat(true, limit1.substring(limit1.length() - 8, limit1.length() - 4));
        float minLimit2 = NFCUtils.strFromat(true, limit2.substring(limit2.length() - 8, limit2.length() - 4));
        float maxLimit0 = NFCUtils.strFromat(true, limit0.substring(limit0.length() - 4));
        float maxLimit1 = NFCUtils.strFromat(true, limit1.substring(limit1.length() - 4));
        float maxLimit2 = NFCUtils.strFromat(true, limit2.substring(limit2.length() - 4));
        byte[] rev;
        if (baseNfc instanceof INfcV) {
            rev = new byte[]{(byte) 0x01, 0, 0};
        } else {
            rev = new byte[]{(byte) 0x01, 0, 0, 0, 0};
        }
        String tempStatus = "0";
        int allCount = tpCount;
        byte[] bytesFlag = baseNfc.send((byte) 0xcf, rev);
        int flag = bytesFlag[2] & 0x10;
        String hexCount;
        int currentCount = 0;
        // 测量中
        if (flag == 16) {
            hexCount = TransUtil.byteToHex(baseNfc.readReg(new byte[]{(byte) 0xc0, (byte) 0x91}));
            tpCount = Integer.parseInt(hexCount.substring(4) + hexCount.substring(2, 4), 16);
            currentCount = tpCount;
            tempStatus = "1";
        } else {
            hexCount = TransUtil.byteToHex(baseNfc.readMemory(new byte[]{(byte) 0xb1, (byte) 0x88}, 3));
            tpCount = Integer.parseInt(hexCount.substring(hexCount.length() - 6, hexCount.length() - 4) + hexCount.substring(hexCount.length() - 8, hexCount.length() - 6), 16);
            int pointer = Integer.parseInt(hexCount.substring(hexCount.length() - 4, hexCount.length() - 2), 16);
            tempStatus = "3";
            currentCount = tpCount * 8 + pointer + 1;

        }
        int readSize = 0;
        if (currentCount % 8 == 0) {
            readSize = currentCount / 8 * 4;
        } else {
            readSize = (currentCount / 8 + 1) * 4;
        }

        String tempData = "";
        if (readSize > size) {

            tempData = readData(baseNfc, size);
        } else if (readSize > 0) {

            tempData = readData(baseNfc, readSize);
        }
        if (tempData.length() > readSize * 2) {
            tempData = tempData.substring(0, readSize * 2);
        }
        ArrayList<String> recordPoint = new ArrayList<>();
        byte[] bytes = TransUtil.hexToByte(tempData);

        for (int i = 0; i < bytes.length; i++) {
            recordPoint.add(String.valueOf(bytes[i] & 0x07));
            recordPoint.add(String.valueOf(bytes[i] >> 4 & 0x07));
        }
        int size1 = recordPoint.size();
        for (int i = 0; i < size1 - currentCount; i++) {
            recordPoint.remove(recordPoint.size() - 1);
        }

        if (currentCount != allCount && !"1".equals(tempStatus)) {
            tempStatus = "2";
            //异常停止
            if (currentCount > 1) {
                currentCount--;
                recordPoint.remove(recordPoint.size() - 1);
            }
        }
        if (recordPoint.size() == 0 && !"0".equals(delayTime)) {
            tempStatus = "0";
        }

        Object[] tempArr = recordPoint.toArray();
        String[] response = new String[7 + tempArr.length];
        System.arraycopy(tempArr, 0, response, 7, tempArr.length);
        response[0] = tempStatus;
        response[1] = "limit2";
        response[2] = currentCount + "/" + allCount;
        response[3] = minLimit0 + ";" + maxLimit0 + ";" + minLimit1 + ";" + maxLimit1 + ";" + minLimit2 + ";" + maxLimit2;
        response[4] = delayTime;
        response[5] = intervalTime;
        response[6] = startTime;
        callback.onResult(true, response);

    }

    /**
     * 读取温度数据
     *
     * @param baseNfc
     * @param size
     * @return
     * @throws Exception
     */
    private static String readData(BaseNfc baseNfc, int size) throws Exception {
        int address = 4096;
        // 每次读取会多出 4byte,所以计算次数要加248+4
        int count = size / 252;
        int percent = size % 252;
        StringBuffer buffer = new StringBuffer();
        if (size < 252) {
            String readData = TransUtil.byteToHex(baseNfc.readMemory(new byte[]{(byte) 0x10, (byte) 0x00}, percent));

            if (baseNfc instanceof INfcV) {

                buffer.append(readData.substring(2));
            } else {
                buffer.append(readData);
            }
        }

        for (int i = 0; i < count; i++) {
            if (i != 0) {
                address = address + 252;
            }
            String readData = TransUtil.byteToHex(baseNfc.readMemory(new byte[]{(byte) ((address >> 8) & 0xff), (byte) (address & 0xff)}, 248));

            if (baseNfc instanceof INfcV) {
                buffer.append(readData.substring(2));
            } else {
                buffer.append(readData);
            }
        }
        if (count != 0 && percent != 0) {
            address = address + 252;
            String readData = TransUtil.byteToHex(baseNfc.readMemory(new byte[]{(byte) ((address >> 8) & 0xff), (byte) (address & 0xff)}, (byte) (percent - 4)));

            if (baseNfc instanceof INfcV) {
                buffer.append(readData.substring(2));
            } else {
                buffer.append(readData);
            }
        }
        return buffer.toString();
    }


    /**
     * 配置标准模式
     *
     * @param baseNfc
     * @param mode     0代表设置为2个小数点 1代表设置3个小数点
     * @param byteList
     * @param callback
     * @throws Exception
     */
    private static void configStandardMode(BaseNfc baseNfc, int mode, List<byte[]> byteList, OnResultCallback callback) throws Exception {

        byte[] rev;
        if (baseNfc instanceof INfcV) {
            rev = new byte[]{(byte) 0x01, 0, 0};
        } else {
            rev = new byte[]{(byte) 0x01, 0, 0, 0, 0};
        }
        byte[] bytesFlag = baseNfc.send((byte) 0xcf, rev);
        int flag = bytesFlag[2] & 0x10;
        if (flag == 16) {
            callback.onResult(false);
            return;
        }
        byte[] sendCommand = new byte[5];
        byte[] bytes = baseNfc.readMemory(new byte[]{(byte) 0xb0, 0x40}, 3);
        System.arraycopy(bytes, 0, sendCommand, sendCommand.length - bytes.length, bytes.length);
        if (mode == 0) {
            sendCommand[1] = (byte) (sendCommand[1] & (byte) 0x7f);
        } else {
            sendCommand[1] = (byte) (sendCommand[1] | (byte) 0x80);
        }

        byte[] writeMemory = baseNfc.writeMemory(new byte[]{(byte) 0xb0, 0x40}, new byte[]{sendCommand[1], (byte) ~sendCommand[1], sendCommand[3], (byte) ~sendCommand[3]});
        String toHex = TransUtil.byteToHex(writeMemory);
        if ("000000".equals(toHex)) {
            // 成功
            callback.onResult(true);
        } else {
            //失败
            callback.onResult(false, TransUtil.byteToHex(new byte[]{(byte) 0xb0, 0x40, 0x03, sendCommand[1], (byte) ~sendCommand[1], sendCommand[3], (byte) ~sendCommand[3]}), toHex);
        }
    }


    private static float detA;
    private static float detB;
    private static float offset = 0.0F;
    private static boolean chipType = false;  // true 原始数据标志
    private static boolean standard = false;

    private static void checkChipType(byte[] bytes1, byte[] bytes2, BaseNfc baseNfc) throws Exception {
        byte[] userCfg = baseNfc.sendCommand(bytes1);
        byte cfg = userCfg[userCfg.length - 4];
        LogUtil.d("cfg", Byte.toString(cfg));
        int mode = (cfg >> 7) & 0x01;
        LogUtil.d("binary", mode);
        if (mode == 1) {
            // 三个小数点显示
            standard = false;
        } else {
            //两位小数点小数点显示
            standard = true;
        }
        int flag = (cfg >> 2) & 0x7;
        if (flag == 7) {
            //通过readmemery命令获取eepoom区中的vdeta，vdetb和offset
            chipType = true;
            String vet = baseNfc.transceive(bytes2);

            offset = Integer.parseInt(reverse(vet.substring(vet.length() - 12, vet.length() - 8)), 16);
            detA = Integer.parseInt(reverse(vet.substring(vet.length() - 8, vet.length() - 4)), 16);
            detB = Integer.parseInt(reverse(vet.substring(vet.length() - 4)), 16) - 65536;
            if (offset > 10000) {
                offset = offset - 65536;
            }
        } else {
            chipType = false;
        }
    }


    /**
     * 设置密码
     *
     * @param baseNfc
     * @param pwd      4字节密码
     * @param address  地址值
     * @param send
     * @param callback
     * @throws Exception
     */
    private static void settingPassword(BaseNfc baseNfc, byte[] pwd, byte[] address, byte[] send, OnResultCallback callback) throws Exception {
        for (int i = 3; i >= 0; i--) {
            send[send.length - i - 1] = pwd[i];
        }
        if (send.length < 18) {
            send[send.length - 9] = address[0];
            send[send.length - 8] = address[1];
        } else {
            send[send.length - 7] = address[0];
            send[send.length - 6] = address[1];
        }
        String transceive1 = baseNfc.transceive(send);
        //       baseNfc.transceive(new byte[]{0x40, (byte) 0xb1, (byte) 0xb0, (byte) 0x7C, 0, 0, 0});
        //        baseNfc.transceive(new byte[]{0x40, (byte) 0xb1, (byte) 0xb0, (byte) 0xBC, 0, 0, 0});
        //       baseNfc.transceive(new byte[]{0x40, (byte) 0xb1, (byte) 0xb0, (byte) 0xFC, 0, 0, 0});
        if (transceive1.contains("0000")) {
            callback.onResult(true);
        } else {
            callback.onResult(false);
        }
    }

    /**
     * 更新密码
     *
     * @param baseNfc
     * @param oldPwd
     * @param newPwd
     * @param address
     * @param byteList
     * @param callback
     * @throws Exception
     */
    private static void updatePassword(BaseNfc baseNfc, byte[] oldPwd, byte[] newPwd, byte[] address, List<byte[]> byteList, OnResultCallback callback) throws Exception {
        byte[] encryptionPwd = encryptionPwd(baseNfc, oldPwd, byteList);
        if (address[1] != (byte) 0x30) {
            byte[] bytes = byteList.get(2);
            bytes[bytes.length - 5] = 0x03;
            byte[] bytes1 = byteList.get(3);
            byteList.add(bytes);
            byteList.add(bytes1);
        }
        boolean pwd = authPwd(baseNfc, encryptionPwd, byteList);
        if (pwd) {
            settingPassword(baseNfc, newPwd, address, byteList.get(3), callback);
        } else {
            callback.onResult(false);
        }

    }

    /**
     * 进行密码乱序操作
     *
     * @param baseNfc
     * @param pwd
     * @param byteList
     * @return
     * @throws Exception
     */
    private static byte[] encryptionPwd(BaseNfc baseNfc, byte[] pwd, List<byte[]> byteList) throws Exception {
        //首先获取随机数
        byte[] hexToByte = baseNfc.sendCommand(byteList.get(0));
        byte[] randomByte = new byte[4];
        randomByte[0] = hexToByte[2];
        randomByte[1] = hexToByte[4];
        randomByte[2] = hexToByte[1];
        randomByte[3] = hexToByte[3];
        String random = TransUtil.byteToHex(randomByte);
        random = TransUtil.hexStringToBinary(random);
        random = random.substring(random.length() - 3) + random.substring(0, random.length() - 3);
        String randomHex = TransUtil.binaryString2hexString(random);
        byte[] bytesRandom = TransUtil.hexStringToBytes(randomHex);
        //获取auth_rb_cfg
        byte[] bytes = baseNfc.sendCommand(byteList.get(1));
        for (int i = 0; i < bytesRandom.length; i++) {
            bytesRandom[i] = (byte) (bytesRandom[i] ^ bytes[bytes.length - 1]);
            pwd[i] = (byte) (pwd[i] ^ bytesRandom[i]);
        }
        return pwd;
    }

    /**
     * 密码认证
     *
     * @param baseNfc
     * @param pwd      乱序密码
     * @param byteList
     * @return
     * @throws Exception
     */
    private static boolean authPwd(BaseNfc baseNfc, byte[] pwd, List<byte[]> byteList) throws Exception {
        byte[] bytes = byteList.get(2);
        for (int i = 3; i >= 0; i--) {
            bytes[bytes.length - i - 1] = pwd[i];
        }
        byte[] transceive = baseNfc.sendCommand(bytes);
        SystemClock.sleep(100);
        if ((transceive[1] & (byte) 0x80) == (byte) 0x80) {
            return true;
        }
        return false;
    }


    private static String reverse(String s) {
        if (s.length() == 4) {
            String s1 = s.substring(0, 2);
            String s2 = s.substring(2, 4);
            return s2 + s1;
        }
        return s;
    }

    public static float calculate(String str) {
        LogUtil.d(str);
        int k = 0;
        String flag = Integer.toBinaryString(Integer.parseInt(str, 16));
        if (Integer.parseInt(flag.substring(0, 1)) != 0) {
            String temp = reverse(str);
            int j = Integer.parseInt(temp, 16);
            k = 0x1fff & j;
        }
        double t = (k / 8192.0) * (detA / 16.0) + detB / 16.0 + offset / 16.0;
        double u = k / 8192.0;
        float temp = new BigDecimal(t).setScale(3, BigDecimal.ROUND_HALF_UP).floatValue();

        // 测试
//        LogUtil.d("before", temp);
//        if (str.equals("0CCB") || str.equals("0CCC") || str.equals("0CCD")) {
//            temp = (float) (temp - 0.3);
//        }
//        LogUtil.d("after", temp);


        return temp;
    }

    private static float strFromat(boolean standard, String str) {
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
        float number = (float) 4.00;
        if (standard) {
            number = (float) 4.00;
        } else {
            number = (float) 8.00;
        }
        if (chars[1] >= nu) {

            int i = -((0xffff - Integer.parseInt(newstr, 16)) & 0x03ff) - 1;
            resultTem = (float) (i / number);
        } else {
            int a = Integer.parseInt(hexString, 16);
            resultTem = (float) (a / number);
        }
        LogUtil.d(resultTem + "");
        return resultTem;
    }

    private static int parseField(String data) {
        byte[] bytes = TransUtil.hexToByte(data);
        int filed = (bytes[1] >> 5) & 0x1;
        LogUtil.d("filed", filed);
        return filed;
    }

    private static float strFromat(String str) {
        return strFromat(standard, str);
    }

    private static float formatTemp(String data) {
        if (chipType) {
            return calculate(data);
        }
        return strFromat(data);
    }

    private static void formatCompressTemp(List<Float> floatList, List<String> stringList, String data) {
        LogUtil.d(data);
        //        data ="FBDF4FFFF1BBBFFE";
        String reversalData = TransUtil.reversalData(data);
        String toBinary = TransUtil.hexStringToBinary(reversalData);
        LogUtil.d(toBinary);
        for (int i = 0; i <= 20; i += 10) {
            float resultTem = 0;
            String tempData = toBinary.substring(toBinary.length() - 10 - i, toBinary.length() - i);
            String bStr = tempData.substring(1);

            String zHexS = TransUtil.binaryString2hexString("0000000" + bStr.substring(0, 9));
            int x = 0;
            char[] chars = tempData.toCharArray();
            char nu = '1';
            float number = (float) 4.00;
            if (standard) {
                number = (float) 4.00;
                //                 zHexS = TransUtil.binaryString2hexString("0000000" + bStr.substring(0,9));
                //                  x = Integer.parseInt(bStr.substring(7,8))*2+Integer.parseInt(bStr.substring(8,9));
            } else {
                number = (float) 8.00;
                //                 zHexS = TransUtil.binaryString2hexString("0000000000" + bStr.substring(0,6));
                //                 x = Integer.parseInt(bStr.substring(6,7))*4+Integer.parseInt(bStr.substring(7,8))*2+Integer.parseInt(bStr.substring(8,9));
            }
            if (chars[0] >= nu) {
                int b = -((0xffff - Integer.parseInt(zHexS, 16)) & 0x01ff) - 1;
                //               int b = ~Integer.parseInt(zHexS, 16) +1;

                resultTem = (float) (b / number);
            } else {
                int a = Integer.parseInt(zHexS, 16);
                resultTem = (float) (a / number);
            }
            LogUtil.d(resultTem + "");
            floatList.add(resultTem);
            stringList.add(resultTem + "");

        }


    }

    /**
     * 计算单次体温数据
     *
     * @param data
     * @return
     */
    private static float singleTemp(String data) {
        return strFromat(data);
        //        if (!chipType) {
        //            return strFromat(data);
        //        }
        //        String flag = Integer.toBinaryString(Integer.parseInt(reverse(data), 16));
        //        LogUtil.d(flag);
        //        if (flag.length() < 10) {
        //
        //            return (float) (Integer.parseInt(flag, 2) / 8.0);
        //
        //        } else {
        //            char[] chars = flag.substring(1).toCharArray();
        //            for (int i = 0; i < chars.length; i++) {
        //                char temp = 0;
        //                if (chars[i] == '1') {
        //                    temp = '0';
        //                }
        //                if (chars[i] == '0') {
        //                    temp = '1';
        //                }
        //                chars[i] = temp;
        //            }
        //            int result = -(Integer.parseInt(new String(chars), 2) + 1);
        //            return (float) (result / 8.0);

        //        }


    }

}
