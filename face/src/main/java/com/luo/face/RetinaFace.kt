package com.luo.face

import android.content.res.AssetManager
import android.graphics.Bitmap
import com.luo.face.module.BoxRetina

/**
 * @author: Luo-DH
 * @date: 1/24/21
 *      人脸检测方法
 *      - 先调用init方法对模型进行初始化
 */
object RetinaFace {

    init {
        System.loadLibrary("native-lib")
    }

    //加载模型接口 AssetManager用于加载assert中的权重文件
    external fun init(mgr: AssetManager?):Boolean

    external fun detect(bitmap: Bitmap?, ratio: Float): List<BoxRetina>

    external fun detectWithROI(bitmap: Bitmap?, ratio: Float, rect: FloatArray): List<BoxRetina>

}