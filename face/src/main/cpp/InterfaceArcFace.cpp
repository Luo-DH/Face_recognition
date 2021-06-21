//
// Created by LDH on 12/22/20.
//
#include "jni.h"
#include <android/asset_manager_jni.h>
#include "arcface/ArcFace.hpp"

/**
 * 初始化人脸识别模型
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_luo_learnc01_face_ArcFace_init(JNIEnv *env, jobject thiz, jobject manager) {
    AAssetManager *mgr = AAssetManager_fromJava(env, manager);
    ArcFace::face = new ArcFace(mgr);
}

/**
 * 获得单张人脸的特征值
 */
extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_luo_learnc01_face_ArcFace_getFeature(JNIEnv *env, jobject thiz, jobject bitmap) {


    ncnn::Mat in = ncnn::Mat::from_android_bitmap_resize(
            env,
            bitmap,
            ncnn::Mat::PIXEL_RGBA2RGB,
            112,
            112);

    vector<float> faceFeature = ArcFace::face->getFeature(in);




//    //将传入的人脸对齐
//    ncnn::Mat det = mRecognize->preprocess(ncnn_img, mtcnnLandmarks);

    // 得到人脸特征(得到vector<float>类型的特征向量)
//    std::vector<float> faceFeature;
//    mRecognize->start(det, faceFeature);
    // 新建一个float类型的数组用于转换
    jfloatArray faceFeatureArray = env->NewFloatArray(faceFeature.size());
    env->SetFloatArrayRegion(faceFeatureArray, 0, faceFeature.size(), faceFeature.data());

//    // 将该内存空间释放掉
//    env->ReleaseByteArrayElements(face_date, faceDate, 0);
//    env->ReleaseIntArrayElements(landmarks, mtcnn_landmarks, 0);

    return faceFeatureArray;
}


extern "C"
JNIEXPORT jfloat JNICALL
Java_com_luo_learnc01_face_ArcFace_calCosineDistance(JNIEnv *env, jobject thiz,
                                                     jfloatArray feature1, jfloatArray feature2) {

//
//    jfloat *featureData1 = (jfloat *) env->GetFloatArrayElements(feature1, 0);
//    jsize featureSize1 = env->GetArrayLength(feature1);
//    jfloat *featureData2 = (jfloat *) env->GetFloatArrayElements(feature2, 0);
//    jsize featureSize2 = env->GetArrayLength(feature2);
//    std::vector<float> featureVector1(featureSize1), featureVector2(featureSize2);
//    for (int i = 0; i < featureSize1; i++) {
//        featureVector1.push_back(featureData1[i]);
//        featureVector2.push_back(featureData2[i]);
//    }
//
//    cv::Mat feature_1 = cv::Mat(featureVector1, true);
//    cv::Mat feature_2 = cv::Mat(featureVector2, true);
//
//    cv::Mat v1 = Zscore(feature_1);
//    cv::Mat v2 = Zscore(feature_2);
//
//    double dot = v1.dot(v2);
//    double denom_v1 = norm(v1);
//    double denom_v2 = norm(v2);
//    return dot / (denom_v1 * denom_v2);
//    return CosineDistance(Zscore(feature_1), Zscore(feature_2));

    jfloat *featureData1 = (jfloat *) env->GetFloatArrayElements(feature1, 0);
    jsize featureSize1 = env->GetArrayLength(feature1);
    jfloat *featureData2 = (jfloat *) env->GetFloatArrayElements(feature2, 0);
    jsize featureSize2 = env->GetArrayLength(feature2);
    // 创建转换的<float>类型的vector
    std::vector<float> featureVector1(featureSize1), featureVector2(featureSize1);
    if (featureSize1 != featureSize2) {
        return 0;
    }
    for (int i = 0; i < featureSize1; i++) {
        featureVector1.push_back(featureData1[i]);
        featureVector2.push_back(featureData2[i]);
    }
    double score = calculSimilar(featureVector1, featureVector2, 1);
    env->ReleaseFloatArrayElements(feature1, featureData1, 0);
    env->ReleaseFloatArrayElements(feature2, featureData2, 0);
    return score;
}

