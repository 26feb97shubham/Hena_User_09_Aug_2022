package com.heena.user

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.heena.user.activities.*
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.deviceId

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private var context : Context?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        setContentView(R.layout.activity_splash)
        deviceId(context)
        setUpViews()
    }

    private fun setUpViews() {
        if (Utility.getLanguage().isEmpty()){
            Utility.changeLanguage(this,"ar")
        }else{
            Utility.changeLanguage(this,Utility.getLanguage())
        }

        Log.e("FirstTime", ""+SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.FIRSTTIME, false))

        Handler(Looper.getMainLooper()).postDelayed(
                {
                    if(!SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.ISSELECTLANGUAGE,false)
                        && !(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.ISINTRODUCTION,false))
                        && !(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.IsLogin,false)))
                    {
                        val intent = Intent(this, ChooseLangActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else if((SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.ISSELECTLANGUAGE,false))
                        && !(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.ISINTRODUCTION,false))
                        && !(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.IsLogin,false)))
                    {
                        val intent = Intent(this, IntroductionActivity::class.java)
                        startActivity(intent)
                        finish()
                    }else if((SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.ISSELECTLANGUAGE,false))
                        && (SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.ISINTRODUCTION,false))
                        && !(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.IsLogin,false)))
                    {
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }else if((SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.ISSELECTLANGUAGE,false))
                        && (SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.ISINTRODUCTION,false))
                        && (SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.IsLogin,false)))
                    {
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    }else{
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                    }
                },
                2000,
        )
    }
}