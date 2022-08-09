package com.heena.user.bottomsheetdialog

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.heena.user.R
import com.heena.user.`interface`.ClickInterface
import com.heena.user.adapters.FeaturedNaqashatAdapter
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.extras.SpaceItemDecoration
import com.heena.user.models.ManagerItem
import com.heena.user.models.ManagerListingResponse
import com.heena.user.utils.LogUtils
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import kotlinx.android.synthetic.main.fragment_all_managers_list.view.*
import kotlinx.android.synthetic.main.fragment_all_managers_list.view.rv_featured_naqashat
import kotlinx.android.synthetic.main.fragment_availablenaqasha_bottom_sheet.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AvailablenaqashaBottomSheetFragment : Fragment() {
    private var onAvailableNaqashaCallback : ClickInterface.OnAvailableNaqashaClick?=null
    private var mView : View?=null
    private var managersList = ArrayList<ManagerItem>()
    private lateinit var featuredNaqashatAdapter: FeaturedNaqashatAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_availablenaqasha_bottom_sheet, container, false)
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
        )
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mView!!.rv_available_naqasha.setOnClickListener{

        }
    }

    fun onAvailableNaqashaCallback(onAvailableNaqashaCallback : ClickInterface.OnAvailableNaqashaClick){
        this.onAvailableNaqashaCallback = onAvailableNaqashaCallback
    }


    private fun getManagerList() {
        val call = Utility.apiInterface.ManagersListing(sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang,""))
        call?.enqueue(object : Callback<ManagerListingResponse?> {
            override fun onResponse(call: Call<ManagerListingResponse?>, response: Response<ManagerListingResponse?>) {
                if(response.isSuccessful){
                    if (response.body()!=null){
                        if (response.body()!!.status==1){
                            mView!!.tv_no_naqashats_found.visibility=View.GONE
                            mView!!.rv_available_naqasha.visibility=View.VISIBLE
                            managersList = response.body()!!.manager as ArrayList<ManagerItem>
                            setFeaturedNaqashatAdapter(managersList)
                        }else{
                            mView!!.noManagersView.visibility=View.VISIBLE
                            mView!!.rv_featured_naqashat.visibility=View.GONE
                        }
                    }else{
                        mView!!.noManagersView.visibility=View.VISIBLE
                        mView!!.rv_featured_naqashat.visibility=View.GONE
                        Utility.showSnackBarOnResponseError(mView!!.availableNaqashatBottomSheetFrameLayout,
                            response.body()!!.message.toString(),
                            requireContext())
                    }
                }else{
                    mView!!.noManagersView.visibility=View.VISIBLE
                    mView!!.rv_featured_naqashat.visibility=View.GONE
                    Utility.showSnackBarOnResponseError(mView!!.availableNaqashatBottomSheetFrameLayout,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext())
                }
            }

            override fun onFailure(call: Call<ManagerListingResponse?>, throwable: Throwable) {
                if (mView!!.swipeRefresh.isRefreshing) {
                    mView!!.swipeRefresh.isRefreshing = false
                }
                mView!!.managers_listing_frag_progressBar.visibility = View.GONE
                mView!!.noManagersView.visibility=View.GONE
                mView!!.rv_featured_naqashat.visibility=View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(mView!!.availableNaqashatBottomSheetFrameLayout,
                    requireContext().getString(R.string.check_internet),
                    requireContext())
            }

        })
    }

    private fun setFeaturedNaqashatAdapter(managersList: ArrayList<ManagerItem>) {
        mView!!.rv_featured_naqashat.layoutManager = GridLayoutManager(requireContext(), 2)
        mView!!.rv_featured_naqashat.itemAnimator = DefaultItemAnimator()
        featuredNaqashatAdapter = FeaturedNaqashatAdapter(requireContext(), managersList, object : ClickInterface.OnRecyclerItemClick{
            override fun OnClickAction(position: Int) {
                findNavController().navigate(R.id.action_allManagerListFragment_to_featuredNaqashatProfileFragment)
            }
        })
        mView!!.rv_featured_naqashat.adapter = featuredNaqashatAdapter
        mView!!.rv_featured_naqashat.addItemDecoration(SpaceItemDecoration(4))
    }


    companion object {
        const val TAG = "AvailablenaqashaBottomSheetFragment"
        private var instance: SharedPreferenceUtility? = null
        fun newInstance(): AvailablenaqashaBottomSheetFragment {
            return AvailablenaqashaBottomSheetFragment()
        }

        @Synchronized
        fun getInstance(): SharedPreferenceUtility {
            if (instance == null) {
                instance = SharedPreferenceUtility()
            }
            return instance as SharedPreferenceUtility
        }
    }

}