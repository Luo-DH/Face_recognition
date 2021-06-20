//
// Created by LDH on 1/24/21.
//
#include "jni.h"
#include "android/asset_manager_jni.h"
#include "retinaface/RetinaFace.hpp"
static ncnn::Net retinaface2;

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_luo_face_RetinaFaceDB_init(JNIEnv *env, jobject thiz, jobject assetManager) {
    AAssetManager *mgr = AAssetManager_fromJava(env, assetManager);
//    retinaface2.opt.use_vulkan_compute = true;
//    retinaface2.opt.use_int8_arithmetic = true;
//    retinaface2.opt.use_fp16_arithmetic = true;
    //init param
    int ret = retinaface2.load_param(mgr, "mnet.25-opt.param");
//    int ret = retinaface2.load_param(mgr, "face.param");
    if (ret != 0) {
        __android_log_print(ANDROID_LOG_DEBUG, "RetinaFace", "load_param failed");
        return JNI_FALSE;
    }
    //init bin
    ret = retinaface2.load_model(mgr, "mnet.25-opt.bin");
//    ret = retinaface2.load_model(mgr, "face.bin");
    if (ret != 0) {
        __android_log_print(ANDROID_LOG_DEBUG, "RetinaFace", "load_model failed");
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_luo_face_RetinaFaceDB_detect(
        JNIEnv *env,
        jobject thiz,
        jobject bitmap,
        jfloat ratio
) {


    // -------------------------- 创建ArrayList对象----------------------------
    jclass list_jcls = env->FindClass("java/util/ArrayList");

    jmethodID list_init = env->GetMethodID(list_jcls, "<init>", "()V");

    jobject list_obj = env->NewObject(list_jcls, list_init);

    // ArrayList 中的add方法
    jmethodID list_add = env->GetMethodID(list_jcls, "add", "(Ljava/lang/Object;)Z");
    // -------------------------- 创建ArrayList对象----------------------------


    // -------------------------- 创建landmark的array对象----------------------
    jclass rectF_jcls = env->FindClass("android/graphics/PointF");

    // -------------------------- 创建landmark的array对象----------------------


    // -------------------------- 创建box对象----------------------------------
    jclass box_jcls = env->FindClass("com/luo/face/module/BoxRetina");


    jfieldID box_x1 = env->GetFieldID(box_jcls, "x1", "I");
    jfieldID box_y1 = env->GetFieldID(box_jcls, "y1", "I");
    jfieldID box_x2 = env->GetFieldID(box_jcls, "x2", "I");
    jfieldID box_y2 = env->GetFieldID(box_jcls, "y2", "I");

    jfieldID box_landmarks = env->GetFieldID(box_jcls, "landmarks", "[Landroid/graphics/PointF;");

    // -------------------------- 创建box对象----------------------------------


    // -------------------------- 创建rectF对象----------------------------------
    jclass tmp_rectF_jcls = env->FindClass("android/graphics/PointF");
    jfieldID tmp_rectF_x = env->GetFieldID(tmp_rectF_jcls, "x", "F");
    jfieldID tmp_rectF_y = env->GetFieldID(tmp_rectF_jcls, "y", "F");
    // -------------------------- 创建rectF对象----------------------------------


    ncnn::Extractor ex = retinaface2.create_extractor();
    ex.set_num_threads(2);
    ncnn::Mat in = ncnn::Mat::from_android_bitmap(env, bitmap, ncnn::Mat::PIXEL_BGR);
    std::vector<FaceObject> objs = detect_retinaface(retinaface2, in);
    for (auto obj: objs) {

        jobjectArray tmpArray = env->NewObjectArray(5, rectF_jcls, nullptr);
        for (int i = 0; i < 5; i++) {
            env->PushLocalFrame(1);
            jobject rect_obj = env->AllocObject(tmp_rectF_jcls);
            env->SetFloatField(rect_obj, tmp_rectF_x, obj.landmark[i].x * ratio);
            env->SetFloatField(rect_obj, tmp_rectF_y, obj.landmark[i].y * ratio);
            rect_obj = env->PopLocalFrame(rect_obj);
            env->SetObjectArrayElement(tmpArray, i, rect_obj);
        }

        jobject jobj = env->AllocObject(box_jcls);
        env->SetIntField(jobj, box_x1, (int) (obj.rect.x * ratio));
        env->SetIntField(jobj, box_y1, (int) (obj.rect.y * ratio));
        env->SetIntField(jobj, box_x2, (int) (obj.rect.x * ratio + obj.rect.width * ratio));
        env->SetIntField(jobj, box_y2, (int) (obj.rect.y * ratio + obj.rect.height * ratio));

        env->SetObjectField(jobj, box_landmarks, tmpArray);

        env->CallBooleanMethod(list_obj, list_add, jobj);
    }

    return list_obj;
}extern "C"
JNIEXPORT jobject JNICALL
Java_com_luo_face_RetinaFaceDB_detectWithROI(
        JNIEnv *env,
        jobject thiz,
        jobject bitmap,
        jfloat ratio,
        jfloatArray my_rect
) {

    jfloat *wav_date;
    wav_date = env->GetFloatArrayElements(my_rect, 0);

    // -------------------------- 创建ArrayList对象----------------------------
    jclass list_jcls = env->FindClass("java/util/ArrayList");

    jmethodID list_init = env->GetMethodID(list_jcls, "<init>", "()V");

    jobject list_obj = env->NewObject(list_jcls, list_init);

    // ArrayList 中的add方法
    jmethodID list_add = env->GetMethodID(list_jcls, "add", "(Ljava/lang/Object;)Z");
    // -------------------------- 创建ArrayList对象----------------------------


    // -------------------------- 创建landmark的array对象----------------------
    jclass rectF_jcls = env->FindClass("android/graphics/PointF");

    // -------------------------- 创建landmark的array对象----------------------

    // -------------------------- 创建box对象----------------------------------
    jclass box_jcls = env->FindClass("com/luo/face/module/BoxRetina");


    jfieldID box_x1 = env->GetFieldID(box_jcls, "x1", "I");
    jfieldID box_y1 = env->GetFieldID(box_jcls, "y1", "I");
    jfieldID box_x2 = env->GetFieldID(box_jcls, "x2", "I");
    jfieldID box_y2 = env->GetFieldID(box_jcls, "y2", "I");

    jfieldID box_landmarks = env->GetFieldID(box_jcls, "landmarks", "[Landroid/graphics/PointF;");

    // -------------------------- 创建box对象----------------------------------


    // -------------------------- 创建rectF对象----------------------------------
    jclass tmp_rectF_jcls = env->FindClass("android/graphics/PointF");
    jfieldID tmp_rectF_x = env->GetFieldID(tmp_rectF_jcls, "x", "F");
    jfieldID tmp_rectF_y = env->GetFieldID(tmp_rectF_jcls, "y", "F");
    // -------------------------- 创建rectF对象----------------------------------


    ncnn::Extractor ex = retinaface2.create_extractor();
    ex.set_num_threads(2);
    ncnn::Mat in = ncnn::Mat::from_android_bitmap(env, bitmap, ncnn::Mat::PIXEL_BGR);
    std::vector<FaceObject> objs = detect_retinaface(retinaface2, in);
    for (auto obj: objs) {

        jobjectArray tmpArray = env->NewObjectArray(5, rectF_jcls, nullptr);
        for (int i = 0; i < 5; i++) {
            env->PushLocalFrame(1);
            jobject rect_obj = env->AllocObject(tmp_rectF_jcls);
            env->SetFloatField(rect_obj, tmp_rectF_x, obj.landmark[i].x * ratio + wav_date[0]);
            env->SetFloatField(rect_obj, tmp_rectF_y, obj.landmark[i].y * ratio + wav_date[1]);
            rect_obj = env->PopLocalFrame(rect_obj);
            env->SetObjectArrayElement(tmpArray, i, rect_obj);
        }

        jobject jobj = env->AllocObject(box_jcls);
        env->SetIntField(jobj, box_x1, (int) (obj.rect.x * ratio + wav_date[0]));
        env->SetIntField(jobj, box_y1, (int) (obj.rect.y * ratio + wav_date[1]));
        env->SetIntField(jobj, box_x2, (int) (obj.rect.x * ratio + obj.rect.width * ratio + wav_date[0]));
        env->SetIntField(jobj, box_y2, (int) (obj.rect.y * ratio + obj.rect.height * ratio + wav_date[1]));

        env->SetObjectField(jobj, box_landmarks, tmpArray);

        env->CallBooleanMethod(list_obj, list_add, jobj);
    }

    return list_obj;
}
