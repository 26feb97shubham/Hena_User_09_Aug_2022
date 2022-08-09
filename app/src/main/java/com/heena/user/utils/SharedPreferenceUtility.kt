package com.heena.user.utils

import android.content.Context
import android.content.SharedPreferences
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.heena.user.application.MyApp
import com.heena.user.models.ServiceItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.heena.user.models.AddressItem
import java.io.IOException
import java.lang.reflect.Type
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class SharedPreferenceUtility {

    private val sharedPreferences: SharedPreferences

    private val editor: SharedPreferences.Editor
        get() = sharedPreferences.edit()

    init {
        instance = this
        sharedPreferences = MyApp.instance!!.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)

    }

    fun hideSoftKeyBoard(context: Context, view: View) {
        try {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    fun isPasswordValid(str: String): Boolean {
        var ch: Char
        var capitalFlag = false
        var lowerCaseFlag = false
        var numberFlag = false
        if(str.length>=6) {
            for (i in 0 until str.length) {
                ch = str[i]
                if (Character.isDigit(ch)) {
                    numberFlag = true
                } else if (Character.isUpperCase(ch)) {
                    capitalFlag = true
                } else if (Character.isLowerCase(ch)) {
                    lowerCaseFlag = true
                }
                if (numberFlag && capitalFlag && lowerCaseFlag) return true
            }
        }

        return false
    }
    fun isUserNameValid(username: String): Boolean {
        var isValid = false
        val expression = "^[a-zA-Z0-9]([._](?![._])|[a-zA-Z0-9]){3,18}[a-zA-Z0-9]\$"
        val inputStr: CharSequence = username
        val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher: Matcher = pattern.matcher(inputStr)
        if (matcher.matches()) {
            isValid = true
        }
        return isValid
    }

    fun delete(key: String) {
        if (sharedPreferences.contains(key)) {
            editor.remove(key).commit()
        }
    }

    fun save(key: String, value: Any?) {
        val editor = editor
        if (value is Boolean) {
            editor.putBoolean(key, (value as Boolean?)!!)
        } else if (value is Int) {
            editor.putInt(key, (value as Int?)!!)
        } else if (value is Float) {
            editor.putFloat(key, (value as Float?)!!)
        } else if (value is Long) {
            editor.putLong(key, (value as Long?)!!)
        } else if (value is String) {
            editor.putString(key, value as String?)
        } else if (value is Enum<*>) {
            editor.putString(key, value.toString())
        } else if (value is HashSet<*>) {
            editor.putStringSet(key, value as Set<String>?)
        } else if (value != null) {
            throw RuntimeException("Attempting to save non-supported preference")
        }

        editor.commit()
    }

    operator fun <T> get(key: String): T {
        return (sharedPreferences.all[key] as T?)!!
    }

    operator fun <T> get(key: String, defValue: T): T {
        val returnValue = sharedPreferences.all[key] as T?
        return returnValue ?: defValue
    }

    fun isEmailValid(email: String): Boolean {
        return Pattern.compile(
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]|[\\w-]{2,}))@"
                        + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                        + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        + "[0-9]{1,2}|25[0-5]|2[0-4][0-9]))|"
                        + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$"
        ).matcher(email).matches()
    }

    fun isCharacterAllowed(validateString: String): Boolean {
        var containsInvalidChar = false
        for (i in 0 until validateString.length) {
            val type = Character.getType(validateString[i])
            containsInvalidChar = !(type == Character.SURROGATE.toInt() || type == Character.OTHER_SYMBOL.toInt())
        }
        return containsInvalidChar
    }

    companion object {
        private const val SHARED_PREFS_NAME = "Hena User"
        private var instance: SharedPreferenceUtility? = null
        const val FCMTOKEN = "fcmToken"
        const val DeviceId = "device_id"
        const val TOKEN = "token"
        const val UserId = "userId"
        const val ManagerId = "managerId"
        const val UserEmail = "userEmail"
        const val UserPhone = "userPhone"
        const val SelectedLang = "selectedLang"
        const val IsLogin = "isLogin"
        const val IsGuestUserLogin = "isGuestUserLogin"
        const val IsWelcomeShow = "isWelcomeShow"
        const val IsRemembered = "isRemembered"
        const val FIRSTTIME = "isFirstTime"
        const val ISSELECTLANGUAGE = "isSelectLanguage"
        const val Username = "username"
        const val Password= "password"
        const val SavedLat= "savedLat"
        const val SavedLng= "savedLng"
        const val SavedAddress= "savedAddress"
        const val SavedBookingDate= "savedBookingDate"
        const val ISINTRODUCTION = "isIntroduction"
        const val ProfilePic= "profilepic"
        const val IsResend = "isResend"
        const val IsVerified = "isVerified"
        const val PLACECLICK = "placeClick"
        const val ISBUTTONSELECTED = "isBottonSelected"
        const val NOOFLADIES = "noOfLadies"
        const val NOOFCHILDRENS = "noOF"
        var myAddressItem : AddressItem?=null
        var is_first_time = true


        @Synchronized
        fun getInstance(): SharedPreferenceUtility {
            if (instance == null) {
                instance = SharedPreferenceUtility()
            }
            return instance as SharedPreferenceUtility
        }

    }
}