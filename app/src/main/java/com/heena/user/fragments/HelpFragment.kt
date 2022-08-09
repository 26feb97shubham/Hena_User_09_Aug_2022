package com.heena.user.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.heena.user.R
import com.heena.user.`interface`.APIInterface.Companion.createBuilder
import com.heena.user.`interface`.ClickInterface
import com.heena.user.adapters.MainHelpCateforyAdapter
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.models.DashHelpCategoryResponse
import com.heena.user.models.HelpCategory
import com.heena.user.utils.LogUtils
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.apiInterface
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_filtered_offers_and_discounts.view.*
import kotlinx.android.synthetic.main.fragment_help.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HelpFragment : Fragment() {
    private var mView : View?=null
    private var main_help_category_list = ArrayList<HelpCategory>()
    private var main_help_category : HelpCategory?=null
    private var mainHelpCategoryAdapter : MainHelpCateforyAdapter?=null
    var admin_id = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView =inflater.inflate(R.layout.fragment_help, container, false)
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
        )
        mView!!.rv_help_text.visibility = View.VISIBLE
        mView!!.mtv_help_desc.text=requireContext().getString(R.string.good_day_nhow_can_we_help_you_today)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().tv_title.text = ""

        requireActivity().iv_back.setSafeOnClickListener {
            requireActivity().iv_back.startAnimation(
                AlphaAnimation(1f, 0.5f)
            )
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        requireActivity().iv_notification.setSafeOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F,0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }

        showHelp()
    }

    private fun showHelp() {
        mView!!.frag_help_progressBar.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val builder = createBuilder(arrayOf("user_id","lang","role"),
            arrayOf(sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0].toString(),
                sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""], "user"))
        val call = apiInterface.dashHelpCategory(builder.build())
        call!!.enqueue(object : Callback<DashHelpCategoryResponse?>{
            override fun onResponse(
                call: Call<DashHelpCategoryResponse?>,
                response: Response<DashHelpCategoryResponse?>
            ) {
                mView!!.frag_help_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        main_help_category_list.clear()
                        admin_id = response.body()!!.admin_id
                        main_help_category_list = response.body()!!.help_category as ArrayList<HelpCategory>
                        setMainHelpCategoryAdapter(main_help_category_list, admin_id)
                    }
                }else{
                    Utility.showSnackBarOnResponseError(mView!!.helpFragmentConstraintLayout,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext())
                }
            }

            override fun onFailure(call: Call<DashHelpCategoryResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(mView!!.helpFragmentConstraintLayout,
                    requireContext().getString(R.string.response_isnt_successful),
                    requireContext())
                mView!!.frag_help_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }

    private fun setMainHelpCategoryAdapter(mainHelpCategoryList: ArrayList<HelpCategory>, admin_id : Int) {
        mView!!.rv_help_text.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL, false)
        mainHelpCategoryAdapter = MainHelpCateforyAdapter(requireContext(), mainHelpCategoryList, admin_id, object : ClickInterface.mainhelpCategoryClicked{
            override fun mainHelpCategory(position: Int) {
                main_help_category = mainHelpCategoryList[position]
                Utility.helpCategory = main_help_category
            }

        }, findNavController())
        mView!!.rv_help_text.adapter = mainHelpCategoryAdapter
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
}