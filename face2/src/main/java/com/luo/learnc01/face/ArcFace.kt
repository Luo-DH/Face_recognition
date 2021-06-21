package com.luo.learnc01.face

import android.content.res.AssetManager
import android.graphics.Bitmap

class ArcFace {
    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }

    /**
     * 初始化模型
     */
    external fun init(manager: AssetManager)

    /**
     * 获得单张图片的特征值
     */
    external fun getFeature(bitmap: Bitmap): FloatArray

    /**
     * 获得单张图片的特征值
     *      经过人脸矫正
     */
    external fun getFeatureWithWrap(bitmap: Bitmap, landmarks: IntArray): FloatArray


    external fun getFeatureWithWrap2(
        faceDate: ByteArray?,
        w: Int,
        h: Int,
        landmarks: IntArray?
    ): FloatArray?

    /**
     * 特征值比对
     */
    external fun calCosineDistance(feature1: FloatArray, feature2: FloatArray): Float

    // 特征值比对
    external fun compareFeature(
        feature1: FloatArray?,
        feature2: FloatArray?
    ): Double
}