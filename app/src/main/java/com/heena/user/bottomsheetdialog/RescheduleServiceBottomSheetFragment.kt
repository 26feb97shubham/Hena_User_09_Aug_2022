package com.heena.user.bottomsheetdialog

import android.os.Bundle
import android.util.Log
import android.view.*
import com.bumptech.glide.Glide
import com.heena.user.R
import com.heena.user.`interface`.ClickInterface
import com.heena.user.utils.SharedPreferenceUtility
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.heena.user.`interface`.APIInterface.Companion.createBuilder
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.models.Booking
import com.heena.user.models.Dates
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.already_booked_date
import com.heena.user.utils.Utility.already_booked_time
import com.heena.user.utils.Utility.apiInterface
import com.heena.user.utils.Utility.reference
import kotlinx.android.synthetic.main.fragment_rate_naqasha_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_reschedule_service_bottom_sheet.view.*
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.HashMap

class RescheduleServiceBottomSheetFragment : BottomSheetDialogFragment() {
    private var onRescheduleServiceCallback : ClickInterface.OnRescheduleServiceClick?=null
    private var mView : View?=null
    private var bookingDate : String?= null
    private var bookingFromTime : String?= null
    private var bookingToTime : String?= null
    private var myAppointmentDateTime : String?= null
    private var myOldAppointmentDateTime : String?= null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_reschedule_service_bottom_sheet, container, false)
        return mView
    }
    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Glide.with(requireContext()).load(booking!!.manager!!.image).into(mView!!.civ_profile_reschedule)
        val managerName = if (booking!!.manager!!.name!!.isEmpty()){
            booking!!.manager!!.username
        }else{
            booking!!.manager!!.name
        }




        mView!!.tv_naqasha_name_reschedule.text = managerName
        mView!!.tv_calendar_reschedule.text = booking!!.booking_date + " - "+ booking!!.booking_from
        myOldAppointmentDateTime = booking!!.booking_date + " - "+ booking!!.booking_from
        myAppointmentDateTime = myOldAppointmentDateTime
        if (booking!!.manager!!.avg_star.equals("")|| booking!!.manager!!.avg_star.equals(null)){
            mView!!.ratingBar_reschedule.rating = 0F
            mView!!.txtRating_reschedule.text = "0"
        }else{
            mView!!.ratingBar_reschedule.rating = booking!!.manager!!.avg_star!!.toFloat()
            mView!!.txtRating_reschedule.text = booking!!.manager!!.avg_star
        }

        mView!!.calendar_layout_reschedule.setOnClickListener {
            reference = "reschedule"
            if (reference.equals("reschedule")){
                already_booked_time = booking!!.booking_from.toString()
                already_booked_date = booking!!.booking_date.toString()
            }else{
                already_booked_time = ""
                already_booked_date = ""
            }
            val calendarFragment = BottomSheetCalendarFragment.newInstance(
                reference,
                already_booked_time,
                already_booked_date,
                dates
            )
            calendarFragment.show(requireActivity().supportFragmentManager, BottomSheetCalendarFragment.TAG)
            calendarFragment.setSubscribeClickListenerCallback(object : ClickInterface.onSaveAppointmentDateTimeCallback {
                override fun SaveAppointmentDateTimeCallback(hashMap: HashMap<String, String>) {
                    myAppointmentDateTime = hashMap.get("myappointmentDate") + " - " + hashMap.get("myappointmentFromTime")
                    bookingDate = hashMap.get("myappointmentDate")
                    bookingFromTime = hashMap.get("myappointmentFromTime")
                    bookingToTime = hashMap.get("myappointmentToTime")
                    mView!!.tv_calendar_reschedule.text = myAppointmentDateTime.toString()
                }
            })
        }
        mView!!.tv_book_service_reschedule.setOnClickListener {
            reschedule()
        }
    }

    private fun reschedule(){
        if (myAppointmentDateTime.isNullOrEmpty()){
            Utility.showSnackBarValidationError(mView!!.rescheduleServiceBottomSheetDialog,
                requireContext().getString(R.string.please_enter_a_valid_date_time),
                requireContext())
        }else if (myAppointmentDateTime.equals(myOldAppointmentDateTime)){
            Utility.showSnackBarValidationError(mView!!.rescheduleServiceBottomSheetDialog,
                requireContext().getString(R.string.please_enter_a_time_above_than_previous_time),
                requireContext())
        }
        else{
            requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            mView!!.frag_reschedule_progressBar.visibility= View.VISIBLE
            val builder = createBuilder(arrayOf("user_id", "service_id", "manager_id", "booking_id", "b_to", "b_from",
                "b_date", "lang"),
                arrayOf(sharedPreferenceInstance!!.get(SharedPreferenceUtility.UserId, 0).toString(),
                    booking!!.service_id.toString(),
                    booking!!.manager_id.toString(),
                    booking!!.booking_id.toString(),
                    bookingToTime.toString(),
                    bookingFromTime.toString(),
                    bookingDate.toString(),
                sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]))
            val call = apiInterface.booking_reschedule(builder.build())
            call.enqueue(object : Callback<ResponseBody?>{
                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: Response<ResponseBody?>
                ) {
                    mView!!.frag_reschedule_progressBar.visibility = View.GONE
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    try {
                        if (response.body()!=null){
                            val responseBody = response.body()!!.string()
                            val jsonObject = JSONObject(responseBody)
                            if (jsonObject.getInt("status")==1){
                                Log.e("response", jsonObject.toString())
                                Utility.showSnackBarOnResponseSuccess(mView!!.rescheduleServiceBottomSheetDialog,
                                    jsonObject.getString("message"),
                                    requireContext())
                                onRescheduleServiceCallback!!.OnRescheduleService()
                                dismiss()
                                /*showBookingConfirmDialog()
                                requireContext().startActivity(Intent(requireContext(), HomeActivity::class.java))*/
                            }else{
                                Utility.showSnackBarOnResponseError(mView!!.rescheduleServiceBottomSheetDialog,
                                    jsonObject.getString("message"),
                                    requireContext())
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
                    mView!!.frag_reschedule_progressBar.visibility = View.GONE
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    Utility.showSnackBarOnResponseSuccess(mView!!.rescheduleServiceBottomSheetDialog,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext())
                }
            })
        }
    }

    fun setRescheduleServiceClickCallback(onRescheduleServiceCallback : ClickInterface.OnRescheduleServiceClick){
        this.onRescheduleServiceCallback = onRescheduleServiceCallback
    }

    companion object {
        const val TAG = "RescheduleServiceBottomSheetFragment"
        private var instance: SharedPreferenceUtility? = null
        private var booking : Booking?=null
        private var dates = ArrayList<Dates>()

        fun newInstance(bundle: Bundle): RescheduleServiceBottomSheetFragment {
            //this.context = context;
            booking = bundle.getSerializable("booking") as Booking?
            dates = bundle.getStringArrayList("dates") as ArrayList<Dates>
            return RescheduleServiceBottomSheetFragment()
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