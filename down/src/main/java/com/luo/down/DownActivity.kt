package com.luo.down

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.bumptech.glide.Glide
import com.luo.base.Test
import com.luo.base.activity.BaseActivity
import com.luo.base.db.entity.FaceFeature
import com.luo.base.net.FaceDetail
import com.luo.base.net.ServiceCreator
import com.luo.base.net.api.FaceService
import retrofit2.Call

import retrofit2.Callback
import retrofit2.Response

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

        val api = ServiceCreator.create<FaceService>()

        findViewById<TextView>(R.id.down_tv).setOnClickListener {
            api.getAllFaces().enqueue(object : Callback<List<FaceDetail>> {
                override fun onResponse(
                    call: Call<List<FaceDetail>>,
                    response: Response<List<FaceDetail>>
                ) {
                    Glide.with(this@DownActivity.applicationContext).load(response.body()?.get(0)?.imgUrl).into(findViewById(R.id.down_iv))
                }

                override fun onFailure(call: Call<List<FaceDetail>>, t: Throwable) {
                }

            })
        }
    }
}