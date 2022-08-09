package com.heena.user.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
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
import kotlinx.android.synthetic.main.fragment_change_password.view.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class ChangePasswordFragment : Fragment() {

    private var mView : View?=null
    private var oldPassword: String = ""
    private var newPassword: String = ""
    private var confirmPassword: String = ""
    private var profile_picture: String = ""
    private var oldPassVis=false
    private var newPassVis=false
    private var confmPassVis=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            profile_picture = it.getString("profile_picture").toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_change_password, container, false)
        setUpViews()
        return mView
    }

    private fun setUpViews() {
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
        )

        requireActivity().iv_back.setSafeOnClickListener {
            requireActivity().iv_back.startAnimation(
                AlphaAnimation(1f, 0.5f)
            )
            sharedPreferenceInstance!!
                .hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        requireActivity().iv_notification.setSafeOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F,0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }


        requireActivity().tv_title.text = ""

        mView!!.btnChangePass.setSafeOnClickListener {
            mView!!.btnChangePass.startAnimation(AlphaAnimation(1f, 0.5f))
            validateAndChangePassword()
        }

        mView!!.edtNewPass.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(charSeq: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(sharedPreferenceInstance!!.isPasswordValid(charSeq.toString())){
                    mView!!.imgPassVerify.visibility=View.VISIBLE

                }
                else{
                    mView!!.imgPassVerify.visibility=View.GONE

                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

        mView!!.edtConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(charSeq: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val pass = mView!!.edtNewPass.text.toString()

                if(!TextUtils.isEmpty(pass)){
                    if(!pass.equals(charSeq.toString(), false)){
                        mView!!.imgConfPassVerify.visibility=View.GONE
                        mView!!.txtPassMatch.visibility=View.GONE
                    }
                    else{
                        mView!!.imgConfPassVerify.visibility=View.VISIBLE
                        mView!!.txtPassMatch.visibility=View.VISIBLE
                        sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), mView!!.edtConfirmPassword)
                    }
                }
                else{
                    Utility.showSnackBarValidationError(mView!!.changePasswordFragmentConstraintLayout,
                        requireContext().getString(R.string.please_first_enter_your_password),
                        requireContext())
                }

            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })


        mView!!.imgEyeOldPass.setOnClickListener {
            if(oldPassVis){
                oldPassVis=false
                val start=mView!!.edtOldPass.selectionStart
                val end=mView!!.edtOldPass.selectionEnd
                mView!!.edtOldPass.transformationMethod = null
                mView!!.edtOldPass.setSelection(start, end)
                mView!!.imgEyeOldPass.setImageResource(R.drawable.visible)
            }
            else{
                oldPassVis=true
                val start=mView!!.edtOldPass.selectionStart
                val end=mView!!.edtOldPass.selectionEnd
                mView!!.edtOldPass.transformationMethod = PasswordTransformationMethod()
                mView!!.edtOldPass.setSelection(start, end)
                mView!!.imgEyeOldPass.setImageResource(R.drawable.invisible)
            }
        }

        mView!!.imgEyeNewPass.setOnClickListener {
            if(newPassVis){
                newPassVis=false
                val start=mView!!.edtNewPass.selectionStart
                val end=mView!!.edtNewPass.selectionEnd
                mView!!.edtNewPass.transformationMethod = null
                mView!!.edtNewPass.setSelection(start, end)
                mView!!.imgEyeNewPass.setImageResource(R.drawable.visible)
            }
            else{
                newPassVis=true
                val start=mView!!.edtNewPass.selectionStart
                val end=mView!!.edtNewPass.selectionEnd
                mView!!.edtNewPass.transformationMethod = PasswordTransformationMethod()
                mView!!.edtNewPass.setSelection(start, end)
                mView!!.imgEyeNewPass.setImageResource(R.drawable.invisible)
            }
        }
        mView!!.imgEyeConfPass.setOnClickListener {
            if(confmPassVis){
                confmPassVis=false
                val start=mView!!.edtConfirmPassword.selectionStart
                val end=mView!!.edtConfirmPassword.selectionEnd
                mView!!.edtConfirmPassword.transformationMethod = null
                mView!!.edtConfirmPassword.setSelection(start, end)
                mView!!.imgEyeConfPass.setImageResource(R.drawable.visible)
            }
            else{
                confmPassVis=true
                val start=mView!!.edtConfirmPassword.selectionStart
                val end=mView!!.edtConfirmPassword.selectionEnd
                mView!!.edtConfirmPassword.transformationMethod = PasswordTransformationMethod()
                mView!!.edtConfirmPassword.setSelection(start, end)
                mView!!.imgEyeConfPass.setImageResource(R.drawable.invisible)
            }
        }
    }

    private fun validateAndChangePassword() {
        oldPassword= mView!!.edtOldPass.text.toString()
        newPassword= mView!!.edtNewPass.text.toString()
        confirmPassword= mView!!.edtConfirmPassword.text.toString()

        when {
            TextUtils.isEmpty(oldPassword) -> {
                Utility.showSnackBarValidationError(mView!!.changePasswordFragmentConstraintLayout,
                    requireContext().getString(R.string.please_enter_your_old_password),
                    requireContext())
            }
            sharedPreferenceInstance!![SharedPreferenceUtility.Password, ""] != oldPassword -> {
                Utility.showSnackBarValidationError(mView!!.changePasswordFragmentConstraintLayout,
                    requireContext().getString(R.string.entered_old_password_is_wrong),
                    requireContext())
            }
            TextUtils.isEmpty(newPassword) -> {
                Utility.showSnackBarValidationError(mView!!.changePasswordFragmentConstraintLayout,
                    requireContext().getString(R.string.please_enter_your_new_password),
                    requireContext())
            }
            !sharedPreferenceInstance!!.isPasswordValid(newPassword) -> {
                Utility.showSnackBarValidationError(mView!!.changePasswordFragmentConstraintLayout,
                    requireContext().getString(R.string.password_length_valid),
                    requireContext())
            }
            TextUtils.isEmpty(confirmPassword) -> {
                Utility.showSnackBarValidationError(mView!!.changePasswordFragmentConstraintLayout,
                    requireContext().getString(R.string.please_enter_your_confirm_password),
                    requireContext())
            }
            confirmPassword != newPassword -> {
                Utility.showSnackBarValidationError(mView!!.changePasswordFragmentConstraintLayout,
                    requireContext().getString(R.string.password_doesnt_match_with_confirm_password),
                    requireContext())
            }
            else -> {
                changePassword()
            }
        }

    }
    private fun changePassword() {
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        mView!!.progressBar.visibility= View.VISIBLE

        val builder = createBuilder(arrayOf("user_id", "new_pwd", "old_pwd", "lang"),
            arrayOf(sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0].toString()
                , newPassword, oldPassword, sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""].toString()))
        val call = apiInterface.changePassword(builder.build())
        call!!.enqueue(object : Callback<MyResponse?> {
            override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                mView!!.progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.body() != null) {
                        if(response.body()!!.status==1){
                            Utility.showSnackBarOnResponseSuccess(mView!!.changePasswordFragmentConstraintLayout,
                                response.body()!!.message.toString(),
                                requireContext())
                            findNavController().popBackStack()
                        }
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
                Utility.showSnackBarOnResponseError(mView!!.changePasswordFragmentConstraintLayout,
                    requireContext().getString(R.string.response_isnt_successful),
                    requireContext())
                mView!!.progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        })

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
}