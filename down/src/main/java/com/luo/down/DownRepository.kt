package com.luo.down

import android.graphics.Bitmap
import com.luo.base.FaceApplication
import com.luo.base.net.FaceDetail
import com.luo.base.net.api.Api
import com.luo.learnc01.others.Utils

/**
 * @author: Luo-DH
 * @date: 2021/6/21
 */
object DownRepository {

    suspend fun getAllFaces() =
        Api.getFaceApi().getAllFaces()

    fun getAllFacesFromAssets() =
        listOf(Utils.readFromAssets(FaceApplication.context, "Luo.jpeg")!!)


    suspend fun checkFace(face: FaceDetail, faceDatas: ArrayList<FaceDetail>) {

    }
}