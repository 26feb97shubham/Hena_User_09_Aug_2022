package com.heena.user.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import com.heena.user.R
import com.heena.user.`interface`.APIInterface.Companion.createBuilder
import com.heena.user.models.MyResponse
import com.heena.user.utils.LogUtils
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.apiInterface
import com.heena.user.utils.Utility.isNetworkAvailable
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_reset_password.*
import okhttp3.FormBody
import retrofit2.Call
import retrofit2.Response

class ResetPasswordActivity : AppCompatActivity() {
    lateinit var builder : FormBody.Builder
    var password : String?= null
    private var cnfrmpass : String? = null
    private var emailaddress : String?=null
    private var otp : String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)
        if (intent.extras!=null){
            emailaddress = intent.getStringExtra("email").toString()
            otp = intent.getStringExtra("otp").toString()
        }

        btnSubmit.setSafeOnClickListener {
            validation()
        }
    }

    private fun validation() {
        password = edtPassword.text.toString().trim()
        cnfrmpass = edtConfirmPassword.text.toString().trim()
        if (TextUtils.isEmpty(password)) {
            Utility.showSnackBarValidationError(resetPasswordActivity, getString(R.string.please_enter_your_password), this)
        } else if (password!!.length in 16 downTo 6) {
            Utility.showSnackBarValidationError(resetPasswordActivity, getString(R.string.password_length_valid), this)
        } else if (!SharedPreferenceUtility.getInstance().isPasswordValid(password!!)) {
            Utility.showSnackBarValidationError(resetPasswordActivity, getString(R.string.password_length_valid), this)
        } else if (TextUtils.isEmpty(cnfrmpass)) {
            Utility.showSnackBarValidationError(resetPasswordActivity, getString(R.string.please_enter_your_confirm_password), this)
        } else if (!cnfrmpass.equals(password)) {
            Utility.showSnackBarValidationError(resetPasswordActivity, getString(R.string.password_doesnt_match_with_verify_password), this)
        }else{
            if (isNetworkAvailable()){
                getResetPass()
            }else{
                Utility.showSnackBarValidationError(resetPasswordActivity, getString(R.string.check_internet), this)
            }
        }
    }

    private fun getResetPass() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        reset_pass_progressBar.visibility= View.VISIBLE

        builder = createBuilder(arrayOf("email", "otp", "lang", "password"),
                arrayOf(emailaddress!!,otp!!, SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""].toString(),cnfrmpass!!))

        val call = apiInterface.resetpassword(builder.build())
        call!!.enqueue(object : retrofit2.Callback<MyResponse?> {
            override fun onResponse(
                    call: Call<MyResponse?>,
                    response: Response<MyResponse?>
            ) {
                reset_pass_progressBar.visibility= View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        Utility.showSnackBarOnResponseSuccess(resetPasswordActivity,
                            response.body()!!.message.toString(),
                        this@ResetPasswordActivity)
                        startActivity(
                                Intent(this@ResetPasswordActivity, LoginActivity::class.java))
                    }else{
                        Utility.showSnackBarOnResponseError(resetPasswordActivity,
                        response.body()!!.message.toString(),
                        this@ResetPasswordActivity)
                    }
                }else{
                    Utility.showSnackBarOnResponseError(resetPasswordActivity,
                    getString(R.string.response_isnt_successful),
                    this@ResetPasswordActivity)
                }
            }

            override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                LogUtils.e("msg", t.message)
                Utility.showSnackBarOnResponseError(resetPasswordActivity,
                    getString(R.string.check_internet),
                    this@ResetPasswordActivity)
                reset_pass_progressBar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}