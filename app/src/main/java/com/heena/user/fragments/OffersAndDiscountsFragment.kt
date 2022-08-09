package com.heena.user.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.navigation.fragment.findNavController
import com.heena.user.R
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_offers_and_discounts.view.*


class OffersAndDiscountsFragment : Fragment() {
    var mView : View ?= null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_offers_and_discounts, container, false)
        requireActivity().tv_title.text = ""
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
        )
        requireActivity().tv_title.text = ""

        requireActivity().iv_back.setSafeOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F, 0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        requireActivity().iv_notification.setSafeOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F, 0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }

        mView!!.cl_ten_percent.setSafeOnClickListener {
            val bundle = Bundle()
            bundle.putInt("filter", 1)
            findNavController().navigate(R.id.filteredoffersAndDiscountsFragment, bundle)
        }

        mView!!.cl_fifteen.setSafeOnClickListener {
            val bundle = Bundle()
            bundle.putInt("filter", 2)
            findNavController().navigate(R.id.filteredoffersAndDiscountsFragment, bundle)
        }

        mView!!.cl_thirty.setSafeOnClickListener {
            val bundle = Bundle()
            bundle.putInt("filter", 3)
            findNavController().navigate(R.id.filteredoffersAndDiscountsFragment, bundle)
        }

        mView!!.cl_fifty_seventy_five.setSafeOnClickListener {
            val bundle = Bundle()
            bundle.putInt("filter", 4)
            findNavController().navigate(R.id.filteredoffersAndDiscountsFragment, bundle)
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