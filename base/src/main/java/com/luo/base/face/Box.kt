package com.luo.base.face

import android.graphics.PointF

data class Box(
    val x1: Int,
    val y1: Int,
    val x2: Int,
    val y2: Int,

    val landmarks: Array<PointF>
)