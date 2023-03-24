package com.fmsh.nfcinstruct.tools;

import android.nfc.Tag;
import android.nfc.tech.NfcV;

import com.fmsh.nfcinstruct.utils.LogUtil;
import com.fmsh.nfcinstruct.utils.TransUtil;

import java.io.IOException;

/**
 * @author wuyajiang
 * @date 2020/8/11
 */
public class INfcV extends BaseNfc {
    private NfcV mNfcV;
    private final byte[] mId;

    public INfcV(NfcV nfcV){
        this.mNfcV = nfcV;
        Tag tag = mNfcV.getTag();
        mId = tag.getId();
    }

    @Override
    public String transceive(byte[] bytes) throws IOException {
        if(bytes[0] == (byte) 0x22){
            System.arraycopy(mId, 0, bytes, 3, mId.length);
        }
        LogUtil.d(TransUtil.byteToHex(bytes));
        byte[] transceive = mNfcV.transceive(bytes);
        String byteToHex = TransUtil.byteToHex(transceive);
        LogUtil.d(byteToHex);
        return byteToHex;
    }

    @Override
    public byte[] sendCommand(byte[] bytes) throws IOException {
        if(bytes[0] == (byte) 0x22){
            System.arraycopy(mId, 0, bytes, 3, mId.length);
        }
        LogUtil.d(TransUtil.byteToHex(bytes));
        byte[] transceive = mNfcV.transceive(bytes);
        LogUtil.d(TransUtil.byteToHex(transceive));
        return transceive;
    }

    @Override
    public byte[] readMemory(byte[] address ,int length) throws IOException {
//        bytes = new byte[]{0x22, (byte) 0xb1, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xb1, (byte) 0x38, 0x00, (byte) 0x00};
        byte[] bytes = new byte[]{0x02, (byte) 0xb1, 0x1d,  address[0], address[1], (byte) 0, (byte) length};
//        byte[] bytes = new byte[]{0x22, (byte) 0xb1, 0x1d,  0,0,0,0,0,0,0,0,address[0], address[1], (byte) length, (byte) 0};
        return sendCommand(bytes);
    }

    @Override
    public byte[] writeMemory(byte[] address, byte[] data) throws IOException {
       byte[] bytes = new byte[]{0x02, (byte) 0xb3, 0x1d,  address[0], address[1], 0x03, data[0], data[1], data[2], data[3]};

        return sendCommand(bytes);
    }

    @Override
    public byte[] readReg(byte[] address) throws IOException {
       byte[] bytes = new byte[]{0x02, (byte) 0xc6, 0x1d,  address[0], address[1]};
        return sendCommand(bytes);
    }

    @Override
    public byte[] writeReg(byte[] address, byte[] data) throws IOException {
       byte[] bytes = new byte[]{0x02, (byte) 0xc5, 0x1d, address[0], address[1], data[0], data[1]};
        return sendCommand(bytes);
    }

    @Override
    public byte[] send(byte address, byte[] data) throws IOException {
        byte[] command = new byte[3+ data.length];
        command [0] = 0x02;
        command [1] = address;
        command [2] = 0x1d;
        System.arraycopy(data,0,command,3,data.length);
        return sendCommand(command);
    }


}
