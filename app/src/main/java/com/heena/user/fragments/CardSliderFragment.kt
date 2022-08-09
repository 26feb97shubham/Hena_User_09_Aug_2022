package com.heena.user.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.heena.user.R
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.models.Cards
import com.heena.user.utils.SharedPreferenceUtility
import kotlinx.android.synthetic.main.fragment_card_slider.view.*

class CardSliderFragment(private var cards : Cards) : Fragment() {
    var interval = 4
    var separator = ' '
    private var mView : View?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mView = inflater.inflate(R.layout.fragment_card_slider, container, false)
        if (sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""] == "en"
        ) {
            mView!!.ivCardImage.setImageResource(R.drawable.card_bg)
        } else {
            mView!!.ivCardImage.setImageResource(R.drawable.arabic_card)
        }

        addDetails(cards)

        return mView as View
    }


    private fun addDetails(cards: Cards) {
        mView!!.tv_expiry_date_card.text = cards.expiry_date
        val card_number = cards.first_six+"******"+cards.number

        val sb: StringBuilder = StringBuilder(card_number)

        for (i in 0 until card_number.length / interval) {
            sb.insert((i + 1) * interval + i, separator)
        }

        val withDashes = sb.toString()
        Log.e("cardNumber", withDashes)
        mView!!.tv_card_number_card.text = withDashes
        mView!!.tv_account_holdee_name_card.text = cards.name
    }
}