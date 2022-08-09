package com.heena.user.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.heena.user.Dialogs.NoInternetDialog
import com.heena.user.R
import com.heena.user.`interface`.ClickInterface
import com.heena.user.adapters.*
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.models.Booking
import com.heena.user.models.BookingDetailsResponse
import com.heena.user.models.BookingItem
import com.heena.user.models.BookingListingResponse
import com.heena.user.utils.LogUtils
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.apiInterface
import com.heena.user.utils.Utility.booking_item_type
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_appointments.view.*
import kotlinx.android.synthetic.main.fragment_bookingdetails.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class AppointmentsFragment : Fragment() {
    var mView : View? = null
    private var drawable1 : Drawable?= null
    private var drawable2 : Drawable?= null
    private lateinit var bookingAdapter: BookingAdapter
    private var bookingList = ArrayList<BookingItem>()
    private val arabic_animId = R.anim.layout_animation_right_to_left
    private val english_animId = R.anim.layout_animation_left_to_right
    private lateinit var layoutAnimationController: LayoutAnimationController
    private var booking_id = 0
    private var booking : Booking?=null
    var dates = java.util.ArrayList<String>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_appointments, container, false)
        return mView
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
        )

        requireActivity().tv_title.text = getString(R.string.my_bookings)

        Utility.payment_status = ""

        if (!Utility.hasConnection(requireContext())){
            val noInternetDialog = NoInternetDialog()
            noInternetDialog.isCancelable = false
            noInternetDialog.setRetryCallback(object : NoInternetDialog.RetryInterface{
                override fun retry() {
                    noInternetDialog.dismiss()
                    booking_item_type = 1
                    getBookings(booking_item_type)
                }

            })
            noInternetDialog.show(requireActivity().supportFragmentManager, "Appointments Fragment")
        }else{
            booking_item_type = 1
            getBookings(booking_item_type)
        }

        requireActivity().iv_back.setSafeOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().navigate(R.id.action_appointmentFragment_to_homeFragment)
        }

        requireActivity().iv_notification.setSafeOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F,0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }

        mView!!.rv_tabs_listing.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)


        mView!!.tv_current_bookings.setSafeOnClickListener {
            drawable1 = resources.getDrawable(R.drawable.little_gold_curved_01, null)
            drawable2 = resources.getDrawable(R.drawable.curved_white_filled_rect_box_01, null)
            mView!!.tv_current_bookings.background = drawable1
            mView!!.tv_bookings_history.background = drawable2
            mView!!.tv_current_bookings.setTextColor(Color.parseColor("#FFFFFFFF"))
            mView!!.tv_bookings_history.setTextColor(Color.parseColor("#D0B67A"))
            booking_item_type = 1
            getBookings(booking_item_type)
        }

        mView!!.tv_bookings_history.setSafeOnClickListener {
            drawable1 = resources.getDrawable(R.drawable.curved_white_filled_rect_box_01, null)
            drawable2 = resources.getDrawable(R.drawable.little_gold_curved_01, null)
            mView!!.tv_current_bookings.background = drawable1
            mView!!.tv_bookings_history.background = drawable2
            mView!!.tv_current_bookings.setTextColor(Color.parseColor("#D0B67A"))
            mView!!.tv_bookings_history.setTextColor(Color.parseColor("#FFFFFFFF"))
            booking_item_type = 2
            getBookings(booking_item_type)
        }
    }

    private fun getBookings(bookingItemType: Int) {
        mView!!.frag_appointments_progressBar.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val hashMap = HashMap<String, String>()
        hashMap["lang"] =
            sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]
        hashMap["type"] = bookingItemType.toString()
        hashMap["user_id"] =
            sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0].toString()
        val call = apiInterface.bookingListing(hashMap)
        call?.enqueue(object : Callback<BookingListingResponse?>{
            override fun onResponse(
                call: Call<BookingListingResponse?>,
                response: Response<BookingListingResponse?>
            ) {
                mView!!.frag_appointments_progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!!.status == 1){
                        bookingList.clear()
                        mView!!.tv_no_bookings_found.visibility = View.GONE
                        mView!!.nsv_bookings.visibility = View.VISIBLE
                        bookingList = response.body()!!.booking as ArrayList<BookingItem>
                        setBookingsAdapter()
                    }else{
                        mView!!.tv_no_bookings_found.visibility = View.VISIBLE
                        mView!!.nsv_bookings.visibility = View.GONE
                        Utility.showSnackBarOnResponseError(mView!!.appointmentsFragmentConstraintLayout,
                            response.body()!!.message.toString(),
                            requireContext())
                    }
                }else{
                    mView!!.tv_no_bookings_found.visibility = View.VISIBLE
                    mView!!.nsv_bookings.visibility = View.GONE
                    Utility.showSnackBarOnResponseError(mView!!.appointmentsFragmentConstraintLayout,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext())
                }
            }

            override fun onFailure(call: Call<BookingListingResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(mView!!.appointmentsFragmentConstraintLayout,
                    requireContext().getString(R.string.response_isnt_successful),
                    requireContext())
                mView!!.frag_appointments_progressBar.visibility= View.GONE
                mView!!.tv_no_bookings_found.visibility = View.VISIBLE
                mView!!.nsv_bookings.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setBookingsAdapter() {
        bookingAdapter = BookingAdapter(requireContext(), bookingList, object : ClickInterface.OnRecyclerItemClick{
            override fun OnClickAction(position: Int) {
                booking_id = bookingList[position].booking_id!!
                getBookingDetails(booking_id)
            }
        })

        layoutAnimationController =
            if (sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""] =="ar"){
                AnimationUtils.loadLayoutAnimation(requireContext(), arabic_animId)
            }else{
                AnimationUtils.loadLayoutAnimation(requireContext(), english_animId)
            }
        mView!!.rv_tabs_listing.layoutAnimation = layoutAnimationController
        mView!!.rv_tabs_listing.adapter = bookingAdapter
        bookingAdapter.notifyDataSetChanged()
        mView!!.rv_tabs_listing.scheduleLayoutAnimation()
    }

    private fun getBookingDetails(booking_id: Int) {
        mView!!.frag_appointments_progressBar.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val call = apiInterface.bookingDetails(
            booking_id,
            sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]
        )

        call?.enqueue(object : Callback<BookingDetailsResponse?>{
            override fun onResponse(
                call: Call<BookingDetailsResponse?>,
                response: Response<BookingDetailsResponse?>
            ) {
                mView!!.frag_appointments_progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        booking= response.body()!!.booking
                        val bundle = Bundle()
                        bundle.putInt("booking_id", booking_id)
                        bundle.putSerializable("booking",booking)
                        findNavController().navigate(R.id.action_appointmentFragment_to_bookingDetailsFragment, bundle)
                    }else{
                        LogUtils.shortToast(requireContext(), response.body()!!.message)
                        Utility.showSnackBarOnResponseError(mView!!.appointmentsFragmentConstraintLayout,
                            response.body()!!.message.toString(),
                            requireContext())
                    }
                }else{
                    LogUtils.shortToast(requireContext(), getString(R.string.response_isnt_successful))
                    Utility.showSnackBarOnResponseError(mView!!.appointmentsFragmentConstraintLayout,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext())
                }
            }

            override fun onFailure(call: Call<BookingDetailsResponse?>, throwable: Throwable) {
                LogUtils.shortToast(requireContext(), throwable.message)
                mView!!.frag_appointments_progressBar.visibility= View.GONE
                Utility.showSnackBarOnResponseError(mView!!.appointmentsFragmentConstraintLayout,
                    requireContext().getString(R.string.response_isnt_successful),
                    requireContext())
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }

    override fun onResume() {
        super.onResume()
        if (!Utility.hasConnection(requireContext())){
            val noInternetDialog = NoInternetDialog()
            noInternetDialog.isCancelable = false
            noInternetDialog.setRetryCallback(object : NoInternetDialog.RetryInterface{
                override fun retry() {
                    noInternetDialog.dismiss()
                    booking_item_type = 1
                    getBookings(booking_item_type)
                }

            })
            noInternetDialog.show(requireActivity().supportFragmentManager, "Appointments Fragment")
        }else{
            booking_item_type = 1
            getBookings(booking_item_type)
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
    }
}