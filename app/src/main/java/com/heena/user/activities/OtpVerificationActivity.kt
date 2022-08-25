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
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.models.MyResponse
import com.heena.user.models.OTPResend
import com.heena.user.utils.LogUtils
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.apiInterface
import com.heena.user.utils.Utility.isNetworkAvailable
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_forgot_password.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_otp_verification.*
import okhttp3.FormBody
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class OtpVerificationActivity : AppCompatActivity() {
    private lateinit var ref: String
    lateinit var pin: String
    lateinit var user_id: String
    lateinit var builder : FormBody.Builder
    lateinit var emailaddress : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verification)
        setUpViews()
    }

    private fun setUpViews() {
        btnVerify.isEnabled=false
        btnVerify.alpha=0.5f

        if(intent.extras!=null){
            emailaddress = intent.getStringExtra("emailaddress").toString()
            ref = intent.getStringExtra("ref").toString()
        }

        pin = ""
        pin = firstPinView.text.toString().trim()

        firstPinView.doOnTextChanged { charSeq, start, before, count ->
            if(charSeq!!.length==4){
                btnVerify.isEnabled=true
                btnVerify.alpha=1f
                sharedPreferenceInstance!!.hideSoftKeyBoard(this@OtpVerificationActivity, firstPinView)

            }
            else{
                btnVerify.isEnabled=false
                btnVerify.alpha=0.5f
            }
        }

        btnVerify.setSafeOnClickListener {
            btnVerify.startAnimation(AlphaAnimation(1f, 0.5f))
            validateAndVerification()
        }

        resend.setSafeOnClickListener {
            resend.startAnimation(AlphaAnimation(1f, 0.5f))
            firstPinView.setText("")
            sharedPreferenceInstance!!.hideSoftKeyBoard(this@OtpVerificationActivity, it)
            if (isNetworkAvailable()){
                if(ref=="1"){
                    resendOtp()
                }
                else{
                    forgotPassword()
                }
            }else{
                Utility.showSnackBarValidationError(otpVerificationActivity, getString(R.string.check_internet), this)
            }
        }
    }

    private fun forgotPassword() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        otp_verify_progressBar.visibility = View.VISIBLE

        val builder =createBuilder(arrayOf("email", "lang"),
                arrayOf(emailaddress.trim { it <= ' ' },
                        sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""].toString()))

        val call = apiInterface.forgotPassword(builder.build())
        call!!.enqueue(object : Callback<MyResponse?> {
            override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                otp_verify_progressBar.visibility= View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if(response.isSuccessful){
                        if (response.body()!!.status == 1){
                            Utility.showSnackBarOnResponseSuccess(otpVerificationActivity,
                                response.body()!!.message!!.toString(),
                            this@OtpVerificationActivity)
                            startActivity(
                                    Intent(this@OtpVerificationActivity, OtpVerificationActivity::class.java).
                                    putExtra("ref", "2").putExtra("emailaddress", emailaddress))
                            finish()
                        }else{
                            LogUtils.longToast(this@OtpVerificationActivity, response.body()!!.message)
                            Utility.showSnackBarOnResponseError(otpVerificationActivity,
                                response.body()!!.message!!.toString(),
                                this@OtpVerificationActivity)
                        }
                    }else{
                        Utility.showSnackBarOnResponseError(otpVerificationActivity,
                            getString(R.string.response_isnt_successful),
                            this@OtpVerificationActivity)
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
                Utility.showSnackBarOnResponseError(otpVerificationActivity,
                    getString(R.string.check_internet),
                    this@OtpVerificationActivity)
                otp_verify_progressBar.visibility= View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        })

    }

    private fun validateAndVerification() {
        pin = firstPinView.text.toString().trim()
        if (TextUtils.isEmpty(pin)) {
            Utility.showSnackBarValidationError(otpVerificationActivity, getString(R.string.please_enter_your_otp), this)

        }
        else if ((pin.length < 4)) {
            Utility.showSnackBarValidationError(otpVerificationActivity, getString(R.string.otp_length_valid), this)
        }

        else {
            if (isNetworkAvailable()){
                if(ref=="1"){
                    verifyAccount()
                }
                else{
                    forgotPassVerify()
                }
            }else{
                Utility.showSnackBarValidationError(otpVerificationActivity, getString(R.string.check_internet), this)
            }
        }

    }
    private fun forgotPassVerify() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        otp_verify_progressBar.visibility= View.VISIBLE
        builder = createBuilder(arrayOf("email", "otp", "lang"),
                arrayOf(emailaddress, pin.trim { it <= ' ' }, sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""].toString()))


        val call = apiInterface.forgotpassverifyotp(builder.build())
        call!!.enqueue(object : Callback<MyResponse?>{
            override fun onResponse(
                    call: Call<MyResponse?>,
                    response: Response<MyResponse?>
            ) {
                otp_verify_progressBar.visibility= View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        Utility.showSnackBarOnResponseSuccess(otpVerificationActivity,
                            response.body()!!.message.toString(),
                            this@OtpVerificationActivity)
                        startActivity(
                                Intent(this@OtpVerificationActivity,
                                    ResetPasswordActivity::class.java).putExtra("email", emailaddress).putExtra("otp",pin))
                        finish()
                    }else{
                        Utility.showSnackBarOnResponseError(otpVerificationActivity,
                            response.body()!!.message.toString(),
                            this@OtpVerificationActivity)
                    }
                }else{
                    Utility.showSnackBarOnResponseError(otpVerificationActivity,
                        getString(R.string.response_isnt_successful),
                        this@OtpVerificationActivity)
                }
            }

            override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                LogUtils.e("msg", t.message)
                Utility.showSnackBarOnResponseError(otpVerificationActivity,
                    getString(R.string.response_isnt_successful),
                    this@OtpVerificationActivity)
                otp_verify_progressBar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }


    private fun verifyAccount() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        otp_verify_progressBar.visibility= View.VISIBLE

        builder = createBuilder(arrayOf("email", "otp", "lang"),
            arrayOf(emailaddress, pin.trim { it <= ' ' }, sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]))

        val call = apiInterface.verifyotp(builder.build())
        call!!.enqueue(object : Callback<MyResponse?> {
            override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                otp_verify_progressBar.visibility= View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.isSuccessful){
                        if (response.body()!!.status==1){
                            sharedPreferenceInstance!!.save(SharedPreferenceUtility.IsVerified, true)
                            Utility.showSnackBarOnResponseSuccess(otpVerificationActivity,
                                response.body()!!.message.toString(),
                                this@OtpVerificationActivity)
                            startActivity(Intent(this@OtpVerificationActivity, LoginActivity::class.java))
                            finish()

                        }else{
                            Utility.showSnackBarOnResponseError(otpVerificationActivity,
                                response.body()!!.message.toString(),
                                this@OtpVerificationActivity)
                        }
                    }else{
                        Utility.showSnackBarOnResponseError(otpVerificationActivity,
                            getString(R.string.response_isnt_successful),
                            this@OtpVerificationActivity)
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
                Utility.showSnackBarOnResponseError(otpVerificationActivity,
                    getString(R.string.check_internet),
                    this@OtpVerificationActivity)
                otp_verify_progressBar.visibility= View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        })

    }
    private fun resendOtp() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        otp_verify_progressBar.visibility= View.VISIBLE

        val builder = createBuilder(arrayOf("email", "lang", "country_code", "phone"),
            arrayOf(sharedPreferenceInstance!![SharedPreferenceUtility.UserEmail, ""]
                ,sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""].toString(), "+971",
                sharedPreferenceInstance!![SharedPreferenceUtility.UserPhone, ""].toString()))


        val call = apiInterface.otpresend(builder.build())
        call!!.enqueue(object : Callback<OTPResend?> {
            override fun onResponse(call: Call<OTPResend?>, response: Response<OTPResend?>) {
                otp_verify_progressBar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.isSuccessful) {
                        if (response.body()!!.status == 1) {
                            sharedPreferenceInstance!!
                                .save(SharedPreferenceUtility.IsResend, true)
                            LogUtils.shortToast(this@OtpVerificationActivity, response.body()!!.message)
                            Utility.showSnackBarOnResponseSuccess(otpVerificationActivity,
                                response.body()!!.message.toString(),
                                this@OtpVerificationActivity)
                        } else {
                            Utility.showSnackBarOnResponseError(otpVerificationActivity,
                                response.body()!!.message.toString(),
                                this@OtpVerificationActivity)
                        }
                    } else {
                        Utility.showSnackBarOnResponseError(otpVerificationActivity,
                            getString(R.string.response_isnt_successful),
                            this@OtpVerificationActivity)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<OTPResend?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(otpVerificationActivity,
                    getString(R.string.check_internet),
                    this@OtpVerificationActivity)
                otp_verify_progressBar.visibility= View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        })

    }


    override fun onBackPressed() {
        super.onBackPressed()
        finish()
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