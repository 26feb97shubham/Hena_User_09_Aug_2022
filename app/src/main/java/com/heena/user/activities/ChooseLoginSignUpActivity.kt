package com.heena.user.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.widget.Toast
import com.heena.user.R
import com.heena.user.application.MyApp
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_choose_login_sign_up.*

class ChooseLoginSignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_login_sign_up)
        setUpViews()
    }

    private fun setUpViews() {
        btnLogin.setSafeOnClickListener {
            MyApp.sharedPreferenceInstance!!.save(SharedPreferenceUtility.ISBUTTONSELECTED, true)
            btnLogin.startAnimation(AlphaAnimation(1f, 0.5f))
            startActivity(Intent(this, LoginActivity::class.java))
        }
        btnSignUp.setSafeOnClickListener {
            MyApp.sharedPreferenceInstance!!.save(SharedPreferenceUtility.ISBUTTONSELECTED, true)
            btnSignUp.startAnimation(AlphaAnimation(1f, 0.5f))
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
    override fun onBackPressed() {
        Utility.exitApp(this, this)
    }
}