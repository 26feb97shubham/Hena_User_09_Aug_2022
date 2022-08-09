package com.heena.user.bottomsheetdialog

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.heena.user.*
import com.heena.user.`interface`.ClickInterface
import com.heena.user.adapters.SlotsAdapter
import com.heena.user.models.BookingSlots
import com.heena.user.models.Dates
import com.heena.user.models.Slots
import com.heena.user.utils.Utility
import kotlinx.android.synthetic.main.fragment_add_address_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_bottom_sheet_calendar.view.*
import kotlinx.android.synthetic.main.layout_slots.view.*
import java.lang.Exception
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class BottomSheetCalendarFragment : BottomSheetDialogFragment()  {
    private var mView : View?=null

    private var myappointmentDate  = ""
    private var myappointmentFromTime  = ""
    private var myappointmentToTime  = ""

    @RequiresApi(Build.VERSION_CODES.N)
    private var c: Calendar = Calendar.getInstance()
    @RequiresApi(Build.VERSION_CODES.N)
    private var bookedCalendar: Calendar = Calendar.getInstance()

    @RequiresApi(Build.VERSION_CODES.N)
    val minCalendar = Calendar.getInstance()

    private val myTimeFormat = "kk:mm:ss"
    private val myTimeFormat1 = "kk:mm"
    private val myDateFormat = "yyyy-MM-dd"

    private val slotsList = ArrayList<Slots>()
    private val mySlotsList = ArrayList<Slots>()
    private var myBookingSlotsList = ArrayList<BookingSlots>()


    private var onSaveAppointmentDateTimeCallback : ClickInterface.onSaveAppointmentDateTimeCallback?=null

    var calendars: ArrayList<Calendar> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_bottom_sheet_calendar, container, false)
        return mView
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val simpleDateFormat = SimpleDateFormat(myDateFormat)

