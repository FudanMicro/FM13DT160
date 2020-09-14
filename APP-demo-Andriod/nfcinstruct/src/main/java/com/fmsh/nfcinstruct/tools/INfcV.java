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
        System.arraycopy(mId, 0, bytes, 3, mId.length);
        LogUtil.d(TransUtil.byteToHex(bytes));
        byte[] transceive = mNfcV.transceive(bytes);
        String byteToHex = TransUtil.byteToHex(transceive);
        LogUtil.d(byteToHex);
        return byteToHex;
    }

    @Override
    public byte[] sendCommand(byte[] bytes) throws IOException {
        System.arraycopy(mId, 0, bytes, 3, mId.length);
        LogUtil.d(TransUtil.byteToHex(bytes));
        byte[] transceive = mNfcV.transceive(bytes);
        LogUtil.d(TransUtil.byteToHex(transceive));
        return transceive;
    }
}
