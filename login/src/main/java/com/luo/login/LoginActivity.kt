package com.luo.login

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import androidx.activity.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.dialog.MaterialDialogs
import com.luo.base.activity.ActivityController
import com.luo.base.activity.BaseActivity
import com.luo.base.showMaterialAlertDialog
import com.luo.login.databinding.ActivityLoginBinding
import com.luo.login.viewModel.LoginActivityViewModel

class LoginActivity : BaseActivity() {

    companion object {
        fun actionStart(context: Context) {
            com.luo.base.actionStart<LoginActivity>(context)
        }
    }

    private lateinit var binding: ActivityLoginBinding
    private lateinit var loadingDialog: Dialog

    private val viewModel by viewModels<LoginActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setClickListener()
        setObserver()
    }

    private fun setClickListener() {
        binding.loginBtnBack.setOnClickListener {
            this.backActivity()
        }
        binding.loginBtnLogin.setOnClickListener {
            loadingDialog = showLoading()
        }
    }

    private fun setObserver() {

    }

    /**
     * 当用户点击返回按钮会调用此方法
     *  因为当前登陆页面只能在未登录的情况下进入，
     *  如果已经登陆了，需要退出登陆才能进来，
     *  因此在当前页面是不能够进行其他操作，
     *  所以用户在当前页面选择退出的情况下应该退出程序。
     */
    override fun onBackPressed() {
        this.backActivity()
    }

    /**
     * 退出当前页面方法
     */
    private fun backActivity() {
        showMaterialAlertDialog(
            this,
            message = resources.getString(R.string.login_alert_title)
        ) {
            ActivityController.finishAll()
        }
    }

    /**
     * 显示正在登陆dialog
     */
    private fun showLoading() =
        showMaterialAlertDialog(
            context = this,
            title = "正在登陆",
            conform = "取消登陆",
            canCancel = false,
            view = layoutInflater.inflate(R.layout.dialog_loading, null),
            cancel = ""
        )

}