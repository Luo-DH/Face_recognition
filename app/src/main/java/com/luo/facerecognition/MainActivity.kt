package com.luo.facerecognition

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.luo.base.activity.BaseActivity
import com.luo.facerecognition.databinding.ActivityMainBinding
import com.luo.learnc01.face.ArcFace
import com.luo.learnc01.face.RetinaFace2
import com.luo.login.LoginActivity

/**
 * 主页面，主要供用户选择进入如下页面
 *  - 蓝牙连接
 *  - 识别页面
 *  - 设置页面
 */
class MainActivity : BaseActivity() {


    companion object {
        fun actionStart(context: Context) {
            com.luo.base.actionStart<MainActivity>(context)
        }
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 检查token，判断是否需要跳转到登陆页面
        checkToken()
        // 初始化UI
        initUI()
        // 设置点击监听
        setClickListener()
    }

    private fun checkToken() {
        LoginActivity.actionStart(this)
        RetinaFace2().init(assets)
        ArcFace().init(assets)
    }

    private fun initUI() {
//        supportFragmentManager.beginTransaction().replace(
//            R.id.main_frg, MainFragment.newInstance()
//        ).commit()
    }

    private fun setClickListener() {
//        // 蓝牙连接
//        binding.mainBtnBlue.setOnClickListener {
////            BlueActivity.actionStart(this)
//            val transaction = supportFragmentManager.beginTransaction()
//            transaction.commit()
//        }
//        // 识别页面
//        binding.mainBtnRecognize.setOnClickListener {
//        }
//        // 设置页面
//        binding.mainBtnSetting.setOnClickListener {
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
    }
}