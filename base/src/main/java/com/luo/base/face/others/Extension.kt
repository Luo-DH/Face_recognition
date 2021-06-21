//package com.luo.base.face.others
//
//import android.graphics.Bitmap
//import android.graphics.Matrix
//import com.luo.base.face.Bbox
//import com.luo.base.face.Box
//import com.luo.base.face.DBMsg
//
//fun Bbox.toCropBitmap(bitmap: Bitmap) =
//    Bitmap.createBitmap(
//        bitmap,
//        this.x1,
//        this.y1,
//        (this.x2 - this.x1).coerceAtMost(bitmap.width),
//        (this.y2 - this.y1).coerceAtMost(bitmap.height)
//    )
//
////fun Box.toCropBitmap(bitmap: Bitmap) =
////    Bitmap.createBitmap(
////        bitmap,
////        this.x1,
////        this.y1,
////        (this.x2 - this.x1).coerceAtMost(bitmap.width - this.x1),
////        (this.y2 - this.y1).coerceAtMost(bitmap.height - this.y1)
////    )
//
//fun FloatArray.toCropBitmap(bitmap: Bitmap) =
//    Bitmap.createBitmap(
//        bitmap,
//        this[0].toInt(),
//        this[1].toInt(),
//        this[2].toInt() - this[0].toInt(),
//        this[3].toInt() - this[1].toInt()
//    )
//
//
//fun Bitmap.toRotaBitmap(): Bitmap {
//    val matrix = Matrix()
//    matrix.postRotate(-90F)
//    return Bitmap.createBitmap(
//        this,
//        0, 0,
////        Math.min(this.width, this.height),
////        Math.min(this.width, this.height),
//        width, height,
//        matrix,
//        false
//    )
//}
//
//fun Box.toGetLandmarks(): IntArray {
//    val landmarks = IntArray(10)
//    var i = 0
//    this.landmarks.forEach {
//        landmarks[i] = it.x.toInt() - this.x1
//        landmarks[i + 5] = it.y.toInt() - this.y1
//        i += 1
//    }
//    return landmarks
//}
//
//
//fun DBMsg.toRandom(i: Int) =
//    DBMsg(
//        this.floatArray.map {
//            it.plus(0.0001f + (i + Math.random().toFloat()) / 10000)
//        }.toFloatArray(),
//        this.bitmap
//    )
//
