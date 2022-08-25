package com.heena.user.application

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Base64.DEFAULT
import android.util.Base64.encode
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import com.heena.user.R
import com.google.android.libraries.places.api.Places
import com.google.firebase.FirebaseApp
import com.heena.user.utils.LogUtils
import com.heena.user.utils.SharedPreferenceUtility
import io.socket.client.IO
import io.socket.client.Manager
import io.socket.client.Socket
import io.socket.utf8.UTF8.encode
import okhttp3.OkHttpClient
import java.util.*
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.security.cert.CertificateException

class  MyApp : Application() {
    companion object {
        var socket: Socket? = null
        var instance: MyApp? = null
        var sharedPreferenceInstance : SharedPreferenceUtility?=null
        var SOCKET_URL: String = "https://alniqasha.ae:17303"
    }
    public override fun attachBaseContext(base: Context) {
        instance = this
        MultiDex.install(this)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.attachBaseContext(base)

    }
    @Synchronized
    fun getInstance(): MyApp? {
        return instance
    }
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        sharedPreferenceInstance = SharedPreferenceUtility.getInstance()

        if (!Places.isInitialized()) {
            val plainApiKey =getString(R.string.places_api_key)//

            val encodedString: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Base64.getEncoder().encodeToString(plainApiKey.toByteArray())
            } else {
                getString(R.string.places_api_key)
            }

            Places.initialize(this, encodedString, Locale.US)
        }

         AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

         try {
             val mySSLContext = SSLContext.getInstance("TLS")
             val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                 override fun getAcceptedIssuers(): Array<out java.security.cert.X509Certificate>? {
                     return arrayOf()
                 }

                 @Throws(CertificateException::class)
                 override fun checkClientTrusted(
                     p0: Array<out java.security.cert.X509Certificate>?,
                     authType: String?
                 ) {
                 }

                 @Throws(CertificateException::class)
                 override fun checkServerTrusted(
                     p0: Array<out java.security.cert.X509Certificate>?,
                     authType: String?
                 ) {
                 }
             })
             mySSLContext.init(null, trustAllCerts, null)
             val myHostnameVerifier = HostnameVerifier { hostname, session -> true }
             val okHttpClient = OkHttpClient.Builder()
                 .hostnameVerifier(myHostnameVerifier)
                 .sslSocketFactory(mySSLContext.socketFactory, object : X509TrustManager {

                     override fun getAcceptedIssuers(): Array<out java.security.cert.X509Certificate>? {
                         return arrayOf()
                     }

                     @Throws(CertificateException::class)
                     override fun checkClientTrusted(
                         p0: Array<out java.security.cert.X509Certificate>?,
                         authType: String?
                     ) {
                     }

                     @Throws(CertificateException::class)
                     override fun checkServerTrusted(
                         p0: Array<out java.security.cert.X509Certificate>?,
                         authType: String?
                     ) {
                     }
                 })
                 .build()
             // default settings for all sockets
             IO.setDefaultOkHttpWebSocketFactory(okHttpClient)
             IO.setDefaultOkHttpCallFactory(okHttpClient)
             // set as an option
             val opts = IO.Options()
             opts.callFactory = okHttpClient
             opts.webSocketFactory = okHttpClient
             opts.forceNew = true
             //  socket = IO.socket(ChatConstant.CHAT_SERVER_URL, opts);
             socket = IO.socket(SOCKET_URL, opts)
              socket!!.io().open(Manager.OpenCallback { e ->
                  if (e != null) {
                      LogUtils.e("call", "call: " + e.message)
                  }
              })
         } catch (e: Exception) {
             throw RuntimeException(e)
         }
    }

     fun getSocket(): Socket? {
         return socket
     }
}