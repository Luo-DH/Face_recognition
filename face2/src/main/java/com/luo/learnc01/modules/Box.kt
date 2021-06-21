package com.luo.learnc01.modules

import android.graphics.PointF
import android.graphics.RectF

data class Box(
    val x1: Int,
    val y1: Int,
    val x2: Int,
    val y2: Int,

    val landmarks: Array<PointF>
)