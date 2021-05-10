package com.fmsh.nfcinstruct.tools;


import android.nfc.tech.NfcA;

import com.fmsh.nfcinstruct.utils.LogUtil;
import com.fmsh.nfcinstruct.utils.TransUtil;

import java.io.IOException;

/**
 * @author wuyajiang
 * @date 2020/8/11
 */
public class INfcA extends BaseNfc {
    private NfcA mNfcA;

    public INfcA(NfcA nfcA) {
        this.mNfcA = nfcA;
    }

    @Override
    public String transceive(byte[] bytes) throws IOException {
        LogUtil.d(TransUtil.byteToHex(bytes));
        byte[] transceive = mNfcA.transceive(bytes);
        String byteToHex = TransUtil.byteToHex(transceive);
        LogUtil.d(byteToHex);
        return byteToHex;
    }

    @Override
    public byte[] sendCommand(byte[] bytes) throws IOException {
        LogUtil.d(TransUtil.byteToHex(bytes));
        byte[] transceive = mNfcA.transceive(bytes);
        LogUtil.d(TransUtil.byteToHex(transceive));

        return transceive;
    }

    @Override
    public byte[] readMemory(byte[] address, int length) throws IOException {
        byte[] bytes = new byte[]{0x40, (byte) 0xb1, address[0], address[1], 0x00, (byte) length, 0};
        return sendCommand(bytes);

    }

    @Override
    public byte[] writeMemory(byte[] address, byte[] data) throws IOException {
        //设置用户配置模式
        byte[] bytes = new byte[]{0x40, (byte) 0xb3, address[0], address[1], 0x03, 0, 0, data[0], data[1], data[2], data[3]};
        return sendCommand(bytes);
    }

    @Override
    public byte[] readReg(byte[] address) throws IOException {
        byte[] bytes = new byte[]{0x40, (byte) 0xc6, address[0], address[1], 0x00, 0x00, 0x00};
        return sendCommand(bytes);
    }

    @Override
    public byte[] writeReg(byte[] address, byte[] data) throws IOException {
        byte[] bytes = new byte[]{0x40, (byte) 0xc5, address[0], address[1], data[0], data[1], 0x00};
        return sendCommand(bytes);
    }


}
