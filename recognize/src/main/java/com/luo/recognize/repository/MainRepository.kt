package com.luo.recognize.repository

import android.content.Context
import android.graphics.*
import androidx.camera.core.ImageProxy
import com.luo.face.ArcFace
import com.luo.face.RetinaFace
import com.luo.face.module.BoxRetina

import com.luo.recognize.modules.Bbox
import com.luo.recognize.modules.Box
import com.luo.recognize.modules.DBMsg
import com.luo.recognize.others.Utils
import java.io.ByteArrayOutputStream

class MainRepository {


    /**
     * 在bitmap上画出人脸框
     */
    fun drawBoxRects(mutableBitmap: Bitmap, box: Bbox?): Bitmap? {
        if (box == null) {
            return mutableBitmap
        }
        val canvas = Canvas(mutableBitmap)
        val boxPaint = Paint()
        boxPaint.alpha = 200
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = 4 * mutableBitmap.width / 800.0f
        boxPaint.textSize = 40 * mutableBitmap.width / 800.0f
        boxPaint.color = Color.RED
        boxPaint.style = Paint.Style.FILL

        boxPaint.style = Paint.Style.STROKE
        val rect = RectF(box.x1.toFloat(), box.y1.toFloat(), box.x2.toFloat(), box.y2.toFloat())
        canvas.drawRect(rect, boxPaint)
        canvas.drawCircle(box.landmark1, box.landmark2, 1F, boxPaint)
        canvas.drawCircle(box.landmark3, box.landmark4, 2F, boxPaint)
        canvas.drawCircle(box.landmark5, box.landmark6, 3F, boxPaint)
        canvas.drawCircle(box.landmark7, box.landmark8, 4F, boxPaint)
        canvas.drawCircle(box.landmark9, box.landmark10, 5F, boxPaint)
        return mutableBitmap
    }

    /**
     * 在bitmap上画出人脸框
     */
    fun drawBoxRects(mutableBitmap: Bitmap, box: BoxRetina?): Bitmap? {
        if (box == null) {
            return mutableBitmap
        }
        val canvas = Canvas(mutableBitmap)
        val boxPaint = Paint()
        boxPaint.alpha = 200
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = 4 * mutableBitmap.width / 800.0f
        boxPaint.textSize = 40 * mutableBitmap.width / 800.0f
        boxPaint.color = Color.RED
        boxPaint.style = Paint.Style.FILL

        boxPaint.style = Paint.Style.STROKE
        val rect = RectF(box.x1.toFloat(), box.y1.toFloat(), box.x2.toFloat(), box.y2.toFloat())
        canvas.drawRect(rect, boxPaint)
        box.landmarks.forEach {
            canvas.drawCircle(it.x, it.y, 5F, boxPaint)
        }

        return mutableBitmap
    }


    fun imageToBitmap(image: ImageProxy): Bitmap {
        val nv21 = imageToNV21(image)
        val yuvImage =
            YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(
            Rect(
                0,
                0,
                yuvImage.width,
                yuvImage.height
            ), 100, out
        )
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun imageToNV21(image: ImageProxy): ByteArray {
        val planes = image.planes
        val y = planes[0]
        val u = planes[1]
        val v = planes[2]
        val yBuffer = y.buffer
        val uBuffer = u.buffer
        val vBuffer = v.buffer
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        val nv21 = ByteArray(ySize + uSize + vSize)
        // U and V are swapped
        yBuffer[nv21, 0, ySize]
        vBuffer[nv21, ySize, vSize]
        uBuffer[nv21, ySize + vSize, uSize]
        return nv21
    }

    fun readFromAssets(context: Context) =
        HashMap<String, Bitmap>().apply {

            for (i in 1..80) {
                Utils.readFromAssets(context, "${i}.jpg").also {
                    if (it != null) {
                        this[i.toString()] = it
                    } else {
//                        LogUtils.d(i)
                    }
                }
            }
        }

    fun readFromAssetsByName(context: Context, myMap: HashMap<String, Bitmap>) =
        myMap.apply {
            this["zeng"] = Utils.readFromAssets(context, "ZENG.png")!!
            this["luo"] = Utils.readFromAssets(context, "LUO.jpg")!!
            this["ldh"] = Utils.readFromAssets(context, "LDH.jpg")!!
//            this["马家瑜"] = Utils.readFromAssets(context, "马家瑜.jpg")!!
        }


    fun detectDataBaseFaceRetina2(bitmap: Bitmap) =
        RetinaFace.detect(bitmap, 1f).let { box ->
            box.maxByOrNull { (it.x2 - it.x1) * (it.y2 - it.y1) }
        }

//
//    fun getDataBaseFeature(bitmap: Bitmap) =
//        ArcFace().getFeature(bitmap)

//    fun getDataBaseFeatureAndBitmap(bitmap: Bitmap, landmarks: IntArray) =
//        DBMsg(
//            ArcFace.getFeatureWithWrap2(bitmap, landmarks)!!,
//            bitmap
//        )

    fun getDataBaseFeatureAndBitmap2(
        bitmap: Bitmap, landmarks: IntArray
    ) =
        ArcFace.getFeatureWithWrap2(
            Utils.getPixelsRGBA(bitmap),
            bitmap.width,
            bitmap.height,
            landmarks
        )?.let {
            DBMsg(
                it,
                Utils.toCompressBitmap(bitmap)
            )
        }

}