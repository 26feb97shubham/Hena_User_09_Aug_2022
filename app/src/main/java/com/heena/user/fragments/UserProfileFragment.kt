package com.heena.user.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.heena.user.R
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.models.UserProfileResponse
import com.heena.user.utils.LogUtils
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_edit_profile.view.*
import kotlinx.android.synthetic.main.fragment_user_profile.view.*
import kotlinx.android.synthetic.main.side_top_view.*
import kotlinx.android.synthetic.main.side_top_view.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserProfileFragment : Fragment() {

    private var mView : View?=null
    var profile_picture:String=""
    val requestOption = RequestOptions().centerCrop()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_user_profile, container, false)
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
        )
        setUpViews()
        return mView
    }

    private fun setUpViews() {
        requireActivity().iv_back.setSafeOnClickListener {
            requireActivity().iv_back.startAnimation(
                AlphaAnimation(1f, 0.5f)
            )
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        requireActivity().tv_title.text = ""

        requireActivity().iv_notification.setSafeOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F, 0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }
    }

    companion object {
        private var instance: SharedPreferenceUtility? = null
        @Synchronized
        fun getInstance(): SharedPreferenceUtility {
            if (instance == null) {
                instance = SharedPreferenceUtility()
            }
            return instance as SharedPreferenceUtility
        }
    }

    override fun onResume() {
        super.onResume()
        getProfile()
    }

    private fun getProfile() {
        mView!!.progressBar.visibility= View.VISIBLE
        val call = Utility.apiInterface.getLoggedInUserProfile(sharedPreferenceInstance!!.get(SharedPreferenceUtility.UserId, 0),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, ""))
        call?.enqueue(object : Callback<UserProfileResponse?> {
            override fun onResponse(call: Call<UserProfileResponse?>, response: Response<UserProfileResponse?>) {
                mView!!.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        if (response.body()!!.profile!!.image.toString().contains("default", false)) {
                            Glide.with(requireContext()).load(R.drawable.golden_logo)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .listener(object : RequestListener<Drawable> {
                                    override fun onLoadFailed(
                                        p0: GlideException?,
                                        p1: Any?,
                                        p2: com.bumptech.glide.request.target.Target<Drawable>?,
                                        p3: Boolean
                                    ): Boolean {
                                        Log.e("err", p0?.message.toString())
                                        requireActivity().headerView.user_icon_progress_bar_side_top_view.visibility =
                                            View.GONE
                                        return false
                                    }

                                    override fun onResourceReady(
                                        p0: Drawable?,
                                        p1: Any?,
                                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                                        dataSource: com.bumptech.glide.load.DataSource?,
                                        p4: Boolean
                                    ): Boolean {
                                        requireActivity().headerView.user_icon_progress_bar_side_top_view.visibility =
                                            View.GONE
                                        return false
                                    }
                                }).placeholder(R.drawable.user)
                                .apply(requestOption).into(mView!!.img)
                            requireActivity().userIcon.borderWidth = 5
                        } else {
                            Glide.with(requireContext()).load(response.body()!!.profile!!.image.toString())
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .listener(object : RequestListener<Drawable> {
                                    override fun onLoadFailed(
                                        p0: GlideException?,
                                        p1: Any?,
                                        p2: com.bumptech.glide.request.target.Target<Drawable>?,
                                        p3: Boolean
                                    ): Boolean {
                                        Log.e("err", p0?.message.toString())
                                        requireActivity().headerView.user_icon_progress_bar_side_top_view.visibility =
                                            View.GONE
                                        return false
                                    }

                                    override fun onResourceReady(
                                        p0: Drawable?,
                                        p1: Any?,
                                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                                        dataSource: com.bumptech.glide.load.DataSource?,
                                        p4: Boolean
                                    ): Boolean {
                                        requireActivity().headerView.user_icon_progress_bar_side_top_view.visibility =
                                            View.GONE
                                        return false
                                    }
                                }).placeholder(R.drawable.user)
                                .apply(requestOption).into(mView!!.img)
                        }
                        mView!!.tv_profileName.text = response.body()!!.profile!!.username
                        mView!!.tv_phone.text = response.body()!!.profile!!.country_code + response.body()!!.profile!!.phone
                        mView!!.tv_email.text = response.body()!!.profile!!.email
                    }else{
                        Utility.showSnackBarOnResponseError(mView!!.userProfileFragmentConstraintLayout,
                            response.body()!!.message.toString(),
                            requireContext())
                    }
                }else{
                    Utility.showSnackBarOnResponseError(mView!!.userProfileFragmentConstraintLayout,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext())

                }
            }

            override fun onFailure(call: Call<UserProfileResponse?>, t: Throwable) {
                mView!!.progressBar.visibility = View.GONE
                Utility.showSnackBarOnResponseError(mView!!.userProfileFragmentConstraintLayout,
                    requireContext().getString(R.string.response_isnt_successful),
                    requireContext())
            }
        })
    }
}