void getAffineMatrix(float *src_5pts, const float *dst_5pts, float *M) {
    float src[10], dst[10];
    memcpy(src, src_5pts, sizeof(float) * 10);
    memcpy(dst, dst_5pts, sizeof(float) * 10);

    float ptmp[2];
    ptmp[0] = ptmp[1] = 0;
    for (int i = 0; i < 5; ++i) {
        ptmp[0] += src[i];
        ptmp[1] += src[5 + i];
    }
    ptmp[0] /= 5;
    ptmp[1] /= 5;
    for (int i = 0; i < 5; ++i) {
        src[i] -= ptmp[0];
        src[5 + i] -= ptmp[1];
        dst[i] -= ptmp[0];
        dst[5 + i] -= ptmp[1];
    }

    float dst_x = (dst[3] + dst[4] - dst[0] - dst[1]) / 2, dst_y =
            (dst[8] + dst[9] - dst[5] - dst[6]) / 2;
    float src_x = (src[3] + src[4] - src[0] - src[1]) / 2, src_y =
            (src[8] + src[9] - src[5] - src[6]) / 2;
    float theta = atan2(dst_x, dst_y) - atan2(src_x, src_y);

    float scale = sqrt(pow(dst_x, 2) + pow(dst_y, 2)) / sqrt(pow(src_x, 2) + pow(src_y, 2));
    float pts1[10];
    float pts0[2];
    float _a = sin(theta), _b = cos(theta);
    pts0[0] = pts0[1] = 0;
    for (int i = 0; i < 5; ++i) {
        pts1[i] = scale * (src[i] * _b + src[i + 5] * _a);
        pts1[i + 5] = scale * (-src[i] * _a + src[i + 5] * _b);
        pts0[0] += (dst[i] - pts1[i]);
        pts0[1] += (dst[i + 5] - pts1[i + 5]);
    }
    pts0[0] /= 5;
    pts0[1] /= 5;

    float sqloss = 0;
    for (int i = 0; i < 5; ++i) {
        sqloss += ((pts0[0] + pts1[i] - dst[i]) * (pts0[0] + pts1[i] - dst[i])
                   + (pts0[1] + pts1[i + 5] - dst[i + 5]) * (pts0[1] + pts1[i + 5] - dst[i + 5]));
    }

    float square_sum = 0;
    for (int i = 0; i < 10; ++i) {
        square_sum += src[i] * src[i];
    }
    for (int t = 0; t < 200; ++t) {
        _a = 0;
        _b = 0;
        for (int i = 0; i < 5; ++i) {
            _a += ((pts0[0] - dst[i]) * src[i + 5] - (pts0[1] - dst[i + 5]) * src[i]);
            _b += ((pts0[0] - dst[i]) * src[i] + (pts0[1] - dst[i + 5]) * src[i + 5]);
        }
        if (_b < 0) {
            _b = -_b;
            _a = -_a;
        }
        float _s = sqrt(_a * _a + _b * _b);
        _b /= _s;
        _a /= _s;

        for (int i = 0; i < 5; ++i) {
            pts1[i] = scale * (src[i] * _b + src[i + 5] * _a);
            pts1[i + 5] = scale * (-src[i] * _a + src[i + 5] * _b);
        }

        float _scale = 0;
        for (int i = 0; i < 5; ++i) {
            _scale += ((dst[i] - pts0[0]) * pts1[i] + (dst[i + 5] - pts0[1]) * pts1[i + 5]);
        }
        _scale /= (square_sum * scale);
        for (int i = 0; i < 10; ++i) {
            pts1[i] *= (_scale / scale);
        }
        scale = _scale;

        pts0[0] = pts0[1] = 0;
        for (int i = 0; i < 5; ++i) {
            pts0[0] += (dst[i] - pts1[i]);
            pts0[1] += (dst[i + 5] - pts1[i + 5]);
        }
        pts0[0] /= 5;
        pts0[1] /= 5;

        float _sqloss = 0;
        for (int i = 0; i < 5; ++i) {
            _sqloss += ((pts0[0] + pts1[i] - dst[i]) * (pts0[0] + pts1[i] - dst[i])
                        + (pts0[1] + pts1[i + 5] - dst[i + 5]) *
                          (pts0[1] + pts1[i + 5] - dst[i + 5]));
        }
        if (abs(_sqloss - sqloss) < 1e-2) {
            break;
        }
        sqloss = _sqloss;
    }

    for (int i = 0; i < 5; ++i) {
        pts1[i] += (pts0[0] + ptmp[0]);
        pts1[i + 5] += (pts0[1] + ptmp[1]);
    }

    M[0] = _b * scale;
    M[1] = _a * scale;
    M[3] = -_a * scale;
    M[4] = _b * scale;
    M[2] = pts0[0] + ptmp[0] - scale * (ptmp[0] * _b + ptmp[1] * _a);
    M[5] = pts0[1] + ptmp[1] - scale * (-ptmp[0] * _a + ptmp[1] * _b);
}

