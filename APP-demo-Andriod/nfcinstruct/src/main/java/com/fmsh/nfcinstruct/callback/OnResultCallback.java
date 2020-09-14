package com.fmsh.nfcinstruct.callback;

/**
 * @author wuyajiang
 * @date 2020/8/10
 */
public interface OnResultCallback {
    /**
     * 结果回调
     * @param status 状态
     * @param response 数据信息
     */
    void onResult(boolean status,String... response);

    /**
     * 失败结果回调
     * @param errorMsg
     */
    void onFailed(String errorMsg);
}
