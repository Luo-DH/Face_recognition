package com.luo.down

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.luo.base.FaceApplication
import com.luo.base.face.Face
import com.luo.base.net.FaceDetail
import com.luo.learnc01.face.ArcFace
import com.luo.learnc01.face.RetinaFace2
import com.luo.learnc01.others.Utils
import com.luo.learnc01.others.toCropBitmap
import com.luo.learnc01.others.toGetLandmarks
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author: Luo-DH
 * @date: 2021/6/21
 */
class DownViewModel: ViewModel() {

    private val _faces = MutableLiveData<List<FaceDetail>>()
    val faces: LiveData<List<FaceDetail>> = _faces

    private val _facesFromAssets = MutableLiveData<List<Bitmap>>()
    val facesFromAssets: LiveData<List<Bitmap>> = _facesFromAssets

    private val _faceDatas = MutableLiveData<ArrayList<FaceDetail>>() .also { it.value = ArrayList<FaceDetail>() }
    val faceDataas: LiveData<ArrayList<FaceDetail>> = _faceDatas

    /**
     * 获取所有的人脸
     */
    fun getAllFaces() {
        viewModelScope.launch {
            val res = DownRepository.getAllFaces()
            _faces.postValue(res)
        }
    }

    fun getAllFacesFromAssets() {
        viewModelScope.launch {
            val res = DownRepository.getAllFacesFromAssets()
            _facesFromAssets.postValue(res)
        }
    }

    fun checkFace(face: FaceDetail) {
        viewModelScope.launch {
            Glide.with(FaceApplication.context).asBitmap().load(face.imgUrl).into(object :
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
                        com.luo.base.face.FaceDetail(
                            name = face.name,
                            fea = fea,
                            smallBitmap = Utils.scaleBitmap(resBitmap, .1f)!!
                        )
                    )
                    _faceDatas.value?.add(face)
                    _faceDatas.postValue(_faceDatas.value)
                }
                override fun onLoadCleared(placeholder: Drawable?) {
                }

            })

        }

    }

}