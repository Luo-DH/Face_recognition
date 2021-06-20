//
// Created by LDH on 1/24/21.
//
#include "jni.h"
#include "android/asset_manager_jni.h"
#include "retinaface/RetinaFace.hpp"
#include "cpu.h"

static ncnn::Net retinaface2;
#define USE_GPU

#ifdef USE_GPU
static ncnn::UnlockedPoolAllocator *blob_pool_allocator = 0;
static ncnn::UnlockedPoolAllocator *workspace_pool_allocator = 0;

static ncnn::VulkanDevice *vkdev = 0;
static ncnn::VkBlobAllocator *blob_vkallocator = 0;
static ncnn::VkStagingAllocator *staging_vkallocator = 0;
#endif
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_luo_face_RetinaFace_init(JNIEnv *env, jobject thiz, jobject asset_manager) {

#ifdef USE_GPU
    // ***********************开启gpu加速***********************
    ncnn::Option opt;
    opt.lightmode = true;
    opt.num_threads = 2;

    blob_pool_allocator = new ncnn::UnlockedPoolAllocator;
    workspace_pool_allocator = new ncnn::UnlockedPoolAllocator;

    opt.blob_allocator = blob_pool_allocator;
    opt.workspace_allocator = workspace_pool_allocator;

    const int gpu_device = 0;// FIXME hardcode
    vkdev = ncnn::get_gpu_device(0);

    blob_vkallocator = new ncnn::VkBlobAllocator(vkdev);
    staging_vkallocator = new ncnn::VkStagingAllocator(vkdev);

    opt.blob_vkallocator = blob_vkallocator;
    opt.workspace_vkallocator = blob_vkallocator;
    opt.staging_vkallocator = staging_vkallocator;


    opt.use_winograd_convolution = true;
    opt.use_sgemm_convolution = true;

    opt.use_vulkan_compute = true;

    opt.use_fp16_packed = true;
    opt.use_fp16_storage = true;
    opt.use_fp16_arithmetic = true;
    opt.use_int8_storage = true;
    opt.use_int8_arithmetic = false;

    opt.use_shader_pack8 = true;

    opt.use_bf16_storage = true;

    ncnn::set_cpu_powersave(0);

    // use vulkan compute
    if (ncnn::get_gpu_count() != 0)
        opt.use_vulkan_compute = true;

    retinaface2.opt = opt;
    // ***********************开启gpu加速***********************
#endif

    // ***********************读取权重文件***********************
    AAssetManager *mgr = AAssetManager_fromJava(env, asset_manager);
    //init param
    retinaface2.load_param(mgr, "mnet.25-opt.param");
    // init bin
    retinaface2.load_model(mgr, "mnet.25-opt.bin");
    // ***********************读取权重文件***********************
    return true;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_luo_face_RetinaFace_detect(
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
Java_com_luo_face_RetinaFace_detectWithROI(
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
        env->SetIntField(jobj, box_x2,
                         (int) (obj.rect.x * ratio + obj.rect.width * ratio + wav_date[0]));
        env->SetIntField(jobj, box_y2,
                         (int) (obj.rect.y * ratio + obj.rect.height * ratio + wav_date[1]));

        env->SetObjectField(jobj, box_landmarks, tmpArray);

        env->CallBooleanMethod(list_obj, list_add, jobj);
    }

    return list_obj;
}
