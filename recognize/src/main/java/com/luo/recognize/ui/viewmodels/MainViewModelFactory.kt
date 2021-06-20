package com.luo.recognize.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.luo.recognize.repository.MainRepository

class MainViewModelFactory(
    val repository: MainRepository,
    val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainViewModel(repository, context) as T
    }

}