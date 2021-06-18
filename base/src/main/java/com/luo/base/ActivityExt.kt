package com.luo.base

import android.content.Context
import android.content.Intent
import android.view.View
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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
    canCancel: Boolean = true,
    view: View? = null,
    block: (() -> Unit)? = null
) =
    MaterialAlertDialogBuilder(context)
        .setTitle(title)
        .setMessage(message)
        .setNeutralButton(cancel) { _, _ ->
        }
        .setPositiveButton(conform) { _, _ ->
            if (block != null) {
                block()
            }
        }
        .setView(view)
        .show()
        .apply {
            if (!canCancel) {
                setCanceledOnTouchOutside(false)
                setCancelable(false)
            }
        }
