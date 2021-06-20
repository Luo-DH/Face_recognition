package com.luo.face.module

import android.graphics.PointF

/**
 * @author: Luo-DH
 * @date: 1/24/21
 */
data class BoxRetina(
        val x1: Int,
        val y1: Int,
        val x2: Int,
        val y2: Int,

        val landmarks: Array<PointF>
)