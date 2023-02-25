package com.codewithshadow.linkedin_clone.ui.splash

import com.codewithshadow.linkedin_clone.base.BaseActivity
import android.os.Bundle
import com.codewithshadow.linkedin_clone.R
import android.content.Intent
import android.os.Handler
import com.codewithshadow.linkedin_clone.ui.login.LoginActivity

class SplashScreenActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler().postDelayed({
            val intent = Intent(this@SplashScreenActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }, 1000)
    }

}