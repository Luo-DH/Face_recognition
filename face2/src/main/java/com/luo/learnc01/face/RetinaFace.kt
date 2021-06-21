package com.luo.learnc01.face

import android.content.res.AssetManager
import android.graphics.Bitmap
import com.luo.learnc01.modules.Bbox
import com.luo.learnc01.modules.Box

class RetinaFace {

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }


    /**
     * 初始化模型
     */
    external fun init(manager: AssetManager): Boolean

    /**
     * 人脸检测方法
     */
    external fun detect(bitmap: Bitmap): FloatArray

    external fun detect2(bitmap: Bitmap): Array<Bbox>

    external fun detect3(bitmap: Bitmap, ratio: Float): Array<Bbox>

    external fun detect4(bitmap: Bitmap, ratio: Float, rect: FloatArray): Array<Bbox>

    external fun detect5(bitmap: Bitmap, ratio: Float, rect: FloatArray): List<Box>
}