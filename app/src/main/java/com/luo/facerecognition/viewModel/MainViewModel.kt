package com.luo.facerecognition.viewModel

import android.bluetooth.BluetoothSocket
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @author: Luo-DH
 * @date: 2021/6/20
 */
class MainViewModel: ViewModel() {

    private val _socket = MutableLiveData<BluetoothSocket>()
    val socket: LiveData<BluetoothSocket> = _socket

    fun setSocket(socket: BluetoothSocket) {
        _socket.value = socket
    }
}