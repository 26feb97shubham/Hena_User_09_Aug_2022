package com.heena.user.Dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.heena.user.R
import com.heena.user.utils.LogUtils
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import kotlinx.android.synthetic.main.fragment_nointernet_dialog.view.*

class NoInternetDialog : DialogFragment() {

    private var retryCallback : RetryInterface?=null

//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        return super.onCreateDialog(savedInstanceState)
//    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_nointernet_dialog, container, false)
        setUpViews(view)
        return view
    }

    private fun setUpViews(view: View) {
        view.retry.setOnClickListener {
            if (Utility.hasConnection(requireContext())){
               retryCallback!!.retry()
            }else{
                LogUtils.shortToast(requireContext(), "Still No Internet")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            dialog.window!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    fun setRetryCallback(retryCallback: RetryInterface){
        this.retryCallback = retryCallback
    }


    interface RetryInterface{
        fun retry()
    }

}