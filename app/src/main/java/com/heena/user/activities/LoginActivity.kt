package com.heena.user.activities

import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import com.heena.user.Dialogs.NoInternetDialog
import com.heena.user.R
import com.heena.user.`interface`.APIInterface
import com.heena.user.`interface`.APIInterface.Companion.createBuilder
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.broadcastreceiver.ConnectivityReceiver
import com.heena.user.models.LoginResponse
import com.heena.user.utils.LogUtils
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.SharedPreferenceUtility.Companion.is_first_time
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.apiInterface
import com.heena.user.utils.Utility.getFCMToken
import com.heena.user.utils.Utility.networkChangeReceiver
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_forgot_password.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class LoginActivity : AppCompatActivity(), ConnectivityReceiver.ConnectivityReceiverListener {
    var remembered:Boolean=false
    var username: String = ""
    var password: String = ""
    private var showPass = false
    private var device_token : String?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        tv_signup.setText(R.string.sign_up)
        setUpViews()
    }
    private fun setUpViews() {

        networkChangeReceiver = ConnectivityReceiver()
        networkChangeReceiver!!.NetworkChangeReceiver(this)
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, intentFilter)

        if(sharedPreferenceInstance!![SharedPreferenceUtility.IsRemembered, false]){
            remembered = true
            if(sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]=="en") {
                chkRememberMe.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check, 0, 0, 0)
            }
            else{
                chkRememberMe.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check, 0)
            }
            username=sharedPreferenceInstance!![SharedPreferenceUtility.Username, ""]
            password=sharedPreferenceInstance!![SharedPreferenceUtility.Password, ""]
            edtUsername.setText(username)
            edtPass.setText(password)
        }else{
            remembered = false
            edtUsername.setText("")
            edtPass.setText("")
            if(sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]=="en") {
                chkRememberMe.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ellipse, 0, 0, 0)
            }
            else{
                chkRememberMe.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ellipse, 0)
            }
        }

        iv_pass_show_hide_login.setSafeOnClickListener {
            if (showPass){
                showPass = false
                edtPass.transformationMethod = null
                iv_pass_show_hide_login.setImageResource(R.drawable.visible)
            }else{
                showPass = true
                edtPass.transformationMethod = PasswordTransformationMethod()
                iv_pass_show_hide_login.setImageResource(R.drawable.invisible)
            }
        }
        chkRememberMe.setOnClickListener {
            if(remembered){
                remembered=false
                sharedPreferenceInstance!!.save(SharedPreferenceUtility.Username, username)
                if(sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]=="en") {
                    chkRememberMe.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ellipse, 0, 0, 0)
                }
                else{
                    chkRememberMe.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ellipse, 0)
                }

            }
            else{
                remembered=true
                if(sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]=="en") {
                    chkRememberMe.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check, 0, 0, 0)
                }
                else{
                    chkRememberMe.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check, 0)
                }
            }
        }

        txtForgotPass.setSafeOnClickListener {
            txtForgotPass.startAnimation(AlphaAnimation(1f, 0.5f))
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }


        btnLogin.setSafeOnClickListener {
            btnLogin.startAnimation(AlphaAnimation(1f, 0.5f))
            sharedPreferenceInstance!!.hideSoftKeyBoard(this, btnLogin)

            if(!Utility.hasConnection(this)){
                val noInternetDialog = NoInternetDialog()
                noInternetDialog.isCancelable = false
                noInternetDialog.setRetryCallback(object : NoInternetDialog.RetryInterface{
                    override fun retry() {
                        noInternetDialog.dismiss()
                        validateAndLogin()
                    }

                })
                noInternetDialog.show(supportFragmentManager, "Login Activity")
            }else{
                validateAndLogin()
            }
        }

        tv_signup.setSafeOnClickListener {
            tv_login.startAnimation(AlphaAnimation(1f,0.5f))
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        btnGuestUser.setSafeOnClickListener {
            btnGuestUser.startAnimation(AlphaAnimation(1f, 0.5f))
            sharedPreferenceInstance!!.hideSoftKeyBoard(this, btnGuestUser)
            sharedPreferenceInstance!!.save(SharedPreferenceUtility.IsLogin, false)
            sharedPreferenceInstance!!.save(SharedPreferenceUtility.IsGuestUserLogin, true)
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }


    private fun validateAndLogin() {
        username = edtUsername.text.toString().trim()
        password= edtPass.text.toString().trim()

        when {
            TextUtils.isEmpty(username) -> {
                Utility.showSnackBarValidationError(loginActivity, getString(R.string.please_enter_your_username_email), this)
            }
            TextUtils.isEmpty(password) -> {
                Utility.showSnackBarValidationError(loginActivity, getString(R.string.please_enter_your_password), this)
            }
            else -> {
                if(remembered){
                    sharedPreferenceInstance!!.save(SharedPreferenceUtility.IsRemembered, remembered)
                    sharedPreferenceInstance!!.save(SharedPreferenceUtility.Username, username)
                    sharedPreferenceInstance!!.save(SharedPreferenceUtility.Password, password)
                } else{
                    sharedPreferenceInstance!!.save(SharedPreferenceUtility.IsRemembered, remembered)
                    sharedPreferenceInstance!!.save(SharedPreferenceUtility.Username, "")
                    sharedPreferenceInstance!!.save(SharedPreferenceUtility.Password, "")
                }
                getLogin()
            }
        }

    }

    private fun getLogin() {
        if (!Utility.hasConnection(this)){
            val noInternetDialog = NoInternetDialog()
            noInternetDialog.isCancelable = false
            noInternetDialog.show(supportFragmentManager, "Login Activity")
        }else{
            device_token =
                if (sharedPreferenceInstance!![SharedPreferenceUtility.FCMTOKEN, ""] == ""){
                    getFCMToken()
                    sharedPreferenceInstance!![SharedPreferenceUtility.FCMTOKEN, ""]
                }else{
                    sharedPreferenceInstance!![SharedPreferenceUtility.FCMTOKEN, ""]
                }


            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            progressBar.visibility= View.VISIBLE

            val builder = createBuilder(arrayOf("username", "password", "device_token","device_type","lang"),
                arrayOf(username.trim { it <= ' ' },
                    password.trim { it <= ' ' },
                    device_token.toString(),
                    "1",
                    sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""].toString()))

            val call = apiInterface.login(builder.build())
            call!!.enqueue(object : Callback<LoginResponse?> {
                override fun onResponse(call: Call<LoginResponse?>, response: Response<LoginResponse?>) {
                    progressBar.visibility= View.GONE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    try {
                        if(response.isSuccessful){
                            sharedPreferenceInstance!!.save(SharedPreferenceUtility.FIRSTTIME, false)
                            is_first_time = false
                            when (response.body()!!.status) {
                                1 -> {
                                    sharedPreferenceInstance!!.save(SharedPreferenceUtility.IsLogin, true)
                                    sharedPreferenceInstance!!.save(SharedPreferenceUtility.IsGuestUserLogin, false)
                                    sharedPreferenceInstance!!.save(SharedPreferenceUtility.IsVerified, true)
                                    sharedPreferenceInstance!!.save(SharedPreferenceUtility.UserId, response.body()!!.user!!.user_id)
                                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                                }
                                2 -> {
                                    val user = response.body()!!.user
                                    sharedPreferenceInstance!!.save(SharedPreferenceUtility.IsLogin, false)
                                    sharedPreferenceInstance!!.save(SharedPreferenceUtility.IsGuestUserLogin, false)
                                    sharedPreferenceInstance!!.save(SharedPreferenceUtility.IsVerified, false)
                                    sharedPreferenceInstance!!.save(SharedPreferenceUtility.UserEmail, user!!.email)
                                    sharedPreferenceInstance!!.save(SharedPreferenceUtility.UserPhone, user!!.phone)
                                    sharedPreferenceInstance!!.save(SharedPreferenceUtility.IsVerified, false)
                                    sharedPreferenceInstance!!.save(SharedPreferenceUtility.UserId, response.body()!!.user!!.user_id)
                                    Utility.showSnackBarOnResponseSuccess(loginActivity,
                                        response.body()!!.message.toString(),
                                        this@LoginActivity)
                                    startActivity(Intent(this@LoginActivity, OtpVerificationActivity::class.java).putExtra("ref", "1").putExtra("emailaddress",
                                        sharedPreferenceInstance!![SharedPreferenceUtility.UserEmail, ""]
                                    ))
                                }
                                else -> {
                                    Utility.showSnackBarOnResponseError(loginActivity,
                                        response.body()!!.message.toString(),
                                        this@LoginActivity)
                                }
                            }
                        }else{
                            Utility.showSnackBarOnResponseError(loginActivity,
                                getString(R.string.response_isnt_successful),
                                this@LoginActivity)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(call: Call<LoginResponse?>, throwable: Throwable) {
                    LogUtils.e("msg", throwable.message)
                    Utility.showSnackBarOnResponseError(loginActivity,
                        getString(R.string.check_internet),
                        this@LoginActivity)
                    progressBar.visibility = View.GONE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, intentFilter)
        if(sharedPreferenceInstance!![SharedPreferenceUtility.IsRemembered, false]){
            if(sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]=="en") {
                chkRememberMe.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check, 0, 0, 0)
            }
            else{
                chkRememberMe.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check, 0)
            }
            username=sharedPreferenceInstance!![SharedPreferenceUtility.Username, ""]
            password=sharedPreferenceInstance!![SharedPreferenceUtility.Password, ""]
            edtUsername.setText(username)
            edtPass.setText(password)
        }else{
            edtUsername.setText("")
            edtPass.setText("")
            if(sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]=="en") {
                chkRememberMe.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ellipse, 0, 0, 0)
            }
            else{
                chkRememberMe.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ellipse, 0)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver)
        }
    }

    override fun onBackPressed() {
        Utility.exitApp(this, this)
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

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        getFCMToken()
    }
}