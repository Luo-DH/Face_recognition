package com.luo.recognize.others

import android.content.Context
import android.graphics.*
import com.luo.face.module.BoxRetina
import com.luo.recognize.modules.Bbox
import com.luo.recognize.modules.Box
import java.io.IOException
import java.nio.ByteBuffer

object Utils {
    /**
     * 从assets中读取图片
     *
     * @param context
     * @param filename
     * @return
     */
    fun readFromAssets(context: Context, filename: String?): Bitmap? {
        val bitmap: Bitmap
        val asm = context.assets
        try {
            val `is` = asm.open(filename!!)
            bitmap = BitmapFactory.decodeStream(`is`)
            `is`.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        return bitmap
    }
    /**
     * 在bitmap上画出人脸框
     */
    fun drawBoxRect(bitmap: Bitmap, box: Box?): Bitmap? {
        if (box == null) {
            return bitmap
        }
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val paint = Paint()
        paint.color = Color.RED
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f

        val rect = RectF(box.x1.toFloat(), box.y1.toFloat(), box.x2.toFloat(), box.y2.toFloat())
        canvas.drawRect(rect, paint)
        box.landmarks?.forEach {
            canvas.drawCircle(it.x, it.y, 5F, paint)
        }

        return mutableBitmap
    }


    /**
     * 按比例缩放图片
     *
     * @param origin 原图
     * @param ratio  比例
     * @return 新的bitmap
     */
    fun scaleBitmap(origin: Bitmap?, ratio: Float): Bitmap? {
        if (origin == null) {
            return null
        }
        val width = origin.width
        val height = origin.height
        val matrix = Matrix()
        matrix.preScale(ratio, ratio)
        val newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false)
        if (newBM == origin) {
            return newBM
        }
//        origin.recycle()
        return newBM
    }


    /**
     * 根据人脸框裁剪图片
     *
     * @param bitmap 输入的图片，可能包含人脸，也可能不包含人脸
     * @param box 人脸框信息，如果上一帧没有人脸，则为null
     */
    fun cropBitmap(bitmap: Bitmap, box: Bbox?): Bitmap =
        if (box == null) {
            bitmap
        } else {
            Bitmap.createBitmap(
                bitmap,
                (box.x1 - 30).coerceAtLeast(0),
                (box.y1 - 30).coerceAtLeast(0),
                (box.x2 - box.x1 + 90).coerceAtMost(
                    bitmap.width - (box.x1 - 30).coerceAtLeast(0)
                ),
                (box.y2 - box.y1 + 90).coerceAtMost(
                    bitmap.height - (box.y1 - 30).coerceAtLeast(0)
                )
            )
        }

    fun cropBitmap(bitmap: Bitmap, box: BoxRetina?): Bitmap =
        if (box == null) {
            bitmap
        } else {
            Bitmap.createBitmap(
                bitmap,
                (box.x1 - 30).coerceAtLeast(0),
                (box.y1 - 30).coerceAtLeast(0),
                (box.x2 - box.x1 + 90).coerceAtMost(
                    bitmap.width - (box.x1 - 30).coerceAtLeast(0)
                ),
                (box.y2 - box.y1 + 90).coerceAtMost(
                    bitmap.height - (box.y1 - 30).coerceAtLeast(0)
                )
            )
        }


    /**
     * 仿射变换
     *
     * @param bitmap   原图片
     * @param landmark landmark
     * @return 变换后的图片
     */
    fun warpAffine(bitmap: Bitmap, landmark: Array<PointF>): Bitmap? {
        val x =
            (landmark[0].x + landmark[1].x + landmark[2].x) / 3.toFloat()
        val y =
            (landmark[0].y + landmark[1].y + landmark[2].y) / 3.toFloat()
        val dy = landmark[1].y - landmark[0].y.toFloat()
        val dx = landmark[1].x - landmark[0].x.toFloat()
        val degrees = Math.toDegrees(
            Math.atan2(
                dy.toDouble(),
                dx.toDouble()
            )
        ).toFloat()
        val matrix = Matrix()
        matrix.setRotate(-degrees, x, y)
        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }


    fun getPixelsRGBA(image: Bitmap): ByteArray? {
        // 计算图像由多少个像素点组成
        val bytes = image.byteCount
        val buffer = ByteBuffer.allocate(bytes) // 创建一个新的buffer
        image.copyPixelsToBuffer(buffer) // 将数据赋值给buffer
        return buffer.array()
    }

    /**
     * 对传入对bitmap进行压缩
     *      缩放压缩，以及改变通道数量
     *      通过生成新的小的图片，之后删除原来图片达到压缩目的
     *
     * @param bitmap: 要压缩的图片
     */
    fun toCompressBitmap(bitmap: Bitmap): Bitmap {

        val matrix = Matrix().also { it.setScale(0.125f, 0.125f) }
        val smallBitmap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        bitmap.recycle()
        return smallBitmap

    }


}