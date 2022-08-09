package com.heena.user.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.heena.user.R
import com.heena.user.adapters.ScreenSlidePagerAdapter
import com.heena.user.utils.SharedPreferenceUtility
import com.google.android.material.tabs.TabLayoutMediator
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.utils.Utility
import kotlinx.android.synthetic.main.activity_introduction.*

class IntroductionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_introduction)
        setUpViews()
    }

    private fun setUpViews() {
        sharedPreferenceInstance!!.save(SharedPreferenceUtility.IsWelcomeShow, true)
        val pagerAdapter = ScreenSlidePagerAdapter(this)
        intro_view_pager.adapter = pagerAdapter

        //Attaching tab to view pager
        TabLayoutMediator(tabLayout,   intro_view_pager){ _, _ ->
        }.attach()
    }

    override fun onBackPressed() {
        Utility.exitApp(this, this)
    }
}