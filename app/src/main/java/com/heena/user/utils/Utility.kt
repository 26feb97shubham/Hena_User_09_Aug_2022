package com.heena.user.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.heena.user.`interface`.APIInterface
import com.heena.user.application.MyApp
import com.heena.user.broadcastreceiver.ConnectivityReceiver
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import com.heena.user.R
import com.heena.user.activities.LoginActivity
import com.heena.user.extras.SafeClickListener
import com.heena.user.models.Content
import com.heena.user.models.HelpCategory
import com.heena.user.models.HelpSubCategory
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.custom_toast_layout.*
import java.util.*
import kotlin.collections.ArrayList
import android.util.TypedValue
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnCompleteListener

import com.heena.user.*
import com.heena.user.`interface`.APIInterface.Companion.getClient
import com.heena.user.`interface`.APIInterface.Companion.getDPOPaymentClient
import com.heena.user.`interface`.APIInterface.Companion.getManagerClient
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import java.text.SimpleDateFormat


object Utility {
    val apiInterface: APIInterface = getClient()!!.create(APIInterface::class.java)
    val dpoPaymentApiInterface: APIInterface = getDPOPaymentClient()!!.create(APIInterface::class.java)
    val apiManagerInterface: APIInterface = getManagerClient()!!.create(APIInterface::class.java)
    val IMAGE_DIRECTORY_NAME = "Heena_User"
    var booking_item_type = 0
    var favourite_item_type = 0
    var sender_admin = 0 /* 1 for sender and 2 for receiver*/
    var networkChangeReceiver: ConnectivityReceiver? = null
    var helpCategory:HelpCategory?=null
    var helpSubCategory:HelpSubCategory?=null
    var content : ArrayList<Content>?=null
    var payment_flag = false
    var payment_status = ""
    var mSelectedItem = -1
    var reference = ""
    var already_booked_time = ""
    var already_booked_date = ""
    var isreview = false
    const val addCardURL = "https://alniqasha.ae/page/payment_form/"
    const val paymentURL = "https://secure.3gdirectpay.com/payv3.php?ID="
    const val DPOPaymentRedirectURL = "https://alniqasha.ae/success"
    var doubleClick:Boolean=false
    fun changeLanguage(context: Context, language: String){
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        context.resources
            .updateConfiguration(config, context.resources.displayMetrics)
    }
    fun getLanguage() : String{
        return sharedPreferenceInstance!!.get(
            SharedPreferenceUtility.SelectedLang,
            ""
        )
    }

    fun showSnackBarOnResponseError(view: View, message: String, context: Context) {
        val snackBar = Snackbar.make(
            view, message, Snackbar.LENGTH_LONG
        )
        snackBar.setBackgroundTint(ContextCompat.getColor(context, R.color.colorSnackBarRed))
        snackBar.setTextColor(ContextCompat.getColor(context, R.color.white))
        snackBar.allowInfiniteLines()
        snackBar.show()
    }

    fun showSnackBarValidationError(view: View, message: String, context: Context) {
        val snackBar = Snackbar.make(
            view, message, Snackbar.LENGTH_LONG
        )
        snackBar.setBackgroundTint(ContextCompat.getColor(context, R.color.colorSnackBarRed))
        snackBar.setTextColor(ContextCompat.getColor(context, R.color.white))
        snackBar.allowInfiniteLines()
        snackBar.show()
    }

    fun showSnackBarOnResponseSuccess(view: View, message: String, context: Context) {
        val snackBar = Snackbar.make(
            view, message, Snackbar.LENGTH_LONG
        )
        snackBar.setBackgroundTint(ContextCompat.getColor(context, R.color.gold))
        snackBar.setTextColor(ContextCompat.getColor(context, R.color.black))
        snackBar.allowInfiniteLines()
        snackBar.show()
    }


    fun setLanguage(context: Context, language: String){
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        context.resources
            .updateConfiguration(config, context.resources.displayMetrics)
    }

    private fun Snackbar.allowInfiniteLines(): Snackbar {
        return apply { (view.findViewById<View?>(R.id.snackbar_text) as? TextView?)?.isSingleLine = false }
    }

    fun isNetworkAvailable(): Boolean {
        return try {
            val connectivityManager = MyApp.instance!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
        }catch (e: NullPointerException){
            showLog(e.localizedMessage)
            false
        }
    }

    private fun showLog(localizedMessage: String?) {
        Log.e("NetworkChangeReceiver", "" + localizedMessage)
    }

    fun convertDoubleValueWithCommaSeparator(doubleValue: Double): String {
        return String.format(Locale.ENGLISH,"%,.2f", doubleValue)
    }

    fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }

    fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }

    fun hasConnection(ct: Context): Boolean {
        val cm = (ct.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
        @SuppressLint("MissingPermission") val wifiNetwork =
            cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        if (wifiNetwork != null && wifiNetwork.isConnected) {
            return true
        }
        val mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        if (mobileNetwork != null && mobileNetwork.isConnected) {
            return true
        }
        val activeNetwork = cm.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }

    fun deviceId(context : Context?){
        val deviceId = Settings.Secure.getString(context?.contentResolver, Settings.Secure.ANDROID_ID)
        Log.e("deviceId", deviceId)
        sharedPreferenceInstance!!.save(SharedPreferenceUtility.DeviceId,deviceId.toString())
    }

    fun getFCMToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("getInstanceId", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                val fcmToken = task.result
                Log.e("getInstanceId", fcmToken)
                sharedPreferenceInstance!!.save(
                    SharedPreferenceUtility.FCMTOKEN,
                    fcmToken.toString()
                )
            })

    }

    fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
        val safeClickListener = SafeClickListener {
            onSafeClick(it)
        }
        setOnClickListener(safeClickListener)
    }

    fun exitApp(context: Context, activity : Activity) {
        val toast = Toast.makeText(
            context,
            context.getString(R.string.please_click_back_again_to_exist),
            Toast.LENGTH_SHORT
        )

        if(doubleClick){
            activity.finishAffinity()
            doubleClick=false
        }
        else{
            doubleClick=true
            Handler(Looper.getMainLooper()).postDelayed({
                toast.show()
                doubleClick = false
            }, 500)
        }
    }

    fun loginSignUpAccessAlertDialogBox(context: Context){
        val loginSignUpAccessAlertDialog = AlertDialog.Builder(context)
        loginSignUpAccessAlertDialog.setCancelable(false)
        loginSignUpAccessAlertDialog.setTitle(context.getString(R.string.login_or_signup))
        loginSignUpAccessAlertDialog.setMessage(context.getString(R.string.please_login_signup_to_access_this_functionality))
        loginSignUpAccessAlertDialog.setPositiveButton(context.getString(R.string.ok)
        ) { dialog, _ ->
            context.startActivity(Intent(context, LoginActivity::class.java))
            dialog!!.dismiss()
        }
        loginSignUpAccessAlertDialog.setNegativeButton(context.getString(R.string.cancel)
        ) { dialog, _ -> dialog!!.cancel() }
        loginSignUpAccessAlertDialog.show()
    }

    fun spToPx(sp: Float, context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            context.resources.displayMetrics
        )
            .toInt()
    }
}