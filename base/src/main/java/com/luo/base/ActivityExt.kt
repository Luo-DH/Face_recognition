package com.luo.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.luo.base.activity.ActivityController
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

fun Activity.showAlertDialog(
    title: String,
    message: String = "",
    cancel: String = "取消",
    conform: String = "确认",
    cancelBlock: (() -> Unit)? = null,
    conformBlock: (() -> Unit)? = null
) {
    MaterialAlertDialogBuilder(FaceApplication.context)
        .setTitle(title)
        .setMessage(message)
        .setNeutralButton(cancel) { _, _ ->
            if (cancelBlock != null) {
                cancelBlock()
            }
        }
        .setPositiveButton(conform) { _, _ ->
            if (conformBlock != null) {
                conformBlock()
            }
        }
        .show()
}