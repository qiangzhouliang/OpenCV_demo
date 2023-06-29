package com.swan.opencv_demo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;

import com.swan.opencv_demo.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Bitmap mFaceBitmap;
    private FaceDetection mFaceDetection;
    private File mCascadeFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mFaceBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.face);
        binding.faceImage.setImageBitmap(mFaceBitmap);

        copyCascadeFile();

        mFaceDetection = new FaceDetection();
        mFaceDetection.loadCascade(mCascadeFile.getAbsolutePath());

    }

    private void copyCascadeFile() {
        try {
            // load cascade file from application resources
            InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
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
}