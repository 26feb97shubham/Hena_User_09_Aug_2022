package com.heena.user.bottomsheetdialog

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.heena.user.R
import com.heena.user.`interface`.ClickInterface
import com.heena.user.utils.LogUtils
import com.heena.user.utils.SharedPreferenceUtility
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.heena.user.utils.Utility
import kotlinx.android.synthetic.main.fragment_bottom_sheet_calendar.view.*
import kotlinx.android.synthetic.main.fragment_cancel_service_bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_cancel_service_bottom_sheet.view.*

class CancelServiceBottomSheetFragment : BottomSheetDialogFragment() {
    private var rsn_for_cancellation : String ?= null
    private var mView : View?=null
    private var is_accept_policy = false
    private var cancelServiceClick : ClickInterface.OnCancelServiceClick?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_cancel_service_bottom_sheet, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mView!!.iv_toggle_off.setOnClickListener {
            is_accept_policy = true
            mView!!.iv_toggle_off.visibility = View.GONE
            mView!!.iv_toggle_on.visibility = View.VISIBLE
        }



        mView!!.iv_toggle_on.setOnClickListener {
            is_accept_policy = false
            mView!!.iv_toggle_off.visibility = View.VISIBLE
            mView!!.iv_toggle_on.visibility = View.GONE
        }

        tv_proceed.setOnClickListener {
            rsn_for_cancellation = mView!!.et_rsn_for_cancellation.text.toString().trim()
            if (TextUtils.isEmpty(rsn_for_cancellation)){
                Utility.showSnackBarValidationError(mView!!.cancelServiceBottomSheetDialog,
                    requireContext().getString(R.string.please_enter_valid_message),
                    requireContext())
            } else if (!is_accept_policy){
                Utility.showSnackBarValidationError(mView!!.cancelServiceBottomSheetDialog,
                    requireContext().getString(R.string.please_accept_the_cancellation_policy),
                    requireContext())
            } else{
                cancelServiceClick!!.OnCancelService(booking_id, rsn_for_cancellation!!)
                dismiss()
            }
        }

        mView!!.tv_cancellation_policy_txt.setOnClickListener {
            openCancellationPolicyDialog()
        }
    }

    fun setCancelServiceClick(cancelServiceClick : ClickInterface.OnCancelServiceClick){
        this.cancelServiceClick = cancelServiceClick
    }

    fun openCancellationPolicyDialog(){
        val builder = AlertDialog.Builder(requireContext())
            .create()
        val view = layoutInflater.inflate(R.layout.cancellation_policy_custom_alert_dialog_box,null)
        val  button = view.findViewById<Button>(R.id.closeDialogBox)
        builder.setView(view)
        button.setOnClickListener {
            builder.dismiss()
        }
        builder.setCanceledOnTouchOutside(false)
        builder.show()
    }


    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }

    companion object {
        const val TAG = "CancelServiceBottomSheetFragment"
        private var booking_id : Int?=null
        fun newInstance(bundle: Bundle): CancelServiceBottomSheetFragment {
            booking_id = bundle.getInt("booking_id")
            return CancelServiceBottomSheetFragment()
        }
    }
}