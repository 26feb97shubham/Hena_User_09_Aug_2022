package com.heena.user

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.heena.user.`interface`.APIInterface.Companion.createBuilder
import com.heena.user.utils.LogUtils
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility.apiInterface
import com.heena.user.utils.Utility.payment_flag
import kotlinx.android.synthetic.main.custom_toast_layout.*
import kotlinx.android.synthetic.main.fragment_tap_payment_gateway.view.*
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class TapPaymentGateway : Fragment() {

    private var mView : View?=null

    private var paymentURL : String?=null
    private var redirectURL : String?=null
    private var tap_id : String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            paymentURL = it.getString("url")
            redirectURL = it.getString("redirect_url")
            tap_id = it.getString("tap_id")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_tap_payment_gateway, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mView!!.wv_payment_gateway.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                mView!!.payment_gateway_progressBar.visibility= View.VISIBLE
                if(progress>=80){
                    mView!!.payment_gateway_progressBar.visibility= View.GONE
                }

            }
        }
        mView!!.wv_payment_gateway.webViewClient = object : WebViewClient(){
            override fun shouldOverrideUrlLoading(
                view: WebView, url: String?
            ): Boolean {
                if(!redirectURL.equals("")){
                    redirect(tap_id)
                    return true
                }else{
                    return false
                }
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }
        }
        mView!!.wv_payment_gateway.settings.allowContentAccess=true
        mView!!.wv_payment_gateway.settings.loadWithOverviewMode=true
        mView!!.wv_payment_gateway.settings.useWideViewPort=true
        mView!!.wv_payment_gateway.settings.loadsImagesAutomatically=true
        mView!!.wv_payment_gateway.loadUrl(paymentURL.toString())
    }

    private fun redirect(tap_id: String?) {
        mView!!.payment_gateway_progressBar.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val builder = createBuilder(arrayOf("tap_id", "lang"),
            arrayOf(tap_id.toString(), SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""]))
        val call = apiInterface.booking_redirect(builder.build())
        call.enqueue(object : Callback<ResponseBody?>{
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                try {
                    if (response.body()!=null){
                        val responseBody = response.body()!!.string()
                        val jsonObject = JSONObject(responseBody)
                        if (jsonObject.getInt("status")==1){
                            Log.e("response", jsonObject.toString())
                            SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.SavedAddress,"")
                            SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.SavedLat,"")
                            SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.SavedLng,"")
                            showBookingConfirmDialog(requireContext())
                            payment_flag = true
                            findNavController().navigate(R.id.appointmentFragment)
                        }else{
                            LogUtils.shortToast(requireContext(), jsonObject.getString("message"))
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
            }

        })
    }


    fun showBookingConfirmDialog(context: Context) {
        val customToastLayout = layoutInflater.inflate(R.layout.custom_toast_layout,llCustomToastContainer)
        val customToast = Toast(context)
        customToast.view = customToastLayout
        customToast.setGravity(Gravity.CENTER,0,0)
        customToast.duration = Toast.LENGTH_LONG
        customToast.show()
    }
}