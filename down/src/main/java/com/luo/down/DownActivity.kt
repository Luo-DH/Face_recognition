package com.luo.down

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.luo.base.activity.BaseActivity
import com.luo.base.face.Face
import com.luo.base.face.FaceDetail
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
//        ArcFace.init(assets)
//        RetinaFace.init(assets)
        RetinaFace2().init(assets)
        ArcFace().init(assets)
        viewModel.getAllFaces()
    }

    private fun setupObserver() {
        viewModel.faces.observe(this) { faces ->
            thread {
                for (face in faces) {
                    Glide.with(this).asBitmap().load(face.imgUrl).into(object :
                        CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            val res = RetinaFace2().detect(resource, 1f)
                            if (res.isEmpty()) {
                                return
                            }
                            val resBitmap = res[0].toCropBitmap(resource)
                            val fea = ArcFace().getFeatureWithWrap2(
                                Utils.getPixelsRGBA(resBitmap),
                                resBitmap.width,
                                resBitmap.height,
                                res[0].toGetLandmarks()
                            )
                            Face.faceDetail.add(
                                FaceDetail(
                                    name = face.name,
                                    fea = fea,
                                    smallBitmap = Utils.scaleBitmap(resBitmap, .1f)!!
                                )
                            )
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                        }

                    })
                }

//                binding.downTv.text = Face.faceDetail.size.toString()

            }
        }
    }
}