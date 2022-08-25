package com.heena.user.fragments

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.view.animation.AlphaAnimation
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.heena.user.Dialogs.NoInternetDialog
import com.heena.user.R
import com.heena.user.`interface`.APIInterface
import com.heena.user.`interface`.APIUtils
import com.heena.user.`interface`.ClickInterface
import com.heena.user.activities.HomeActivity
import com.heena.user.application.MyApp
import com.heena.user.extras.MyWebViewClient
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.DPOPaymentRedirectURL
import com.heena.user.utils.Utility.payment_status
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.custom_toast_layout.*
import kotlinx.android.synthetic.main.custom_toast_layout.view.*
import kotlinx.android.synthetic.main.fragment_payment_details.view.*
import kotlinx.android.synthetic.main.fragment_service_payment.view.*
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class PaymentDetailsFragment : Fragment(){
    var TransToken = ""
    var title = ""
    var user_id = ""
    var service_id = ""
    var manager_id = ""
    var address_id = ""
    var b_to = ""
    var b_from = ""
    var b_date = ""
    var message = ""
    var adr_lat = ""
    var adr_long = ""
    var adr_location = ""
    var c_ladies = ""
    var c_children = ""
    var total_price = ""
    var TransID = ""
    var TransactionToken = ""
    var mView : View?=null
    private lateinit var cancelPaymentDialog: androidx.appcompat.app.AlertDialog.Builder
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString("title", "")
            TransToken = it.getString("TransToken", "")
            user_id = it.getString("user_id", "")
            service_id = it.getString("service_id", "")
            manager_id = it.getString("manager_id", "")
            address_id = it.getString("address_id", "")
            b_to = it.getString("b_to", "")
            b_from = it.getString("b_from", "")
            b_date = it.getString("b_date", "")
            message = it.getString("message", "")
            adr_lat = it.getString("adr_lat", "")
            adr_long = it.getString("adr_long", "")
            adr_location = it.getString("adr_location", "")
            c_ladies = it.getString("c_ladies", "")
            c_children = it.getString("c_children", "")
            total_price = it.getString("total_price", "")

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_payment_details, container, false)
        Utility.changeLanguage(
            requireContext(),
            MyApp.sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
        )
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
    }

    private fun setUpViews() {
        requireActivity().tv_title.text = ""

        setOnClickBottomItemView()

        if (!Utility.hasConnection(requireContext())){
            val noInternetDialog = NoInternetDialog()
            noInternetDialog.isCancelable = false
            noInternetDialog.setRetryCallback(object : NoInternetDialog.RetryInterface{
                override fun retry() {
                    noInternetDialog.dismiss()
                    webView()
                }
            })
            noInternetDialog.show(requireActivity().supportFragmentManager, "Payment Fragment")
        }else{
            webView()
        }
    }

    private fun webView() {
        mView!!.webViewPayment.webViewClient = MyWebViewClient()
        mView!!.webViewPayment.settings.javaScriptEnabled = true
        mView!!.webViewPayment.settings.setSupportZoom(true)
        mView!!.webViewPayment.settings.builtInZoomControls = true
        //Enable Multitouch if supported by ROM
        mView!!.webViewPayment.settings.useWideViewPort = true
        mView!!.webViewPayment.settings.loadWithOverviewMode = false
        mView!!.webViewPayment.setBackgroundColor(Color.TRANSPARENT)
        if (Build.VERSION.SDK_INT >= 11) mView!!.webViewPayment?.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null)
        mView!!.webViewPayment.loadUrl(Utility.paymentURL +"${APIUtils.ServicePaymentTOKEN}")

        mView!!.webViewPayment.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                super.onProgressChanged(view, progress)
                mView!!.paymentFragmentProgressBar.visibility = View.GONE

            }
        }
        mView!!.webViewPayment.webViewClient = object : WebViewClient(){
            override fun shouldOverrideUrlLoading(
                view: WebView, url: String?
            ): Boolean {
                mView!!.paymentFragmentProgressBar.visibility = View.GONE
                return if(!url.equals("")){
                    if(url!!.contains(TransToken)){
                        APIUtils.ServicePayment = false
                        mView!!.webViewPayment.loadUrl(url)

                        mView!!.webViewPayment.webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView, progress: Int) {
                                super.onProgressChanged(view, progress)
                                mView!!.paymentFragmentProgressBar.visibility = View.GONE

                            }
                        }
                        mView!!.webViewPayment.webViewClient = object : WebViewClient(){
                            override fun shouldOverrideUrlLoading(
                                view: WebView, url: String?
                            ): Boolean {
                                mView!!.paymentFragmentProgressBar.visibility = View.GONE
                                Log.e("url", url.toString())
                                if (url.toString().contains("$DPOPaymentRedirectURL?TransID=")){
                                    APIUtils.ServicePayment = false
                                    TransID =url!!.split("TransID=")[1].split("&")[0]
                                    TransactionToken = url.split("TransID=")[1].split("&")[3].split("TransactionToken=")[1]
                                    if(adr_lat.isEmpty()){
                                        adr_lat = "0"
                                    }
                                    if (adr_long.isEmpty()){
                                        adr_long = "0"
                                    }
                                    successtransaction()
//                                    findNavController().popBackStack()

                                }else{
                                    true
                                }
                                return true
                            }
                            override fun onPageFinished(view: WebView?, url: String?) {
                                mView!!.paymentFragmentProgressBar.visibility = View.GONE
                                super.onPageFinished(view, url)
                            }
                        }
                    }
                    true
                }else{
                    true
                }
                return true
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                mView!!.paymentFragmentProgressBar.visibility = View.GONE
                super.onPageFinished(view, url)
            }
        }
    }
    fun showBookingConfirmDialog(context: Context, message : String) {
        val customToastLayout = layoutInflater.inflate(R.layout.custom_toast_layout,llCustomToastContainer)
        val customToast = Toast(context)
        customToast.view = customToastLayout
        customToast.setGravity(Gravity.CENTER,0,0)
        customToast.duration = Toast.LENGTH_LONG
        customToast.show()
    }


    fun successtransaction(){
        mView!!.paymentFragmentProgressBar.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        if(adr_lat.isEmpty()){
            adr_lat = "0"
        }else if(adr_long.isEmpty()){
            adr_long = "0"
        }
        val builder = APIInterface.createBuilder(
            arrayOf(
                "user_id", "service_id", "manager_id",
                "address_id", "b_to", "b_from",
                "b_date", "message", "adr_lat",
                "adr_long", "adr_location", "c_ladies",
                "c_children", "total_price", "TransactionToken", "TransID"
            ),
            arrayOf(
                user_id,
                service_id,
                manager_id,
                address_id,
                b_to,
                b_from,
                b_date,
                message,
                adr_lat,
                adr_long,
                adr_location,
                c_ladies,
                c_children,
                total_price, TransactionToken, TransID)
        )



        val call = Utility.apiInterface.successtransaction(builder.build())
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                mView!!.paymentFragmentProgressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if(response.isSuccessful) {
                        if (response.body() != null) {
                            val jsonObject = JSONObject(response.body()!!.string())
                            if (jsonObject.getInt("status") == 1) {
                                SharedPreferenceUtility.getInstance()
                                    .save(SharedPreferenceUtility.SavedAddress, "")
                                SharedPreferenceUtility.getInstance()
                                    .save(SharedPreferenceUtility.SavedLat, "")
                                SharedPreferenceUtility.getInstance()
                                    .save(SharedPreferenceUtility.SavedLng, "")
                                MyApp.sharedPreferenceInstance!!.save(SharedPreferenceUtility.SavedBookingDate, "")
                                showBookingConfirmDialog(requireContext(), getString(R.string.service_booked_successfully))
                                Utility.payment_flag = true
                                findNavController().navigate(R.id.appointmentFragment)
                            } else {
                                Utility.showSnackBarOnResponseError(
                                    mView!!.paymentFragmentDPOConstraintLayout,
                                    requireContext().getString(R.string.response_isnt_successful),
                                    requireContext()
                                )
                            }


                        } else {
                            Utility.showSnackBarOnResponseError(
                                mView!!.paymentFragmentDPOConstraintLayout,
                                requireContext().getString(R.string.response_isnt_successful),
                                requireContext()
                            )
                        }
                    }else{
                        Utility.showSnackBarOnResponseError(
                            mView!!.paymentFragmentDPOConstraintLayout,
                            requireContext().getString(R.string.response_isnt_successful),
                            requireContext()
                        )
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
                Utility.showSnackBarOnResponseError(mView!!.paymentFragmentDPOConstraintLayout,
                    requireContext().getString(R.string.response_isnt_successful),
                    requireContext())
            }

        })
    }

    private fun showCancelPaymentDialog() {
        cancelPaymentDialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())

        cancelPaymentDialog.setTitle(R.string.cancel_payment)
        cancelPaymentDialog.setMessage(R.string.cancel_payment_message)
        cancelPaymentDialog.setCancelable(false)
        cancelPaymentDialog.setPositiveButton(R.string.yes, object : DialogInterface.OnClickListener{
            override fun onClick(p0: DialogInterface?, p1: Int) {
                requireActivity().finish()
                showBookingConfirmDialog(R.string.cancel_message.toString(), 0)
                startActivity(Intent(requireContext(), HomeActivity::class.java))
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
    private fun showBookingConfirmDialog(message: String, status : Int) {
        val customToastLayout = layoutInflater.inflate(R.layout.custom_toast_layout,llCustomToastContainer)
        val customToast = Toast(requireContext())
        customToast.view = customToastLayout
        customToast.setGravity(Gravity.CENTER,0,0)
        customToast.duration = Toast.LENGTH_LONG
        if (status==0) {
            customToastLayout.tv_booking_confirmed.text = getString(R.string.cancellation_status)
            customToastLayout.tv_booking_confirmed_message.text =message
        }else{
            customToastLayout.tv_booking_confirmed.text = getString(R.string.payment_status)
            customToastLayout.tv_booking_confirmed_message.text = getString(R.string.payment_successful_message)
        }
        customToast.show()
    }

    private fun setOnClickBottomItemView() {
        requireActivity().itemAppointment.setOnClickListener {
            if (findNavController().currentDestination?.id != R.id.appointmentFragment) {
                requireActivity().itemAppointment.startAnimation(AlphaAnimation(1f, 0.5f))
                showCancelPaymentDialog()
            }

        }
        requireActivity().itemFavourites.setOnClickListener {
            if (findNavController().currentDestination?.id != R.id.favouritesFragment) {
                requireActivity().itemFavourites.startAnimation(AlphaAnimation(1f, 0.5f))
                showCancelPaymentDialog()
            }

        }
        requireActivity().itemHome.setOnClickListener {
            if (findNavController().currentDestination?.id != R.id.homeFragment) {
                requireActivity().itemHome.startAnimation(AlphaAnimation(1f, 0.5f))
                showCancelPaymentDialog()
            }
        }

        requireActivity().itemCategories.setOnClickListener {
            requireActivity().itemCategories.startAnimation(AlphaAnimation(1f, 0.5f))
            showCancelPaymentDialog()
        }

        requireActivity().itemSettings.setOnClickListener {
            if (findNavController().currentDestination?.id != R.id.settingsFragment) {
                requireActivity().itemSettings.startAnimation(AlphaAnimation(1f, 0.5f))
                showCancelPaymentDialog()
            }
        }
        requireActivity().iv_back.setSafeOnClickListener {
            requireActivity().iv_back.startAnimation(
                AlphaAnimation(1f, 0.5f)
            )
            showCancelPaymentDialog()
        }
        requireActivity().iv_notification.setSafeOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F, 0.5F))
             showCancelPaymentDialog()
        }

        requireActivity().img.setSafeOnClickListener {
            requireActivity().img.startAnimation(AlphaAnimation(1F, 0.5F))
            showCancelPaymentDialog()
        }
    }
}