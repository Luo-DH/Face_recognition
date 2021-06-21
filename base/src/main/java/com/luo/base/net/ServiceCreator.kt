package com.luo.base.net

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * @author: Luo-DH
 * @date: 2021/6/21
 */
object ServiceCreator {

    private const val BASE_URL = "http://192.168.31.236:9001/api/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun <T> create(serviceClass: Class<T>): T = retrofit.create(serviceClass)

    inline fun <reified T> create(): T = create(T::class.java)
}