void warpAffineMatrix(ncnn::Mat src, ncnn::Mat &dst, float *M, int dst_w, int dst_h) {
    int src_w = src.w;
    int src_h = src.h;

    unsigned char *src_u = new unsigned char[src_w * src_h * 3]{0};
    unsigned char *dst_u = new unsigned char[dst_w * dst_h * 3]{0};

    src.to_pixels(src_u, ncnn::Mat::PIXEL_RGB);

    float m[6];
    for (int i = 0; i < 6; i++)
        m[i] = M[i];
    float D = m[0] * m[4] - m[1] * m[3];
    D = D != 0 ? 1. / D : 0;
    float A11 = m[4] * D, A22 = m[0] * D;
    m[0] = A11;
    m[1] *= -D;
    m[3] *= -D;
    m[4] = A22;
    float b1 = -m[0] * m[2] - m[1] * m[5];
    float b2 = -m[3] * m[2] - m[4] * m[5];
    m[2] = b1;
    m[5] = b2;

    for (int y = 0; y < dst_h; y++) {
        for (int x = 0; x < dst_w; x++) {
            float fx = m[0] * x + m[1] * y + m[2];
            float fy = m[3] * x + m[4] * y + m[5];

            int sy = (int) floor(fy);
            fy -= sy;
            if (sy < 0 || sy >= src_h) continue;

            short cbufy[2];
            cbufy[0] = (short) ((1.f - fy) * 2048);
            cbufy[1] = 2048 - cbufy[0];

            int sx = (int) floor(fx);
            fx -= sx;
            if (sx < 0 || sx >= src_w) continue;

            short cbufx[2];
            cbufx[0] = (short) ((1.f - fx) * 2048);
            cbufx[1] = 2048 - cbufx[0];

            if (sy == src_h - 1 || sx == src_w - 1)
                continue;
            for (int c = 0; c < 3; c++) {
                dst_u[3 * (y * dst_w + x) + c] = (
                        src_u[3 * (sy * src_w + sx) + c] * cbufx[0] * cbufy[0] +
                        src_u[3 * ((sy + 1) * src_w + sx) + c] * cbufx[0] * cbufy[1] +
                        src_u[3 * (sy * src_w + sx + 1) + c] * cbufx[1] * cbufy[0] +
                        src_u[3 * ((sy + 1) * src_w + sx + 1) + c] * cbufx[1] * cbufy[1]
                ) >> 22;
            }
        }
    }

    dst = ncnn::Mat::from_pixels(dst_u, ncnn::Mat::PIXEL_BGR, dst_w, dst_h);
    delete[] src_u;
    delete[] dst_u;
}

ncnn::Mat preprocess(ncnn::Mat img, int *info) {
    int image_w = 112; //96 or 112
    int image_h = 112;

    float dst[10] = {30.2946, 65.5318, 48.0252, 33.5493, 62.7299,
                     51.6963, 51.5014, 71.7366, 92.3655, 92.2041};

    if (image_w == 112)
        for (int i = 0; i < 5; i++)
            dst[i] += 8.0;

    float src[10];
    for (int i = 0; i < 10; i++) {
        src[i] = info[i];
    }

    float M[6];
    getAffineMatrix(src, dst, M);
    ncnn::Mat out;
    warpAffineMatrix(img, out, M, image_w, image_h);
    return out;
}

//ncnn::Mat preprocess(ncnn::Mat img, FaceInfo info)
//{
//    int image_w = 112; //96 or 112
//    int image_h = 112;
//
//    float dst[10] = {30.2946, 65.5318, 48.0252, 33.5493, 62.7299,
//                     51.6963, 51.5014, 71.7366, 92.3655, 92.2041};
//
//    if (image_w == 112)
//        for (int i = 0; i < 5; i++)
//            dst[i] += 8.0;
//
//    float src[10];
//    for (int i = 0; i < 5; i++)
//    {
//        src[i] = info.landmark[2 * i];
//        src[i + 5] = info.landmark[2 * i + 1];
//    }
//
//    float M[6];
//    getAffineMatrix(src, dst, M);
//    ncnn::Mat out;
//    warpAffineMatrix(img, out, M, image_w, image_h);
//    return out;
//}

extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_luo_learnc01_face_ArcFace_getFeatureWithWrap(JNIEnv *env, jobject thiz, jobject bitmap,
                                                      jintArray landmarks) {

    ncnn::Mat in = ncnn::Mat::from_android_bitmap_resize(
            env,
            bitmap,
            ncnn::Mat::PIXEL_RGBA2RGB,
            112,
            112);

    // 得到五个人脸坐标点
    jint *mtcnn_landmarks = env->GetIntArrayElements(landmarks, NULL);

    int *mtcnnLandmarks = (int *) mtcnn_landmarks;

    // 人脸对齐
    ncnn::Mat inputImg = preprocess(in, mtcnnLandmarks);

    vector<float> faceFeature = ArcFace::face->getFeature(inputImg);


    // 新建一个float类型的数组用于转换
    jfloatArray faceFeatureArray = env->NewFloatArray(faceFeature.size());
    env->SetFloatArrayRegion(faceFeatureArray, 0, faceFeature.size(), faceFeature.data());

    return faceFeatureArray;
}

extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_luo_learnc01_face_ArcFace_getFeatureWithWrap2(JNIEnv *env, jobject thiz,
                                                       jbyteArray face_date, jint w, jint h,
                                                       jintArray landmarks) {
    jbyte *faceDate = env->GetByteArrayElements(face_date, NULL);

    unsigned char *faceImageCharDate = (unsigned char *) faceDate;

    // 将传入的Mat转换为ncnn的::Mat
    ncnn::Mat ncnn_img_db = ncnn::Mat::from_pixels(faceImageCharDate, ncnn::Mat::PIXEL_RGBA2RGB, w,
                                                   h);

    // 得到五个人脸坐标点
    jint *mtcnn_landmarks = env->GetIntArrayElements(landmarks, NULL);

    int *mtcnnLandmarks = (int *) mtcnn_landmarks;

    // 人脸对齐
    ncnn::Mat inputImg = preprocess(ncnn_img_db, mtcnnLandmarks);

    vector<float> faceFeature = ArcFace::face->getFeature(inputImg);


    // 新建一个float类型的数组用于转换
    jfloatArray faceFeatureArray = env->NewFloatArray(faceFeature.size());
    env->SetFloatArrayRegion(faceFeatureArray, 0, faceFeature.size(), faceFeature.data());

    return faceFeatureArray;

}

extern "C"
JNIEXPORT jdouble JNICALL
Java_com_luo_learnc01_face_ArcFace_compareFeature(JNIEnv *env_db, jobject thiz_db,
                                                  jfloatArray feature1_db,
                                                  jfloatArray feature2_db) {
    jfloat *featureData1_db = (jfloat *) env_db->GetFloatArrayElements(feature1_db, 0);
    jsize featureSize1_db = env_db->GetArrayLength(feature1_db);
    jfloat *featureData2_db = (jfloat *) env_db->GetFloatArrayElements(feature2_db, 0);
    jsize featureSize2_db = env_db->GetArrayLength(feature2_db);
// 创建转换的<float>类型的vector
    std::vector<float> featureVector1_db(featureSize1_db), featureVector2_db(featureSize1_db);
    if (featureSize1_db != featureSize2_db) {
        return 0;
    }
    for (int i = 0; i < featureSize1_db; i++) {
        featureVector1_db.push_back(featureData1_db[i]);
        featureVector2_db.push_back(featureData2_db[i]);
    }
    double score_db = calculSimilar(featureVector1_db, featureVector2_db, 1);
    env_db->ReleaseFloatArrayElements(feature1_db, featureData1_db, 0);
    env_db->ReleaseFloatArrayElements(feature2_db, featureData2_db, 0);
    return score_db;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_luo_learnc01_face_RetinaFace2_test(JNIEnv *env, jobject thiz, jobject bitmap) {
    ncnn::Mat in = ncnn::Mat::from_android_bitmap(env, bitmap, ncnn::Mat::PIXEL_BGR) ;
}