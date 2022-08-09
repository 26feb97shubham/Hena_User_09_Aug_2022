package com.heena.user.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.heena.user.Dialogs.NoInternetDialog
import com.heena.user.R
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.models.OfferItemX
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.convertDoubleValueWithCommaSeparator
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_view_offer.view.*

class ViewOfferFragment : Fragment() {
    var mView : View? = null
    private var offerDetail : OfferItemX?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            offerDetail = it.getSerializable("offerDetails") as OfferItemX?
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_view_offer, container, false)
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
        )

        requireActivity().tv_title.visibility = View.GONE


        requireActivity().iv_back.setSafeOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        requireActivity().iv_notification.setSafeOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F,0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Glide.with(requireContext()).load(offerDetail!!.gallery!![0]).into(mView!!.rivOfferImage)
        mView!!.tvOfferServiceName.text = offerDetail!!.name
        if (sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "").equals("ar")){
            mView!!.tvOfferCategoryName.text = offerDetail!!.category!!.name
        }else{
            mView!!.tvOfferCategoryName.text = offerDetail!!.category!!.name
        }

        var offerDuration = offerDetail!!.offer!!.started_at + " - "+offerDetail!!.offer!!.ended_at
        mView!!.tvOfferDuration.text = offerDuration
        mView!!.tvOfferPrice.text = "AED "+convertDoubleValueWithCommaSeparator(offerDetail!!.offer!!.price!!.main!!.toDouble())

        Glide.with(requireContext()).load(offerDetail!!.user!!.image).into(mView!!.rivSupplierImage)
        mView!!.tvHennayaName.text = offerDetail!!.user!!.name
        if (offerDetail!!.user!!.avg_star.isNullOrEmpty()){
            mView!!.rbHennayaName.rating = 0F
        }else{
            mView!!.rbHennayaName.rating = offerDetail!!.user!!.avg_star!!.toFloat()
        }


    }
}