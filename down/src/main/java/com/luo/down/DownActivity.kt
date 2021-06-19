package com.luo.down

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.luo.base.Test
import com.luo.base.activity.BaseActivity
import com.luo.base.db.entity.FaceFeature
import kotlin.concurrent.thread

/**
 * 同步页面
 *  - 进入到当前页面，访问数据库
 *  - 进行网络连接，同步数据
 *  - 顶部显示进度，支持中断
 *  - 下边用list展示已经同步的情况
 */
class DownActivity : BaseActivity() {

    companion object {
        fun actionStart(context: Context) {
            com.luo.base.actionStart<DownActivity>(context)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_down)
        findViewById<TextView>(R.id.textView).setOnClickListener {
            thread {
                val dao = Test.getDao()
            }
        }
    }
}