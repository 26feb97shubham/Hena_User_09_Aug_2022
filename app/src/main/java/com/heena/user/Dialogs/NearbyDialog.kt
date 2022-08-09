package com.heena.user.Dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.DialogFragment
import com.heena.user.R
import kotlinx.android.synthetic.main.fragment_nearby_dialog.view.*

class NearbyDialog : DialogFragment() {
    private var completionCallback: NearByInterface? = null
    var progressChangedValue = 0
    var isProgressChanged = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        return dialog
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_nearby_dialog, container, false)
        setUpViews(view)
        return view
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    fun setDataCompletionCallback(completionCallback: NearByInterface?) {
        this.completionCallback = completionCallback
    }

    private fun setUpViews(view: View) {
        view.tv_starting.text = view.nearby_seekbar.progress.toString()
        view.cancel_tv.setOnClickListener {
            dismiss()
        }

        view.ok_tv.setOnClickListener {
            if (!isProgressChanged){
                completionCallback?.nearby(50)
            }else{
                completionCallback?.nearby(progressChangedValue)
            }
            dismiss()
        }

        view.nearby_seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                progressChangedValue = p1
                isProgressChanged = true
                view.tv_starting.text = progressChangedValue.toString()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

        })
    }

    interface NearByInterface {
        fun nearby(progressChangedValue: Int)
    }
}