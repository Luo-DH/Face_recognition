package com.luo.facerecognition

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.luo.base.FaceApplication
import com.luo.login.LoginActivity

/**
 * 主页面，主要供用户选择进入如下页面
 *  - 蓝牙连接
 *  - 识别页面
 *  - 设置页面
 */
class MainActivity : AppCompatActivity() {


    companion object {
        private val TAG = "MainActivity"
        fun actionStart(context: Context) {
            com.luo.base.actionStart<MainActivity>(context)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        LoginActivity.actionStart(this)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
    }
}