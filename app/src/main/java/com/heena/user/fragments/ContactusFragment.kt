package com.heena.user.fragments

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.navigation.fragment.findNavController
import com.heena.user.R
import com.heena.user.`interface`.APIInterface.Companion.createBuilder
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.models.MyResponse
import com.heena.user.utils.LogUtils
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.apiInterface
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_contactus.view.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class ContactusFragment : Fragment() {
    private var mView : View ?= null
    private var fullName : String = ""
    private var emailAddress : String = ""
    private var mymessage : String = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(
                R.layout.fragment_contactus, container, false)
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
        )
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().iv_back.setSafeOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().navigate(R.id.action_contactUsFragment_to_homeFragment)
        }

        requireActivity().iv_notification.setSafeOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F,0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }

        requireActivity().tv_title.text = ""

        mView!!.tv_contact_us_submit.setSafeOnClickListener {
            validate()
        }
    }
    private fun validate(){
        fullName = mView!!.et_fullname.text.toString().trim()
        emailAddress = mView!!.et_email_address.text.toString().trim()
        mymessage = mView!!.et_message.text.toString().trim()

        if (TextUtils.isEmpty(fullName)){
            Utility.showSnackBarValidationError(mView!!.contactUsFragmentConstraintLayout,
                requireContext().getString(R.string.please_enter_your_full_name),
                requireContext())
        }else if(TextUtils.isEmpty(emailAddress) && !sharedPreferenceInstance!!.isEmailValid(emailAddress)) {
            Utility.showSnackBarValidationError(mView!!.contactUsFragmentConstraintLayout,
                requireContext().getString(R.string.please_enter_valid_email),
                requireContext())
        }else if (TextUtils.isEmpty(mymessage)){
            Utility.showSnackBarValidationError(mView!!.contactUsFragmentConstraintLayout,
                requireContext().getString(R.string.please_enter_valid_message),
                requireContext())
        }else{
            save()
        }
    }

    private fun save() {
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        mView!!.fragment_contact_us_progressBar.visibility= View.VISIBLE
        val builder = createBuilder(arrayOf("user_id","name","email","message","lang"),
        arrayOf(
            sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0].toString(),
        fullName,
        emailAddress,
        mymessage,
            sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]
        ))

        val call = apiInterface.contactUs(builder.build())
        call?.enqueue(object : Callback<MyResponse?>{
            override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                mView!!.fragment_contact_us_progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if(response.isSuccessful){
                        if (response.body()!!.status==1){
                            Utility.showSnackBarOnResponseSuccess(mView!!.contactUsFragmentConstraintLayout,
                                response.body()!!.message.toString(),
                                requireContext())
                            findNavController().navigate(R.id.action_contactUsFragment_to_homeFragment)
                        }
                        else{
                            Utility.showSnackBarOnResponseError(mView!!.contactUsFragmentConstraintLayout,
                                response.body()!!.message.toString(),
                                requireContext())
                        }
                    }else{
                        Utility.showSnackBarOnResponseError(mView!!.contactUsFragmentConstraintLayout,
                            requireContext().getString(R.string.response_isnt_successful),
                            requireContext())
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<MyResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(mView!!.contactUsFragmentConstraintLayout,
                    requireContext().getString(R.string.response_isnt_successful),
                    requireContext())
                mView!!.fragment_contact_us_progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
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