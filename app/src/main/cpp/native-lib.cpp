#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>
#include <android/bitmap.h>
#include <android/log.h>

#define TAG "JNI_TAG"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

using namespace cv;

// bitmap 转成 Mat
void bitmap2Mat(JNIEnv *env, Mat &mat, jobject bitmap);
// mat 转成 bitmap
void mat2Bitmap(JNIEnv *env, Mat mat, jobject bitmap);

void mosaicFace(Mat mat);

// bitmap 转成 Mat
void bitmap2Mat(JNIEnv *env, Mat &mat, jobject bitmap) {
    // Mat 里面有个 type：
    //       CV_8UC4 刚好对上我们的 bitmap 中 ARGB_8888
    //       CV_8UC2 刚好对上我们的 bitmap 中 ARGB_565

    // 1 获取bitmap信息
    AndroidBitmapInfo info;
    void *pixels;
    AndroidBitmap_getInfo(env, bitmap,&info);

    // 锁定 bitmap 画布
    AndroidBitmap_lockPixels(env, bitmap, &pixels);
    // 指定 mat 的宽高和type BGRA
    mat.create(info.height, info.width, CV_8UC4);

    if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888){
        // 对应的 mat 应该是 CV_8UC4
        Mat temp(info.height, info.width, CV_8UC4, pixels);
        // 把数据 temp 赋值到 mat 里面
        temp.copyTo(mat);
    } else if (info.format == ANDROID_BITMAP_FORMAT_RGB_565){
        // 对应的 mat 应该是 CV_8UC2
        Mat temp(info.height, info.width, CV_8UC2, pixels);
        // mat 是 CV_8UC4，CV_8UC2 -> CV_8UC4
        cvtColor(temp, mat, COLOR_BGR5652BGRA);
    }
    // 其他要自己去转

    // 解锁画布
    AndroidBitmap_unlockPixels(env, bitmap);


}

// mat 转成 bitmap
void mat2Bitmap(JNIEnv *env, Mat mat, jobject bitmap) {
    // 1 获取bitmap信息
    AndroidBitmapInfo info;
    void *pixels;
    AndroidBitmap_getInfo(env, bitmap,&info);

    // 锁定 bitmap 画布
    AndroidBitmap_lockPixels(env, bitmap, &pixels);

    if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888){ // c4
        Mat temp(info.height, info.width, CV_8UC4, pixels);
        if (mat.type() == CV_8UC4){
            mat.copyTo(temp);
        } else if (mat.type() == CV_8UC2){
            cvtColor(mat, temp, COLOR_BGR5652BGRA);
        } else if (mat.type() == CV_8UC1){ // 灰度 mat
            cvtColor(mat, temp, COLOR_GRAY2RGBA);
        }
    } else if (info.format == ANDROID_BITMAP_FORMAT_RGB_565){ // c2
        Mat temp(info.height, info.width, CV_8UC2, pixels);
        if (mat.type() == CV_8UC4){
            cvtColor(mat, temp, COLOR_RGBA2BGR565);
        } else if (mat.type() == CV_8UC2){
            mat.copyTo(temp);
        } else if (mat.type() == CV_8UC1){ // 灰度 mat
            cvtColor(mat, temp, COLOR_GRAY2BGR565);
        }
    }
    // 其他要自己去转

    // 解锁bitmap画布
    AndroidBitmap_unlockPixels(env, bitmap);
}
CascadeClassifier cascadeClassifier;

void mosaicFace(Mat src) {

    // 获取图片的宽高
    int src_w = src.cols;
    int src_h = src.rows;
    // 省略 人脸识别
    int rows_s = src_h >> 2;
    int rows_e = src_h * 3 / 4;
    int cols_s = src_w >> 2;
    int cols_e = src_w * 3 / 4;
    // 马赛克大小
    int size = 30;
    for (int row = rows_s; row < rows_e; row += size) {
        for (int col = cols_s; col < cols_e; col += size) {
            int pixels = src.at<int>(row, col);
            // 10 * 10 的范围内都有第一个 像素值
            for (int m_rows = 1; m_rows < size; ++m_rows) {
                for (int m_cols = 0; m_cols < size; ++m_cols) {
                    src.at<int>(row + m_rows, col + cols_s) = pixels;
                }
            }
        }
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_swan_opencv_1demo_FaceDetection_loadCascade(JNIEnv *env, jobject thiz, jstring file_path_) {
    const char *filePath = env->GetStringUTFChars(file_path_, 0);
    cascadeClassifier.load(filePath);
    LOGE("加载分类器文件成功");

    env->ReleaseStringUTFChars(file_path_, filePath);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_swan_opencv_1demo_FaceDetection_faceDetectionFaceInfo(JNIEnv *env, jobject thiz,
                                                               jobject bitmap) {
    // 检测人脸,opencv 有一个非常关键的类 Mat，opencv 是 c和c++写的，只会处理Mat，Android里面是bitmap
    // 1 bitmap 转成 opencv能操作的 c++对象 Mat， Mat 是一个矩阵
    Mat mat;
    bitmap2Mat(env, mat, bitmap);

    // 处理灰度：opencv处理灰度图, 提高效率
    Mat gray_mat;
    cvtColor(mat, gray_mat, COLOR_BGRA2GRAY);

    // 再次处理 直方均衡补偿
    Mat equalize_mat;
    equalizeHist(gray_mat, equalize_mat);

    // 识别人脸，我们也可以直接使用 彩色图去做，识别人脸要加载人脸分类器文件
    std::vector<Rect> faces;
    cascadeClassifier.detectMultiScale(equalize_mat,faces, 1.1, 5);
    LOGE("人脸个数：%d",faces.size());
    if (faces.size() == 1){
        Rect faceRect = faces[0];

        // 在人脸部分画个图
        rectangle(mat, faceRect, Scalar(255, 155, 155), 8);
        // 把 mat 我们又重新放到bitmap里面去
        mat2Bitmap(env, mat, bitmap);

        // 保存人脸信息 Mat，图片 jpg
        Mat face_info_mat(equalize_mat, faceRect);
        // 保存 face_info_mat

    }

    // 保存人脸信息

    return 0;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_swan_opencv_1demo_FaceDetection_faceDetection(JNIEnv *env, jobject thiz,
                                                       jlong nativeObj) {
    Mat *src = reinterpret_cast<Mat *>(nativeObj);

    int width = src->rows;
    int height = src->cols;

    Mat grayMat;
    // 2. 转成灰度图，提升运算速度，灰度图所对应的 CV_8UC1 单颜色通道，信息量少 0-255 1u
    cvtColor(*src, grayMat, COLOR_BGR2GRAY);

    // 4. 检测人脸，这是个大问题
    std::vector<Rect> faces;
    cascadeClassifier.detectMultiScale(grayMat, faces, 1.1, 3, 0, Size(width / 2, height / 2));
    LOGE("人脸size = %d", faces.size());

    if(faces.size() != 1){
        // 打个马赛克
        mosaicFace(*src);
        return;
    }

    // 把脸框出来
    Rect faceRect = faces[0];
    rectangle(*src, faceRect, Scalar(255, 0, 0, 255), 4, LINE_AA);

    mosaicFace((*src)(faceRect));
}