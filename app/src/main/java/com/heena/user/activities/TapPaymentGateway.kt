package com.heena.user.activities

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.heena.user.R
import com.heena.user.`interface`.APIInterface.Companion.createBuilder
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import kotlinx.android.synthetic.main.activity_tap_payment_gateway.*
import kotlinx.android.synthetic.main.custom_toast_layout.*
import kotlinx.android.synthetic.main.custom_toast_layout.view.*
import kotlinx.android.synthetic.main.fragment_tap_payment_gateway.view.*
import kotlinx.android.synthetic.main.item_faq.*
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class TapPaymentGateway : AppCompatActivity() {
    private var paymentURL : String?=null
    private var redirectURL : String?=null
    private var tap_id : String?=null
    private var status = 0

    lateinit var cancelPaymentDialog: AlertDialog.Builder

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tap_payment_gateway)
        Utility.changeLanguage(
            this,
            sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]
        )
        if (intent.extras!=null){
            paymentURL = intent.extras!!.getString("url")
            redirectURL = intent.extras!!.getString("redirect_url")
            tap_id = intent.extras!!.getString("tap_id")
        }
   

        wv_payment_gateway_activity.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                payment_gateway_progressBar_activity.visibility= View.VISIBLE
                if(progress>=80){
                    payment_gateway_progressBar_activity.visibility= View.GONE
                }

            }
        }
        wv_payment_gateway_activity.webViewClient = object : WebViewClient(){
            override fun shouldOverrideUrlLoading(
                view: WebView, url: String?
            ): Boolean {
                return if(!redirectURL.equals("")){
                    this@TapPaymentGateway.redirect(tap_id)
                    true
                }else{
                    false
                }
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }
        }
        wv_payment_gateway_activity.settings.javaScriptEnabled=true
        wv_payment_gateway_activity.settings.allowContentAccess=true
//        webView.settings.builtInZoomControls=true
        wv_payment_gateway_activity.settings.loadWithOverviewMode=true
        wv_payment_gateway_activity.settings.useWideViewPort=true
        wv_payment_gateway_activity.settings.loadsImagesAutomatically=true
        wv_payment_gateway_activity.loadUrl(paymentURL.toString())
    }

    private fun redirect(tap_id: String?) {
        payment_gateway_progressBar_activity.visibility = View.VISIBLE
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val builder = createBuilder(arrayOf("tap_id", "lang"),
            arrayOf(tap_id.toString(), sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]))
        val call = Utility.apiInterface.booking_redirect(builder.build())
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                payment_gateway_progressBar_activity.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.body()!=null){
                        val responseBody = response.body()!!.string()
                        val jsonObject = JSONObject(responseBody)
                        status = jsonObject.getInt("status")
                        if (status==1){
                            Log.e("response", jsonObject.toString())
                            showBookingConfirmDialog(jsonObject.getString("message").toString(), status)
                            startActivity(Intent(this@TapPaymentGateway, HomeActivity::class.java))
                            //finish()
                        }else if (status==0){
                            Log.e("response", jsonObject.toString())
                            showBookingConfirmDialog(jsonObject.getString("message").toString(), status)
                            startActivity(Intent(this@TapPaymentGateway, HomeActivity::class.java))
                            //finish()
                        }
                    }
                }catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Log.e("error", t.message.toString())
                payment_gateway_progressBar_activity.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }

    private fun showBookingConfirmDialog(message: String, status : Int) {
        val customToastLayout = layoutInflater.inflate(R.layout.custom_toast_layout,llCustomToastContainer)
        val customToast = Toast(applicationContext)
        customToast.view = customToastLayout
        customToast.setGravity(Gravity.CENTER,0,0)
        customToast.duration = Toast.LENGTH_LONG
        if (status==0) {
            customToastLayout.tv_booking_confirmed.text = getString(R.string.booking_failed)
            customToastLayout.tv_booking_confirmed_message.text = message
        }else{
            customToastLayout.tv_booking_confirmed.text = getString(R.string.booking_confirmed)
            customToastLayout.tv_booking_confirmed_message.text = message
        }
        customToast.show()
    }

    override fun onBackPressed() {
        cancelPaymentDialog = AlertDialog.Builder(this)

        cancelPaymentDialog.setTitle(R.string.cancel_payment)
        cancelPaymentDialog.setMessage(R.string.cancel_payment_message)
        cancelPaymentDialog.setCancelable(false)
        cancelPaymentDialog.setPositiveButton(R.string.yes, object : DialogInterface.OnClickListener{
            override fun onClick(p0: DialogInterface?, p1: Int) {
                finish()
                showBookingConfirmDialog(R.string.cancel_message.toString(), 0)
                startActivity(Intent(this@TapPaymentGateway, HomeActivity::class.java))
            }
        })

        cancelPaymentDialog.setNegativeButton(R.string.no, object : DialogInterface.OnClickListener{
            override fun onClick(p0: DialogInterface?, p1: Int) {
                p0!!.cancel()
            }
        })



        val cancelAlert = cancelPaymentDialog.create()
        cancelAlert.setTitle(R.string.cancel_payment)
        cancelAlert.show()
    }
}