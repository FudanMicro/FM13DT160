package com.fmsh.nfcinstruct.tools;

import java.io.IOException;

/**
 * @author wuyajiang
 * @date 2020/8/11
 */
public abstract class BaseNfc {

    //    public abstract byte[] transceive(byte[] bytes) throws IOException;

    public abstract String transceive(byte[] bytes) throws IOException;

    public abstract byte[] sendCommand(byte[] bytes) throws IOException;

    public abstract byte[] readMemory(byte[] address,int length) throws IOException;

    public abstract byte[] writeMemory(byte[] address,byte[] data) throws IOException;

    public abstract byte[] readReg(byte[] address) throws IOException;

    public abstract byte[] writeReg(byte[] address,byte[] data) throws IOException;


    public abstract byte[] send(byte address,byte[] data) throws IOException;


}
