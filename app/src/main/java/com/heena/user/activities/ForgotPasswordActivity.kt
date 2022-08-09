package com.heena.user.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.core.widget.doOnTextChanged
import com.heena.user.R
import com.heena.user.`interface`.APIInterface.Companion.createBuilder
import com.heena.user.application.MyApp
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.models.MyResponse
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.apiInterface
import com.heena.user.utils.Utility.isNetworkAvailable
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_forgot_password.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class ForgotPasswordActivity : AppCompatActivity() {
    var emailAddress: String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        setUpViews()
    }
    private fun setUpViews() {
        edtemailaddress.doOnTextChanged { text, start, before, count ->
            if (!TextUtils.isEmpty(emailAddress) && !sharedPreferenceInstance!!.isEmailValid(emailAddress!!)) {
                Utility.showSnackBarValidationError(forgotPasswordActivityLayout, getString(R.string.please_enter_valid_email), this)
            }
        }

        btnSubmit.setSafeOnClickListener {
            btnSubmit.startAnimation(AlphaAnimation(1f, 0.5f))
            sharedPreferenceInstance!!.hideSoftKeyBoard(this, btnSubmit)
            validateAndForgot()
        }
    }

    private fun validateAndForgot() {
        emailAddress = edtemailaddress.text.toString().trim()

        if(TextUtils.isEmpty(emailAddress)){
            Utility.showSnackBarValidationError(forgotPasswordActivityLayout, getString(R.string.please_enter_valid_email), this)
        }else{
            if(isNetworkAvailable()){
                forgotPassword()
            }else{
                Utility.showSnackBarValidationError(forgotPasswordActivityLayout, getString(R.string.check_internet), this)
            }
        }
    }

    private fun forgotPassword() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        frgt_pass_progressBar.visibility = View.VISIBLE

        val builder = createBuilder(arrayOf("email", "lang"),
                arrayOf(emailAddress!!.trim { it <= ' ' },
                        sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""].toString()))

        val call = apiInterface.forgotPassword(builder.build())
        call!!.enqueue(object : Callback<MyResponse?> {
            override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                frgt_pass_progressBar.visibility= View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if(response.isSuccessful){
                        if (response.body()!!.status == 1){
                            Utility.showSnackBarOnResponseSuccess(forgotPasswordActivityLayout,
                                response.body()!!.message.toString(),
                                this@ForgotPasswordActivity)
                            startActivity(
                                    Intent(this@ForgotPasswordActivity,
                                        OtpVerificationActivity::class.java).putExtra("ref", "2").
                                    putExtra("emailaddress", emailAddress))
                            finish()
                        }else{
                            Utility.showSnackBarOnResponseError(forgotPasswordActivityLayout,
                                response.body()!!.message.toString(),
                                this@ForgotPasswordActivity)
                        }
                    }else{
                        Utility.showSnackBarOnResponseError(forgotPasswordActivityLayout,
                            getString(R.string.response_isnt_successful),
                            this@ForgotPasswordActivity)
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
                Utility.showSnackBarOnResponseError(forgotPasswordActivityLayout,
                    getString(R.string.check_internet),
                    this@ForgotPasswordActivity)
                frgt_pass_progressBar.visibility= View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
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