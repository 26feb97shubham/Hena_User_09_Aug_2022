package com.heena.user.bottomsheetdialog

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.heena.user.R
import com.heena.user.models.Profile
import com.heena.user.utils.SharedPreferenceUtility
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.heena.user.`interface`.APIInterface.Companion.createBuilder
import com.heena.user.`interface`.ClickInterface
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.models.MyResponse
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.apiInterface
import com.heena.user.utils.Utility.isreview
import kotlinx.android.synthetic.main.fragment_rate_naqasha_bottom_sheet.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RateNaqashaBottomSheetFragment : BottomSheetDialogFragment() {
    private var mView : View?=null
    private var review_message = ""
    private var stars = 0.0
    private var onReviewSubmit: ClickInterface.onReviewSubmit?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_rate_naqasha_bottom_sheet, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        isreview = false
        super.onViewCreated(view, savedInstanceState)
        Glide.with(requireContext()).load(managerProfile!!.image).into(mView!!.civ_profile)
        if (managerProfile!!.name.equals("")){
            mView!!.tv_naqasha_name.text = managerProfile!!.username
        }else{
            mView!!.tv_naqasha_name.text = managerProfile!!.name
        }
        if (managerProfile!!.comment_avg.equals("0") || managerProfile!!.comment_avg.equals("")){
            mView!!.ratingBar.rating = 0F
            mView!!.txtRating.text = "0"
        }else{
            mView!!.ratingBar.rating = managerProfile!!.comment_avg!!.toFloat()
            mView!!.txtRating.text = managerProfile!!.comment_avg
        }
        mView!!.tv_submit.setOnClickListener {
            validateandsave()
        }
    }

    private fun validateandsave() {
        review_message = mView!!.et_reviews.text.toString().trim()
        stars = mView!!.ratingBar2.rating.toDouble()

        if (TextUtils.isEmpty(review_message)){
            Utility.showSnackBarValidationError(mView!!.rateNaqashatBottomSheetDialog,
                requireContext().getString(R.string.please_enter_valid_message),
                requireContext())
        }else{
            save()
        }
    }

    private fun save() {
       val builder = createBuilder(arrayOf("user_id", "manager_id", "star", "message", "lang"),
       arrayOf(
           sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0].toString(),
               sharedPreferenceInstance!![SharedPreferenceUtility.ManagerId, ""].toString(),
       stars.toString(),
       review_message,
           sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]
       ))

        val call = apiInterface.addReview(builder.build())
        call?.enqueue(object : Callback<MyResponse?>{
            override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        onReviewSubmit!!.reviewSubmit(response.body()!!.message.toString())
                        dismiss()
                    }else{
                        Utility.showSnackBarOnResponseError(mView!!.rateNaqashatBottomSheetDialog,
                            response.body()!!.message.toString(),
                            requireContext())
                        dismiss()
                    }
                }else{
                    Utility.showSnackBarOnResponseError(mView!!.rateNaqashatBottomSheetDialog,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext())
                    dismiss()
                }
            }

            override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                Utility.showSnackBarOnResponseError(mView!!.rateNaqashatBottomSheetDialog,
                    requireContext().getString(R.string.response_isnt_successful),
                    requireContext())
                dismiss()
            }

        })
    }

    fun ReviewSubmitCallback(onReviewSubmit: ClickInterface.onReviewSubmit){
        this.onReviewSubmit = onReviewSubmit
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }

    companion object {
        const val TAG = "RateNaqashaBottomSheetFragment"
        private var instance: SharedPreferenceUtility? = null
        private var managerProfile : Profile?=null
        fun newInstance(bundle: Bundle): RateNaqashaBottomSheetFragment {
            managerProfile = bundle.getSerializable("manager_profile") as Profile?
            return RateNaqashaBottomSheetFragment()
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