package com.luo.down

import com.luo.base.net.api.Api

/**
 * @author: Luo-DH
 * @date: 2021/6/21
 */
object DownRepository {

    suspend fun getAllFaces() =
        Api.getFaceApi().getAllFaces()


}