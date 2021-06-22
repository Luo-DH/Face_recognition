package com.luo.down

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.luo.base.activity.BaseActivity
import com.luo.base.face.Face
import com.luo.base.face.FaceDetail
import com.luo.down.adapter.FaceAdapter
import com.luo.down.adapter.FaceAdapter2
import com.luo.down.databinding.ActivityDownBinding

import com.luo.learnc01.face.ArcFace
import com.luo.learnc01.face.RetinaFace2
import com.luo.learnc01.others.Utils
import com.luo.learnc01.others.toCropBitmap
import com.luo.learnc01.others.toGetLandmarks

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

    private val viewModel by viewModels<DownViewModel>()

    private lateinit var binding: ActivityDownBinding

    private val faceAdapter = FaceAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //初始化数据
        initData()

        // 设置数据监听
        setupObserver()

    }

    private fun initData() {

        viewModel.getAllFaces()

        binding.downRv.apply {
            this.adapter = faceAdapter
            this.layoutManager = LinearLayoutManager(this@DownActivity)
        }
    }

    private val handler = Handler()

    private fun setupObserver() {
        viewModel.faces.observe(this) { faces ->
//            thread {
                for (face in faces) {
                    viewModel.checkFace(face)
                }
                handler.post {
                    binding.downTv.apply {
                        setTextColor(Color.GREEN)
                        text = "初始化完毕"
                    }
                }
//            }
        }
        viewModel.faceDataas.observe(this) { faces ->
            faceAdapter.faces = faces
        }
    }
}