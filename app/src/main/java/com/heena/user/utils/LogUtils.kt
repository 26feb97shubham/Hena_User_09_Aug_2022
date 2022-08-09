package com.heena.user.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.viewbinding.BuildConfig


object LogUtils {
    fun e(key: String?, message: String?) {
        if (BuildConfig.DEBUG) {
            if (message != null) {
                Log.e(key, message)
            }
        }
    }
    fun d(key: String?, message: String?) {
        if (BuildConfig.DEBUG) {
            if (message != null) {
                Log.d(key, message)
            }
        }
    }

    fun shortToast(context: Context?, message: String?) {
        if (context != null) {
            val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
            toast.show()
        }
    }

    fun longToast(context: Context?, message: String?) {
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
}