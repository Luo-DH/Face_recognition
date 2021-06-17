package com.luo.login

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.luo.base.FaceApplication

class LoginActivity : AppCompatActivity() {

    companion object {
        fun actionStart(context: Context) {
            com.luo.base.actionStart<LoginActivity>(context)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }
}