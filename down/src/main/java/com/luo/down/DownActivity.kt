package com.luo.down

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.luo.base.Test
import com.luo.base.activity.BaseActivity
import com.luo.base.db.entity.FaceFeature
import com.luo.base.net.FaceDetail
import com.luo.base.net.ServiceCreator
import com.luo.base.net.api.FaceService
import com.luo.down.databinding.ActivityDownBinding
import com.luo.face.ArcFace
import com.luo.face.Face
import com.luo.face.RetinaFace
import com.luo.face.other.Utils
import com.luo.face.other.toGetLandmarks
import retrofit2.Call

import retrofit2.Callback
import retrofit2.Response
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

        findViewById<TextView>(R.id.down_tv).setOnClickListener {
            val res1 = ArcFace.calCosineDistance(Face.faceDetail[0].fea!!, Face.faceDetail[1].fea!!)
            val res2 = ArcFace.compareFeature(Face.faceDetail[0].fea!!, Face.faceDetail[1].fea!!)
            Log.d(TAG, "onCreate: ${res1} ==== $res2")
        }
    }

    private fun initData() {
        ArcFace.init(assets)
        RetinaFace.init(assets)
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
                            val res = RetinaFace.detect(resource, 1f)[0]
                            val resBitmap = Utils.cropBitmap(resource, res)
                            val fea = ArcFace.getFeatureWithWrap2(
                                Utils.getPixelsRGBA(resBitmap),
                                resBitmap.width,
                                resBitmap.height,
                                res.toGetLandmarks()
                            )
                            Face.faceDetail.add(
                                com.luo.face.module.FaceDetail(
                                    name = face.name,
                                    fea = fea,
                                    box = res,
                                    smallBitmap = Utils.scaleBitmap(resBitmap, .1f)!!
                                )
                            )
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                        }

                    })
                }
            }
        }
    }
}