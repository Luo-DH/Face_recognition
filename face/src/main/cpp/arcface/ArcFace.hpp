//
// Created by LDH on 12/22/20.
//

#ifndef LEARNC01_ARCFACE_HPP
#define LEARNC01_ARCFACE_HPP

#include <cmath>
#include <vector>
#include <string>
#include "net.h"

using namespace std;
float calcSimilar(std::vector<float> feature1, std::vector<float> feature2);
double calculSimilar(std::vector<float>& v1, std::vector<float>& v2, int distance_metric);

class ArcFace {

public:
    ArcFace(AAssetManager *mgr);

    ~ArcFace();

    vector<float> getFeature(ncnn::Mat img);

    static ArcFace *face;

private:
    ncnn::Net net;

    const int feature_dim = 128;

    void normalize(vector<float> &feature);

//    ncnn::Mat preprocess(ncnn::Mat img, int *info);

//    double calculSimilar(std::vector<float>& v1, std::vector<float>& v2, int distance_metric);
};

#endif
