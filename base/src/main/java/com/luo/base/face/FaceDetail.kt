package com.luo.base.face

import android.graphics.Bitmap

/**
 * @author: Luo-DH
 * @date: 2021/6/21
 */
data class FaceDetail(
    val name: String,
    val fea: FloatArray?,
    val smallBitmap: Bitmap
)
