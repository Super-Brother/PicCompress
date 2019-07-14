package com.wenchao.library.listener;

import com.wenchao.library.bean.Photo;

import java.util.ArrayList;

/**
 * @author wenchao
 * @date 2019/7/14.
 * @time 10:04
 * description：
 */
public interface CompressImage {

    /**
     * 开始压缩
     */
    void compress();

    /**
     * 图片压缩结果监听
     */
    interface CompressListener {

        /**
         * 成功
         *
         * @param images
         */
        void onCompressSuccess(ArrayList<Photo> images);

        /**
         * 失败
         *
         * @param images
         * @param error
         */
        void onCompressFailed(ArrayList<Photo> images, String... error);
    }
}
