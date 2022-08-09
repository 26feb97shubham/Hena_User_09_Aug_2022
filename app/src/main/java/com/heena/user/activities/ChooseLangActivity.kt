package com.heena.user.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.animation.AlphaAnimation
import android.widget.Toast
import com.heena.user.R
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.utils.LogUtils
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_choose_lang.*

class ChooseLangActivity : AppCompatActivity() {
    private var selectLang:String=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_lang)
        setUpViews()
    }

    private fun setUpViews() {
        selectLang = "ar"
        if(sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]=="en"){
            selectEnglish()

        }
        else if(sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]=="ar"){
            selectArabic()
        }

        arabicView.setSafeOnClickListener {
            if(selectLang != "ar") {
                arabicView.startAnimation(AlphaAnimation(1f, 0.5f))
                selectArabic()
            }
        }
        englishView.setSafeOnClickListener {
            if(selectLang != "en") {
                englishView.startAnimation(AlphaAnimation(1f, 0.5f))
                selectEnglish()
            }
        }

        btnContinue.setSafeOnClickListener {
            btnContinue.startAnimation(AlphaAnimation(1f, 0.5f))
            if(TextUtils.isEmpty(selectLang)){
                LogUtils.shortToast(this, getString(R.string.please_choose_your_language))
            }
            else{
                Utility.changeLanguage(this, selectLang)
                sharedPreferenceInstance!!.save(SharedPreferenceUtility.FIRSTTIME, true)
                sharedPreferenceInstance!!.save(SharedPreferenceUtility.ISSELECTLANGUAGE, true)
                sharedPreferenceInstance!!.save(SharedPreferenceUtility.SelectedLang, selectLang)
                startActivity(Intent(this, IntroductionActivity::class.java))
                finishAffinity()
            }
        }
    }

    private fun selectArabic() {
        selectLang = "ar"
        Utility.changeLanguage(this, selectLang)
        setTextForLang()
        arabic_sub_view.setBackgroundResource(R.drawable.curved_white_filled_rect_box)
        english_sub_view.setBackgroundResource(R.drawable.transparent_gold_border)
    }

    private fun selectEnglish() {
        selectLang = "en"
        Utility.changeLanguage(this, selectLang)
        setTextForLang()
        english_sub_view.setBackgroundResource(R.drawable.curved_white_filled_rect_box)
        arabic_sub_view.setBackgroundResource(R.drawable.transparent_gold_border)
    }

    private fun setTextForLang(){
        tv_choose_lang.text = getString(R.string.choose_language)
        btnContinue.text = getString(R.string.continue_txt)
    }

    override fun onBackPressed() {
        Utility.exitApp(this, this)
    }
}