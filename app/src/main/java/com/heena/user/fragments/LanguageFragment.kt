package com.heena.user.fragments

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.navigation.fragment.findNavController
import com.heena.user.R
import com.heena.user.activities.HomeActivity
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.utils.LogUtils
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import kotlinx.android.synthetic.main.activity_choose_lang.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_language.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
class LanguageFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var mView : View ?= null
    private var selectLang:String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for requireContext() fragment
        mView = inflater.inflate(R.layout.fragment_language, container, false)
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
        )
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
    }

    companion object {
        /**
         * Use requireContext() factory method to create a new instance of
         * requireContext() fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LanguageFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LanguageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun setUpViews() {
        requireActivity().tv_title.text = ""

        requireActivity().iv_back.setOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F, 0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        requireActivity().iv_notification.setOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F, 0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }

        if(sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]=="en"){
            selectEnglish()

        }
        else if(sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]=="ar"){
            selectArabic()
        }

        arabicView.setOnClickListener {
            if(selectLang != "ar") {
                arabicView.startAnimation(AlphaAnimation(1f, 0.5f))
                selectArabic()
            }
        }
        englishView.setOnClickListener {
            if(selectLang != "en") {
                englishView.startAnimation(AlphaAnimation(1f, 0.5f))
                selectEnglish()
            }
        }

        btnContinue.setOnClickListener {
            btnContinue.startAnimation(AlphaAnimation(1f, 0.5f))
            if(TextUtils.isEmpty(selectLang)){
                Utility.showSnackBarValidationError(mView!!.languageFragmentConstraintLayout,
                    requireContext().getString(R.string.please_choose_your_language),
                    requireContext())

            }
            else{
                Utility.changeLanguage(requireContext(), selectLang)
                sharedPreferenceInstance!!.save(SharedPreferenceUtility.FIRSTTIME, true)
                sharedPreferenceInstance!!.save(SharedPreferenceUtility.ISSELECTLANGUAGE, true)
                sharedPreferenceInstance!!.save(SharedPreferenceUtility.SelectedLang, selectLang)
//                findNavController().navigate(R.id.homeFragment)
                startActivity(Intent(requireContext(), HomeActivity::class.java))
                requireActivity().finishAffinity()
            }
        }
    }

    private fun selectArabic() {
        selectLang = "ar"
        setTextForLang()
        arabic_sub_view.setBackgroundResource(R.drawable.curved_white_filled_rect_box)
        english_sub_view.setBackgroundResource(R.drawable.transparent_gold_border)
    }

    private fun selectEnglish() {
        selectLang = "en"
        setTextForLang()
        english_sub_view.setBackgroundResource(R.drawable.curved_white_filled_rect_box)
        arabic_sub_view.setBackgroundResource(R.drawable.transparent_gold_border)
    }

    private fun setTextForLang(){
        tv_choose_lang.text = getString(R.string.choose_language)
        btnContinue.text = getString(R.string.continue_txt)
    }
}