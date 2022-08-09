package com.heena.user.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.view.animation.AlphaAnimation
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.heena.user.R
import com.heena.user.`interface`.APIInterface.Companion.createBuilder
import com.heena.user.`interface`.ClickInterface
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.bottomsheetdialog.*
import com.heena.user.models.*
import com.heena.user.utils.LogUtils
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.apiInterface
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.custom_toast_layout.*
import kotlinx.android.synthetic.main.fragment_bookingdetails.view.*
import kotlinx.android.synthetic.main.fragment_bookingdetails.view.ratingBar
import kotlinx.android.synthetic.main.fragment_bookingdetails.view.txtRating
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class BookingdetailsFragment : Fragment() {
    var mView : View?= null
    private var booking_id : Int?=null
    private var booking_status : Int ?= null
    private var booking : Booking?=null
    private var priorSeventyHourscancellationDateTimeMilliseconds:Long?=null
    private var seventyHourscancellationDateTimeMilliseconds:Long?=null
    private var bookingDateTimeString: String?=null
    var dates = ArrayList<String>()
    private var managerId = 0
    private var managerProfile : Profile?=null
    private var currentCalendar : Calendar?=null
    private var currentDateString : String ?=null
    private var currentDate : Date ?=null
    private var bookingDateCalendar : Calendar?=null
    private var cancelAndRescheduleBookingStatus = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            booking_id = it.getInt("booking_id")
            booking = it.getSerializable("booking") as Booking?
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_bookingdetails, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
        )

        requireActivity().tv_title.text = ""

        requireActivity().iv_back.setSafeOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().navigate(R.id.action_bookingDetailsFragment_to_homeFragment)
        }

        requireActivity().iv_notification.setSafeOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F,0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }


        setBookingDetails(booking)

        mView!!.tv_cancel_booking.setSafeOnClickListener {
            if (cancelAndRescheduleBookingStatus){
                val cancelBookingDialog = AlertDialog.Builder(requireContext())
                cancelBookingDialog.setCancelable(false)
                cancelBookingDialog.setTitle(requireContext().getString(R.string.cancel_booking))
                cancelBookingDialog.setMessage(requireContext().getString(R.string.are_you_sure_you_want_to_cancel_booking))
                cancelBookingDialog.setPositiveButton(requireContext().getString(R.string.yes)
                ) { dialog, _ ->
                    cancelBooking()
                    dialog!!.dismiss()
                }
                cancelBookingDialog.setNegativeButton(requireContext().getString(R.string.no)
                ) { dialog, _ -> dialog!!.cancel() }
                cancelBookingDialog.show()
            }else{
                Utility.showSnackBarValidationError(mView!!.bookingDetailsFragmentConstraintLayout,
                    requireContext().getString(R.string.booking_reschedule_policy_doesnot_match),
                    requireContext())
            }
        }

        mView!!.tv_reschedule_booking.setSafeOnClickListener {
            if(cancelAndRescheduleBookingStatus){
                val rescheduleBookingDialog = AlertDialog.Builder(requireContext())
                rescheduleBookingDialog.setCancelable(false)
                rescheduleBookingDialog.setTitle(requireContext().getString(R.string.reschedule_booking))
                rescheduleBookingDialog.setMessage(requireContext().getString(R.string.are_you_sure_you_want_to_reschedule_booking))
                rescheduleBookingDialog.setPositiveButton(requireContext().getString(R.string.yes)
                ) { dialog, _ ->
                    rescheduleBooking()
                    dialog!!.dismiss()
                }
                rescheduleBookingDialog.setNegativeButton(requireContext().getString(R.string.no)
                ) { dialog, _ -> dialog!!.cancel() }
                rescheduleBookingDialog.show()
            }else{
                Utility.showSnackBarValidationError(mView!!.bookingDetailsFragmentConstraintLayout,
                    requireContext().getString(R.string.booking_reschedule_policy_doesnot_match),
                    requireContext())
            }
        }

        mView!!.tv_rate_naqasha.setSafeOnClickListener {
            val bundle = Bundle()
            bundle.putSerializable("manager_profile", managerProfile)
            val rateNaqashaBottomSheetFragment = RateNaqashaBottomSheetFragment.newInstance(bundle)
            rateNaqashaBottomSheetFragment.show(requireActivity().supportFragmentManager, RateNaqashaBottomSheetFragment.TAG)
            rateNaqashaBottomSheetFragment.ReviewSubmitCallback(object : ClickInterface.onReviewSubmit{
                override fun reviewSubmit(toString: String) {
                    Utility.showSnackBarOnResponseSuccess(mView!!.bookingDetailsFragmentConstraintLayout,
                        toString,
                        requireContext())
                }
            })
        }
    }

    private fun setBookingDetails(booking: Booking?) {
        managerId = booking!!.manager_id!!
        dates.clear()
        dates = booking!!.dates as ArrayList<String>
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US)
        bookingDateTimeString = booking.booking_date+" "+booking.booking_from
        Log.e("bookingDateTimeString", ""+bookingDateTimeString)
        bookingDateCalendar = Calendar.getInstance()
        val date = simpleDateFormat.parse(bookingDateTimeString)
        bookingDateCalendar!!.time = date
        Log.e("bookingTimeMS", ""+bookingDateCalendar!!.timeInMillis)
        Log.e("bookingTime", ""+bookingDateCalendar!!.time)
        currentCalendar = Calendar.getInstance()
        currentDateString = simpleDateFormat.format(currentCalendar!!.time)
        currentDate = simpleDateFormat.parse(currentDateString)
        currentCalendar!!.time = currentDate
        Log.e("currentTimeMS", ""+currentCalendar!!.timeInMillis)
        Log.e("currentTime", ""+currentCalendar!!.time )

        if (booking.cancel_msg.isNullOrEmpty()){
            mView!!.view6.visibility = View.GONE
            mView!!.tv_rejected_reason_txt.visibility = View.GONE
            mView!!.tv_rejected_reason.visibility = View.GONE
        }else{
            mView!!.view6.visibility = View.VISIBLE
            mView!!.tv_rejected_reason_txt.visibility = View.VISIBLE
            mView!!.tv_rejected_reason.visibility = View.VISIBLE
            mView!!.tv_rejected_reason_txt.text = booking.cancel_msg
        }

        priorSeventyHourscancellationDateTimeMilliseconds = bookingDateCalendar!!.timeInMillis-259200000
        seventyHourscancellationDateTimeMilliseconds = currentCalendar!!.timeInMillis + 259200000

        cancelAndRescheduleBookingStatus = priorSeventyHourscancellationDateTimeMilliseconds!!>currentCalendar!!.timeInMillis

        if (booking.gallery!!.isEmpty()){
            mView!!.iv_heena_design.setImageResource(R.drawable.def_henna)
        }else{
            Glide.with(requireContext()).load(booking.gallery[0]).into(mView!!.iv_heena_design)
        }
        mView!!.tv_service.text = booking.service!!.name
        Glide.with(requireContext()).load(booking.manager!!.image).into(mView!!.supplierImg)
        if(booking.manager.avg_star.isNullOrEmpty()){
            mView!!.ratingBar.rating = 0F
            mView!!.txtRating.text = "0"
        }else{
            mView!!.ratingBar.rating = booking.manager.avg_star!!.toFloat()
            mView!!.txtRating.text = booking.manager.avg_star
        }
        mView!!.tv_service_desc.text = booking.service.description
        mView!!.tv_ladies_count.text = booking.c_ladies.toString()
        mView!!.tv_childrens_count.text = booking.c_children.toString()
        if(booking.address==null){
            mView!!.tv_address.text = booking.location!!.name
        }else{
            val street = booking.address.street
            val country = booking.address.country
            mView!!.tv_address.text = street+ " ," + country
        }
        mView!!.tv_special_request_desc.text = booking.message

        mView!!.tv_service_charge.text = "AED "+ Utility.convertDoubleValueWithCommaSeparator(booking!!.service!!.price!!.total!!.toDouble())
        val subtotalPrice = booking.price!!.total!!.toDouble() + booking.service.price!!.total!!.toDouble()
        mView!!.tv_sub_total.text = "AED "+Utility.convertDoubleValueWithCommaSeparator(subtotalPrice)
        val totalPrice = subtotalPrice + booking!!.service!!.price!!.total!!.toDouble()
        mView!!.tv_total.text = "AED "+Utility.convertDoubleValueWithCommaSeparator(totalPrice.toDouble())
        val bookingDateTime = booking.booking_date + " - " + booking.booking_from + " to " + booking.booking_to
        mView!!.tv_booking_date_time.text = bookingDateTime

        if (booking.card!=null){
            mView!!.mtvCardname.text = booking.card.card!!.brand
            mView!!.mtvCardlastFour.text = requireContext().getString(R.string.ending_in)+ " "+booking!!.card.card!!.lastFour
        }

        booking_status = booking.status
        when(booking_status){
            1 -> {
                mView!!.tv_status.apply {
                    this.text = getString(R.string.approved)
                    this.isAllCaps = true
                    this.setTextColor(Color.parseColor("#37CC37"))
                }
                mView!!.ll_cancel_reschedule.visibility = View.GONE
                mView!!.ll_rate_chat_with_admin.visibility = View.GONE
                mView!!.tv_cancelLed_booking.visibility = View.GONE
            }
            2 -> {
                mView!!.tv_status.apply {
                    this.text = getString(R.string.cancelled)
                    this.isAllCaps = true
                    this.setTextColor(Color.parseColor("#FF0909"))
                }
                mView!!.ll_cancel_reschedule.visibility = View.GONE
                mView!!.ll_rate_chat_with_admin.visibility = View.GONE
                mView!!.tv_cancelLed_booking.visibility = View.VISIBLE
            }
            3 -> {
                mView!!.tv_status.apply {
                    this.text = getString(R.string.completed)
                    this.isAllCaps = true
                    this.setTextColor(Color.parseColor("#37CC37"))
                }
                mView!!.ll_cancel_reschedule.visibility = View.GONE
                mView!!.ll_rate_chat_with_admin.visibility = View.VISIBLE
                mView!!.tv_cancelLed_booking.visibility = View.GONE
                getManagerDetails()
            }
            4 -> {
                mView!!.tv_status.apply {
                    this.text = getString(R.string.pending)
                    this.isAllCaps = true
                    this.setTextColor(Color.parseColor("#FF9F54"))
                }
                mView!!.ll_cancel_reschedule.visibility = View.VISIBLE
                mView!!.ll_rate_chat_with_admin.visibility = View.GONE
                mView!!.tv_cancelLed_booking.visibility = View.GONE
            }
        }
    }

    private fun rescheduleBooking() {
        val bundle = Bundle()
        bundle.putSerializable("booking",
            booking
        )
        bundle.putStringArrayList("dates", dates)
        val rescheduleServiceBottomSheetFragment = RescheduleServiceBottomSheetFragment.newInstance(bundle)
        rescheduleServiceBottomSheetFragment.show(requireActivity().supportFragmentManager, AvailablenaqashaBottomSheetFragment.TAG)
        rescheduleServiceBottomSheetFragment.setRescheduleServiceClickCallback(object : ClickInterface.OnRescheduleServiceClick{
            override fun OnRescheduleService() {
                showBookingConfirmDialog()
                booking!!.status = 4
                setBookingDetails(booking)
                rescheduleServiceBottomSheetFragment.dismiss()
            }
        })

       /* val bundle = Bundle()
        bundle.putSerializable("booking", booking)
        val cancelledServiceBottomSheetDialogFragment = CancelledServiceBottomSheetDialogFragment.newInstance(bundle)
        cancelledServiceBottomSheetDialogFragment.show(requireActivity().supportFragmentManager, CancelledServiceBottomSheetDialogFragment.TAG)
        cancelledServiceBottomSheetDialogFragment.setChatWithAdminClick(object : ClickInterface.OnChatWithAdminClick{
            override fun OnChatWithAdmin() {
                findNavController().navigate(R.id.action_bookingDetailsFragment_to_chatWithAdminFragment)
            }

        })

        cancelledServiceBottomSheetDialogFragment.setRescheduleClick(object : ClickInterface.OnRescheduleServiceClick{
            override fun OnRescheduleService() {
                val bundle = Bundle()
                bundle.putSerializable("booking",
                    booking
                )
                bundle.putStringArrayList("dates", dates)
                val rescheduleServiceBottomSheetFragment = RescheduleServiceBottomSheetFragment.newInstance(bundle)
                rescheduleServiceBottomSheetFragment.show(requireActivity().supportFragmentManager, AvailablenaqashaBottomSheetFragment.TAG)
                rescheduleServiceBottomSheetFragment.setRescheduleServiceClickCallback(object : ClickInterface.OnRescheduleServiceClick{
                    override fun OnRescheduleService() {
                        showBookingConfirmDialog()
                        booking!!.status = 4
                        setBookingDetails(booking)
                        rescheduleServiceBottomSheetFragment.dismiss()
                        cancelledServiceBottomSheetDialogFragment.dismiss()
                    }
                })
            }

        })*/
    }

    private fun cancelBooking() {
        val bundle = Bundle()
        bundle.putInt("booking_id", booking_id!!)
        val cancelServiceBottomSheetFragment = CancelServiceBottomSheetFragment.newInstance(bundle)
        cancelServiceBottomSheetFragment.show(requireActivity().supportFragmentManager, CancelServiceBottomSheetFragment.TAG)
        cancelServiceBottomSheetFragment.setCancelServiceClick(object : ClickInterface.OnCancelServiceClick{
            override fun OnCancelService(booking_id: Int?, rsn_for_cancellation: String) {
                mView!!.frag_booking_details_progressBar.visibility = View.VISIBLE
                requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                val builder = createBuilder(arrayOf("booking_id","message", "lang"),
                    arrayOf(booking_id.toString(), rsn_for_cancellation.toString(), sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]))
                val call = apiInterface.bookingCancel(builder.build())
                call?.enqueue(object : Callback<MyResponse?>{
                    override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                        if (response.isSuccessful){
                            mView!!.frag_booking_details_progressBar.visibility= View.GONE
                            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                            if (response.body()!!.status==1){
                                booking!!.status = 2
                                setBookingDetails(booking)
                                LogUtils.shortToast(requireContext(), response.body()!!.message)
                                Utility.showSnackBarOnResponseSuccess(mView!!.bookingDetailsFragmentConstraintLayout,
                                    response.body()!!.message.toString(),
                                    requireContext())
                            }else{
                                Utility.showSnackBarOnResponseError(mView!!.bookingDetailsFragmentConstraintLayout,
                                    response.body()!!.message.toString(),
                                    requireContext())
                            }
                        }else{
                            Utility.showSnackBarOnResponseError(mView!!.bookingDetailsFragmentConstraintLayout,
                                requireContext().getString(R.string.response_isnt_successful),
                                requireContext())
                        }
                    }

                    override fun onFailure(call: Call<MyResponse?>, throwable: Throwable) {
                        mView!!.frag_booking_details_progressBar.visibility= View.GONE
                        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                        Utility.showSnackBarOnResponseError(mView!!.bookingDetailsFragmentConstraintLayout,
                            requireContext().getString(R.string.response_isnt_successful),
                            requireContext())
                    }

                })
            }

        })
    }


    private fun showBookingConfirmDialog() {
        val customToastLayout = layoutInflater.inflate(R.layout.custom_toast_layout,llCustomToastContainer)
        val customToast = Toast(requireContext())
        customToast.view = customToastLayout
        customToast.setGravity(Gravity.CENTER,0,0)
        customToast.duration = Toast.LENGTH_LONG
        customToast.show()
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
                    }
                }else{
                    Utility.showSnackBarOnResponseError(mView!!.bookingDetailsFragmentConstraintLayout,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext())
                }
            }

            override fun onFailure(call: Call<ManagerDetailsResponse?>, t: Throwable) {
                Utility.showSnackBarOnResponseError(mView!!.bookingDetailsFragmentConstraintLayout,
                    requireContext().getString(R.string.response_isnt_successful),
                    requireContext())
            }

        })
    }


   /* private fun getBookingDetails() {
        mView!!.frag_booking_details_progressBar.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val call = apiInterface.bookingDetails(booking_id!!,
            sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]
        )

        call?.enqueue(object : Callback<BookingDetailsResponse?>{
            override fun onResponse(
                call: Call<BookingDetailsResponse?>,
                response: Response<BookingDetailsResponse?>
            ) {
                mView!!.frag_booking_details_progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        booking= response.body()!!.booking
                        dates.clear()
                        dates = booking!!.dates as ArrayList<String>
                        if (booking!!.gallery!!.isEmpty()){
                            mView!!.iv_heena_design.setImageResource(R.drawable.def_henna)
                        }else{
                            Glide.with(requireContext()).load(booking!!.gallery!![0]).into(mView!!.iv_heena_design)
                        }
                        mView!!.tv_service.text = booking!!.service!!.name
                        Glide.with(requireContext()).load(booking!!.manager!!.image).into(mView!!.supplierImg)
                        if(booking!!.manager!!.avg_star.equals("")||booking!!.manager!!.avg_star.equals(null)){
                            mView!!.ratingBar.rating = 0F
                            mView!!.txtRating.text = "0"
                        }else{
                            mView!!.ratingBar.rating = booking!!.manager!!.avg_star!!.toFloat()
                            mView!!.txtRating.text = booking!!.manager!!.avg_star
                        }
                        mView!!.tv_service_desc.text = booking!!.service!!.description
                        mView!!.tv_ladies_count.text = booking!!.c_ladies.toString()
                        mView!!.tv_childrens_count.text = booking!!.c_children.toString()
                        if(booking!!.address==null){
                            mView!!.tv_address.text = booking!!.location!!.name
                        }else{
                            val street = booking!!.address!!.street
                            val country = booking!!.address!!.country
                            mView!!.tv_address.text = street+ " ," + country
                        }
                        mView!!.tv_special_request_desc.text = booking!!.message
                        mView!!.tv_service_charge.text = "AED "+ Utility.convertDoubleValueWithCommaSeparator(booking!!.service!!.price!!.total!!.toDouble())
                        val subtotalPrice = booking!!.price!!.total!!.toDouble() - booking!!.service!!.price!!.total!!.toDouble()
                        mView!!.tv_sub_total.text = "AED "+Utility.convertDoubleValueWithCommaSeparator(subtotalPrice)
                        mView!!.tv_total.text = "AED "+Utility.convertDoubleValueWithCommaSeparator(booking!!.price!!.total!!.toDouble())
                        val bookingDateTime = booking!!.booking_date + " - " + booking!!.booking_from + " to " + booking!!.booking_to
                        mView!!.tv_booking_date_time.text = bookingDateTime

                        if (booking!!.card!=null){
                            mView!!.mtvCardname.text = booking!!.card.card!!.brand
                            mView!!.mtvCardlastFour.text = requireContext().getString(R.string.ending_in)+ " "+booking!!.card.card!!.lastFour
                        }

                        booking_status = booking!!.status
                        when(booking_status){
                            1 -> {
                                mView!!.tv_status.apply {
                                    this.text = getString(R.string.approved)
                                    this.isAllCaps = true
                                    this.setTextColor(Color.parseColor("#37CC37"))
                                }
                                mView!!.ll_cancel_reschedule.visibility = View.GONE
                                mView!!.ll_rate_chat_with_admin.visibility = View.GONE
                                mView!!.tv_cancelLed_booking.visibility = View.GONE
                            }
                            2 -> {
                                mView!!.tv_status.apply {
                                    this.text = getString(R.string.cancelled)
                                    this.isAllCaps = true
                                    this.setTextColor(Color.parseColor("#FF0909"))
                                }
                                mView!!.ll_cancel_reschedule.visibility = View.GONE
                                mView!!.ll_rate_chat_with_admin.visibility = View.GONE
                                mView!!.tv_cancelLed_booking.visibility = View.VISIBLE
                            }
                            3 -> {
                                mView!!.tv_status.apply {
                                    this.text = getString(R.string.completed)
                                    this.isAllCaps = true
                                    this.setTextColor(Color.parseColor("#37CC37"))
                                }
                                mView!!.ll_cancel_reschedule.visibility = View.GONE
                                mView!!.ll_rate_chat_with_admin.visibility = View.VISIBLE
                                mView!!.tv_cancelLed_booking.visibility = View.GONE
                            }
                            4 -> {
                                mView!!.tv_status.apply {
                                    this.text = getString(R.string.pending)
                                    this.isAllCaps = true
                                    this.setTextColor(Color.parseColor("#FF9F54"))
                                }
                                mView!!.ll_cancel_reschedule.visibility = View.VISIBLE
                                mView!!.ll_rate_chat_with_admin.visibility = View.GONE
                                mView!!.tv_cancelLed_booking.visibility = View.GONE
                            }
                        }
                    }else{
                        LogUtils.shortToast(requireContext(), response.body()!!.message)
                    }
                }else{
                    LogUtils.shortToast(requireContext(), getString(R.string.response_isnt_successful))
                }
            }

            override fun onFailure(call: Call<BookingDetailsResponse?>, throwable: Throwable) {
                LogUtils.shortToast(requireContext(), throwable.message)
                mView!!.frag_booking_details_progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }*/

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