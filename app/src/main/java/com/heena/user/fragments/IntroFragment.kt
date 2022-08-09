package com.heena.user.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.heena.user.R
import com.heena.user.activities.ChooseLoginSignUpActivity
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.utils.SharedPreferenceUtility

class IntroFragment(val position: Int) : Fragment() {
    private var btnContinue : TextView? = null
    private var ivLogo : ImageView? = null
    private var mainIV : ImageView? = null
    private var mainView : ConstraintLayout?= null
    private var tvIntroContent : TextView? = null
    private var mView: View? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_intro, container, false)
        setUpViews()
        return mView
    }

    private fun setUpViews() {
        btnContinue = mView!!.findViewById(R.id.tv_continue)
        ivLogo = mView!!.findViewById(R.id.iv_logo)
        mainView = mView!!.findViewById(R.id.mainView)
        mainIV = mView!!.findViewById(R.id.mainIV)
        tvIntroContent = mView!!.findViewById(R.id.tv_intro_content)


        if (position==0){
             mainIV!!.setImageResource(R.drawable.intro_screen_1_bg)
            ivLogo!!.visibility = View.GONE
            btnContinue!!.visibility = View.GONE
            tvIntroContent!!.text = getString(R.string.intro_page_1_txt)
        }else if(position == 1){
          mainIV!!.setImageResource(R.drawable.intro_screen_2_bg)
            ivLogo!!.visibility = View.GONE
            btnContinue!!.visibility = View.GONE
            tvIntroContent!!.text = getString(R.string.intro_page_2_txt)
        }else {
              mainIV!!.setImageResource(R.drawable.intro_screen_3_bg)
            ivLogo!!.visibility = View.VISIBLE
            btnContinue!!.visibility = View.VISIBLE
            tvIntroContent!!.text = getString(R.string.intro_page_3_txt)

            btnContinue!!.setOnClickListener {
                btnContinue!!.startAnimation(AlphaAnimation(1f, 0.5f))
                sharedPreferenceInstance!!.save(SharedPreferenceUtility.ISINTRODUCTION, true)
                startActivity(Intent(requireActivity(), ChooseLoginSignUpActivity::class.java))
            }
        }
    }

    companion object{
        private var instance: SharedPreferenceUtility? = null
        @Synchronized
        fun getInstance(): SharedPreferenceUtility {
            if (instance == null) {
                instance = SharedPreferenceUtility()
            }
            return instance as SharedPreferenceUtility
        }
    }
}