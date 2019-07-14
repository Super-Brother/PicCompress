package com.wenchao.library;

import android.content.Context;
import android.text.TextUtils;

import com.wenchao.library.bean.Photo;
import com.wenchao.library.config.CompressConfig;
import com.wenchao.library.core.CompressImageUtil;
import com.wenchao.library.listener.CompressImage;
import com.wenchao.library.listener.CompressResultListener;

import java.io.File;
import java.util.ArrayList;

/**
 * @author wenchao
 * @date 2019/7/13.
 * @time 11:40
 * description：压缩管理类
 */
public class CompressImageManager implements CompressImage {

    /**
     * 压缩工具类
     */
    private CompressImageUtil compressImageUtil;
    /**
     * 需要压缩的集合
     */
    private ArrayList<Photo> images;
    /**
     * 压缩监听
     */
    private CompressImage.CompressListener listener;
    /**
     * 压缩配置类
     */
    private CompressConfig config;

    private CompressImageManager(Context context,
                                 CompressConfig config,
                                 ArrayList<Photo> images,
                                 CompressImage.CompressListener listener) {
        compressImageUtil = new CompressImageUtil(context, config);
        this.config = config;
        this.images = images;
        this.listener = listener;
    }

    public static CompressImage build(Context context,
                                      CompressConfig config,
                                      ArrayList<Photo> images,
                                      CompressImage.CompressListener listener) {
        return new CompressImageManager(context, config, images, listener);
    }

    /**
     * 压缩的实现
     */
    @Override
    public void compress() {
        if (images == null || images.isEmpty()) {
            listener.onCompressFailed(images, "图片为空");
            return;
        }
        for (Photo image : images) {
            if (image == null) {
                listener.onCompressFailed(images, "图片为空");
                return;
            }
        }
        compress(images.get(0));
    }

    private void compress(Photo photo) {
        //原文件为空，继续压缩
        if (TextUtils.isEmpty(photo.getOriginalPath())) {
            continueCompress(photo, false);
            return;
        }
        File file = new File(photo.getOriginalPath());
        if (!file.exists() || !file.isFile()) {
            continueCompress(photo, false);
            return;
        }
        //不需要压缩
        if (file.length() < config.getMaxSize()) {
            continueCompress(photo, true);
            return;
        }
        compressImageUtil.compress(photo.getOriginalPath(), new CompressResultListener() {
            @Override
            public void onCompressSuccess(String imgPath) {
                photo.setCompressPath(imgPath);
                continueCompress(photo, true);
            }

            @Override
            public void onCompressFailed(String imgPath, String error) {
                continueCompress(photo, false, error);
            }
        });
    }

    /**
     * @param photo        需要压缩的图片对象
     * @param isCompressed 是否压缩过
     * @param error        压缩出现了异常
     */
    private void continueCompress(Photo photo, boolean isCompressed, String... error) {
        photo.setCompressed(isCompressed);
        //获取当前压缩的这张图片对象的索引
        int index = images.indexOf(photo);
        if (index == images.size() - 1) {
            handlerCallback(error);
        } else {
            compress(images.get(index + 1));
        }
    }

    private void handlerCallback(String... error) {
        if (error.length > 0) {
            listener.onCompressFailed(images, "压缩图片错误");
        }
        for (Photo image : images) {
            if (!image.isCompressed()) {
                listener.onCompressFailed(images, "压缩图片错误");
                return;
            }
        }
        listener.onCompressSuccess(images);
    }
}
