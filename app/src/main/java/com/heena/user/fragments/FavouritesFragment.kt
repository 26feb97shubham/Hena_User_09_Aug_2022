package com.heena.user.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.heena.user.Dialogs.NoInternetDialog
import com.heena.user.R
import com.heena.user.`interface`.APIInterface.Companion.createBuilder
import com.heena.user.`interface`.ClickInterface
import com.heena.user.adapters.FavouritesAdapter
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.bottomsheetdialog.BookServiceBottomSheetFragment
import com.heena.user.models.*
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.apiInterface
import com.heena.user.utils.Utility.favourite_item_type
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_appointments.view.rv_tabs_listing
import kotlinx.android.synthetic.main.fragment_book_service_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_favourites.view.*
import kotlinx.android.synthetic.main.fragment_favourites.view.tv_services
import kotlinx.android.synthetic.main.grid_services_recycler_items.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class FavouritesFragment : Fragment() {

    var mView : View? = null
    private var layout_resource : Int = 0
    private var drawable1 : Drawable?= null
    private var drawable2 : Drawable?= null

    private var favouritesAdapter: FavouritesAdapter?=null

    private var servicesList = ArrayList<ServiceItemY>()
    private var managerList = ArrayList<Service>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_favourites, container, false)
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
        )
        layout_resource = R.layout.item_fav_naqashat

        requireActivity().tv_title.text = getString(R.string.favorites)

        if (!Utility.hasConnection(requireContext())){
            val noInternetDialog = NoInternetDialog()
            noInternetDialog.isCancelable = false
            noInternetDialog.setRetryCallback(object : NoInternetDialog.RetryInterface{
                override fun retry() {
                    noInternetDialog.dismiss()
                    favourite_item_type = 1
                    getFavManagers()
                }
            })
            noInternetDialog.show(requireActivity().supportFragmentManager, "Favorites Fragment")
        }

        requireActivity().iv_back.setSafeOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().navigate(R.id.action_favouritesFragment_to_homeFragment)
        }

        requireActivity().iv_notification.setSafeOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F,0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }
        return mView
    }

    private fun getFavManagers() {
        mView!!.fragment_fav_progressBar.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val call = apiInterface.getFavManager(
            sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0],
            sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]
        )

        call?.enqueue(object : Callback<FavManagerListingResponse?>{
            override fun onResponse(call: Call<FavManagerListingResponse?>, response: Response<FavManagerListingResponse?>) {
                mView!!.fragment_fav_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        managerList.clear()
                        mView!!.tv_no_fav_found.visibility = View.GONE
                        mView!!.nsv_fav.visibility = View.VISIBLE
                        managerList = response.body()!!.service as ArrayList<Service>
                        favourite_item_type = 1
                        setFavAdapter(null, managerList)
                    }else{
                        mView!!.tv_no_fav_found.visibility = View.VISIBLE
                        mView!!.nsv_fav.visibility = View.GONE
                    }
                }else{
                    Utility.showSnackBarOnResponseError(mView!!.favoritesFragmentConstraintLayout,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext())

                    mView!!.tv_no_fav_found.visibility = View.VISIBLE
                    mView!!.nsv_fav.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<FavManagerListingResponse?>, t: Throwable) {
                mView!!.fragment_fav_progressBar.visibility = View.GONE
                Utility.showSnackBarOnResponseError(mView!!.favoritesFragmentConstraintLayout,
                    getString(R.string.response_isnt_successful),
                    requireContext())

                mView!!.tv_no_fav_found.visibility = View.VISIBLE
                mView!!.nsv_fav.visibility = View.GONE
            }

        })
    }

    private fun getFavServices() {
        mView!!.fragment_fav_progressBar.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val call = apiInterface.getFavServices(
            sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0],
            sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]
        )

        call?.enqueue(object : Callback<FavoriteServicesListingResponse?>{
            override fun onResponse(call: Call<FavoriteServicesListingResponse?>, response: Response<FavoriteServicesListingResponse?>) {
                mView!!.fragment_fav_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        servicesList.clear()
                        mView!!.tv_no_fav_found.visibility = View.GONE
                        mView!!.nsv_fav.visibility = View.VISIBLE
                        servicesList = response.body()!!.service as ArrayList<ServiceItemY>
                        favourite_item_type = 2
                        setFavAdapter(servicesList, null)
                    }else{
                        mView!!.tv_no_fav_found.visibility = View.VISIBLE
                        mView!!.nsv_fav.visibility = View.GONE
                    }
                }else{
                    Utility.showSnackBarOnResponseError(mView!!.favoritesFragmentConstraintLayout,
                        getString(R.string.response_isnt_successful),
                        requireContext())
                    mView!!.tv_no_fav_found.visibility = View.VISIBLE
                    mView!!.nsv_fav.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<FavoriteServicesListingResponse?>, t: Throwable) {
                mView!!.fragment_fav_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                Utility.showSnackBarOnResponseError(mView!!.favoritesFragmentConstraintLayout,
                    getString(R.string.response_isnt_successful),
                    requireContext())
                mView!!.tv_no_fav_found.visibility = View.VISIBLE
                mView!!.nsv_fav.visibility = View.GONE
            }

        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setFavAdapter(servicesList: ArrayList<ServiceItemY>?, managerList: ArrayList<Service>?) {
        mView!!.rv_tabs_listing.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        if (servicesList!=null){
            favouritesAdapter = FavouritesAdapter(requireContext(),null, servicesList, object : ClickInterface.onFavItemClick{
                override fun delFav(position: Int) {
                    val builder = createBuilder(arrayOf("lang", "user_id","service_id"),
                            arrayOf(
                                sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""],
                                    sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0].toString(),
                                    servicesList[position].service_id.toString()))
                    val call = apiInterface.delServiceFav(builder.build())
                    call?.enqueue(object : Callback<MyResponse?> {
                        override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                            if (response.isSuccessful){
                                if (response.body()!!.status==1){
                                    servicesList.removeAt(position)
                                    if(servicesList!!.size==0){
                                        mView!!.nsv_fav.visibility = View.GONE
                                        mView!!.tv_no_fav_found.visibility = View.VISIBLE
                                    }else{
                                        mView!!.nsv_fav.visibility = View.VISIBLE
                                        mView!!.tv_no_fav_found.visibility = View.GONE
                                    }
                                    if (mView!!.rv_tabs_listing.adapter!=null){
                                        mView!!.rv_tabs_listing.adapter!!.notifyDataSetChanged()
                                    }
                                    Utility.showSnackBarOnResponseSuccess(mView!!.favoritesFragmentConstraintLayout,
                                        response.body()!!.message.toString(),
                                        requireContext())
                                }
                            }else{
                                Utility.showSnackBarOnResponseError(mView!!.favoritesFragmentConstraintLayout,
                                    requireContext().getString(R.string.response_isnt_successful),
                                    requireContext())
                            }
                        }

                        override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                            Utility.showSnackBarOnResponseError(mView!!.favoritesFragmentConstraintLayout,
                                requireContext().getString(R.string.response_isnt_successful),
                                requireContext())
                            Log.e("error", t.message.toString())
                        }

                    })
                }

                override fun openFav(position: Int) {
                    val bundle = Bundle()
                    val serviceItem = servicesList[position] as ServiceItemY
                    bundle.putSerializable("service", serviceItem)
                    /*val bookServiceBottomSheetFragment = BookServiceBottomSheetFragment.newInstance(
                        bundle,
                        position
                    )
                    bookServiceBottomSheetFragment.show(
                        requireActivity().supportFragmentManager,
                        BookServiceBottomSheetFragment.TAG
                    )
                    bookServiceBottomSheetFragment.setSubscribeClickListenerCallback(object :
                        ClickInterface.OnBookServiceClick {
                        override fun OnBookService() {
                            val bundle = Bundle()
                            bundle.putSerializable("service", serviceItem)
                            findNavController().navigate(
                                R.id.action_homeFragment_to_servicvePaymentFragment,
                                bundle
                            )
                        }

                        override fun onLikeAction(position: Int, likeStatus: Boolean, itemView: View) {
                            //likeService(itemView, position, favouritesAdapter)
                        }

                        override fun onDislikeAction(position: Int, unLikeStatus: Boolean, itemView: View) {
                            disLikeService(itemView, position, favouritesAdapter, bookServiceBottomSheetFragment, servicesList)
                        }

                    })*/
                }

            })
            mView!!.rv_tabs_listing.adapter = favouritesAdapter
            favouritesAdapter!!.notifyDataSetChanged()
        }else if(managerList!=null){
            favouritesAdapter = FavouritesAdapter(requireContext(), managerList, null, object : ClickInterface.onFavItemClick{
                override fun delFav(position: Int) {
                    val builder = createBuilder(arrayOf("lang", "user_id","manager_id"),
                            arrayOf(
                                sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""],
                                    sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0].toString(),
                                    managerList[position].mangaer_id.toString()))
                    val call = apiInterface.delManagerFav(builder.build())
                    call?.enqueue(object : Callback<MyResponse?> {
                        override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                            if (response.isSuccessful){
                                if (response.body()!!.status==1){
                                    managerList.removeAt(position)
                                    if (mView!!.rv_tabs_listing.adapter!=null){
                                        mView!!.rv_tabs_listing.adapter!!.notifyDataSetChanged()
                                    }
                                    if(managerList!!.size==0){
                                        mView!!.nsv_fav.visibility = View.GONE
                                        mView!!.tv_no_fav_found.visibility = View.VISIBLE
                                    }else{
                                        mView!!.nsv_fav.visibility = View.VISIBLE
                                        mView!!.tv_no_fav_found.visibility = View.GONE
                                    }
                                    Utility.showSnackBarOnResponseSuccess(mView!!.favoritesFragmentConstraintLayout,
                                        response.body()!!.message.toString(),
                                        requireContext())
                                }
                            }else{
                                Utility.showSnackBarOnResponseError(mView!!.favoritesFragmentConstraintLayout,
                                    requireContext().getString(R.string.response_isnt_successful),
                                    requireContext())
                            }
                        }

                        override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                            Utility.showSnackBarOnResponseError(mView!!.favoritesFragmentConstraintLayout,
                                requireContext().getString(R.string.response_isnt_successful),
                                requireContext())
                            Log.e("error", t.message.toString())
                        }

                    })
                }

                override fun openFav(position: Int) {
                    val bundle = Bundle()
                    val managerName = if (managerList[position].name!!.isEmpty()){
                        managerList[position].username
                    }else{
                        managerList[position].name
                    }
                    bundle.putString("managerName", managerName)
                    bundle.putInt("managerId", managerList[position].mangaer_id!!)
                    findNavController().navigate(R.id.myProfileFragment, bundle)
                }

            })
            mView!!.rv_tabs_listing.adapter = favouritesAdapter
        }
        favouritesAdapter!!.notifyDataSetChanged()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mView!!.tv_naqashat.setSafeOnClickListener {
            layout_resource = R.layout.current_booking_recycler_items_listing
            drawable1 = resources.getDrawable(R.drawable.little_gold_curved_01, null)
            drawable2 = resources.getDrawable(R.drawable.curved_white_filled_rect_box_01, null)
            mView!!.tv_naqashat.background = drawable1
            mView!!.tv_services.background = drawable2
            mView!!.tv_naqashat.setTextColor(Color.parseColor("#FFFFFFFF"))
            mView!!.tv_services.setTextColor(Color.parseColor("#D0B67A"))
            getFavManagers()
        }

        mView!!.tv_services.setSafeOnClickListener {
            layout_resource = R.layout.default_listing_recycler_item
            drawable1 = resources.getDrawable(R.drawable.curved_white_filled_rect_box_01, null)
            drawable2 = resources.getDrawable(R.drawable.little_gold_curved_01, null)
            mView!!.tv_naqashat.background = drawable1
            mView!!.tv_services.background = drawable2
            mView!!.tv_naqashat.setTextColor(Color.parseColor("#D0B67A"))
            mView!!.tv_services.setTextColor(Color.parseColor("#FFFFFFFF"))
            getFavServices()
        }


    }

    private fun disLikeService(
        itemView: View, position: Int,
        favouritesAdapter1: FavouritesAdapter?,
        bookServiceBottomSheetFragment1: BookServiceBottomSheetFragment,
        servicesList1: ArrayList<ServiceItemY>
    ){
        if (sharedPreferenceInstance!!.get(SharedPreferenceUtility.IsGuestUserLogin, false)) {
            Utility.loginSignUpAccessAlertDialogBox(requireContext())
        } else {
            val serviceItem = this.servicesList[position]
            val builder = createBuilder(
                arrayOf("lang", "user_id", "service_id"),
                arrayOf(
                    sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""],
                    sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0].toString(),
                    serviceItem.service_id.toString()
                )
            )
            val call = apiInterface.delServiceFav(builder.build())
            call?.enqueue(object : Callback<MyResponse?> {
                override fun onResponse(
                    call: Call<MyResponse?>,
                    response: Response<MyResponse?>
                ) {
                    if (response.isSuccessful) {
                        if (response.body()!!.status == 1) {
                            servicesList1.removeAt(position)
                            if(servicesList1!!.size==0){
                                mView!!.nsv_fav.visibility = View.GONE
                                mView!!.tv_no_fav_found.visibility = View.VISIBLE
                            }else{
                                mView!!.nsv_fav.visibility = View.VISIBLE
                                mView!!.tv_no_fav_found.visibility = View.GONE
                            }
                            if(itemView.iv_add_to_fav == null && itemView.iv_fav_added == null){
                                itemView.iv_add_to_fav_bottomSheet_book_service.visibility = View.VISIBLE
                                itemView.iv_fav_added_bottomSheet_book_service.visibility = View.GONE
                            }
                            if(itemView.bookServiceBottomSheetFragment!=null){
                                Utility.showSnackBarOnResponseSuccess(
                                    itemView.bookServiceBottomSheetFragment,
                                    response.body()!!.message.toString(),
                                    requireContext()
                                )
                            }else{
                                Utility.showSnackBarOnResponseSuccess(
                                    mView!!.favoritesFragmentConstraintLayout,
                                    response.body()!!.message.toString(),
                                    requireContext()
                                )
                            }
                            serviceItem.is_favorite = 0
//                            favouritesAdapter1!!.notifyItemChanged(position)
                            favouritesAdapter1!!.notifyDataSetChanged()
                            bookServiceBottomSheetFragment1.dismiss()
                        }
                    } else {
                        if(itemView.bookServiceBottomSheetFragment!=null){
                            Utility.showSnackBarOnResponseError(
                                itemView.bookServiceBottomSheetFragment,
                                response.body()!!.message.toString(),
                                requireContext()
                            )
                        }else{
                            Utility.showSnackBarOnResponseError(
                                mView!!.favoritesFragmentConstraintLayout,
                                response.body()!!.message.toString(),
                                requireContext()
                            )
                        }
                    }
                }

                override fun onFailure(
                    call: Call<MyResponse?>,
                    t: Throwable
                ) {
                    if(itemView.bookServiceBottomSheetFragment!=null){
                        Utility.showSnackBarOnResponseError(
                            itemView.bookServiceBottomSheetFragment,
                            t.message.toString(),
                            requireContext()
                        )
                    }else{
                        Utility.showSnackBarOnResponseError(
                            mView!!.favoritesFragmentConstraintLayout,
                            t.message.toString(),
                            requireContext()
                        )
                    }
                }

            })
        }
    }

    override fun onResume() {
        super.onResume()
        getFavManagers()
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