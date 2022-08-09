package com.heena.user.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.location.Address
import android.location.Geocoder
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged
import com.heena.user.Dialogs.NoInternetDialog
import com.heena.user.R
import com.heena.user.`interface`.APIInterface
import com.heena.user.broadcastreceiver.ConnectivityReceiver
import com.heena.user.models.SignUpResponse
import com.heena.user.utils.LogUtils
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AddressComponent
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.heena.user.`interface`.APIInterface.Companion.createBuilder
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.utils.Utility.apiInterface
import com.heena.user.utils.Utility.getFCMToken
import com.heena.user.utils.Utility.isNetworkAvailable
import com.heena.user.utils.Utility.networkChangeReceiver
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.fragment_change_password.view.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.*

class SignUpActivity : AppCompatActivity(), ConnectivityReceiver.ConnectivityReceiverListener {
    private var spannableString : SpannableString?= null
    var username: String = ""
    private var mobilenumber: String = ""
    var emailaddress: String = ""
    var password: String = ""
    private var confirmPassword: String = ""
    private var countryName: String = ""
    var location : String? = ""
    private var show_pass = false
    private var show_cnfrm_pass = false
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0
    private var address : String = ""
    private var isChecked: Boolean=false
    private var device_token : String?= null
    var doubleClick:Boolean=false
    private val appPerms = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private var activityResultLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()) { result ->
            var allAreGranted = true
            for(b in result.values) {
                allAreGranted = allAreGranted && b
            }

            if(!allAreGranted) {
                LogUtils.shortToast(this, getString(R.string.please_allow_permissions))
            }
        }
    private var placeAPIresultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            val place = Autocomplete.getPlaceFromIntent(data!!)
            val latlng = place.latLng
            mLatitude = latlng!!.latitude
            mLongitude = latlng.longitude
            val addressComponents: MutableList<AddressComponent> = place.addressComponents!!.asList()
            var ard = 0
            var add = ""
            for (addressComponent in addressComponents) {
                Log.e("addressss--", addressComponent.name)
                countryName = addressComponent.name
                if (ard == 0) {
                    add = addressComponent.name
                }

                var flag = false
                val types: MutableList<String> = addressComponent.types
                for (type in types) {
                    if (type.equals(
                            "locality",
                            true
                        ) || type == "administrative_area_level_2" || type.equals(
                            "administrative_area_level_1",
                            true
                        )
                    ) {
                        flag = true
                    }
                }
                if (flag) {
                    val center = LatLng(mLatitude, mLongitude)
                    val locality = getLocality(center)
                    val countryName = getCountry(center)

                    var address = ""

                    if (add.isNotEmpty()) {
                        address = add
                    }
                    if (locality.isNotEmpty() && address.isNotEmpty() && address != locality) {
                        address = "$address, $locality"
                    }
                    if (!(addressComponent.name.isEmpty() || address.isEmpty() || address == addressComponent.name)) {
                        address = address + ", " + addressComponent.name
                    }
                    if (!(countryName.isEmpty() || address.isEmpty())) {
                        address = "$address, $countryName"
                    }
                    edtlocation_signup.text = address
                    break
                }
                ard++
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        networkChangeReceiver = ConnectivityReceiver()
        networkChangeReceiver!!.NetworkChangeReceiver(this)
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, intentFilter)
        spannableString = SpannableString(getString(R.string.log_in))
        spannableString!!.setSpan(UnderlineSpan(), 0, spannableString!!.length, 0)
        tv_login_signup.text = spannableString
        activityResultLauncher.launch(appPerms)
        setUpViews()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpViews() {
        btnSignUp.setSafeOnClickListener {
            btnSignUp.startAnimation(AlphaAnimation(1f, 0.5f))
            sharedPreferenceInstance!!.hideSoftKeyBoard(this, btnSignUp)
            if(!Utility.hasConnection(this)){
                val noInternetDialog = NoInternetDialog()
                noInternetDialog.isCancelable = false
                noInternetDialog.setRetryCallback(object : NoInternetDialog.RetryInterface{
                    override fun retry() {
                        noInternetDialog.dismiss()
                        validateAndSignUp()
                    }

                })
                noInternetDialog.show(supportFragmentManager, "SignUp Activity")
            }
            validateAndSignUp()
        }

        tv_login_signup.setSafeOnClickListener {
            tv_login_signup.startAnimation(AlphaAnimation(1f,0.5f))
            startActivity(Intent(this, LoginActivity::class.java))
        }

        scrollView.setOnTouchListener { _, _ ->
            edtUsername_signup.clearFocus()
            edtemailaddress_signup.clearFocus()
            edtmobilenumber_signup.clearFocus()
            edtpassword_signup.clearFocus()
            edtcnfrmpassword_signup.clearFocus()
            false
        }
        edtcnfrmpassword_signup.doOnTextChanged { charSeq, _, _, _ ->
            val pass =edtpassword_signup.text.toString()

            if(!TextUtils.isEmpty(pass)){
                if(!pass.equals(charSeq.toString(), false)){
                    edtcnfrmpassword_signup.error=getString(R.string.password_doesnt_match_with_verify_password)
                }
            }
            else{
                edtpassword_signup.error=getString(R.string.please_first_enter_your_password)
            }
        }

        iv_cnfrm_pass_show_hide.setOnClickListener {
            if (show_cnfrm_pass){
                show_cnfrm_pass = false
                iv_cnfrm_pass_show_hide.setImageResource(R.drawable.visible)
                edtcnfrmpassword_signup.transformationMethod = HideReturnsTransformationMethod()
            }else{
                show_cnfrm_pass = true
                iv_cnfrm_pass_show_hide.setImageResource(R.drawable.invisible)
                edtcnfrmpassword_signup.transformationMethod = PasswordTransformationMethod()
            }
        }

        iv_pass_show_hide.setOnClickListener {
            if (show_pass){
                show_pass = false
                iv_pass_show_hide.setImageResource(R.drawable.visible)
                edtpassword_signup.transformationMethod = HideReturnsTransformationMethod()
            }else{
                show_pass = true
                iv_pass_show_hide.setImageResource(R.drawable.invisible)
                edtpassword_signup.transformationMethod = PasswordTransformationMethod()
            }
        }
        edtlocation_signup.setSafeOnClickListener {
            edtlocation_signup.startAnimation(AlphaAnimation(1f, 0.5f))
            val fields: MutableList<Place.Field> = ArrayList()
            fields.add(Place.Field.NAME)
            fields.add(Place.Field.ID)
            fields.add(Place.Field.LAT_LNG)
            fields.add(Place.Field.ADDRESS)
            fields.add(Place.Field.ADDRESS_COMPONENTS)

            // Start the autocomplete intent.
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this)
            placeAPIresultLauncher.launch(intent)
        }

        imgChk.setOnClickListener {
            imgChk.startAnimation(AlphaAnimation(1f, 0.5f))
            if(isChecked){
                isChecked=false
                imgChk.setImageResource(R.drawable.un_check)
            }
            else{
                isChecked=true
                imgChk.setImageResource(R.drawable.check)
            }
        }

        txtTermsConditions.setSafeOnClickListener {
            startActivity(Intent(this, TermsAndConditionsActivity::class.java))
        }

    }

    private fun validateAndSignUp() {
        username = edtUsername_signup.text.toString().trim()
        mobilenumber = edtmobilenumber_signup.text.toString().trim()
        emailaddress = edtemailaddress_signup.text.toString().trim()
        password= edtpassword_signup.text.toString().trim()
        confirmPassword= edtcnfrmpassword_signup.text.toString().trim()
        address = edtlocation_signup.text.toString().trim()

        if (TextUtils.isEmpty(username)) {
            Utility.showSnackBarValidationError(singnUpConstraintLayout, getString(R.string.please_enter_your_username), this)
        } else if (!isCharacterAllowed(username)) {
            Utility.showSnackBarValidationError(singnUpConstraintLayout, getString(R.string.emojis_are_not_allowed), this)
        }else if (TextUtils.isEmpty(mobilenumber)) {
            Utility.showSnackBarValidationError(singnUpConstraintLayout, getString(R.string.please_enter_your_phone_number), this)
        }else if ((mobilenumber.length < 7 || mobilenumber.length > 15)) {
            Utility.showSnackBarValidationError(singnUpConstraintLayout, getString(R.string.mob_num_length_valid), this)
        }else if (TextUtils.isEmpty(emailaddress)) {
            Utility.showSnackBarValidationError(singnUpConstraintLayout, getString(R.string.please_enter_valid_email), this)
        }else if (!sharedPreferenceInstance!!.isEmailValid(emailaddress)) {
            Utility.showSnackBarValidationError(singnUpConstraintLayout, getString(R.string.please_enter_valid_email), this)
        }else if (TextUtils.isEmpty(address)) {
            Utility.showSnackBarValidationError(singnUpConstraintLayout, getString(R.string.please_enter_valid_loc), this)
        }else if (TextUtils.isEmpty(password)) {
            Utility.showSnackBarValidationError(singnUpConstraintLayout, getString(R.string.please_enter_your_password), this)
        }else if (!sharedPreferenceInstance!!.isPasswordValid(password)) {
            Utility.showSnackBarValidationError(singnUpConstraintLayout, getString(R.string.password_length_valid), this)
        }else if (TextUtils.isEmpty(confirmPassword)) {
            Utility.showSnackBarValidationError(singnUpConstraintLayout, getString(R.string.please_enter_your_confirm_password), this)
        }else if (confirmPassword != password) {
            Utility.showSnackBarValidationError(singnUpConstraintLayout, getString(R.string.password_doesnt_match_with_verify_password), this)
        }else if(!isChecked){
            Utility.showSnackBarValidationError(singnUpConstraintLayout, getString(R.string.please_accept_terms_conditions), this)
        }else{
            sharedPreferenceInstance!!.save(SharedPreferenceUtility.Password, confirmPassword)
            getSignUp()
        }
    }

    private fun getSignUp() {
        if (!Utility.hasConnection(this)){
            val noInternetDialog = NoInternetDialog()
            noInternetDialog.isCancelable = false
            noInternetDialog.show(supportFragmentManager, "SignUp Activity")
        }else{
            device_token =
                if (sharedPreferenceInstance!![SharedPreferenceUtility.FCMTOKEN, ""] == ""){
                    getFCMToken()
                    sharedPreferenceInstance!![SharedPreferenceUtility.FCMTOKEN, ""]
                }else{
                    sharedPreferenceInstance!![SharedPreferenceUtility.FCMTOKEN, ""]
                }
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            progressBarSignUp.visibility= View.VISIBLE
            if (isNetworkAvailable()){
                val builder = createBuilder(arrayOf("username","country_code", "phone", "email", "password","device_token", "device_type","lat", "long", "lang", "address", "role"),
                    arrayOf(username.trim { it <= ' ' },
                        "+971",
                        mobilenumber.trim { it <= ' ' },
                        emailaddress.trim { it <= ' ' },
                        password.trim { it <= ' ' },
                        sharedPreferenceInstance!![SharedPreferenceUtility.FCMTOKEN, ""].toString(),
                        "1",
                        mLatitude.toString(),
                        mLongitude.toString(),
                        sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""].toString(),
                    address, "user"))

                val call = apiInterface.signUp(builder.build())
                call!!.enqueue(object : Callback<SignUpResponse?> {
                    override fun onResponse(call: Call<SignUpResponse?>, response: Response<SignUpResponse?>) {
                        progressBarSignUp.visibility= View.GONE
                        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                        try {
                            if(response.isSuccessful){
                                sharedPreferenceInstance!!.save(SharedPreferenceUtility.FIRSTTIME, false)
                                SharedPreferenceUtility.is_first_time = false
                                if (response.body()!!.status==1){
                                    sharedPreferenceInstance!!.save(SharedPreferenceUtility.ProfilePic, response.body()!!.image)
                                    sharedPreferenceInstance!!.save(SharedPreferenceUtility.TOKEN, response.body()!!.token)
                                    sharedPreferenceInstance!!.save(SharedPreferenceUtility.UserId, response.body()!!.token)
                                    sharedPreferenceInstance!!.save(SharedPreferenceUtility.UserEmail, emailaddress)
                                    sharedPreferenceInstance!!.save(SharedPreferenceUtility.UserPhone, mobilenumber)
                                    sharedPreferenceInstance!!.save(SharedPreferenceUtility.Username, username)
                                    sharedPreferenceInstance!!.save(SharedPreferenceUtility.Password, password)
                                    Utility.showSnackBarOnResponseSuccess(singnUpConstraintLayout,
                                    response.body()!!.message.toString(),
                                    this@SignUpActivity)
                                    startActivity(Intent(this@SignUpActivity, OtpVerificationActivity::class.java).
                                    putExtra("ref", "1").
                                    putExtra("emailaddress", emailaddress))
                                    finish()
                                }else{
                                    Utility.showSnackBarOnResponseError(singnUpConstraintLayout,
                                        response.body()!!.message.toString(),
                                        this@SignUpActivity)
                                }
                            }else{
                                Utility.showSnackBarOnResponseError(singnUpConstraintLayout,
                                    getString(R.string.response_isnt_successful),
                                    this@SignUpActivity)
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(call: Call<SignUpResponse?>, throwable: Throwable) {
                        LogUtils.e("msg", throwable.message)
                        Utility.showSnackBarOnResponseError(singnUpConstraintLayout,
                            getString(R.string.check_internet),
                            this@SignUpActivity)
                        progressBarSignUp.visibility = View.GONE
                        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    }
                })
            }else{
                Utility.showSnackBarOnResponseError(singnUpConstraintLayout,
                    getString(R.string.check_internet),
                    this@SignUpActivity)
            }
        }
    }

    private fun isCharacterAllowed(validateString: String): Boolean {
        var containsInvalidChar = false
        for (element in validateString) {
            val type = Character.getType(element)
            containsInvalidChar = !(type == Character.SURROGATE.toInt() || type == Character.OTHER_SYMBOL.toInt())
        }
        return containsInvalidChar
    }

    private fun getLocality(latLng: LatLng): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses: List<Address>
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            var subLocality = ""
            if (!addresses[0].subLocality.isNullOrEmpty()) {
                subLocality = addresses[0].subLocality
            } else {
                if (!addresses[0].locality.isNullOrEmpty()) {
                    subLocality = addresses[0].locality
                }
            }
            return subLocality
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }


    private fun getCountry(latLng: LatLng): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses: List<Address>
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            var subLocality = ""
            if (!addresses[0].countryName.isNullOrEmpty()) {
                subLocality = addresses[0].countryName
            }
            return subLocality
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }


    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver)
        }
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        getFCMToken()
    }

    override fun onBackPressed() {
        Utility.exitApp(this, this)
    }
}