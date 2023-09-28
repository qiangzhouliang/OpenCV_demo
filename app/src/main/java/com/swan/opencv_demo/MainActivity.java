package com.swan.opencv_demo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.swan.opencv_demo.databinding.ActivityMainBinding;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener {

    private ActivityMainBinding binding;
    private Bitmap mFaceBitmap;
    private FaceDetection mFaceDetection;
    private File mCascadeFile;
    JavaCameraView cameraView;

    static {
        System.loadLibrary("opencv_demo");
        System.loadLibrary("opencv_java3");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {

            // Give first an explanation, if needed.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            } else {
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    1);
            }
        }

        cameraView = binding.sampleText;
        // 设置打开前置摄像头还是后置摄像头
        cameraView.setCameraIndex(Camera.CameraInfo.CAMERA_FACING_FRONT);
        cameraView.setCvCameraViewListener(this);

        //mFaceBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.face);
        //binding.faceImage.setImageBitmap(mFaceBitmap);

        copyCascadeFile();

        mFaceDetection = new FaceDetection();
        mFaceDetection.loadCascade(mCascadeFile.getAbsolutePath());

        File mInFile = new File(getApplication().getFilesDir().getPath()+"/img.jpeg");
        Log.e("TAG", mInFile.getAbsolutePath());


    }

    private void copyCascadeFile() {
        try {
            // load cascade file from application resources
            InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            //mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
            mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_default.xml");
            if (mCascadeFile.exists()) {
                return;
            }

            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void faceDetection(View view) {
        // 识别人脸，保存人脸信息
        mFaceDetection.faceDetectionFaceInfo(mFaceBitmap);
        binding.faceImage.setImageBitmap(mFaceBitmap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.enableView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    // 获取相机的数据 -> Mat -> surfaceView 上
    @Override
    public Mat onCameraFrame(Mat inputFrame) {
        // 这里面处理业务逻辑
        if (this.getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        && cameraView.getCameraIndex() == Camera.CameraInfo.CAMERA_FACING_FRONT){
            Core.rotate(inputFrame, inputFrame, Core.ROTATE_90_COUNTERCLOCKWISE);
        } else if (this.getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            && cameraView.getCameraIndex() == Camera.CameraInfo.CAMERA_FACING_BACK) {
            Core.rotate(inputFrame, inputFrame, Core.ROTATE_90_CLOCKWISE);
        }
        mFaceDetection.faceDetection(inputFrame);
        Log.e("TAG", "onCameraFrame: "+inputFrame.channels() );
        return inputFrame;
    }
}