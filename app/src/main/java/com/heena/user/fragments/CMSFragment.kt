package com.heena.user.fragments

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.navigation.fragment.findNavController
import com.heena.user.Dialogs.NoInternetDialog
import com.heena.user.R
import com.heena.user.`interface`.APIUtils
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.extras.MyWebViewClient
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.addCardURL
import com.heena.user.utils.Utility.paymentURL
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_home.tv_title
import kotlinx.android.synthetic.main.fragment_c_m_s.*
import kotlinx.android.synthetic.main.fragment_c_m_s.view.*
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
class CMSFragment : Fragment() {
    var lang = ""
    var mView : View?=null
    var title = ""
    var aboutUsUrl = ""
    var faqUrl = ""
    var privacyPolicyUrl = ""
    var tncUrl = ""
    var TransToken = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString("title", "")
            TransToken = it.getString("TransToken", "")

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_c_m_s, container, false)
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
        )
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
    }

    private fun setUpViews() {
        requireActivity().tv_title.text = ""

        requireActivity().iv_back.setSafeOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            if (title.equals("addCards")){
                findNavController().popBackStack()
            }else{
                findNavController().navigate(R.id.action_cmsFragment_to_homeFragment)
            }
        }

        requireActivity().iv_notification.setSafeOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F,0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }

        when {
            sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""] == "en" -> {
                aboutUsUrl = "https://alniqasha.ae/about-us/en"
                privacyPolicyUrl = "https://alniqasha.ae/privacy-policy/en"
                tncUrl = "https://alniqasha.ae/terms-and-conditions/en"
                faqUrl = "https://alniqasha.ae/faq/en"
            }
            else -> {
                aboutUsUrl = "https://alniqasha.ae/about-us/ar"
                privacyPolicyUrl = "https://alniqasha.ae/privacy-policy/ar"
                tncUrl = "https://alniqasha.ae/terms-and-conditions/ar"
                faqUrl = "https://alniqasha.ae/faq/ar"
            }
        }

        instance = sharedPreferenceInstance!!
        lang = instance!![SharedPreferenceUtility.SelectedLang, ""].toString()
        Utility.setLanguage(requireContext(),lang)

        if (!Utility.hasConnection(requireContext())){
            val noInternetDialog = NoInternetDialog()
            noInternetDialog.isCancelable = false
            noInternetDialog.setRetryCallback(object : NoInternetDialog.RetryInterface{
                override fun retry() {
                    noInternetDialog.dismiss()
                    webView()
                    when (title) {
                        getString(R.string.about_us) -> {
                            mView!!.web_view.loadUrl(aboutUsUrl)
                        }
                        getString(R.string.privacy_and_policy) -> {
                            mView!!.web_view.loadUrl(privacyPolicyUrl)
                        }
                        getString(R.string.terms_and_conditions) -> {
                            mView!!.web_view.loadUrl(tncUrl)
                        }
                        getString(R.string.frequently_asked_questions) -> {
                            mView!!.web_view.loadUrl(faqUrl)
                        }
                        "addCards" -> {
                            mView!!.web_view.loadUrl(addCardURL+"${SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.UserId, 0]}")
                        }
                        getString(R.string.payment) -> {
                            mView!!.web_view.loadUrl(paymentURL+"${APIUtils.ServicePaymentTOKEN}")
                        }
                        else -> {
                            mView!!.web_view.loadUrl(aboutUsUrl)
                        }
                    }
                }

            })
            noInternetDialog.show(requireActivity().supportFragmentManager, "CMS Fragment")
        }else{
            webView()
        }
    }

    private fun webView() {
        mView!!.web_view.webViewClient = MyWebViewClient()
        mView!!.web_view.settings.javaScriptEnabled = true
        mView!!.web_view.settings.setSupportZoom(true)
        mView!!.web_view.settings.builtInZoomControls = true
        //Enable Multitouch if supported by ROM
        mView!!.web_view.settings.useWideViewPort = true
        mView!!.web_view.settings.loadWithOverviewMode = false
        mView!!.web_view.setBackgroundColor(Color.TRANSPARENT)
        if (Build.VERSION.SDK_INT >= 11) mView!!.web_view?.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null)

        /*   mView!!.web_view.loadUrl(url)*/
        if (title.equals("addCards")){
            mView!!.tv_title.text = ""
        }else{
            mView!!.tv_title.text = title
        }

        when (title) {
            getString(R.string.about_us) -> {
                mView!!.web_view.loadUrl(aboutUsUrl)
            }
            getString(R.string.privacy_and_policy) -> {
                mView!!.web_view.loadUrl(privacyPolicyUrl)
            }
            getString(R.string.terms_and_conditions) -> {
                mView!!.web_view.loadUrl(tncUrl)
            }
            getString(R.string.frequently_asked_questions) -> {
                mView!!.web_view.loadUrl(faqUrl)
            }
            "addCards" -> {
                mView!!.web_view.loadUrl(addCardURL+"${SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.UserId, 0]}")
            }
            getString(R.string.payment) -> {
                mView!!.web_view.loadUrl(paymentURL+"${APIUtils.ServicePaymentTOKEN}")
            }
            else -> {
                mView!!.web_view.loadUrl(aboutUsUrl)
            }
        }

        mView!!.web_view.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                super.onProgressChanged(view, progress)
                mView!!.cmsFragmentProgressBar.visibility = View.GONE

            }
        }
        mView!!.web_view.webViewClient = object : WebViewClient(){
            override fun shouldOverrideUrlLoading(
                view: WebView, url: String?
            ): Boolean {
                mView!!.cmsFragmentProgressBar.visibility = View.GONE
                return if(!url.equals("")){
                    if(url!!.contains("success")){
                        findNavController().popBackStack()
                    }else if (url!!.contains("google")){
                        findNavController().popBackStack()
                    }
                    true
                }else{
                    true
                }
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                mView!!.cmsFragmentProgressBar.visibility = View.GONE
                super.onPageFinished(view, url)
            }
        }
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

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CMSFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}