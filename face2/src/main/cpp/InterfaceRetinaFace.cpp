//
// Created by LDH on 12/23/20.
//
#include "jni.h"
#include "android/asset_manager_jni.h"
#include "retinaface/RetinaFace.hpp"
#include "cpu.h"

#define TAG "MtcnnSo"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__)

static ncnn::Net retinaface;
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_luo_learnc01_face_RetinaFace_init(JNIEnv *env, jobject thiz, jobject assetManager) {
    AAssetManager *mgr = AAssetManager_fromJava(env, assetManager);
//    retinaface.opt.use_vulkan_compute = true;
//    retinaface.opt.use_int8_arithmetic = true;
//    retinaface.opt.use_fp16_arithmetic = true;
    //init param
    int ret = retinaface.load_param(mgr, "mnet.25-opt.param");
//    int ret = retinaface.load_param(mgr, "face.param");
    if (ret != 0) {
        __android_log_print(ANDROID_LOG_DEBUG, "RetinaFace", "load_param failed");
        return JNI_FALSE;
    }
    //init bin
    ret = retinaface.load_model(mgr, "mnet.25-opt.bin");
    if (ret != 0) {
        __android_log_print(ANDROID_LOG_DEBUG, "RetinaFace", "load_model failed");
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_luo_learnc01_face_RetinaFace_detect(JNIEnv *env, jobject thiz, jobject bitmap) {

    ncnn::Extractor ex = retinaface.create_extractor();
//    ex.set_num_threads(4);
    ncnn::Mat in = ncnn::Mat::from_android_bitmap(env, bitmap, ncnn::Mat::PIXEL_BGR);
    std::vector<FaceObject> objs = detect_retinaface(retinaface, in);
    int count = static_cast<int>(objs.size()), ix = 0;
    LOGD("time: %d", count);
    if (count <= 0)
        return nullptr;
    //result to 1D-array
    count = static_cast<int>(count * 14);
    float *face_info = new float[count];


    std::vector<Bbox> finalBbox;

    for (auto obj : objs) {
        LOGD("time x1:%lf x2:%lf", obj.rect.x, obj.rect.y);
        Bbox box = Bbox();
        for (int k = 0; k < 9; k += 2) {
            box.ppoint[k] = obj.landmark[k / 2].x;
            box.ppoint[k + 1] = obj.landmark[k / 2].y;
        }
        box.x1 = obj.rect.x;
        box.x2 = obj.rect.x + obj.rect.width;
        box.y1 = obj.rect.y;
        box.y2 = obj.rect.y + obj.rect.height;

        box.area = (float) (box.x2 - box.x1) * (float) (box.y2 - box.y1);
        box.exist = true;

        finalBbox.push_back(box);

        face_info[ix++] = obj.rect.x;
        face_info[ix++] = obj.rect.y;
        face_info[ix++] = obj.rect.x + obj.rect.width;
        face_info[ix++] = obj.rect.y + obj.rect.height;
        for (int j = 0; j < 5; j++) {
            face_info[ix++] = obj.landmark[j].x;
            face_info[ix++] = obj.landmark[j].y;
        }
    }
    jfloatArray tFaceInfo = env->NewFloatArray(count);
    env->SetFloatArrayRegion(tFaceInfo, 0, count, face_info);
    delete[] face_info;


    auto result = finalBbox;
    auto box_cls = env->FindClass("com/luo/learnc01/modules/Bbox");
    auto cid = env->GetMethodID(box_cls, "<init>", "(IIIIFFFFFFFFFFFZ)V");
    jobjectArray ret = env->NewObjectArray(result.size(), box_cls, nullptr);
    int ratio = 1;
    int i = 0;
    for (auto &box:result) {
        env->PushLocalFrame(1);
        jobject obj = env->NewObject(
                box_cls,
                cid,
                box.x1 * ratio,
                box.y1 * ratio,
                box.x2 * ratio,
                box.y2 * ratio,
                box.ppoint[0] * ratio,
                box.ppoint[1] * ratio,
                box.ppoint[2] * ratio,
                box.ppoint[3] * ratio,
                box.ppoint[4] * ratio,
                box.ppoint[5] * ratio,
                box.ppoint[6] * ratio,
                box.ppoint[7] * ratio,
                box.ppoint[8] * ratio,
                box.ppoint[9] * ratio,
                box.area,   // 面积
                box.exist  // 是否有人脸
        );
        obj = env->PopLocalFrame(obj);
        env->SetObjectArrayElement(ret, i++, obj);
    }

//    for (int i = 0; i < 14; i++) {
//        LOGD("time: %d %d", i, tFaceInfo[i]);
//    }

    return tFaceInfo;

}


extern "C"
JNIEXPORT jobjectArray JNICALL
Java_com_luo_learnc01_face_RetinaFace_detect2(JNIEnv *env, jobject thiz, jobject bitmap) {

    ncnn::Extractor ex = retinaface.create_extractor();
    ncnn::Mat in = ncnn::Mat::from_android_bitmap(env, bitmap, ncnn::Mat::PIXEL_BGR);
    std::vector<FaceObject> objs = detect_retinaface(retinaface, in);

    int count = static_cast<int>(objs.size());

    auto box_cls = env->FindClass("com/luo/learnc01/modules/Bbox");
    auto cid = env->GetMethodID(box_cls, "<init>", "(IIIIFFFFFFFFFFFZ)V");
    jobjectArray ret = env->NewObjectArray(count, box_cls, nullptr);

    std::vector<Bbox> finalBbox;
    int i = 0;

    for (auto obj_ : objs) {
        env->PushLocalFrame(1);

        jobject obj = env->NewObject(
                box_cls,
                cid,
                (int) obj_.rect.x,
                (int) obj_.rect.y,
                (int) (obj_.rect.x + obj_.rect.width),
                (int) (obj_.rect.y + obj_.rect.height),
                obj_.landmark[0].x,
                obj_.landmark[0].y,
                obj_.landmark[1].x,
                obj_.landmark[1].y,
                obj_.landmark[2].x,
                obj_.landmark[2].y,
                obj_.landmark[3].x,
                obj_.landmark[3].y,
                obj_.landmark[4].x,
                obj_.landmark[4].y,
                (obj_.rect.width * obj_.rect.height),
                true
        );
        obj = env->PopLocalFrame(obj);
        env->SetObjectArrayElement(ret, i++, obj);
    }


    return ret;
}


extern "C"
JNIEXPORT jobjectArray JNICALL
Java_com_luo_learnc01_face_RetinaFace_detect3(JNIEnv *env, jobject thiz, jobject bitmap,
                                              jfloat ratio) {

    ncnn::Extractor ex = retinaface.create_extractor();
    ncnn::Mat in = ncnn::Mat::from_android_bitmap(env, bitmap, ncnn::Mat::PIXEL_BGR);
    std::vector<FaceObject> objs = detect_retinaface(retinaface, in);

    int count = static_cast<int>(objs.size());

    auto box_cls = env->FindClass("com/luo/learnc01/modules/Bbox");
    auto cid = env->GetMethodID(box_cls, "<init>", "(IIIIFFFFFFFFFFFZ)V");
    jobjectArray ret = env->NewObjectArray(count, box_cls, nullptr);

    std::vector<Bbox> finalBbox;
    int i = 0;

    for (auto obj_ : objs) {
        env->PushLocalFrame(1);

        jobject obj = env->NewObject(
                box_cls,
                cid,
                (int) (obj_.rect.x * ratio),
                (int) (obj_.rect.y * ratio),
                (int) (obj_.rect.x * ratio + obj_.rect.width * ratio),
                (int) (obj_.rect.y * ratio + obj_.rect.height * ratio),
                obj_.landmark[0].x * ratio,
                obj_.landmark[0].y * ratio,
                obj_.landmark[1].x * ratio,
                obj_.landmark[1].y * ratio,
                obj_.landmark[2].x * ratio,
                obj_.landmark[2].y * ratio,
                obj_.landmark[3].x * ratio,
                obj_.landmark[3].y * ratio,
                obj_.landmark[4].x * ratio,
                obj_.landmark[4].y * ratio,
                ((obj_.rect.width * ratio) * (obj_.rect.height * ratio)),
                true
        );
        obj = env->PopLocalFrame(obj);
        env->SetObjectArrayElement(ret, i++, obj);
    }


    return ret;
}


extern "C"
JNIEXPORT jobjectArray JNICALL
Java_com_luo_learnc01_face_RetinaFace_detect4(JNIEnv *env, jobject thiz, jobject bitmap,
                                              jfloat ratio, jfloatArray rect) {

    jfloat *wav_date;
    wav_date = env->GetFloatArrayElements(rect, 0);


    ncnn::Extractor ex = retinaface.create_extractor();
    ncnn::Mat in = ncnn::Mat::from_android_bitmap(env, bitmap, ncnn::Mat::PIXEL_BGR);
    std::vector<FaceObject> objs = detect_retinaface(retinaface, in);

    int count = static_cast<int>(objs.size());

    auto box_cls = env->FindClass("com/luo/learnc01/modules/Bbox");
    auto cid = env->GetMethodID(box_cls, "<init>", "(IIIIFFFFFFFFFFFZ)V");
    jobjectArray ret = env->NewObjectArray(count, box_cls, nullptr);

    std::vector<Bbox> finalBbox;
    int i = 0;

    for (auto obj_ : objs) {
        env->PushLocalFrame(1);

        jobject obj = env->NewObject(
                box_cls,
                cid,
                (int) (obj_.rect.x * ratio + wav_date[0]),
                (int) (obj_.rect.y * ratio + wav_date[1]),
                (int) (obj_.rect.x * ratio + obj_.rect.width * ratio + wav_date[0]),
                (int) (obj_.rect.y * ratio + obj_.rect.height * ratio + wav_date[1]),
                obj_.landmark[0].x * ratio,
                obj_.landmark[0].y * ratio,
                obj_.landmark[1].x * ratio,
                obj_.landmark[1].y * ratio,
                obj_.landmark[2].x * ratio,
                obj_.landmark[2].y * ratio,
                obj_.landmark[3].x * ratio,
                obj_.landmark[3].y * ratio,
                obj_.landmark[4].x * ratio,
                obj_.landmark[4].y * ratio,
                ((obj_.rect.width * ratio) * (obj_.rect.height * ratio)),
                true
        );
        obj = env->PopLocalFrame(obj);
        env->SetObjectArrayElement(ret, i++, obj);
    }


    return ret;
}


extern "C"
JNIEXPORT jobject JNICALL
Java_com_luo_learnc01_face_RetinaFace_detect5(JNIEnv *env, jobject thiz, jobject bitmap,
                                              jfloat ratio, jfloatArray rect) {

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
    jclass box_jcls = env->FindClass("com/luo/learnc01/modules/Box");


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


    ncnn::Extractor ex = retinaface.create_extractor();
    ncnn::Mat in = ncnn::Mat::from_android_bitmap(env, bitmap, ncnn::Mat::PIXEL_BGR);
    std::vector<FaceObject> objs = detect_retinaface(retinaface, in);


    for (auto obj: objs) {

        jobjectArray tmpArray = env->NewObjectArray(5, rectF_jcls, nullptr);
        for (int i = 0; i < 5; i++) {
            env->PushLocalFrame(1);
            jobject rect_obj = env->AllocObject(tmp_rectF_jcls);
            env->SetFloatField(rect_obj, tmp_rectF_x, obj.landmark[i].x);
            env->SetFloatField(rect_obj, tmp_rectF_y, obj.landmark[i].y);
            rect_obj = env->PopLocalFrame(rect_obj);
            env->SetObjectArrayElement(tmpArray, i, rect_obj);
        }

        jobject jobj = env->AllocObject(box_jcls);
        env->SetIntField(jobj, box_x1, (int) obj.rect.x);
        env->SetIntField(jobj, box_y1, (int) obj.rect.y);
        env->SetIntField(jobj, box_x2, (int) (obj.rect.x + obj.rect.width));
        env->SetIntField(jobj, box_y2, (int) (obj.rect.y + obj.rect.height));

        env->SetObjectField(jobj, box_landmarks, tmpArray);

        env->CallBooleanMethod(list_obj, list_add, jobj);
    }

    return list_obj;

}


static ncnn::Net retinaface2;

static ncnn::UnlockedPoolAllocator* blob_pool_allocator1 = 0;
static ncnn::UnlockedPoolAllocator* workspace_pool_allocator1 = 0;

static ncnn::VulkanDevice* vkdev1 = 0;
static ncnn::VkBlobAllocator* blob_vkallocator1 = 0;
static ncnn::VkStagingAllocator* staging_vkallocator1 = 0;



extern "C"
JNIEXPORT jboolean JNICALL
Java_com_luo_learnc01_face_RetinaFace2_init(JNIEnv *env, jobject thiz, jobject assetManager) {
    AAssetManager *mgr = AAssetManager_fromJava(env, assetManager);


    ncnn::Option opt;
    opt.lightmode = true;
    opt.num_threads = 4;

    blob_pool_allocator1 = new ncnn::UnlockedPoolAllocator;
    workspace_pool_allocator1 = new ncnn::UnlockedPoolAllocator;

    opt.blob_allocator = blob_pool_allocator1;
    opt.workspace_allocator = workspace_pool_allocator1;

    const int gpu_device = 0;// FIXME hardcode
    vkdev1 = ncnn::get_gpu_device(0);

    blob_vkallocator1 = new ncnn::VkBlobAllocator(vkdev1);
    staging_vkallocator1 = new ncnn::VkStagingAllocator(vkdev1);

    opt.blob_vkallocator = blob_vkallocator1;
    opt.workspace_vkallocator = blob_vkallocator1;
    opt.staging_vkallocator = staging_vkallocator1;


    opt.use_winograd_convolution = true;
    opt.use_sgemm_convolution = true;

    opt.use_vulkan_compute = true;

    opt.use_fp16_packed = true;
    opt.use_fp16_storage = true;
    opt.use_fp16_arithmetic = true;
    opt.use_int8_storage = true;
    opt.use_int8_arithmetic = false;

    opt.use_shader_pack8 = true;
//
    opt.use_bf16_storage = false;

    ncnn::set_cpu_powersave(0);


    retinaface2.opt = opt;
    // use vulkan compute
//    if (ncnn::get_gpu_count() != 0)
        opt.use_vulkan_compute = true;

    retinaface2.opt = opt;




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
Java_com_luo_learnc01_face_RetinaFace2_detect(
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
    jclass box_jcls = env->FindClass("com/luo/learnc01/modules/Box");


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
            LOGD("%f %f", obj.landmark[i].x, obj.landmark[i].y);
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
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_luo_learnc01_face_RetinaFace2_detectWithROI(
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
    jclass box_jcls = env->FindClass("com/luo/learnc01/modules/Box");


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


extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_luo_learnc01_face_RetinaFace2_detect2(JNIEnv *env, jobject thiz, jobject bitmap,
                                               jfloat ratio) {

    ncnn::Extractor ex = retinaface2.create_extractor();
    ex.set_num_threads(2);
    ncnn::Mat in = ncnn::Mat::from_android_bitmap(env, bitmap, ncnn::Mat::PIXEL_BGR);
    std::vector<FaceObject> objs = detect_retinaface(retinaface2, in);
    int count = static_cast<int>(objs.size()), ix = 0;
    if (count <= 0)
        return nullptr;
    //result to 1D-array
    count = static_cast<int>(count * 14);
    float *face_info = new float[count];
    for (auto obj : objs) {
        face_info[ix++] = obj.rect.x;
        face_info[ix++] = obj.rect.y;
        face_info[ix++] = obj.rect.x + obj.rect.width;
        face_info[ix++] = obj.rect.y + obj.rect.height;
        for (int j = 0; j < 5; j++) {
            face_info[ix++] = obj.landmark[j].x;
            face_info[ix++] = obj.landmark[j].y;
        }
    }
    jfloatArray tFaceInfo = env->NewFloatArray(count);
    env->SetFloatArrayRegion(tFaceInfo, 0, count, face_info);
    delete[] face_info;

    return tFaceInfo;
}

