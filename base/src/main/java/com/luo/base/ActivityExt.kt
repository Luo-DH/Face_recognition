package com.luo.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import java.io.Serializable

/**
 * @author: Luo-DH
 * @date: 2021/6/17
 */

inline fun <reified T> actionStart(context: Context) {
    context.startActivity(
        Intent(context, T::class.java)
    )
}