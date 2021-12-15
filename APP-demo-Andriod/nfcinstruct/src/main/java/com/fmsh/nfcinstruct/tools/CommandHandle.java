package com.fmsh.nfcinstruct.tools;

import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.os.SystemClock;

import com.fmsh.nfcinstruct.callback.OnResultCallback;
import com.fmsh.nfcinstruct.utils.TransUtil;

import java.io.IOException;

/**
 * @author wuyajiang
 * @date 2021/10/8
 */
public class CommandHandle {
    private static CommandHandle commandHandle = new CommandHandle();
    private NfcA mNfcA;
    private NfcV mNfcV;

    private CommandHandle() {

    }

    public static CommandHandle getInstance() {
        return commandHandle;
    }

    /**
     * 解析tag包含的芯片类型
     *
     * @param tag
     */
    public byte[] parseTag(Tag tag, Bundle bundle) {
        if (tag == null) {
            return null;
        }
        String[] techList = tag.getTechList();
        for (String taType : techList) {
            if (taType.endsWith("NfcA")) {

                return startA(tag, bundle);
            }
            if (taType.endsWith("NfcV")) {

                return startV(tag, bundle);

            }
        }
        return null;
    }

    private byte[] startA(Tag tag, Bundle bundle) {
        if (mNfcA == null) {
            mNfcA = NfcA.get(tag);
        }
        try {
            if (!mNfcA.isConnected()) {
                mNfcA.connect();
            }
            INfcA iNfcA = new INfcA(mNfcA);
            int position = bundle.getInt("position");
            byte[] result = null;
            switch (position) {
                case 0:
                    result = readMemory(iNfcA, bundle);
                    break;
                case 1:
                    result = writeMemory(iNfcA, bundle);
                    break;
                case 2:
                    result = readReg(iNfcA,bundle);
                    break;
                case 3:
                    result = writeReg(iNfcA,bundle);
                    break;
                case 4:
                    result = passwordAuth(iNfcA,bundle);
                    break;
                case 5:
                    result = wakeUp(iNfcA);
                    break;
                default:
                    break;
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                mNfcA.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private byte[] startV(Tag tag, Bundle bundle) {
        if (mNfcV == null) {
            mNfcV = NfcV.get(tag);
        }
        try {
            if (!mNfcV.isConnected()) {
                mNfcV.connect();
            }
            INfcV iNfcV = new INfcV(mNfcV);
            int position = bundle.getInt("position");
            byte[] result = null;
            switch (position) {
                case 0:
                    result = readMemory(iNfcV, bundle);
                    break;
                case 1:
                    result = writeMemory(iNfcV, bundle);
                    break;
                case 2:
                    result = readReg(iNfcV,bundle);
                    break;
                case 3:
                    result = writeReg(iNfcV,bundle);
                    break;
                case 4:
                    result = passwordAuth(iNfcV,bundle);
                    break;
                case 5:
                    result = wakeUp(iNfcV);
                    break;
                default:
                    break;
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                mNfcV.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private byte[] readMemory(BaseNfc baseNfc, Bundle bundle) throws IOException {

        byte[] address = bundle.getByteArray("address");
        int length = bundle.getInt("length");
        if (baseNfc instanceof INfcA) {
            byte[] data = new byte[]{0x40, (byte) 0xb1, address[0], address[1], (byte) (0xff & (length >> 8)), (byte) (0xff & length), 0x00};
            return baseNfc.sendCommand(data);
        } else {
            byte[] data = new byte[]{0x02, (byte) 0xb1, 0x1d, address[0], address[1], (byte) (0xff & (length >> 8)), (byte) (0xff & length)};
            return baseNfc.sendCommand(data);
        }
    }

    private byte[] writeMemory(BaseNfc baseNfc, Bundle bundle) throws IOException {
        byte[] address = bundle.getByteArray("address");
        byte[] data = bundle.getByteArray("data");
        if (data.length > 4) {
            byte[] temp = new byte[4];
            System.arraycopy(data, 0, temp, 0, 4);
            data = temp;
        }
        if (baseNfc instanceof INfcA) {
            byte[] bytes = new byte[7 + data.length];
            byte[] cmd = {0x40, (byte) 0xb3, (byte) address[0], (byte) address[1], (byte) (data.length - 1), 0x00, 0x00};
            System.arraycopy(cmd, 0, bytes, 0, cmd.length);
            System.arraycopy(data, 0, bytes, cmd.length, data.length);
            return baseNfc.sendCommand(bytes);
        } else {
            byte[] bytes = new byte[6 + data.length];
            byte[] cmd = {0x02, (byte) 0xb3, 0x1d, (byte) address[0], (byte) address[1], (byte) (data.length - 1)};
            System.arraycopy(cmd, 0, bytes, 0, cmd.length);
            System.arraycopy(data, 0, bytes, cmd.length, data.length);
            return baseNfc.sendCommand(bytes);
        }

    }

    /**
     * 写寄存器
     * @param baseNfc
     * @param bundle
     * @return
     * @throws IOException
     */
    private byte[] writeReg(BaseNfc baseNfc, Bundle bundle) throws IOException {
        byte[] address = bundle.getByteArray("address");
        byte[] data = bundle.getByteArray("data");
        if (data.length > 2) {
            byte[] temp = new byte[2];
            System.arraycopy(data, 0, temp, 0, 2);
            data = temp;
        }
        if (data.length < 2) {
            data = new byte[]{0, data[0]};
        }
        if (baseNfc instanceof INfcA) {

            byte[] cmd = {0x40, (byte) 0xc5, (byte) address[0], (byte) address[1], (byte) data[0], data[1], 0x00};
            return baseNfc.sendCommand(cmd);
        } else {

            byte[] cmd = {0x02, (byte) 0xc5, 0x1d, (byte) address[0], (byte) address[1], (byte) data[0], data[1]};
            return baseNfc.sendCommand(cmd);
        }
    }

    /**
     * 读寄存器
     * @param baseNfc
     * @param bundle
     * @return
     * @throws IOException
     */
    private byte[] readReg(BaseNfc baseNfc, Bundle bundle) throws IOException {
        byte[] address = bundle.getByteArray("address");
        if (baseNfc instanceof INfcA) {
            byte[] data = new byte[]{0x40, (byte) 0xc6, address[0], address[1], 0, 0, 0x00};
            return baseNfc.sendCommand(data);
        } else {
            byte[] data = new byte[]{0x02, (byte) 0xc6, 0x1d, address[0], address[1]};
            return baseNfc.sendCommand(data);
        }
    }

    /**
     * 设置密码
     * @param baseNfc
     * @param bundle
     * @return
     * @throws IOException
     */
    private byte[] passwordSetting(BaseNfc baseNfc, Bundle bundle) throws IOException {
        byte[] address = bundle.getByteArray("address");
        byte[] password = bundle.getByteArray("password");
        if (baseNfc instanceof INfcA) {
            byte[] bytes = new byte[]{0x40, (byte) 0xb3, (byte) address[0], address[2], 0x03, 0, 0, (byte) password[0], password[1], password[2], (byte) password[3]};
            return baseNfc.sendCommand(bytes);
        } else {
            byte[] bytes = new byte[]{0x02, (byte) 0xb3, 0x1d, address[0], address[1], 0x03, (byte) password[0], password[1], password[2], (byte) password[3]};
            return baseNfc.sendCommand(bytes);
        }
    }

    /**
     * 密码认证
     *
     * @param baseNfc
     * @param bundle
     * @return
     * @throws IOException
     */
    private byte[] passwordAuth(BaseNfc baseNfc, Bundle bundle) throws IOException {
        byte[] password = bundle.getByteArray("password");
        byte type = bundle.getByte("type");
        //首先获取随机数
        byte[] hexToByte = null;
        if (baseNfc instanceof INfcA) {
            byte[] bytes = new byte[]{0x40, (byte) 0xb2, 0, 0, 0, 0, 0};
            hexToByte = baseNfc.sendCommand(bytes);
        } else {
            byte[] bytes = new byte[]{0x02, (byte) 0xb2, 0x1d};
            hexToByte = baseNfc.sendCommand(bytes);
        }
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

        byte[] auth_rb_cfg = null;
        if (baseNfc instanceof INfcA) {
            auth_rb_cfg = baseNfc.sendCommand(new byte[]{0x40, (byte) 0xb1, (byte) 0xb1, 0x38, 0x00, 0, 0});
        } else {
            auth_rb_cfg = baseNfc.sendCommand(new byte[]{0x02, (byte) 0xb1, 0x1d, (byte) 0xb1, (byte) 0x38, 0, 0});
        }
        for (int i = 0; i < bytesRandom.length; i++) {
            bytesRandom[i] = (byte) (bytesRandom[i] ^ auth_rb_cfg[auth_rb_cfg.length - 1]);
            password[i] = (byte) (password[i] ^ bytesRandom[i]);
        }
        //乱序之后的密码
        byte [] result = null;
        if (baseNfc instanceof INfcA) {
            result =  baseNfc.sendCommand(new byte[]{0x40, (byte) 0xb4,  type, password[3], password[2], password[1], password[0]});
        } else {
            result =  baseNfc.sendCommand(new byte[]{0x02, (byte) 0xb4, 0x1d,  type, password[3], password[2], password[1], password[0]});
        }
        SystemClock.sleep(100);
        return result;

    }

    /**
     * 唤醒
     * @param baseNfc
     * @return
     * @throws IOException
     */
    private byte[] wakeUp(BaseNfc baseNfc) throws IOException {
        if (baseNfc instanceof INfcA) {
            byte[] data = new byte[]{0x40, (byte) 0xc4, 0x00, 0x00, 0x00, 0x00, 0x00};
            return baseNfc.sendCommand(data);
        } else {
            byte[] data = new byte[]{0x02, (byte) 0xc4, 0x1d, 0x00};
            return baseNfc.sendCommand(data);
        }
    }

}
