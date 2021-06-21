package com.luo.down

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luo.base.net.FaceDetail
import kotlinx.coroutines.launch

/**
 * @author: Luo-DH
 * @date: 2021/6/21
 */
class DownViewModel: ViewModel() {

    private val _faces = MutableLiveData<List<FaceDetail>>()
    val faces: LiveData<List<FaceDetail>> = _faces

    /**
     * 获取所有的人脸
     */
    fun getAllFaces() {
        viewModelScope.launch {
            val res = DownRepository.getAllFaces()
            _faces.postValue(res)
        }
    }

}