package com.luo.face

import android.content.res.AssetManager
import android.graphics.Bitmap

/**
 * @author: Luo-DH
 * @date: 1/25/21
 *      人脸识别方法
 */
object ArcFace {
    init {
        System.loadLibrary("native-lib")
    }

    /**
     * 初始化模型
     */
    external fun init(manager: AssetManager)

    /**
     * 获得单张图片的特征值
     */
    external fun getFeature(bitmap: Bitmap): FloatArray

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