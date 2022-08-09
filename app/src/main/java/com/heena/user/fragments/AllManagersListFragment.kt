package com.heena.user.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.heena.user.R
import com.heena.user.`interface`.ClickInterface
import com.heena.user.adapters.GridFeaturedNaqashatAdapter
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.extras.SpaceItemDecoration
import com.heena.user.models.ManagerItem
import com.heena.user.models.ManagerListingResponse
import com.heena.user.utils.LogUtils
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_all_managers_list.view.*
import kotlinx.android.synthetic.main.fragment_home.view.rv_featured_naqashat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AllManagersListFragment : Fragment() {

    private var mView:View?=null
    private var managersList = ArrayList<ManagerItem>()
    private lateinit var featuredNaqashatAdapter: GridFeaturedNaqashatAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.fragment_all_managers_list, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]
        )
        requireActivity().tv_title.text = ""
        requireActivity().iv_back.setSafeOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().navigate(R.id.action_allManagerListFragment_to_homeFragment)
        }
        requireActivity().iv_notification.setSafeOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F,0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }

        getManagerList(true)

        mView!!.swipeRefresh.setOnRefreshListener {
            getManagerList(true)
        }

    }

    private fun getManagerList(isRefresh: Boolean) {
        if(!isRefresh) {
            mView!!.managers_listing_frag_progressBar.visibility = View.VISIBLE
        }
        val call = Utility.apiInterface.ManagersListing(sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""])
        call?.enqueue(object : Callback<ManagerListingResponse?> {
            override fun onResponse(call: Call<ManagerListingResponse?>, response: Response<ManagerListingResponse?>) {
                if(mView!!.swipeRefresh.isRefreshing){
                    mView!!.swipeRefresh.isRefreshing=false
                }
                mView!!.managers_listing_frag_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if(response.isSuccessful){
                    if (response.body()!=null){
                        if (response.body()!!.status==1){
                            managersList.clear()
                            mView!!.noManagersView.visibility=View.GONE
                            mView!!.rv_featured_naqashat.visibility=View.VISIBLE
                            managersList = response.body()!!.manager as ArrayList<ManagerItem>
                            setFeaturedNaqashatAdapter(managersList)
                        }else{
                            mView!!.noManagersView.visibility=View.VISIBLE
                            mView!!.rv_featured_naqashat.visibility=View.GONE
                            Utility.showSnackBarOnResponseError(mView!!.managersListFragmentConstraintLayout,
                               response.body()!!.message.toString(),
                                requireContext())
                        }
                    }else{
                        mView!!.noManagersView.visibility=View.VISIBLE
                        mView!!.rv_featured_naqashat.visibility=View.GONE
                        Utility.showSnackBarOnResponseError(mView!!.managersListFragmentConstraintLayout,
                            response.message(),
                            requireContext())
                    }
                }else{
                    mView!!.noManagersView.visibility=View.VISIBLE
                    mView!!.rv_featured_naqashat.visibility=View.GONE
                    Utility.showSnackBarOnResponseError(mView!!.managersListFragmentConstraintLayout,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext())
                }
            }

            override fun onFailure(call: Call<ManagerListingResponse?>, throwable: Throwable) {
                if (mView!!.swipeRefresh.isRefreshing) {
                    mView!!.swipeRefresh.isRefreshing = false
                }
                mView!!.managers_listing_frag_progressBar.visibility = View.GONE
                mView!!.noManagersView.visibility=View.VISIBLE
                mView!!.rv_featured_naqashat.visibility=View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(mView!!.managersListFragmentConstraintLayout,
                    requireContext().getString(R.string.response_isnt_successful),
                    requireContext())
            }

        })
    }

    private fun setFeaturedNaqashatAdapter(managersList: ArrayList<ManagerItem>) {
        mView!!.rv_featured_naqashat.layoutManager = GridLayoutManager(requireContext(), 2)
        mView!!.rv_featured_naqashat.itemAnimator = DefaultItemAnimator()
        featuredNaqashatAdapter = GridFeaturedNaqashatAdapter(requireContext(), managersList, object : ClickInterface.OnRecyclerItemClick{
            override fun OnClickAction(position: Int) {
                val manager_id = managersList[position].mangaer_id
                val bundle = Bundle()
                val managerName = if(managersList[position].name!!.isEmpty()){
                    managersList[position].username
                }else{
                    managersList[position].name
                }
                bundle.putString("managerName", managerName)
                bundle.putInt("managerId", manager_id!!)
                sharedPreferenceInstance!!.save(SharedPreferenceUtility.ManagerId, manager_id)
                findNavController().navigate(R.id.myProfileFragment, bundle)
            }
        })
        mView!!.rv_featured_naqashat.adapter = featuredNaqashatAdapter
        mView!!.rv_featured_naqashat.addItemDecoration(SpaceItemDecoration(4))
    }

    companion object {
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