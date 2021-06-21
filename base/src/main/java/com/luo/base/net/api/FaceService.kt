package com.luo.base.net.api

import com.luo.base.net.FaceDetail
import retrofit2.Call
import retrofit2.http.GET

/**
 * @author: Luo-DH
 * @date: 2021/6/21
 */
interface FaceService {

    @GET("get-all-faces")
    suspend fun getAllFaces(): List<FaceDetail>
}