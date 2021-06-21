package com.luo.learnc01.modules

import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import java.lang.Integer.min

data class Bbox(
    val x1: Int,
    val y1: Int,
    val x2: Int,
    val y2: Int,

    val landmark1: Float,
    val landmark2: Float,
    val landmark3: Float,
    val landmark4: Float,
    val landmark5: Float,
    val landmark6: Float,
    val landmark7: Float,
    val landmark8: Float,
    val landmark9: Float,
    val landmark10: Float,

    val area: Float,
    val exist: Boolean
)



