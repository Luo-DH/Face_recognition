package com.luo.base

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

/**
 * @author: Luo-DH
 * @date: 2021/6/17
 */
class FaceApplication: Application() {
    companion object {
        /*
         * 可以方便的使用FaceApplication.context获取全局的context
         * 但是编译器提示context不应该设置成静态的，会造成内存泄漏。
         * 由于application的context全局唯一，会在app启动时候进行初始化，并且
         * 整个app生命周期都不会进行回收，因此这里的context不会造成内存泄漏，
         * 可以加上注解忽略警告
         */
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}
