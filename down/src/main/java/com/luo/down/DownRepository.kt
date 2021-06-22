package com.luo.down

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.luo.base.FaceApplication
import com.luo.base.face.Face
import com.luo.base.net.FaceDetail
import com.luo.base.net.api.Api
import com.luo.learnc01.face.ArcFace
import com.luo.learnc01.face.RetinaFace2
import com.luo.learnc01.others.Utils
import com.luo.learnc01.others.toCropBitmap
import com.luo.learnc01.others.toGetLandmarks

/**
 * @author: Luo-DH
 * @date: 2021/6/21
 */
object DownRepository {

    suspend fun getAllFaces() =
        Api.getFaceApi().getAllFaces()


    suspend fun checkFace(face: FaceDetail, faceDatas: ArrayList<FaceDetail>) {

    }
}