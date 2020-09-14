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
    public INfcA(NfcA nfcA){
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
}
