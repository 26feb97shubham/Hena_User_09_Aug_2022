package com.heena.user.bottomsheetdialog

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.heena.user.R
import com.heena.user.`interface`.ClickInterface
import com.heena.user.adapters.ServiceImagesAdapter
import com.heena.user.models.ServiceItem
import com.heena.user.utils.SharedPreferenceUtility
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.extras.StartSnapHelper
import com.heena.user.utils.Utility.loginSignUpAccessAlertDialogBox
import kotlinx.android.synthetic.main.fragment_book_service_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_book_service_bottom_sheet.view.tv_services

class BookServiceBottomSheetFragment : BottomSheetDialogFragment() {
    private var OnBookServiceCallback : ClickInterface.OnBookServiceClick?= null
    private var serviceImagesAdapter : ServiceImagesAdapter?=null
    private var drawable  :Drawable?=null
    private var mView : View?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView  = inflater.inflate(
            R.layout.fragment_book_service_bottom_sheet, container, false)
        return mView
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mView!!.rv_services_images.layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        )

        if (service!!.is_favorite==0){
            mView!!.iv_add_to_fav_bottomSheet_book_service.visibility = View.VISIBLE
            mView!!.iv_fav_added_bottomSheet_book_service.visibility = View.GONE
        }else{
            mView!!.iv_add_to_fav_bottomSheet_book_service.visibility = View.GONE
            mView!!.iv_fav_added_bottomSheet_book_service.visibility = View.VISIBLE
        }
        serviceImagesAdapter = ServiceImagesAdapter(requireContext(), service!!.gallery as ArrayList<String>)
        mView!!.rv_services_images.adapter = serviceImagesAdapter
        StartSnapHelper(requireContext()).attachToRecyclerView(mView!!.rv_services_images)
        serviceImagesAdapter!!.notifyDataSetChanged()

        mView!!.tv_services.text = service!!.name
        mView!!.tv_services_desc.text = service!!.description

        mView!!.tv_book_service!!.setOnClickListener {
            if (sharedPreferenceInstance!!.get(SharedPreferenceUtility.IsGuestUserLogin, false)) {
                loginSignUpAccessAlertDialogBox(requireContext())
            }else{
                OnBookServiceCallback!!.OnBookService()
                dismiss()
            }
        }
        
        mView!!.iv_add_to_fav_bottomSheet_book_service.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                if (sharedPreferenceInstance!!.get(SharedPreferenceUtility.IsGuestUserLogin, false)) {
                    loginSignUpAccessAlertDialogBox(requireContext())
                }else{
                    OnBookServiceCallback!!.onLikeAction(myPosition, true, mView!!)
                }

            }

        })

        mView!!.iv_fav_added_bottomSheet_book_service.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                if (sharedPreferenceInstance!!.get(SharedPreferenceUtility.IsGuestUserLogin, false)) {
                    loginSignUpAccessAlertDialogBox(requireContext())
                }else{
                    OnBookServiceCallback!!.onDislikeAction(myPosition, true, mView!!)
                }
            }

        })
       
    }

    fun setSubscribeClickListenerCallback(OnBookServiceCallback: ClickInterface.OnBookServiceClick){
        this.OnBookServiceCallback = OnBookServiceCallback
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }

    companion object {
        const val TAG = "BookServiceBottomSheetFragment"
        var service : ServiceItem?= null
        var myPosition = 0
        private var instance: SharedPreferenceUtility? = null
        fun newInstance(bundle: Bundle, position: Int): BookServiceBottomSheetFragment {
            //this.context = context;
            service = bundle.getSerializable("service") as ServiceItem?
            myPosition = position
            return BookServiceBottomSheetFragment()
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