package com.luo.login

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Button
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.luo.base.FaceApplication
import com.luo.base.activity.ActivityController
import com.luo.base.activity.BaseActivity
import com.luo.base.showAlertDialog

class LoginActivity : BaseActivity() {

    companion object {
        fun actionStart(context: Context) {
            com.luo.base.actionStart<LoginActivity>(context)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        findViewById<Button>(R.id.login_btn_login).setOnClickListener {
            showAlertDialog(
                title = resources.getString(R.string.login_alert_title),
                cancel = resources.getString(R.string.login_alert_cancel),
                conform = resources.getString(R.string.login_alert_conform),
            ) {
                ActivityController.finishAll()
            }
        }
    }

    /**
     * 当用户点击返回按钮会调用此方法
     *  因为当前登陆页面只能在未登录的情况下进入，
     *  如果已经登陆了，需要退出登陆才能进来，
     *  因此在当前页面是不能够进行其他操作，
     *  所以用户在当前页面选择退出的情况下应该退出程序。
     *
     * 重写返回监听，退出所有activity
     * @param keyCode Int
     * @param event KeyEvent
     * @return Boolean
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        showAlertDialog(
            title = resources.getString(R.string.login_alert_title),
            cancel = resources.getString(R.string.login_alert_cancel),
            conform = resources.getString(R.string.login_alert_conform),
        ) {
            ActivityController.finishAll()
        }
        return super.onKeyDown(keyCode, event)
    }
}