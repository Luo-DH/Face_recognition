package com.luo.face.module

import android.graphics.Bitmap

/**
 * @author: Luo-DH
 * @date: 2021/6/21
 */
data class FaceDetail(
    val name: String,
    val box: BoxRetina?,
    val fea: FloatArray?,
    val smallBitmap: Bitmap
)
