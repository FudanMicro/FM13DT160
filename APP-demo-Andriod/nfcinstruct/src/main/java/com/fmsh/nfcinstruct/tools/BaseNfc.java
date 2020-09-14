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

}
