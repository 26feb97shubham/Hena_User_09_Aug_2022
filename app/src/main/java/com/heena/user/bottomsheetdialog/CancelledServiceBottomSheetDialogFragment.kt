package com.heena.user.bottomsheetdialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.heena.user.R
import com.heena.user.`interface`.ClickInterface
import com.heena.user.models.Booking
import com.heena.user.utils.SharedPreferenceUtility
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.heena.user.utils.Utility
import kotlinx.android.synthetic.main.fragment_bottom_sheet_calendar.view.*
import kotlinx.android.synthetic.main.fragment_cancelled_service_bottom_sheet_dialog.view.*

class CancelledServiceBottomSheetDialogFragment : BottomSheetDialogFragment() {
    private var OnChatWithAdminClick: ClickInterface.OnChatWithAdminClick?=null
    private var OnRescheduleServiceClick: ClickInterface.OnRescheduleServiceClick?=null
    private var mView : View?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(
                R.layout.fragment_cancelled_service_bottom_sheet_dialog,
                container,
                false
        )
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Glide.with(requireContext()).load(booking!!.manager!!.image).into(mView!!.civ_profile)
        mView!!.tv_naqasha_name.text = booking!!.manager!!.name
        mView!!.tv_rsn_for_cancellation.text = booking!!.message
        if (booking!!.manager!!.avg_star.equals("")||booking!!.manager!!.avg_star.equals(null)){
            mView!!.ratingBar.rating = 0F
            mView!!.txtRating.text = "0"
        }else{
            mView!!.ratingBar.rating = booking!!.manager!!.avg_star!!.toFloat()
            mView!!.txtRating.text = booking!!.manager!!.avg_star
        }

        mView!!.reschedule_booking.setOnClickListener {
            OnRescheduleServiceClick!!.OnRescheduleService()
        }
    }

    fun setRescheduleClick(OnRescheduleServiceClick:ClickInterface.OnRescheduleServiceClick){
        this.OnRescheduleServiceClick = OnRescheduleServiceClick
    }

    fun setChatWithAdminClick(OnChatWithAdminClick: ClickInterface.OnChatWithAdminClick){
        this.OnChatWithAdminClick = OnChatWithAdminClick
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }

    companion object {
        const val TAG = "CancelledServiceBottomSheetDialogFragment"
        private var instance: SharedPreferenceUtility? = null
        private var booking : Booking?=null

        fun newInstance(bundle: Bundle): CancelledServiceBottomSheetDialogFragment {
            booking = bundle.getSerializable("booking") as Booking?
            Log.e("booking", booking.toString())
            return CancelledServiceBottomSheetDialogFragment()
        }

        @Synchronized
        fun getInstance(): SharedPreferenceUtility {
            if (instance == null) {
               instance = SharedPreferenceUtility()
            }
            return instance as SharedPreferenceUtility
        }
    }

}