/*        calendars.clear()
        mySlotsList.clear()
        slotsList.clear()
        for (i in 0 until dates.size){
            try {
                val date: Date = simpleDateFormat.parse(dates[i].bookedDate)
                val calendar = Calendar.getInstance()
                calendar.time = date
                calendars.add(calendar)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }

        for (i in 9 until 21){
            val slots = Slots()
            val j = i+1
            slots.fromTime = ""+i + ":00"
            slots.ToTime = ""+j + ":00"
            slots.fromTimeToTime = slots.fromTime+"-"+slots.ToTime
            slotsList.add(slots)
        }*/



        if (my_reference.equals("booking")){
            if (my_already_booked_date.isNullOrEmpty()){
                val myHighlightedDates : ArrayList<Calendar> = arrayListOf()
                for (i in 0 until dates.size){
                    val date: Date = simpleDateFormat.parse(dates[i].bookedDate)
                    val myBookedCalendar = Calendar.getInstance()
                    myBookedCalendar.time = date
                    if (calendars.contains(myBookedCalendar)){
                        calendars.remove(myBookedCalendar)
                        myHighlightedDates.add(myBookedCalendar)
                    }
                }
                mView!!.book_service_calendar.setDate(minCalendar)
                mView!!.book_service_calendar.setMinimumDate(minCalendar)
                mView!!.book_service_calendar.setHighlightedDays(myHighlightedDates)
            }else{
                val sdf = SimpleDateFormat(myDateFormat, Locale.US)
                var date = Date()
                try {
                    date = sdf.parse(my_already_booked_date)
                }catch (e: Exception){

                }
                val calendar : Calendar = Calendar.getInstance()
                calendar.time = date
                mView!!.book_service_calendar.setDate(calendar)
                mView!!.book_service_calendar.setMinimumDate(calendar)
            }


            mView!!.book_service_calendar.setOnDayClickListener {
                val clickedDayCalendar = it.calendar
                val selectedDates: ArrayList<Calendar> = arrayListOf()
                selectedDates.add(clickedDayCalendar)
                if (selectedDates.size==0){
                    Log.e("myappdate", ""+"alrasdasdsad")
                }else{
                    val sdf = SimpleDateFormat(myDateFormat, Locale.US)
                    myappointmentDate = sdf.format(clickedDayCalendar.time).toString()
                    Log.e("myappdate", ""+myappointmentDate)
                }


            }
        }else{
            val date: Date = simpleDateFormat.parse(my_already_booked_date)
            val myBookedCalendar = Calendar.getInstance()
            myBookedCalendar.time = date
            val myHighlightedDates : ArrayList<Calendar> = arrayListOf()
            if (calendars.contains(myBookedCalendar)){
                calendars.remove(myBookedCalendar)
                myHighlightedDates.add(myBookedCalendar)
            }

            mView!!.book_service_calendar.setDate(minCalendar)
            mView!!.book_service_calendar.setMinimumDate(minCalendar)
            mView!!.book_service_calendar.setOnDayClickListener {
                if(my_already_booked_date.equals("")){
                    val clickedDayCalendar = it.calendar
                    val sdf = SimpleDateFormat(myDateFormat, Locale.US)
                    myappointmentDate = sdf.format(clickedDayCalendar.time)
                }else{
                    bookedCalendar = it.calendar
                    val sdf = SimpleDateFormat(myDateFormat, Locale.US)
                    myappointmentDate = sdf.format(bookedCalendar.time)
                }
                Log.e("myappdate", ""+myappointmentDate)
            }
        }

    /*    mView!!.book_service_calendar.setOnDateChangeListener { _, year, month, dayOfMonth ->
            Locale.setDefault(Locale.ENGLISH)
            if(my_already_booked_date.equals("")){
                c.set(Calendar.YEAR, year)
                c.set(Calendar.MONTH, month)
                c.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val sdf = SimpleDateFormat(myDateFormat, Locale.US)
                myappointmentDate = sdf.format(c.time)
            }else{
                bookedCalendar.set(Calendar.YEAR, year)
                bookedCalendar.set(Calendar.MONTH, month)
                bookedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val sdf = SimpleDateFormat(myDateFormat, Locale.US)
                myappointmentDate = sdf.format(bookedCalendar.time)
            }
            Log.e("myappdate", ""+myappointmentDate)
        }*/

       mView!!.ll_from_time.setOnClickListener {
            Locale.setDefault(Locale.ENGLISH)

            if(myappointmentDate.equals("")||myappointmentDate==null){
                Utility.showSnackBarValidationError(mView!!.calendarBottomSheet,
                    requireContext().getString(R.string.please_select_the_date_first),
                    requireContext())
            }else{
                if (my_already_booked_date.equals(myappointmentDate)){
                    Locale.setDefault(Locale.ENGLISH)
                    val nowCal = Calendar.getInstance()
                    val hour = nowCal.get(Calendar.HOUR_OF_DAY)
                    val minute = nowCal.get(Calendar.MINUTE)

                    val tpd = TimePickerDialog(requireContext(), { _, h, m ->
                        val nowCal1 = Calendar.getInstance()
                        nowCal1.set(Calendar.HOUR_OF_DAY, h)
                        nowCal1.set(Calendar.MINUTE, m)
                        val sdf = SimpleDateFormat(myDateFormat, Locale.US)
                        val currentDate = sdf.format(nowCal1.time)
                        val sdf1 = SimpleDateFormat(myDateFormat+" "+myTimeFormat, Locale.US)
                        var oldDate = Date()
                        if(my_already_booked_time.equals("")){
                            oldDate = sdf1.parse(currentDate)
                        }else{
                            oldDate = sdf1.parse(currentDate+" "+my_already_booked_time)
                        }
                        //val oldDate = sdf1.parse(currentDate+" "+ my_already_booked_time)
                        val nowCal2 = Calendar.getInstance()
                        nowCal2.time = oldDate
                        Log.e("oldTime", nowCal2.timeInMillis.toString())
                        Log.e("newTime", nowCal1.timeInMillis.toString())
                        myappointmentFromTime = ""
                        if (nowCal2.timeInMillis<=nowCal1.timeInMillis){
                            val sdf = SimpleDateFormat(myTimeFormat, Locale.US)
                            myappointmentFromTime = sdf.format(nowCal1.time)
                            mView!!.tv_from_time.text = myappointmentFromTime
                        }else{
                            Utility.showSnackBarValidationError(mView!!.calendarBottomSheet,
                                requireContext().getString(R.string.time_is_before_current_time),
                                requireContext())
                        }


                    },hour,minute,false)
                    tpd.show()

                }else{
                    Locale.setDefault(Locale.ENGLISH)
                    val hour = c.get(Calendar.HOUR_OF_DAY)
                    val minute = c.get(Calendar.MINUTE)
                    val sdf = SimpleDateFormat(myDateFormat, Locale.US)
                    val currentDate = sdf.format(c.time)
                    if(myappointmentDate.equals(currentDate)){
                        val tpd = TimePickerDialog(requireContext(), { _, h, m ->
                            c.set(Calendar.HOUR_OF_DAY,h)
                            c.set(Calendar.MINUTE,m)
                            val myCalendar = Calendar.getInstance()
                            val sdf = SimpleDateFormat(myTimeFormat, Locale.US)
                            if (myCalendar.timeInMillis<=c.timeInMillis){
                                val sdf = SimpleDateFormat(myTimeFormat, Locale.US)
                                myappointmentFromTime = sdf.format(c.time)
                                mView!!.tv_from_time.text = myappointmentFromTime
                            }else{
                                Utility.showSnackBarValidationError(mView!!.calendarBottomSheet,
                                    requireContext().getString(R.string.time_is_before_current_time),
                                    requireContext())
                            }
                            /*myappointmentFromTime = sdf.format(c.time)
                            mView!!.tv_from_time.text = myappointmentFromTime*/
                        },hour,minute,false)
                        tpd.show()
                    }else{
                        val tpd = TimePickerDialog(requireContext(), { _, h, m ->
                            c.set(Calendar.HOUR_OF_DAY,h)
                            c.set(Calendar.MINUTE,m)
/*                            val myCalendar = Calendar.getInstance()
                            if (myCalendar.timeInMillis<=c.timeInMillis){
                                val sdf = SimpleDateFormat(myTimeFormat, Locale.US)
                                myappointmentFromTime = sdf.format(c.time)
                                mView!!.tv_from_time.text = myappointmentFromTime
                            }else{
                                Utility.showSnackBarValidationError(mView!!.calendarBottomSheet,
                                    requireContext().getString(R.string.time_is_before_current_time),
                                    requireContext())
                            }*/
                            val sdf = SimpleDateFormat(myTimeFormat, Locale.US)
                            myappointmentFromTime = sdf.format(c.time)
                            mView!!.tv_from_time.text = myappointmentFromTime
                        },hour,minute,false)
                        tpd.show()
                    }

                }
            }
       }

        mView!!.ll_end_time.setOnClickListener {
            if (myappointmentFromTime.equals("")||myappointmentFromTime==null){
                Utility.showSnackBarValidationError(mView!!.calendarBottomSheet,
                    requireContext().getString(R.string.end_date_cannot_be_empty),
                    requireContext())
            }else{
                Locale.setDefault(Locale.ENGLISH)
                val hour = c.get(Calendar.HOUR)
                val minute = c.get(Calendar.MINUTE)

                val tpd = TimePickerDialog(requireContext(), { _, h, m ->

                    val nowCal1 = Calendar.getInstance()
                    nowCal1.set(Calendar.HOUR_OF_DAY, h)
                    nowCal1.set(Calendar.MINUTE, m)
                    val sdf = SimpleDateFormat(myDateFormat, Locale.US)
                    val currentDate = sdf.format(nowCal1.time)
                    val sdf1 = SimpleDateFormat(myDateFormat+" "+myTimeFormat, Locale.US)
                    val oldDate = sdf1.parse(currentDate+" "+ myappointmentFromTime)
                    val nowCal2 = Calendar.getInstance()
                    nowCal2.time = oldDate
                    Log.e("oldTime", nowCal2.timeInMillis.toString())
                    Log.e("newTime", nowCal1.timeInMillis.toString())
                    myappointmentToTime = ""
                    if (nowCal2.timeInMillis<=nowCal1.timeInMillis){
                        val sdf = SimpleDateFormat(myTimeFormat, Locale.US)
                        myappointmentToTime = sdf.format(nowCal1.time)
                        mView!!.tv_to_time.text = myappointmentToTime
                    }else{
                        Utility.showSnackBarValidationError(mView!!.calendarBottomSheet,
                            requireContext().getString(R.string.end_time_must_be_greater_than_start_time),
                            requireContext())
                    }

                },hour,minute,false)
                tpd.show()
            }
        }

        mView!!.set.setOnClickListener {
            validation()
          /*  val selectedSlotsJson = slotsAdapter.getAllSelectedSlots(slotsList)
            Log.e("json", ""+selectedSlotsJson)*/
        }
    }


    private fun validation() {
        myappointmentFromTime = mView!!.tv_from_time.text.toString().trim()
        myappointmentToTime = mView!!.tv_to_time.text.toString().trim()
     /*   myappointmentFromTime = mySlotsList[0].fromTime.toString()
        myappointmentToTime = mySlotsList[mySlotsList.size-1].ToTime.toString()
*/
        when {
            TextUtils.isEmpty(myappointmentDate) -> {
                Utility.showSnackBarValidationError(mView!!.calendarBottomSheet,
                    requireContext().getString(R.string.please_select_a_date),
                    requireContext())
            }
            TextUtils.isEmpty(myappointmentFromTime) -> {
                Utility.showSnackBarValidationError(mView!!.calendarBottomSheet,
                    requireContext().getString(R.string.please_select_a_from_time),
                    requireContext())
            }
            TextUtils.isEmpty(myappointmentToTime) -> {
                Utility.showSnackBarValidationError(mView!!.calendarBottomSheet,
                    requireContext().getString(R.string.please_select_end_time),
                    requireContext())
            }
            myappointmentToTime.equals(myappointmentFromTime) -> {
                Utility.showSnackBarValidationError(mView!!.calendarBottomSheet,
                    requireContext().getString(R.string.end_time_must_be_greater_than_start_time),
                    requireContext())
            }
            else -> {
                val hashMap = HashMap<String, String>()
                val sdfNew = SimpleDateFormat(myTimeFormat, Locale.US)
                val sdf1 = SimpleDateFormat(myTimeFormat1, Locale.US)
                val sdf2 = SimpleDateFormat(myTimeFormat1, Locale.US)
                val date1 = sdf1.parse(myappointmentFromTime)
                val date2 = sdf2.parse(myappointmentToTime)
                val date2StringFrom = sdfNew.format(date1)
                val date2StringTo = sdfNew.format(date2)
                hashMap["myappointmentDate"] = myappointmentDate.toString()
                hashMap["myappointmentFromTime"] = date2StringFrom.toString()
                hashMap["myappointmentToTime"] = date2StringTo.toString()

                onSaveAppointmentDateTimeCallback!!.SaveAppointmentDateTimeCallback(hashMap)
                dismiss()
            }
        }
    }

    fun setSubscribeClickListenerCallback(onSaveAppointmentDateTimeCallback: ClickInterface.onSaveAppointmentDateTimeCallback){
        this.onSaveAppointmentDateTimeCallback = onSaveAppointmentDateTimeCallback
    }


    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }

    companion object {
        const val TAG = "BottomSheetCalendarFragment"
        private var my_reference = ""
        private var my_already_booked_time = ""
        private var my_already_booked_date = ""
        private var dates = ArrayList<Dates>()
        fun newInstance(
            reference: String,
            already_booked_time: String,
            already_booked_date: String,
            dates: ArrayList<Dates>
        ): BottomSheetCalendarFragment {
            Locale.setDefault(Locale.US)
            this.my_reference = reference
            this.my_already_booked_time = already_booked_time
            this.my_already_booked_date = already_booked_date
            this.dates = dates
            return BottomSheetCalendarFragment()
        }
    }



    //---------------------------------------------Booked Slots Code needed to be implemented in future ----------------------------> Done By Shubham Jain -------------------------------> 28/05/2022


    /*myFirstLoop@ for(i in 0 until dates.size){
        if (myappointmentDate.equals(dates[i].bookedDate)){
            myBookingSlotsList.clear()
            for (i in 0 until dates[i].bookedTime.size){
                val slots = BookingSlots()
                slots.fromTime = dates[i].bookedTime[i].bFrom
                slots.ToTime = dates[i].bookedTime[i].bTo
                myBookingSlotsList.add(slots)
            }
            break@myFirstLoop
        }else{
            myBookingSlotsList.clear()
        }
    }

    val slotsAdapter = SlotsAdapter(requireContext(), slotsList,myBookingSlotsList, object : SlotsAdapter.slotsCallback{
        @SuppressLint("ResourceAsColor")
        override fun slotsClicked(position: Int, view : View) {
            val slot = slotsList[position]
            slot.isChecked = !slot.isChecked
            if (slot.isChecked){
                mySlotsList.add(slot)
                myappointmentFromTime = mySlotsList[0].fromTime.toString()
                myappointmentToTime = mySlotsList[mySlotsList.size-1].ToTime.toString()
                view.slotCard.setBackgroundResource(R.drawable.gold_solid_box)
            }else{
                view.slotCard.setBackgroundResource(R.drawable.gray_solid_box)
            }
        }

    })
    mView!!.rvSlots.adapter = slotsAdapter*/

    //---------------------------------------------Booked Slots Code needed to be implemented in future ----------------------------> Done By Shubham Jain -------------------------------> 28/05/2022


}