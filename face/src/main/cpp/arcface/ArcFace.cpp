//
// Created by LDH on 12/22/20.
//
#include "ArcFace.hpp"
#include <opencv2/core.hpp>
#include "cpu.h"

#define TAG "MtcnnSo"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__)

ArcFace *ArcFace::face = nullptr;

//#define USE_GPU

#ifdef USE_GPU
static ncnn::UnlockedPoolAllocator *blob_pool_allocator = 0;
static ncnn::UnlockedPoolAllocator *workspace_pool_allocator = 0;

static ncnn::VulkanDevice *vkdev = 0;
static ncnn::VkBlobAllocator *blob_vkallocator = 0;
static ncnn::VkStagingAllocator *staging_vkallocator = 0;
#endif
ArcFace::ArcFace(AAssetManager *mgr) {

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

//    opt.use_fp16_packed = true;
//    opt.use_fp16_storage = true;
//    opt.use_fp16_arithmetic = true;
//    opt.use_int8_storage = true;
//    opt.use_int8_arithmetic = false;
//
//    opt.use_shader_pack8 = true;
//
//    opt.use_bf16_storage = true;

    ncnn::set_cpu_powersave(0);

    // use vulkan compute
    if (ncnn::get_gpu_count() != 0)
        opt.use_vulkan_compute = true;

    this->net.opt = opt;
    // ***********************开启gpu加速***********************
#endif


    const char *param = "mobilefacenet.param";
    const char *bin = "mobilefacenet.bin";

    this->net.opt.use_vulkan_compute = true;
//    this->net.opt.use_int8_arithmetic = true;
//    this->net.opt.use_fp16_arithmetic = true;
    this->net.load_param(mgr, param);
    this->net.load_model(mgr, bin);
}

ArcFace::~ArcFace() {
    this->net.clear();
}
ncnn::Mat bgr2rgb(ncnn::Mat src)
{
    int src_w = src.w;
    int src_h = src.h;
    unsigned char* u_rgb = new unsigned char[src_w * src_h * 3];
    src.to_pixels(u_rgb, ncnn::Mat::PIXEL_BGR2RGB);
    ncnn::Mat dst = ncnn::Mat::from_pixels(u_rgb, ncnn::Mat::PIXEL_RGB, src_w, src_h);
    delete[] u_rgb;
    return dst;
}

void ArcFace::normalize(vector<float> &feature) {
    float sum = 0;
    for (auto it = feature.begin(); it != feature.end(); it++)
        sum += (float) *it * (float) *it;
    sum = sqrt(sum);
    for (auto it = feature.begin(); it != feature.end(); it++)
        *it /= sum;

}

vector<float> ArcFace::getFeature(ncnn::Mat in) {
//    ncnn::Extractor ex = net.create_extractor();
//    ex.set_num_threads(2);
//    ex.set_light_mode(true);
//    ex.input("data", in);     // input node
//    ncnn::Mat out;
//    ex.extract("fc1", out);     // output node
//    ncnn::Mat test;
//    for (int j = 0; j < 128; j++)
//    {
//        feature_out[j] = out[j];
//    }
//    normalize(feature_out);

    vector<float> feature;
    //cv to NCNN
//    in = bgr2rgb(in);
    ncnn::Extractor ex = net.create_extractor();
    ex.set_light_mode(true);
    ex.set_num_threads(2);
//    ex.set_vulkan_compute(true);
    ex.input("data", in);
    ncnn::Mat out;
    ex.extract("fc1", out);
    feature.resize(this->feature_dim);
    for (int i = 0; i < this->feature_dim; i++)
        feature[i] = out[i];
    normalize(feature);
//    cv::Mat feature__ = cv::Mat(feature, true);
    return feature;
}


float calcSimilar(std::vector<float> feature1, std::vector<float> feature2) {
    //assert(feature1.size() == feature2.size());
    float sim = 0.0;
    for (int i = 0; i < feature1.size(); i++)
        sim += feature1[i] * feature2[i];
    LOGD("LogUtils 差值：%lf\n", sim);
    return sim;
}

/**
 * This is a normalize function before calculating the cosine distance. Experiment has proven it can destory the
 * original distribution in order to make two feature more distinguishable.
 * mean value is set to 0 and std is set to 1
 */
cv::Mat Zscore(const cv::Mat &fc) {
    cv::Mat mean, std;
    meanStdDev(fc, mean, std);
    //cout <<"mean is :"<< mean <<"std is :"<< std << endl;
    cv::Mat fc_norm = (fc - mean) / std;
    return fc_norm;
}

double calculSimilar(std::vector<float>& v1, std::vector<float>& v2, int distance_metric) {
    if (v1.size() != v2.size() || !v1.size())
        return 0;
    double ret = 0.0, mod1 = 0.0, mod2 = 0.0, dist = 0.0, diff = 0.0;

    if (distance_metric == 0) {         // Euclidian distance
        for (std::vector<double>::size_type i = 0; i != v1.size(); ++i) {
            diff = v1[i] - v2[i];
            dist += (diff * diff);
        }
        dist = sqrt(dist);
    } else {                              // Distance based on cosine similarity
        for (std::vector<double>::size_type i = 0; i != v1.size(); ++i) {
            ret += v1[i] * v2[i];
            mod1 += v1[i] * v1[i];
            mod2 += v2[i] * v2[i];
        }
        dist = ret / (sqrt(mod1) * sqrt(mod2));
    }
    return dist;
}

/**
 * This module is using to computing the cosine distance between input feature and ground truth feature
 *  */
inline float CosineDistance(const cv::Mat &v1, const cv::Mat &v2) {
    double dot = v1.dot(v2);
    double denom_v1 = norm(v1);
    double denom_v2 = norm(v2);
    return dot / (denom_v1 * denom_v2);
}



//// 人脸对其
//ncnn::Mat ArcFace::preprocess(ncnn::Mat img, int *info) {
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
//    for (int i = 0; i < 10; i++) {
//        src[i] = info[i];
//    }
//
//    float M[6];
//    getAffineMatrix(src, dst, M);
//    ncnn::Mat out;
//    warpAffineMatrix(img, out, M, image_w, image_h);
//    return out;
//}




