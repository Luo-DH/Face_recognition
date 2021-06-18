package com.luo.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

fun showMaterialAlertDialog(
    context: Context,
    title: String = "警告",
    message: String = "",
    cancel: String = "取消",
    conform: String = "确认",
    block: () -> Unit
) {
    MaterialAlertDialogBuilder(context)
        .setTitle(title)
        .setMessage(message)
        .setNeutralButton(cancel) { _, _ ->
        }
        .setPositiveButton(conform) { _, _ ->
            block()
        }
        .show()
}