package com.luo.base.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * @author: Luo-DH
 * @date: 2021/6/18
 */
open class BaseActivity: AppCompatActivity() {

    protected lateinit var TAG: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TAG = this.localClassName
        ActivityController.addActivity(this)

    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityController.removeActivity(this)
    }

}