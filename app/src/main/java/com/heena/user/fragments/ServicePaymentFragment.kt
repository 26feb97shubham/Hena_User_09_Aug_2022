package com.heena.user.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.animation.AlphaAnimation
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.heena.user.Dialogs.NoInternetDialog
import com.heena.user.R
import com.heena.user.`interface`.APIInterface.Companion.createBuilder
import com.heena.user.`interface`.APIUtils
import com.heena.user.`interface`.APIUtils.ServicePayment
import com.heena.user.`interface`.APIUtils.ServicePaymentTOKEN
import com.heena.user.`interface`.ClickInterface
import com.heena.user.activities.TapPaymentGateway
import com.heena.user.adapters.PaymentCardsAdapter
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.bottomsheetdialog.AddressListBottomSheetFragment
import com.heena.user.bottomsheetdialog.BottomSheetCalendarFragment
import com.heena.user.models.*
import com.heena.user.utils.LogUtils
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.already_booked_date
import com.heena.user.utils.Utility.already_booked_time
import com.heena.user.utils.Utility.apiInterface
import com.heena.user.utils.Utility.convertDoubleValueWithCommaSeparator
import com.heena.user.utils.Utility.dpoPaymentApiInterface
import com.heena.user.utils.Utility.reference
import com.heena.user.utils.Utility.setSafeOnClickListener
import fr.arnaudguyon.xmltojsonlib.XmlToJson
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.custom_toast_layout.*
import kotlinx.android.synthetic.main.fragment_service_payment.view.*
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ServicePaymentFragment : Fragment() {
    // TODO: Rename and change types of parameters
    var mView : View?= null
    var serviceItem : ServiceItem?=null
    private var ladiesCount = "0"
    private var ladyCount = 0.0
    private var childrensCount = "0"
    private var childCount = 0.0
    private var bookingDateTime : String?= null
    private var bookingDate : String?= null
    private var bookingFromTime : String?= null
    private var bookingToTime : String?= null
    private var specialRequest : String?= null
    private var commision : String?= null
    private var myAddressId : String = "0"
    private var lat : String = ""
    private var longi : String = ""
    private var my_address : String?= null

    private var subTotalPrice : Double?=null
    private var TotalPrice : Double?=null
    private var childPrice : Double?=null
    private var ladyPrice : Double?=null
    private var main_commission : Double?=null
    private var manager_id = ""
    private var managerId = 0
    private var card_id = ""
    private var managerName = ""
    lateinit var paymentCardsAdapter: PaymentCardsAdapter
    var cardsList = ArrayList<Cards>()
    var dates = ArrayList<Dates>()
    var service : Service?= null
    var manager : Manager?= null
    var offer : OfferItemZ?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            serviceItem = it.getSerializable("service") as ServiceItem?
            Log.e("service", serviceItem.toString())
        }
    }
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_service_payment, container, false)
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
        )
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        myAddressId = "0"
        mView!!.tv_locations.text =  sharedPreferenceInstance!!.get(SharedPreferenceUtility.SavedAddress, "")
        mView!!.tv_calendar.text = sharedPreferenceInstance!!.get(SharedPreferenceUtility.SavedBookingDate, "")
        lat =  sharedPreferenceInstance!!.get(SharedPreferenceUtility.SavedLat, "")
        longi =  sharedPreferenceInstance!!.get(SharedPreferenceUtility.SavedLng, "")

        mView!!.et_children_count.isEnabled = false
        mView!!.tint_children_count.isEnabled = false


        requireActivity().iv_back.setSafeOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F, 0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().navigate(R.id.action_servicvePaymentFragment_to_homeFragment)
        }

        requireActivity().iv_notification.setSafeOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F, 0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }

        requireActivity().tv_title.text = ""

        if (!Utility.hasConnection(requireContext())){
            val noInternetDialog = NoInternetDialog()
            noInternetDialog.isCancelable = false
            noInternetDialog.setRetryCallback(object : NoInternetDialog.RetryInterface {
                override fun retry() {
                    noInternetDialog.dismiss()
                    showServiceDetails()
                    showCardsListing()
                }

            })
            noInternetDialog.show(requireActivity().supportFragmentManager, "Home Fragment")
        }else{
            showServiceDetails()
            showCardsListing()
        }


        mView!!.calendar_layout.setSafeOnClickListener {
            reference = "booking"
            already_booked_time = bookingFromTime.toString()
            already_booked_date = bookingDate.toString()
            val calendarFragment = BottomSheetCalendarFragment.newInstance(
                reference,
                already_booked_time,
                already_booked_date,
                dates
            )
            calendarFragment.show(requireActivity().supportFragmentManager, BottomSheetCalendarFragment.TAG)
            calendarFragment.setSubscribeClickListenerCallback(object : ClickInterface.onSaveAppointmentDateTimeCallback {
                override fun SaveAppointmentDateTimeCallback(hashMap: HashMap<String, String>) {
                    val myAppointmentDateTime = hashMap.get("myappointmentDate") + " - " + hashMap.get("myappointmentFromTime")
                    bookingDate = hashMap.get("myappointmentDate")
                    bookingFromTime = hashMap.get("myappointmentFromTime")
                    bookingToTime = hashMap.get("myappointmentToTime")
                    sharedPreferenceInstance!!.save(SharedPreferenceUtility.SavedBookingDate, myAppointmentDateTime)
                    mView!!.tv_calendar.text = myAppointmentDateTime.toString()
                }
            })
        }

        mView!!.tv_add_new_card_service_payment.setSafeOnClickListener {
//            findNavController().navigate(R.id.addNewCardFragment)
            val bundle = Bundle()
            bundle.putString("title", "addCards")
            findNavController().navigate(R.id.cmsFragment, bundle)
        }

        mView!!.tint_ladies_count.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                ladiesCount = p0.toString()
                mView!!.et_children_count.isEnabled = true
                mView!!.tint_children_count.isEnabled = true

                /*if (ladiesCount.equals("")){
                    if (childrensCount.equals("")){
                        ladyCount = 0.0
                        childCount = 0.0
                        ladiesCount = "0"
                        childrensCount= "0"
                    }else{
                        ladiesCount = "0"
                        ladyCount = 0.0
                    }
                }else{
                    if (childrensCount.equals("")){
                        childrensCount= "0"
                        childCount = 0.0
                    }
                    ladyCount = ladiesCount.toDouble()
                }

                subTotalPrice = ladyCount.toInt() * ladyPrice!! + childCount.toInt() * childPrice!!
                val subTotal =  "AED ${convertDoubleValueWithCommaSeparator(subTotalPrice!!)}"
                mView!!.tv_sub_total_fee.text =subTotal
                TotalPrice = subTotalPrice!!.toDouble() + main_commission!!.toDouble()
                val Total =  "AED ${convertDoubleValueWithCommaSeparator(TotalPrice!!)}"
                mView!!.tv_total_fee.text = Total*/


                if(bookingDate.isNullOrEmpty()){
                    if (ladiesCount.equals("")){
                        if (childrensCount.equals("")){
                            ladyCount = 0.0
                            childCount = 0.0
                            ladiesCount = "0"
                            childrensCount= "0"
                        }else{
                            ladiesCount = "0"
                            ladyCount = 0.0
                            childCount = childrensCount.toDouble()
                        }
                    }else{
                        if (childrensCount.equals("")){
                            childrensCount= "0"
                            childCount = 0.0
                        }else{
                            childCount = childrensCount.toDouble()
                        }
                        ladyCount = ladiesCount.toDouble()
                    }

                    subTotalPrice = ladyCount.toInt() * ladyPrice!! + childCount.toInt() * childPrice!!
                    val subTotal =  "AED ${convertDoubleValueWithCommaSeparator(subTotalPrice!!)}"
                    mView!!.tv_sub_total_fee.text =subTotal
                    TotalPrice = subTotalPrice!!.toDouble() + main_commission!!.toDouble()
                    val Total =  "AED ${convertDoubleValueWithCommaSeparator(TotalPrice!!)}"
                    mView!!.tv_total_fee.text = Total
                }else {
                    val myDateFormat = "yyyy-MM-dd"
                    val sdf = SimpleDateFormat(myDateFormat, Locale.US)
                    var offerEndDate : Date?=null
                    offerEndDate = if(offer==null){
                        null
                    }else{
                        sdf.parse(offer!!.ended_at)
                    }
                    val myBookingDate = sdf.parse(bookingDate)

                    if (offerEndDate!=null && myBookingDate.after(offerEndDate)) {
                        ladyPrice = service!!.price!!.main!!.toDouble()
                        childPrice = service!!.price!!.main!!.toDouble()
                    } else if(offerEndDate!=null && myBookingDate.before(offerEndDate)) {
                        ladyPrice = offer!!.offer_price
                        childPrice = offer!!.offer_price
                    }else {
                        ladyPrice = service!!.price!!.main!!.toDouble()
                        childPrice = service!!.price!!.main!!.toDouble()
                    }

                    if (ladiesCount.equals("")) {
                        if (childrensCount.equals("")) {
                            ladyCount = 0.0
                            childCount = 0.0
                            ladiesCount = "0"
                            childrensCount = "0"
                        } else {
                            ladiesCount = "0"
                            ladyCount = 0.0
                            childCount = childrensCount.toDouble()
                        }
                    } else {
                        if (childrensCount.equals("")) {
                            childrensCount = "0"
                            childCount = 0.0
                        } else {
                            childCount = childrensCount.toDouble()
                        }
                        ladyCount = ladiesCount.toDouble()
                    }

                    subTotalPrice =
                        ladyCount.toInt() * ladyPrice!! + childCount.toInt() * childPrice!!
                    val subTotal = "AED ${convertDoubleValueWithCommaSeparator(subTotalPrice!!)}"
                    mView!!.tv_sub_total_fee.text = subTotal
                    TotalPrice = subTotalPrice!!.toDouble() + main_commission!!.toDouble()
                    val Total = "AED ${convertDoubleValueWithCommaSeparator(TotalPrice!!)}"
                    mView!!.tv_total_fee.text = Total
                }

            }

            @SuppressLint("SetTextI18n")
            override fun afterTextChanged(p0: Editable?) {
                ladiesCount = p0.toString()
                mView!!.et_children_count.isEnabled = true
                mView!!.tint_children_count.isEnabled = true

                if(bookingDate.isNullOrEmpty()){
                    if (ladiesCount.equals("")){
                        if (childrensCount.equals("")){
                            ladyCount = 0.0
                            childCount = 0.0
                            ladiesCount = "0"
                            childrensCount= "0"
                        }else{
                            ladiesCount = "0"
                            ladyCount = 0.0
                            childCount = childrensCount.toDouble()
                        }
                    }else{
                        if (childrensCount.equals("")){
                            childrensCount= "0"
                            childCount = 0.0
                        }else{
                            childCount = childrensCount.toDouble()
                        }
                        ladyCount = ladiesCount.toDouble()
                    }

                    subTotalPrice = ladyCount.toInt() * ladyPrice!! + childCount.toInt() * childPrice!!
                    val subTotal =  "AED ${convertDoubleValueWithCommaSeparator(subTotalPrice!!)}"
                    mView!!.tv_sub_total_fee.text =subTotal
                    TotalPrice = subTotalPrice!!.toDouble() + main_commission!!.toDouble()
                    val Total =  "AED ${convertDoubleValueWithCommaSeparator(TotalPrice!!)}"
                    mView!!.tv_total_fee.text = Total
                }else{
                    val myDateFormat = "yyyy-MM-dd"
                    val sdf = SimpleDateFormat(myDateFormat, Locale.US)
                    val offerEndDate = sdf.parse(offer!!.ended_at)
                    val myBookingDate = sdf.parse(bookingDate)

                    if(myBookingDate.after(offerEndDate)){
                        ladyPrice = service!!.price!!.main!!.toDouble()
                        childPrice = service!!.price!!.main!!.toDouble()
                    }else{
                        ladyPrice = offer!!.offer_price
                        childPrice = offer!!.offer_price
                    }

                    if (ladiesCount.equals("")){
                        if (childrensCount.equals("")){
                            ladyCount = 0.0
                            childCount = 0.0
                            ladiesCount = "0"
                            childrensCount= "0"
                        }else{
                            ladiesCount = "0"
                            ladyCount = 0.0
                            childCount = childrensCount.toDouble()
                        }
                    }else{
                        if (childrensCount.equals("")){
                            childrensCount= "0"
                            childCount = 0.0
                        }else{
                            childCount = childrensCount.toDouble()
                        }
                        ladyCount = ladiesCount.toDouble()
                    }

                    subTotalPrice = ladyCount.toInt() * ladyPrice!! + childCount.toInt() * childPrice!!
                    val subTotal =  "AED ${convertDoubleValueWithCommaSeparator(subTotalPrice!!)}"
                    mView!!.tv_sub_total_fee.text =subTotal
                    TotalPrice = subTotalPrice!!.toDouble() + main_commission!!.toDouble()
                    val Total =  "AED ${convertDoubleValueWithCommaSeparator(TotalPrice!!)}"
                    mView!!.tv_total_fee.text = Total
                }


            }
        })


        mView!!.tint_children_count.doOnTextChanged { p0, _, _, _ ->
            childrensCount = p0.toString()
            if(bookingDate.isNullOrEmpty()){
                if (childrensCount.equals("")){
                    if (ladiesCount.equals("")){
                        ladiesCount = "0"
                        childrensCount = "0"
                        ladyCount = 0.0
                        childCount = 0.0
                    }else{
                        childrensCount = "0"
                        childCount = 0.0
                        ladyCount = ladiesCount.toDouble()
                    }
                }else{
                    if (ladiesCount.equals("")){
                        ladiesCount = "0"
                        ladyCount = 0.0
                    }else{
                        ladyCount = ladiesCount.toDouble()
                    }
                    childCount = childrensCount.toDouble()
                }
                subTotalPrice = ladyCount.toInt() * ladyPrice!! + childCount.toInt() * childPrice!!
                val subTotal =  "AED ${convertDoubleValueWithCommaSeparator(subTotalPrice!!)}"
                mView!!.tv_sub_total_fee.text =subTotal
                TotalPrice = subTotalPrice!!.toDouble() + main_commission!!.toDouble()
                val Total =  "AED ${convertDoubleValueWithCommaSeparator(TotalPrice!!)}"
                mView!!.tv_total_fee.text = Total
            }else{
                val myDateFormat = "yyyy-MM-dd"
                val sdf = SimpleDateFormat(myDateFormat, Locale.US)
                val offerEndDate = sdf.parse(offer!!.ended_at)
                val myBookingDate = sdf.parse(bookingDate)

                if(myBookingDate.after(offerEndDate)){
                    ladyPrice = service!!.price!!.main!!.toDouble()
                    childPrice = service!!.price!!.main!!.toDouble()
                }else{
                    ladyPrice = offer!!.offer_price
                    childPrice = offer!!.offer_price
                }
                if (childrensCount.equals("")){
                    if (ladiesCount.equals("")){
                        ladiesCount = "0"
                        childrensCount = "0"
                        ladyCount = 0.0
                        childCount = 0.0
                    }else{
                        childrensCount = "0"
                        childCount = 0.0
                        ladyCount = ladiesCount.toDouble()
                    }
                }else{
                    if (ladiesCount.equals("")){
                        ladiesCount = "0"
                        ladyCount = 0.0
                    }else{
                        ladyCount = ladiesCount.toDouble()
                    }
                    childCount = childrensCount.toDouble()
                }
                subTotalPrice = ladyCount.toInt() * ladyPrice!! + childCount.toInt() * childPrice!!
                val subTotal =  "AED ${convertDoubleValueWithCommaSeparator(subTotalPrice!!)}"
                mView!!.tv_sub_total_fee.text =subTotal
                TotalPrice = subTotalPrice!!.toDouble() + main_commission!!.toDouble()
                val Total =  "AED ${convertDoubleValueWithCommaSeparator(TotalPrice!!)}"
                mView!!.tv_total_fee.text = Total
            }
        }



       mView!!.tint_children_count.doAfterTextChanged {
           childrensCount = it.toString()

/*           if (childrensCount.equals("")){
               if (ladiesCount.equals("")){
                   ladiesCount = "0"
                   childrensCount = "0"
                   ladyCount = 0.0
                   childCount = 0.0
               }else{
                   childrensCount = "0"
                   childCount = 0.0
                   ladyCount = ladiesCount.toDouble()
               }
           }else{
               if (ladiesCount.equals("")){
                   ladiesCount = "0"
                   ladyCount = 0.0
               }else{
                   ladyCount = ladiesCount.toDouble()
               }
               childCount = childrensCount.toDouble()
           }
           subTotalPrice = ladyCount.toInt() * ladyPrice!! + childCount.toInt() * childPrice!!
           val subTotal =  "AED ${convertDoubleValueWithCommaSeparator(subTotalPrice!!)}"
           mView!!.tv_sub_total_fee.text =subTotal
           TotalPrice = subTotalPrice!!.toDouble() + main_commission!!.toDouble()
           val Total =  "AED ${Utility.convertDoubleValueWithCommaSeparator(TotalPrice!!)}"
           mView!!.tv_total_fee.text = Total*/

           if(bookingDate.isNullOrEmpty()){
               if (childrensCount.equals("")){
                   if (ladiesCount.equals("")){
                       ladiesCount = "0"
                       childrensCount = "0"
                       ladyCount = 0.0
                       childCount = 0.0
                   }else{
                       childrensCount = "0"
                       childCount = 0.0
                       ladyCount = ladiesCount.toDouble()
                   }
               }else{
                   if (ladiesCount.equals("")){
                       ladiesCount = "0"
                       ladyCount = 0.0
                   }else{
                       ladyCount = ladiesCount.toDouble()
                   }
                   childCount = childrensCount.toDouble()
               }
               subTotalPrice = ladyCount.toInt() * ladyPrice!! + childCount.toInt() * childPrice!!
               val subTotal =  "AED ${convertDoubleValueWithCommaSeparator(subTotalPrice!!)}"
               mView!!.tv_sub_total_fee.text =subTotal
               TotalPrice = subTotalPrice!!.toDouble() + main_commission!!.toDouble()
               val Total =  "AED ${convertDoubleValueWithCommaSeparator(TotalPrice!!)}"
               mView!!.tv_total_fee.text = Total
           }else{
               val myDateFormat = "yyyy-MM-dd"
               val sdf = SimpleDateFormat(myDateFormat, Locale.US)
               val offerEndDate = sdf.parse(offer!!.ended_at)
               val myBookingDate = sdf.parse(bookingDate)

               if(myBookingDate.after(offerEndDate)){
                   ladyPrice = service!!.price!!.main!!.toDouble()
                   childPrice = service!!.price!!.main!!.toDouble()
               }else{
                   ladyPrice = offer!!.offer_price
                   childPrice = offer!!.offer_price
               }
               if (childrensCount.equals("")){
                   if (ladiesCount.equals("")){
                       ladiesCount = "0"
                       childrensCount = "0"
                       ladyCount = 0.0
                       childCount = 0.0
                   }else{
                       childrensCount = "0"
                       childCount = 0.0
                       ladyCount = ladiesCount.toDouble()
                   }
               }else{
                   if (ladiesCount.equals("")){
                       ladiesCount = "0"
                       ladyCount = 0.0
                   }else{
                       ladyCount = ladiesCount.toDouble()
                   }
                   childCount = childrensCount.toDouble()
               }
               subTotalPrice = ladyCount.toInt() * ladyPrice!! + childCount.toInt() * childPrice!!
               val subTotal =  "AED ${convertDoubleValueWithCommaSeparator(subTotalPrice!!)}"
               mView!!.tv_sub_total_fee.text =subTotal
               TotalPrice = subTotalPrice!!.toDouble() + main_commission!!.toDouble()
               val Total =  "AED ${convertDoubleValueWithCommaSeparator(TotalPrice!!)}"
               mView!!.tv_total_fee.text = Total
           }
        }

        mView!!.location_layout.setSafeOnClickListener {
            val addressListBottomSheetFragment= AddressListBottomSheetFragment.newInstance()
            addressListBottomSheetFragment.show(requireActivity().supportFragmentManager, AddressListBottomSheetFragment.TAG)

            addressListBottomSheetFragment.setAddressCallback(object : ClickInterface.setAddressClick {
                override fun setAddress(myaddressItem: AddressItem?) {
                    my_address = myaddressItem!!.flat + " , " + myaddressItem.street
                    myAddressId = myaddressItem.address_id.toString()
                    mView!!.tv_locations.text = my_address.toString()
                }
            })
        }

        mView!!.tv_add_new_location.setSafeOnClickListener {
            val bundle = Bundle()
            bundle.putString("direction", "Payment Fragment")
            findNavController().navigate(R.id.action_servicvePaymentFragment_to_addressBottomSheetFragment, bundle)
        }

        mView!!.supplierImg.setSafeOnClickListener{
            val bundle = Bundle()
            bundle.putString("managerName", managerName)
            bundle.putInt("managerId", managerId)
            findNavController().navigate(R.id.action_servicvePaymentFragment_to_naqashatProfileFragment, bundle)
        }

        mView!!.tv_book_service.setSafeOnClickListener {
            validateAndProceed()
        }
    }

    private fun showCardsListing() {
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        mView!!.frag_service_payment_progressBar.visibility= View.VISIBLE
        val call = apiInterface.showCards(sharedPreferenceInstance!!.get(SharedPreferenceUtility.UserId, 0), sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""])
        call!!.enqueue(object : Callback<ViewCardResponse?> {
            override fun onResponse(
                call: Call<ViewCardResponse?>,
                response: Response<ViewCardResponse?>
            ) {
                mView!!.frag_service_payment_progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.isSuccessful){
                        if (response.body()!!.status==1){
                            mView!!.tv_no_cards_found_service_payment.visibility = View.GONE
                            cardsList.clear()
                            cardsList = (response.body()!!.cards as ArrayList<Cards>?)!!
                            mView!!.rv_cards.layoutManager= LinearLayoutManager(requireContext())
                            paymentCardsAdapter = PaymentCardsAdapter(requireContext(), cardsList,object : ClickInterface.ClickPosInterface{
                                override fun clickPostion(pos: Int, type: String) {
                                    card_id = cardsList[pos].id.toString()
                                    paymentCardsAdapter.notifyDataSetChanged()
                                }
                            })
                            mView!!.rv_cards.adapter=paymentCardsAdapter
                        }else{
                            mView!!.tv_no_cards_found_service_payment.visibility = View.VISIBLE
                        }
                    }else{
                        Utility.showSnackBarOnResponseError(mView!!.paymentServiceFragmentConstraintLayout,
                            requireContext().getString(R.string.response_isnt_successful),
                            requireContext())

                    }
                }catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ViewCardResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(mView!!.paymentServiceFragmentConstraintLayout,
                    requireContext().getString(R.string.response_isnt_successful),
                    requireContext())
                mView!!.frag_service_payment_progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }

    private fun validateAndProceed() {
        ladiesCount = mView!!.tint_ladies_count.text.toString().trim()
        childrensCount = mView!!.tint_children_count.text.toString().trim()
        my_address = mView!!.tv_locations.text.toString().trim()
        specialRequest = mView!!.et_special_request.text.toString().trim()
        bookingDateTime = mView!!.tv_calendar.text.toString().trim()

        Log.e("card_id", ""+card_id)

        when {
            TextUtils.isEmpty(ladiesCount) -> {
                Utility.showSnackBarValidationError(mView!!.paymentServiceFragmentConstraintLayout,
                    requireContext().getString(R.string.please_enter_a_valid_ladies_count),
                    requireContext())
            }
            ladiesCount.equals("0") -> {
                Utility.showSnackBarValidationError(mView!!.paymentServiceFragmentConstraintLayout,
                    requireContext().getString(R.string.ladies_count_must_be_greater_than_zero),
                    requireContext())
            }
            TextUtils.isEmpty(childrensCount) -> {
                Utility.showSnackBarValidationError(mView!!.paymentServiceFragmentConstraintLayout,
                    requireContext().getString(R.string.please_enter_a_valid_childrens_count),
                    requireContext())
            }
            TextUtils.isEmpty(bookingDateTime) -> {
                Utility.showSnackBarValidationError(mView!!.paymentServiceFragmentConstraintLayout,
                    requireContext().getString(R.string.please_enter_a_valid_date_time),
                    requireContext())
            }
            TextUtils.isEmpty(my_address) -> {
                Utility.showSnackBarValidationError(mView!!.paymentServiceFragmentConstraintLayout,
                    requireContext().getString(R.string.please_enter_valid_location),
                    requireContext())
            }
            TextUtils.isEmpty(specialRequest) -> {
                Utility.showSnackBarValidationError(mView!!.paymentServiceFragmentConstraintLayout,
                    requireContext().getString(R.string.please_enter_valid_message),
                    requireContext())
            }
            TextUtils.isEmpty(card_id) -> {
                Utility.showSnackBarValidationError(mView!!.paymentServiceFragmentConstraintLayout,
                    requireContext().getString(R.string.please_choose_a_card_for_payment),
                    requireContext())
            }
            else -> {
                bookService()
            }
        }
    }

    private fun bookService() {
        //live TotalPrice
       /* ServicePayment = true
        mView!!.frag_service_payment_progressBar.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val builder = createBuilder(arrayOf(
            "user_id", "service_id", "manager_id",
            "address_id", "b_to", "b_from",
            "b_date", "message", "adr_lat",
            "adr_long", "adr_location", "c_ladies",
            "c_children", "total_price"),
                arrayOf(
                    sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0].toString(),
                        serviceItem!!.service_id.toString(),
                        manager_id,
                        myAddressId,
                        bookingToTime.toString(),
                        bookingFromTime.toString(),
                        bookingDate.toString(),
                        specialRequest.toString(),
                        lat,
                        longi,
                        my_address.toString(),
                        ladiesCount,
                        childrensCount,
                    TotalPrice.toString()))

        val call = dpoPaymentApiInterface.create_charge(builder.build())
        call.enqueue(object : Callback<ResponseBody?>{
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                mView!!.frag_service_payment_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {

                    if(APIUtils.resultExplanationPaymentStatus){
                        if(response.isSuccessful){
                            *//*    if (response.body()!=null){
                                    val jsonObject = JSONObject(response.body()!!.string())
                                    if (jsonObject.getInt("status")==1){
                                        val data = jsonObject.getJSONObject("data")
                                        sharedPreferenceInstance!!.save(SharedPreferenceUtility.SavedAddress,"")
                                        sharedPreferenceInstance!!.save(SharedPreferenceUtility.SavedLat,"")
                                        sharedPreferenceInstance!!.save(SharedPreferenceUtility.SavedLng,"")
                                        sharedPreferenceInstance!!.save(SharedPreferenceUtility.SavedBookingDate,"")
                                        if (data!=null){
                                            val source = Gson().fromJson(data.getJSONObject("source").toString(), SourceModel::class.java)
                                            val type = if (source.type==null){
                                                ""
                                            }else{
                                                source.type
                                            }
                                            if (type.equals("CARD_NOT_PRESENT")){
                                                Utility.showSnackBarOnResponseError(mView!!.paymentServiceFragmentConstraintLayout,
                                                    requireContext().getString(R.string.card_is_not_valid),
                                                    requireContext())
                                            }else{
                                                val url = data.getJSONObject("transaction").getString("url")
                                                val tapID = data.getString("id")
                                                val redirectUrl = data.getJSONObject("redirect").getString("url")+"/"+tapID
                                                val bundle = Bundle()
                                                bundle.putString("url", url)
                                                bundle.putString("redirect_url", redirectUrl)
                                                bundle.putString("tap_id", tapID)
                                                requireActivity().startActivity(Intent(requireContext(), TapPaymentGateway::class.java).putExtras(bundle))
                                            }
                                        }
                                    }
                                }*//*

                            if (ServicePaymentTOKEN.isEmpty()){
                                Utility.showSnackBarOnResponseError(mView!!.paymentServiceFragmentConstraintLayout,
                                    requireContext().getString(R.string.response_isnt_successful),
                                    requireContext())
                            }else{
                                val bundle = Bundle()
                                bundle.putString("TransToken",ServicePaymentTOKEN)
                                bundle.putString("title",requireContext().getString(R.string.payment))
                                bundle.putString("user_id",sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0].toString())
                                bundle.putString("service_id",serviceItem!!.service_id.toString())
                                bundle.putString("manager_id",manager_id)
                                bundle.putString("address_id",myAddressId)
                                bundle.putString("b_to",bookingToTime.toString())
                                bundle.putString("b_from",bookingFromTime.toString())
                                bundle.putString("b_date",bookingDate.toString())
                                bundle.putString("message",specialRequest.toString())
                                bundle.putString("adr_lat",lat)
                                bundle.putString("adr_long",longi)
                                bundle.putString("adr_location",my_address.toString())
                                bundle.putString("c_ladies",ladiesCount)
                                bundle.putString("c_children",childrensCount)
                                bundle.putString("total_price",TotalPrice.toString())
                                findNavController().navigate(R.id.paymentFragment, bundle)
                            }
                        }else{
                            Utility.showSnackBarOnResponseError(mView!!.paymentServiceFragmentConstraintLayout,
                                requireContext().getString(R.string.response_isnt_successful),
                                requireContext())
                        }
                    }else{
                        Utility.showSnackBarOnResponseError(mView!!.paymentServiceFragmentConstraintLayout,
                            APIUtils.resultExplanationPayment,
                            requireContext())
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
                Log.e("error", t.message.toString())
                Utility.showSnackBarOnResponseError(mView!!.paymentServiceFragmentConstraintLayout,
                    requireContext().getString(R.string.response_isnt_successful),
                    requireContext())
            }

        })*/

        val builder = createBuilder(arrayOf(
            "user_id", "service_id", "manager_id",
            "address_id", "b_to", "b_from",
            "b_date", "message", "adr_lat",
            "adr_long", "adr_location", "c_ladies",
            "c_children","card_id", "total_price"),
            arrayOf(
                sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0].toString(),
                serviceItem!!.service_id.toString(),
                manager_id,
                myAddressId,
                bookingToTime.toString(),
                bookingFromTime.toString(),
                bookingDate.toString(),
                specialRequest.toString(),
                lat,
                longi,
                my_address.toString(),
                ladiesCount,
                childrensCount,
                card_id.toString(),
                TotalPrice.toString()))


        val call = apiInterface.create_charge(builder.build(), SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""])
        call.enqueue(object : Callback<ResponseBody?>{
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                mView!!.frag_service_payment_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if(response.isSuccessful){
                        if (response.body()!=null){
                            val jsonObject = JSONObject(response.body()!!.string())
                            if (jsonObject.getInt("status")==1){
                                val data = jsonObject.getJSONObject("data")
                                if (data!=null){
                                    val source = Gson().fromJson(data.getJSONObject("source").toString(), SourceModel::class.java)
                                    val type = if (source.type==null){
                                        ""
                                    }else{
                                        source.type
                                    }
                                    if (type.equals("CARD_NOT_PRESENT")){
                                        Utility.showSnackBarOnResponseError(mView!!.paymentServiceFragmentConstraintLayout,
                                            requireContext().getString(R.string.card_is_not_valid),
                                            requireContext())
                                    }else{
                                        val url = data.getJSONObject("transaction").getString("url")
                                        val tapID = data.getString("id")
                                        val redirectUrl = data.getJSONObject("redirect").getString("url")+"/"+tapID
                                        val bundle = Bundle()
                                        bundle.putString("url", url)
                                        bundle.putString("redirect_url", redirectUrl)
                                        bundle.putString("tap_id", tapID)
                                        requireActivity().startActivity(Intent(requireContext(), TapPaymentGateway::class.java).putExtras(bundle))
                                    }
                                }
                            }
                        }
                    }else{
                        LogUtils.shortToast(requireContext(), requireContext().getString(R.string.response_isnt_successful))
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
                Log.e("error", t.message.toString())
                LogUtils.shortToast(requireContext(), t.message)
            }

        })
    }

    private fun showServiceDetails() {
        mView!!.frag_service_payment_progressBar.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val serviceId = serviceItem!!.service_id
        val call = apiInterface.showService(serviceId!!, sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""])
        call?.enqueue(object : Callback<ServiceShowResponse?> {
            override fun onResponse(
                    call: Call<ServiceShowResponse?>,
                    response: Response<ServiceShowResponse?>
            ) {
                mView!!.frag_service_payment_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful) {
                    if (response.body()!!.status == 1) {
                        service = response.body()!!.service
                        manager = response.body()!!.manager
                        offer = response.body()!!.offer
                        dates.clear()

                        manager_id = manager?.manager_id.toString()
                        managerId = manager?.manager_id!!
                        sharedPreferenceInstance!!.save(SharedPreferenceUtility.ManagerId, manager_id)
                        managerName = manager?.name.toString()
                        val galleryList = response.body()!!.gallery as ArrayList<GalleryItem>
                        if (galleryList.size==0){
                            Glide.with(requireContext()).load(R.drawable.def_henna).into(mView!!.iv_heena_design)
                        }else{
                            Glide.with(requireContext()).load(galleryList[0].name).into(mView!!.iv_heena_design)
                        }
                        mView!!.tv_service.text = service!!.name

                        if (offer!=null){
                            mView!!.tv_price.visibility = View.GONE
                            mView!!.rl_offer.visibility = View.VISIBLE
                            val servicePrice = "AED ${convertDoubleValueWithCommaSeparator(response.body()!!.service!!.price!!.total!!.toDouble())}"
                            val offerPrice = "AED ${convertDoubleValueWithCommaSeparator(offer!!.offer_price!!)}"
                            mView!!.tv_main_price.text = servicePrice
                            mView!!.tv_offer_price.text = offerPrice
                            ladyPrice = offer!!.offer_price
                            childPrice = offer!!.offer_price
                            val subTotal = "AED ${Utility.convertDoubleValueWithCommaSeparator(offer!!.offer_price!!)}"
                            mView!!.tv_sub_total_fee.text = subTotal
                            val total = offer!!.offer_price!! + response.body()!!.commission!!.toDouble()
                            mView!!.tv_total_fee.text = "AED ${convertDoubleValueWithCommaSeparator(total)}"
                        }else{
                            mView!!.tv_price.visibility = View.VISIBLE
                            mView!!.rl_offer.visibility = View.GONE
                            val servicePrice = "AED ${convertDoubleValueWithCommaSeparator(response.body()!!.service!!.price!!.total!!.toDouble())}"
                            mView!!.tv_price.text = servicePrice
                            ladyPrice = service!!.price!!.main!!.toDouble()
                            childPrice = service!!.price!!.main!!.toDouble()
                            val sub_total = "AED ${convertDoubleValueWithCommaSeparator(service!!.price!!.total!!.toDouble())}"
                            mView!!.tv_sub_total_fee.text = sub_total
                            val total = service!!.price!!.total!!.toDouble() + response.body()!!.commission!!.toDouble()
                            mView!!.tv_total_fee.text = "AED ${convertDoubleValueWithCommaSeparator(total)}"
                        }
                        Glide.with(requireContext()).load(manager!!.image).into(mView!!.supplierImg)

                        if (manager!!.avg_star!!.isNullOrEmpty()){
                            mView!!.ratingBar.rating = 0F
                            mView!!.txtRating.text = String.format("%.1f",0F )
                        }else{
                            mView!!.ratingBar.rating = manager!!.avg_star!!.toFloat()
                            mView!!.txtRating.text = String.format("%.1f",manager!!.avg_star!!.toDouble())
                        }

                        main_commission = response.body()!!.commission!!.toDouble()
                        commision = "AED ${convertDoubleValueWithCommaSeparator(response.body()!!.commission!!.toDouble())}"
                        mView!!.tv_service_fee.text = commision


                        //showCardsListing()


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
                mView!!.frag_service_payment_progressBar.visibility = View.GONE
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