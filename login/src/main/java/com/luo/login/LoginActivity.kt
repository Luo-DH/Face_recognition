package com.luo.login

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.luo.base.FaceApplication

class LoginActivity : AppCompatActivity() {

    companion object {
        fun actionStart(context: Context) {
            context.startActivity(
                Intent(
                    context,
                    LoginActivity::class.java
                )
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }
}