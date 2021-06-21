package com.luo.learnc01.face

import android.content.res.AssetManager
import android.graphics.Bitmap
import com.luo.learnc01.modules.Box

class RetinaFace2 {

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }

    //加载模型接口 AssetManager用于加载assert中的权重文件
    external fun init(mgr: AssetManager?): Boolean

    //模型检测接口,其值=4-box + 5-landmark
    external fun detect(bitmap: Bitmap?, ratio: Float): List<Box>

    external fun detectWithROI(bitmap: Bitmap?, ratio: Float, rect: FloatArray): List<Box>

    external fun detect2(bitmap: Bitmap?, ratio: Float): FloatArray

    external fun test( bitmap: Bitmap?)
}