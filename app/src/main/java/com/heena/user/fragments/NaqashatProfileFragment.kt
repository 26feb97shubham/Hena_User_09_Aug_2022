package com.heena.user.fragments

import android.content.Intent
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
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.heena.user.R
import com.heena.user.`interface`.APIInterface.Companion.createBuilder
import com.heena.user.`interface`.ClickInterface
import com.heena.user.activities.ZoomableImageActivity
import com.heena.user.adapters.*
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.bottomsheetdialog.BookServiceBottomSheetFragment
import com.heena.user.bottomsheetdialog.RateNaqashaBottomSheetFragment
import com.heena.user.extras.SpaceItemDecoration
import com.heena.user.models.*
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.apiInterface
import com.heena.user.utils.Utility.loginSignUpAccessAlertDialogBox
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_book_service_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.fragment_naqashat_profile.view.*
import kotlinx.android.synthetic.main.fragment_naqashat_profile.view.tv_services
import kotlinx.android.synthetic.main.fragment_service_payment.view.*
import kotlinx.android.synthetic.main.grid_services_recycler_items.view.*
import kotlinx.android.synthetic.main.naqashat_offers_recycler_item.view.*
import kotlinx.android.synthetic.main.naqashat_services_recycler_item.view.*
import kotlinx.android.synthetic.main.naqashat_services_recycler_item.view.iv_add_to_fav
import kotlinx.android.synthetic.main.naqashat_services_recycler_item.view.iv_fav_added
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class NaqashatProfileFragment() : Fragment() {
    var mView : View ?=null
    var managerId = 0
    private var offerList = ArrayList<OfferItem>()
    private var serviceList = ArrayList<ServiceItem>()
    private var galleryList = ArrayList<GalleryItem>()
    private var stringGalleryList = ArrayList<String>()
    private var commentsList = ArrayList<CommentsItem>()
    lateinit var galleryStaggeredGridAdapter: GalleryStaggeredGridAdapter
    lateinit var offersAndDiscountsAdapter: OffersAndDiscountsAdapter
    lateinit var reviewsAdapter: ReviewsAdapter
    var servicesAdapter: ServicesAdapter?=null
    private var managerName = ""
    var managerProfile : Profile?=null
    private var manager : Manager?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            managerName = it.getString("managerName").toString()
            managerId = it.getInt("managerId")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_naqashat_profile, container, false)
        setUpViews()
        return mView
    }

    private fun setUpViews() {
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
        )

        Log.e("manager_id", managerId.toString())
        requireActivity().iv_back.setSafeOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }
        requireActivity().iv_notification.setSafeOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F,0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }
        requireActivity().tv_title.text = ""

        mView!!.rv_naqashat_gallery.setHasFixedSize(true)

        getManagerDetails()
        getManagerProfile()

        mView!!.tv_services.setSafeOnClickListener {
            mView!!.naqashat_services_layout.visibility = View.VISIBLE
            mView!!.naqashat_gallery_layout.visibility = View.GONE
            mView!!.naqashat_reviews_layout.visibility = View.GONE
            getManagerProfile()
            mView!!.tv_services.setBackgroundResource(R.drawable.little_gold_curved)
            mView!!.tv_gallery.setBackgroundResource(R.drawable.little_curved_white_filled_gold_border_rect_box)
            mView!!.tv_reviews.setBackgroundResource(R.drawable.little_curved_white_filled_gold_border_rect_box)
            mView!!.tv_services.setTextColor(resources.getColor(R.color.white))
            mView!!.tv_gallery.setTextColor(resources.getColor(R.color.gold))
            mView!!.tv_reviews.setTextColor(resources.getColor(R.color.gold))
        }

        mView!!.tv_gallery.setSafeOnClickListener {
            mView!!.naqashat_services_layout.visibility = View.GONE
            mView!!.naqashat_gallery_layout.visibility = View.VISIBLE
            mView!!.naqashat_reviews_layout.visibility = View.GONE
            getManagerGallery()
            mView!!.tv_services.setBackgroundResource(R.drawable.little_curved_white_filled_gold_border_rect_box)
            mView!!.tv_gallery.setBackgroundResource(R.drawable.little_gold_curved)
            mView!!.tv_reviews.setBackgroundResource(R.drawable.little_curved_white_filled_gold_border_rect_box)
            mView!!.tv_services.setTextColor(resources.getColor(R.color.gold))
            mView!!.tv_gallery.setTextColor(resources.getColor(R.color.white))
            mView!!.tv_reviews.setTextColor(resources.getColor(R.color.gold))
        }

        mView!!.tv_reviews.setSafeOnClickListener {
            mView!!.naqashat_services_layout.visibility = View.GONE
            mView!!.naqashat_gallery_layout.visibility = View.GONE
            mView!!.naqashat_reviews_layout.visibility = View.VISIBLE
            getCommentsList()
            mView!!.tv_services.setBackgroundResource(R.drawable.little_curved_white_filled_gold_border_rect_box)
            mView!!.tv_gallery.setBackgroundResource(R.drawable.little_curved_white_filled_gold_border_rect_box)
            mView!!.tv_reviews.setBackgroundResource(R.drawable.little_gold_curved)
            mView!!.tv_services.setTextColor(resources.getColor(R.color.gold))
            mView!!.tv_gallery.setTextColor(resources.getColor(R.color.gold))
            mView!!.tv_reviews.setTextColor(resources.getColor(R.color.white))
        }

        mView!!.tv_write_a_review.setSafeOnClickListener {
            if (sharedPreferenceInstance!!.get(SharedPreferenceUtility.IsGuestUserLogin)) {
                loginSignUpAccessAlertDialogBox(requireContext())
            }else{
                val bundle = Bundle()
            bundle.putSerializable("manager_profile", managerProfile)
            val rateNaqashaBottomSheetFragment = RateNaqashaBottomSheetFragment.newInstance(bundle)
            rateNaqashaBottomSheetFragment.show(requireActivity().supportFragmentManager, RateNaqashaBottomSheetFragment.TAG)
            rateNaqashaBottomSheetFragment.ReviewSubmitCallback(object : ClickInterface.onReviewSubmit{
                override fun reviewSubmit(toString: String) {
                    getCommentsList()
                }
            })
            }
        }

        mView!!.cl_manager_fav_unfav.setSafeOnClickListener {
            if (sharedPreferenceInstance!!.get(SharedPreferenceUtility.IsGuestUserLogin, false)) {
                loginSignUpAccessAlertDialogBox(requireContext())
            }else{
                val builder = createBuilder(arrayOf("lang", "user_id","manager_id"),
                    arrayOf(sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, ""),
                        sharedPreferenceInstance!!.get(SharedPreferenceUtility.UserId, 0).toString(),
                        managerId.toString()))
                val call = apiInterface.addManagerFav(builder.build())

                call?.enqueue(object : Callback<MyResponse?>{
                    override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                        if (response.isSuccessful){
                            if (response.body()!!.status==1){
                                mView!!.cl_manager_fav_unfav.visibility = View.GONE
                                mView!!.cl_manager_fav_unfav_one.visibility = View.VISIBLE
                                Utility.showSnackBarOnResponseSuccess(mView!!.naqashatProfileFragmentConstraintLayout,
                                    response.body()!!.message.toString(),
                                    requireContext())

                            }
                        }else{
                            Utility.showSnackBarOnResponseError(mView!!.naqashatProfileFragmentConstraintLayout,
                                requireContext().getString(R.string.response_isnt_successful),
                                requireContext())

                        }
                    }

                    override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                        Utility.showSnackBarOnResponseError(mView!!.naqashatProfileFragmentConstraintLayout,
                            requireContext().getString(R.string.response_isnt_successful),
                            requireContext())
                    }

                })
            }

        }

        mView!!.cl_manager_fav_unfav_one.setSafeOnClickListener {
            if (sharedPreferenceInstance!!.get(SharedPreferenceUtility.IsGuestUserLogin, false)) {
                loginSignUpAccessAlertDialogBox(requireContext())
            }else{
                val builder = createBuilder(arrayOf("lang", "user_id","manager_id"),
                    arrayOf(sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, ""),
                        sharedPreferenceInstance!!.get(SharedPreferenceUtility.UserId, 0).toString(),
                        managerId.toString()))
                val call = apiInterface.delManagerFav(builder.build())

                call?.enqueue(object : Callback<MyResponse?>{
                    override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                        if (response.isSuccessful){
                            if (response.body()!!.status==1){
                                mView!!.cl_manager_fav_unfav.visibility = View.VISIBLE
                                mView!!.cl_manager_fav_unfav_one.visibility = View.GONE
                                Utility.showSnackBarOnResponseSuccess(mView!!.naqashatProfileFragmentConstraintLayout,
                                    response.body()!!.message.toString(),
                                    requireContext())
                            }
                        }else{
                            Utility.showSnackBarOnResponseError(mView!!.naqashatProfileFragmentConstraintLayout,
                                requireContext().getString(R.string.response_isnt_successful),
                                requireContext())
                        }
                    }

                    override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                        Utility.showSnackBarOnResponseError(mView!!.naqashatProfileFragmentConstraintLayout,
                            requireContext().getString(R.string.response_isnt_successful),
                            requireContext())
                    }

                })
            }

        }
    }

    private fun getManagerDetails() {
        val call = apiInterface.getManagerDetails(managerId,
                sharedPreferenceInstance!!.get(SharedPreferenceUtility.UserId, 0),
        sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""])
        call?.enqueue(object : Callback<ManagerDetailsResponse?>{
            override fun onResponse(call: Call<ManagerDetailsResponse?>, response: Response<ManagerDetailsResponse?>) {
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        managerProfile = response.body()!!.profile
                        if (response.body()!!.profile!!.image.toString().isEmpty()){
                            Glide.with(requireContext()).load(R.drawable.user).placeholder(R.drawable.user).into(mView!!.civ_profile)
                        }else{
                            Glide.with(requireContext()).load(response.body()!!.profile!!.image).placeholder(R.drawable.user).into(mView!!.civ_profile)
                        }

                        mView!!.tv_naqashat_name_location.text = managerName
                        if (response.body()!!.profile!!.name.equals("")){
                            mView!!.tv_naqashat_name_location.text = response.body()!!.profile!!.username
                        }else{
                            mView!!.tv_naqashat_name_location.text = response.body()!!.profile!!.name
                        }
                        if (response.body()!!.profile!!.comment_avg.equals("")||response.body()!!.profile!!.comment_avg.equals(null)){
                            mView!!.ratingBar_naqashat.rating = 0F
                            mView!!.txtRating_naqashat.text = "0"
                        }else{
                            mView!!.ratingBar_naqashat.rating = response.body()!!.profile!!.comment_avg!!.toFloat()
                            mView!!.txtRating_naqashat.text = String.format(Locale.ENGLISH,"%.1f", response.body()!!.profile!!.comment_avg!!.toDouble())
                        }
                        if (response.body()!!.profile!!.is_favorite.equals("0")){
                            mView!!.cl_manager_fav_unfav_one.visibility = View.GONE
                            mView!!.cl_manager_fav_unfav.visibility = View.VISIBLE
                        }else{
                            mView!!.cl_manager_fav_unfav_one.visibility = View.VISIBLE
                            mView!!.cl_manager_fav_unfav.visibility = View.GONE
                        }
                    }
                }else{
                    Utility.showSnackBarOnResponseError(mView!!.naqashatProfileFragmentConstraintLayout,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext())
                }
            }

            override fun onFailure(call: Call<ManagerDetailsResponse?>, t: Throwable) {
                Utility.showSnackBarOnResponseError(mView!!.naqashatProfileFragmentConstraintLayout,
                    requireContext().getString(R.string.response_isnt_successful),
                    requireContext())
            }

        })
    }

    private fun getCommentsList() {
        val call = apiInterface.commentsListing(managerId,
                sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, ""))

        call?.enqueue(object : Callback<ReviewsListingResponse?>{
            override fun onResponse(call: Call<ReviewsListingResponse?>, response: Response<ReviewsListingResponse?>) {
                if (response.isSuccessful){
                    if(response.body()!!.status==1){
                        commentsList.clear()
                        commentsList = response.body()!!.comments as ArrayList<CommentsItem>
                        mView!!.rv_reviews.visibility = View.VISIBLE
                        mView!!.ll_no_comments_found.visibility = View.GONE
                        setCommentsAdapter()
                    }else{
                        mView!!.rv_reviews.visibility = View.GONE
                        mView!!.ll_no_comments_found.visibility = View.VISIBLE
                    }
                }else{
                    mView!!.rv_reviews.visibility = View.GONE
                    mView!!.ll_no_comments_found.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<ReviewsListingResponse?>, t: Throwable) {
                mView!!.rv_reviews.visibility = View.GONE
                mView!!.ll_no_comments_found.visibility = View.VISIBLE
            }

        })
    }

    private fun setCommentsAdapter() {
        mView!!.rv_reviews.layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                false
        )
        reviewsAdapter = ReviewsAdapter(
                requireContext(),
                commentsList)
        mView!!.rv_reviews.adapter = reviewsAdapter
        reviewsAdapter.notifyDataSetChanged()
    }

    private fun getManagerGallery() {
        val call = apiInterface.getGallery(managerId,
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, ""))
        call!!.enqueue(object : Callback<GalleryResponse?>{
            override fun onResponse(
                call: Call<GalleryResponse?>,
                response: Response<GalleryResponse?>
            ) {
                if (response.isSuccessful){
                    if (response.body()!=null){
                        if (response.body()!!.status==1){
                            galleryList.clear()
                            stringGalleryList.clear()
                            galleryList = response.body()!!.gallery as ArrayList<GalleryItem>
                            for (i in 0 until galleryList.size){
                                stringGalleryList.add(galleryList[i].image!!)
                            }
                            mView!!.rv_naqashat_gallery.visibility = View.VISIBLE
                            mView!!.ll_no_gallery_found.visibility = View.GONE
                            setGalleryAdapter()
                        }else{
                            mView!!.rv_naqashat_gallery.visibility = View.GONE
                            mView!!.ll_no_gallery_found.visibility = View.VISIBLE
                        }
                    }else{
                        mView!!.rv_naqashat_gallery.visibility = View.GONE
                        mView!!.ll_no_gallery_found.visibility = View.VISIBLE
                    }
                }else{
                    mView!!.rv_naqashat_gallery.visibility = View.GONE
                    mView!!.ll_no_gallery_found.visibility = View.VISIBLE
                }
            }
            override fun onFailure(call: Call<GalleryResponse?>, t: Throwable) {
                mView!!.rv_naqashat_gallery.visibility = View.GONE
                mView!!.ll_no_gallery_found.visibility = View.VISIBLE
            }

        })
    }

    private fun setGalleryAdapter() {
        mView!!.rv_naqashat_gallery.layoutManager = StaggeredGridLayoutManager(3,StaggeredGridLayoutManager.VERTICAL)
        galleryStaggeredGridAdapter = GalleryStaggeredGridAdapter(
            requireContext(),
            galleryList,
            object : ClickInterface.OnRecyclerItemClick {
                override fun OnClickAction(position: Int) {
                    val bundle = Bundle()
                    bundle.putStringArrayList("gallery",
                        stringGalleryList
                    )
                    startActivity(Intent(requireContext(), ZoomableImageActivity::class.java).putExtras(bundle))
                }
            }
        )
        mView!!.rv_naqashat_gallery.adapter = galleryStaggeredGridAdapter
        mView!!.rv_naqashat_gallery.addItemDecoration(SpaceItemDecoration(10))
    }

    private fun getManagerProfile() {
        val query = HashMap<String, Any>()
        query.put("lang", sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang,""))
        query.put("user_id", sharedPreferenceInstance!!.get(SharedPreferenceUtility.UserId,0))
        val call = apiInterface.getManagerProfile(managerId,
        query)

        call!!.enqueue(object : Callback<ManagerProfileResponse?>{
            override fun onResponse(
                call: Call<ManagerProfileResponse?>,
                response: Response<ManagerProfileResponse?>
            ) {
                if (response.isSuccessful){
                    if (response.body()!=null){
                        if (response.body()!!.status==1){
                            offerList.clear()
                            serviceList.clear()
                            offerList = response.body()!!.offer as ArrayList<OfferItem>
                            if (offerList.size==0){
                                mView!!.rv_offers_n_discs.visibility = View.GONE
                                mView!!.ll_no_offers_and_disc_found.visibility = View.VISIBLE
                            }else{
                                mView!!.rv_offers_n_discs.visibility = View.VISIBLE
                                mView!!.ll_no_offers_and_disc_found.visibility = View.GONE
                            }
                            mView!!.rv_offers_n_discs.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

                            offersAndDiscountsAdapter = OffersAndDiscountsAdapter(requireContext(),offerList, object : ClickInterface.onOfferItemClick{
                                override fun OnClickAction(position: Int) {
//                                    findNavController().navigate(R.id.action_myProfileFragment_to_offersAndDiscountsFragment)
                                    showServiceDetails(offerList[position].service_id)
                                }

                                override fun onLikeAction(position: Int, itemView: View) {
                                    if (sharedPreferenceInstance!!.get(SharedPreferenceUtility.IsGuestUserLogin)) {
                                        loginSignUpAccessAlertDialogBox(requireContext())
                                    }else{
                                        val offer = offerList[position]
                                        val builder = createBuilder(
                                            arrayOf("lang", "user_id", "service_id"),
                                            arrayOf(
                                                sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""],
                                                sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0]
                                                    .toString(),
                                                offer.service_id.toString()
                                            )
                                        )
                                        val call = apiInterface.addServiceFav(builder.build())
                                        call?.enqueue(object : Callback<MyResponse?> {
                                            override fun onResponse(
                                                call: Call<MyResponse?>,
                                                response: Response<MyResponse?>
                                            ) {
                                                if (response.isSuccessful) {
                                                    if (response.body()!!.status == 1) {
                                                        itemView.iv_add_to_fav_offer.visibility = View.GONE
                                                        itemView.iv_fav_added_offer.visibility = View.VISIBLE
                                                        Utility.showSnackBarOnResponseSuccess(mView!!.naqashatProfileFragmentConstraintLayout,
                                                            response.body()!!.message.toString(),
                                                            requireContext())
                                                        offer.is_favorite=1
                                                        offersAndDiscountsAdapter.notifyDataSetChanged()
                                                    }
                                                } else {
                                                    Utility.showSnackBarOnResponseError(mView!!.naqashatProfileFragmentConstraintLayout,
                                                        requireContext().getString(R.string.response_isnt_successful),
                                                        requireContext())
                                                }
                                            }

                                            override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                                                Utility.showSnackBarOnResponseError(mView!!.naqashatProfileFragmentConstraintLayout,
                                                    requireContext().getString(R.string.response_isnt_successful),
                                                    requireContext())
                                            }

                                        })
                                    }

                                }

                                override fun onDislikeAction(position: Int, itemView: View) {
                                    if (sharedPreferenceInstance!!.get(SharedPreferenceUtility.IsGuestUserLogin)) {
                                        loginSignUpAccessAlertDialogBox(requireContext())
                                    }else{
                                        val offer = offerList[position]
                                        val builder = createBuilder(
                                            arrayOf("lang", "user_id", "service_id"),
                                            arrayOf(
                                                sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""],
                                                sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0]
                                                    .toString(),
                                                offer.service_id.toString()
                                            )
                                        )
                                        val call = Utility.apiInterface.delServiceFav(builder.build())
                                        call?.enqueue(object : Callback<MyResponse?> {
                                            override fun onResponse(
                                                call: Call<MyResponse?>,
                                                response: Response<MyResponse?>
                                            ) {
                                                if (response.isSuccessful) {
                                                    if (response.body()!!.status == 1) {
                                                        itemView.iv_add_to_fav_offer.visibility = View.VISIBLE
                                                        itemView.iv_fav_added_offer.visibility = View.GONE
                                                        val message = response.body()!!.message
                                                        if (message != null) {
                                                            Utility.showSnackBarOnResponseSuccess(mView!!.naqashatProfileFragmentConstraintLayout,
                                                                message,
                                                                requireContext())
                                                        }
                                                        offer.is_favorite=0
                                                        offersAndDiscountsAdapter.notifyDataSetChanged()
                                                    }
                                                } else {
                                                    Utility.showSnackBarOnResponseError(mView!!.naqashatProfileFragmentConstraintLayout,
                                                        requireContext().getString(R.string.response_isnt_successful),
                                                        requireContext())
                                                }
                                            }

                                            override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                                                Utility.showSnackBarOnResponseError(mView!!.naqashatProfileFragmentConstraintLayout,
                                                    requireContext().getString(R.string.response_isnt_successful),
                                                    requireContext())
                                            }
                                        })
                                    }

                                }

                            })
                            mView!!.rv_offers_n_discs.adapter = offersAndDiscountsAdapter
                            offersAndDiscountsAdapter.notifyDataSetChanged()

                            serviceList = response.body()!!.service as ArrayList<ServiceItem>

                            if (serviceList.size==0){
                                mView!!.rv_services_listing.visibility = View.GONE
                                mView!!.ll_no_service_found.visibility = View.VISIBLE
                            }else{
                                mView!!.rv_services_listing.visibility = View.VISIBLE
                                mView!!.ll_no_service_found.visibility = View.GONE
                            }

                            mView!!.rv_services_listing.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

                            servicesAdapter = ServicesAdapter(requireContext(), serviceList, object : ClickInterface.OnServicesItemClick{
                                override fun OnClickAction(position: Int) {
                                    val serviceItem = serviceList[position]
                                    val bundle = Bundle()
                                    bundle.putSerializable("service", serviceItem)
                                    val  bookServiceBottomSheetFragment = BookServiceBottomSheetFragment.newInstance(
                                        bundle,
                                        position
                                    )
                                    bookServiceBottomSheetFragment.show(requireActivity().supportFragmentManager, BookServiceBottomSheetFragment.TAG)
                                    bookServiceBottomSheetFragment.setSubscribeClickListenerCallback(object : ClickInterface.OnBookServiceClick{
                                        override fun OnBookService() {
                                            val bundle = Bundle()
                                            bundle.putSerializable("service", serviceItem)
                                            findNavController().navigate(R.id.action_myProfileFragment_to_servicvePaymentFragment, bundle)
                                        }

                                        override fun onLikeAction(position: Int, likeStatus: Boolean, itemView: View) {
                                            likeService(itemView, position, servicesAdapter)
                                        }

                                        override fun onDislikeAction(position: Int, unLikeStatus: Boolean, itemView: View) {
                                            disLikeService(itemView, position, servicesAdapter)
                                        }

                                    })
                                }

                                override fun onLikeAction(position: Int, itemView: View) {
                                    likeService(itemView, position, servicesAdapter)
                                }

                                override fun onDislikeAction(position: Int, itemView: View) {
                                    disLikeService(itemView, position, servicesAdapter)
                                }

                                override fun onProfileClick(position: Int) {
                                }

                            })

                            mView!!.rv_services_listing.adapter = servicesAdapter
                            servicesAdapter!!.notifyDataSetChanged()
                        }else{
                            mView!!.rv_offers_n_discs.visibility = View.GONE
                            mView!!.ll_no_offers_and_disc_found.visibility = View.VISIBLE
                            mView!!.rv_services_listing.visibility = View.GONE
                            mView!!.ll_no_service_found.visibility = View.VISIBLE
                            Log.e("message 1", response.body()!!.message.toString())
                        }
                    }else{
                        mView!!.rv_offers_n_discs.visibility = View.GONE
                        mView!!.ll_no_offers_and_disc_found.visibility = View.VISIBLE
                        mView!!.rv_services_listing.visibility = View.GONE
                        mView!!.ll_no_service_found.visibility = View.VISIBLE
                        Log.e("message 2", response.message().toString())
                    }
                }else{
                    mView!!.rv_offers_n_discs.visibility = View.GONE
                    mView!!.ll_no_offers_and_disc_found.visibility = View.VISIBLE
                    mView!!.rv_services_listing.visibility = View.GONE
                    mView!!.ll_no_service_found.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<ManagerProfileResponse?>, t: Throwable) {
                mView!!.rv_offers_n_discs.visibility = View.GONE
                mView!!.ll_no_offers_and_disc_found.visibility = View.VISIBLE
                mView!!.rv_services_listing.visibility = View.GONE
                mView!!.ll_no_service_found.visibility = View.VISIBLE
            }
        })

    }

    private fun likeService(itemView: View, position:Int,
                            servicesAdapter1: ServicesAdapter?) {
        if (sharedPreferenceInstance!!.get(SharedPreferenceUtility.IsGuestUserLogin, false)) {
            Utility.loginSignUpAccessAlertDialogBox(requireContext())
        } else {
            val serviceItem = serviceList[position]
            val builder = createBuilder(
                arrayOf("lang", "user_id", "service_id"),
                arrayOf(
                    sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""],
                    sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0].toString(),
                    serviceItem.service_id.toString()
                )
            )
            val call = apiInterface.addServiceFav(builder.build())
            call?.enqueue(object : Callback<MyResponse?> {
                override fun onResponse(
                    call: Call<MyResponse?>,
                    response: Response<MyResponse?>
                ) {
                    if (response.isSuccessful) {
                        if (response.body()!!.status == 1) {
                            if(itemView.iv_add_to_fav == null && itemView.iv_fav_added == null){
                                itemView.iv_add_to_fav_bottomSheet_book_service.visibility = View.GONE
                                itemView.iv_fav_added_bottomSheet_book_service.visibility = View.VISIBLE
                            }
                            serviceItem.is_favorite = 1
                            if(itemView.bookServiceBottomSheetFragment!=null){
                                Utility.showSnackBarOnResponseSuccess(
                                    itemView.bookServiceBottomSheetFragment,
                                    response.body()!!.message.toString(),
                                    requireContext()
                                )
                            }else{
                                Utility.showSnackBarOnResponseSuccess(
                                    mView!!.naqashatProfileFragmentConstraintLayout,
                                    response.body()!!.message.toString(),
                                    requireContext()
                                )
                            }
                            servicesAdapter1!!.notifyDataSetChanged()
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
                                mView!!.naqashatProfileFragmentConstraintLayout,
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
                            mView!!.naqashatProfileFragmentConstraintLayout,
                            t.message.toString(),
                            requireContext()
                        )
                    }
                }

            })
        }
    }

    private fun disLikeService(itemView: View, position:Int,
                               servicesAdapter1: ServicesAdapter?){
        if (sharedPreferenceInstance!!.get(SharedPreferenceUtility.IsGuestUserLogin, false)) {
            Utility.loginSignUpAccessAlertDialogBox(requireContext())
        } else {
            val serviceItem = serviceList[position]
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
                                    mView!!.naqashatProfileFragmentConstraintLayout,
                                    response.body()!!.message.toString(),
                                    requireContext()
                                )
                            }


                            serviceItem.is_favorite = 0
                            servicesAdapter1!!.notifyDataSetChanged()

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
                                mView!!.naqashatProfileFragmentConstraintLayout,
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
                            mView!!.naqashatProfileFragmentConstraintLayout,
                            t.message.toString(),
                            requireContext()
                        )
                    }
                }

            })
        }
    }


    private fun showServiceDetails(serviceId: Int?) {
        mView!!.mainNaqashatProfileFragmentProgressBar.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val call = apiInterface.showService(serviceId!!, sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""])
        call?.enqueue(object : Callback<ServiceShowResponse?> {
            override fun onResponse(
                call: Call<ServiceShowResponse?>,
                response: Response<ServiceShowResponse?>
            ) {
                mView!!.mainNaqashatProfileFragmentProgressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful) {
                    if (response.body()!!.status == 1) {
                        manager = response.body()!!.manager
                    } else {
                        Utility.showSnackBarOnResponseError(mView!!.paymentServiceFragmentConstraintLayout,
                            response.body()!!.message.toString(),
                            requireContext())
                    }
                } else {
                    Utility.showSnackBarOnResponseError(mView!!.paymentServiceFragmentConstraintLayout,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext())
                }
            }

            override fun onFailure(call: Call<ServiceShowResponse?>, throwable: Throwable) {
                Utility.showSnackBarOnResponseError(mView!!.paymentServiceFragmentConstraintLayout,
                    requireContext().getString(R.string.response_isnt_successful),
                    requireContext())
                mView!!.mainNaqashatProfileFragmentProgressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        })
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