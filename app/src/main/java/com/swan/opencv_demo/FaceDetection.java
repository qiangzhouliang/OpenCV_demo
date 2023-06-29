package com.swan.opencv_demo;

import android.graphics.Bitmap;

/**
 * @ClassName FaceDetection
 * @Description
 * @Author swan
 * @Date 2023/6/28 18:15
 **/
public class FaceDetection {
    static {
        System.loadLibrary("opencv_demo");
    }
    /**
     * 检测人脸并保存人脸信息
     * @param mFaceBitmap
     */
    public native int faceDetectionFaceInfo(Bitmap mFaceBitmap);

    /**
     * 加载人脸识别的分类器文件
     * @param filePath
     */
    public native void loadCascade(String filePath);
}
