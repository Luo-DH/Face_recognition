package com.luo.face.other

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.PointF
import com.luo.face.ArcFace
import com.luo.face.module.BoxRetina
import java.io.IOException
import java.lang.ref.SoftReference
import java.nio.ByteBuffer

/**
 * @author: Luo-DH
 * @date: 1/24/21
 */
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
     * @param boxRetina 人脸框信息，如果上一帧没有人脸，则为null
     */
    fun cropBitmap(bitmap: Bitmap, boxRetina: BoxRetina?): Bitmap =
            boxRetina?.toCropBitmap(bitmap)!!


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


    /**
     * 人脸识别方法
     * @param featureMap Map<String, FloatArray> 数据库人脸数据，名字：特征
     * @param feature FloatArray 当前人脸的特征
     * @return Entry<String, Float>?
     */
    fun recognize(featureMap: Map<String, FloatArray>, feature: FloatArray) =
            HashMap<String, Float>().apply {
                featureMap.map {
                    this[it.key] = ArcFace.calCosineDistance(it.value, feature)
                }
            }.maxByOrNull{ it.value }

    fun recognize2(featureMap: Map<String, FloatArray>, feature: FloatArray): Map.Entry<String, Float>? {
        var maxString = ""
        var maxFloat = 0f
        featureMap.forEach {
            val a = ArcFace.calCosineDistance(it.value, feature)
            if (a > maxFloat) maxString = it.key
        }
        return HashMap<String, Float>().also { it[maxString] = maxFloat }.maxByOrNull{ it.value }
    }

    /**
     * 返回投票池票数最多的选项
     * @param voteMap HashMap<String, Int>
     * @return Entry<String, Int>?
     */
    fun getMaxFromVoteMap(voteMap: Map<String, Int>) =
            voteMap.maxByOrNull{ it.value }

    fun toVote(
            name: String,
            voteMap: HashMap<String, Int>,
            highScore: Boolean,
            lowScore: Boolean
    ): HashMap<String, Int> {
        val count = voteMap[name] ?: 0
        if (highScore || lowScore) {
            voteMap.also { it[name] = count + 2 }
        } else {
            voteMap.also { it[name] = count + 1 }
        }
        return voteMap
    }
}

/**
 * 根据人脸框信息裁剪人脸
 * @receiver FaceBox    人脸框
 * @param bitmap Bitmap 有人脸的图片
 * @return (android.graphics.Bitmap..android.graphics.Bitmap?)
 */
fun BoxRetina.toCropBitmap(bitmap: Bitmap): Bitmap =
        Bitmap.createBitmap(
                bitmap,
                this.x1.toInt(),
                this.y1.toInt(),
                (this.x2 - this.x1).coerceAtMost(bitmap.width - this.x1).toInt(),
                (this.y2 - this.y1).coerceAtMost(bitmap.height - this.y1).toInt()
        )

fun BoxRetina.toGetLandmarks(): IntArray {
    val landmarks = IntArray(10)
    var i = 0
    this.landmarks.forEach {
        landmarks[i] = it.x.toInt() - this.x1
        landmarks[i + 5] = it.y.toInt() - this.y1
        i += 1
    }
    return landmarks
}


