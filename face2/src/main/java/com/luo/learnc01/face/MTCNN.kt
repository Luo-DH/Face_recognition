package com.luo.learnc01.face

import android.content.res.AssetManager
import android.graphics.Bitmap
import com.luo.learnc01.modules.Bbox

class MTCNN {
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
     * 人脸检测方法
     */
    external fun detect(bitmap: Bitmap, width: Int, height: Int): Array<Bbox>

}