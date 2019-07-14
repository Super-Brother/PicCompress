package com.wenchao.library.listener;

/**
 * @author wenchao
 * @date 2019/7/14.
 * @time 10:08
 * description：
 */
public interface CompressResultListener {

    /**
     * 成功
     *
     * @param imgPath
     */
    void onCompressSuccess(String imgPath);

    /**
     * 失败
     *
     * @param imgPath
     * @param error
     */
    void onCompressFailed(String imgPath, String error);
}
