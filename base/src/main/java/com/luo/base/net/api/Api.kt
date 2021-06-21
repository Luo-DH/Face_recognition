package com.luo.base.net.api

import com.luo.base.net.ServiceCreator
import retrofit2.Retrofit

/**
 * @author: Luo-DH
 * @date: 2021/6/21
 */
object Api {

    fun getFaceApi() = ServiceCreator.create<FaceService>()

}