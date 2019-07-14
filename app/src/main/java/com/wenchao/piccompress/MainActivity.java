package com.wenchao.piccompress;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.wenchao.library.CompressImageManager;
import com.wenchao.library.bean.Photo;
import com.wenchao.library.config.CompressConfig;
import com.wenchao.library.listener.CompressImage;
import com.wenchao.library.utils.CachePathUtils;
import com.wenchao.library.utils.CommonUtils;
import com.wenchao.library.utils.Constants;
import com.wenchao.piccompress.utils.UriParseUtils;

import java.io.File;
import java.util.ArrayList;

import top.zibin.luban.CompressionPredicate;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

public class MainActivity extends AppCompatActivity implements CompressImage.CompressListener {

    private ProgressDialog dialog;
    /**
     * 拍照源文件路径
     */
    private String cameraCachePath;
    private CompressConfig compressConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] perms = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (checkSelfPermission(perms[0]) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(perms[1]) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(perms, 111);
            }
        }

        compressConfig = CompressConfig.getDefaultConfig();

//        testLuban();
    }

    private void testLuban() {

        String mCacheDir = Constants.BASE_CACHE_PATH + getPackageName() + "/cache/" + Constants.COMPRESS_CACHE;
        Log.e("wenchao", mCacheDir);

        String filePath = Environment.getExternalStorageDirectory() + File.separator + "tiantian.jpg";

        Luban.with(this)
                .load(filePath)
                .ignoreBy(100)
                .setTargetDir(mCacheDir)
                .filter(new CompressionPredicate() {
                    @Override
                    public boolean apply(String path) {
                        return !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif"));
                    }
                })
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() {
                        Log.e("wenchao", "onStart");
                    }

                    @Override
                    public void onSuccess(File file) {
                        Log.e("wenchao", file.getAbsolutePath());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("wenchao", e.getMessage());
                    }
                }).launch();
    }

    public void camera(View view) {
        //7.0 FileProvider
        Uri outputUri;
        File file = CachePathUtils.getCameraCacheFile();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            outputUri = UriParseUtils.getCameraOutPutUri(this, file);
        } else {
            outputUri = Uri.fromFile(file);
        }
        //拍照之后源文件路径
        cameraCachePath = file.getAbsolutePath();
        //启动拍照
        CommonUtils.hasCamera(this, CommonUtils.getCameraIntent(outputUri), Constants.CAMERA_CODE);
    }

    public void album(View view) {
        CommonUtils.openAlbum(this, Constants.ALBUM_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.CAMERA_CODE && resultCode == RESULT_OK) {
            //拍照返回
            //开始压缩
            proCompress(cameraCachePath);
        } else if (requestCode == Constants.ALBUM_CODE && resultCode == RESULT_OK) {
            //相册返回
            if (data != null) {
                Uri uri = data.getData();
                String path = UriParseUtils.getPath(this, uri);
                //开始压缩
                proCompress(path);
            }
        }
    }

    private void proCompress(String path) {
        //集合批量压缩
        ArrayList<Photo> photos = new ArrayList<>();
        photos.add(new Photo(path));
        if (!photos.isEmpty()) {
            compress(photos);
        }
    }

    /**
     * 交给压缩引擎，等待返回
     */
    private void compress(ArrayList<Photo> photos) {
        Log.e("wenchao", "开始压缩");
        if (compressConfig.isShowCompressDialog()) {
            dialog = CommonUtils.showProgressDialog(this, "开始压缩...");
        }
        CompressImageManager.build(this, compressConfig, photos, this).compress();
    }

    @Override
    public void onCompressSuccess(ArrayList<Photo> images) {
        Log.e("wenchao", "压缩成功");
        Log.e("wenchao", images.get(0).getCompressPath());
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    public void onCompressFailed(ArrayList<Photo> images, String... error) {
        Log.e("wenchao", "压缩失败");
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
