package com.heena.user.bottomsheetdialog

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AddressComponent
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.heena.user.R
import com.heena.user.`interface`.ClickInterface
import com.heena.user.adapters.CountryListingAdapter
import com.heena.user.utils.LogUtils
import com.heena.user.utils.SharedPreferenceUtility
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.heena.user.`interface`.APIInterface.Companion.createBuilder
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.models.*
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.apiInterface
import com.heena.user.utils.Utility.apiManagerInterface
import com.heena.user.utils.Utility.isNetworkAvailable
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.fragment_add_address_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_address_bottom_sheet.iv_toggle_off
import kotlinx.android.synthetic.main.fragment_address_bottom_sheet.iv_toggle_on
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class AddAddressBottomSheetFragment : BottomSheetDialogFragment(){
    private var mView: View?=null
    private var flat_villa: String ?=null
    private var building_name : String?=null
    private var title : String?=null
    private var street_area : String?=null
    private var stateName : String?=null
    private var is_default = 0
    private var countryList = ArrayList<Country>()
    private var emiratesList = ArrayList<Emirates>()
    lateinit var countryListingAdapter: CountryListingAdapter
    private var selectedCountry : String?=null
    private var selectedEmirates : String?=null
    private var countryId : Int?=null
    private var is_set_default = false


    var AUTOCOMPLETE_REQUEST_CODE: Int = 500
    var mLatitude: Double = 0.0
    var mLongitude: Double = 0.0
    private var emiratesId : Int?=null
    var countryName: String = ""
    private var current_latitude = 0.0
    private var current_longitude = 0.0
    private var strAddress = ""
    private var strCity = ""
    private var byDefaultStatus = true
    var myStatus = 0
    private val PERMISSIONS_2 = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    var my_click = ""


    private var activityResultLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()) { result ->
            var allAreGranted = true
            for(b in result.values) {
                allAreGranted = allAreGranted && b
            }
            if(allAreGranted) {
                Log.e("Granted", "Permissions")
                val fields: MutableList<Place.Field> = java.util.ArrayList()
                fields.add(Place.Field.NAME)
                fields.add(Place.Field.ID)
                fields.add(Place.Field.LAT_LNG)
                fields.add(Place.Field.ADDRESS)
                fields.add(Place.Field.ADDRESS_COMPONENTS)
                myStatus = AUTOCOMPLETE_REQUEST_CODE
                // Start the autocomplete intent.
                val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(
                    requireContext()
                )
                resultLauncher.launch(intent)
            }else{
                Utility.showSnackBarValidationError(mView!!.addAddressBottomSheetDialog,
                    requireContext().getString(R.string.please_allow_permissions),
                    requireContext())
                Log.e("Denied", "Permissions")
            }
        }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode== Activity.RESULT_OK){
            val place = Autocomplete.getPlaceFromIntent(it.data!!)
            val latlng = place.latLng
            mLatitude = latlng!!.latitude
            mLongitude = latlng.longitude
            val addressComponents: MutableList<AddressComponent> = place.addressComponents!!.asList()
            var ard = 0
            var add = ""
            for (addressComponent in addressComponents) {
                Log.e("addressss--", addressComponent.name)
                countryName = addressComponent.name
                if (ard == 0) {
                    add = addressComponent.name
                }

                var flag = false
                val types: MutableList<String> = addressComponent.types
                for (type in types) {
                    if (type.equals(
                            "locality",
                            true
                        ) || type.equals("administrative_area_level_2") || type.equals(
                            "administrative_area_level_1",
                            true
                        )
                    ) {
                        flag = true
                    }
                }
                if (flag) {
                    val center = LatLng(mLatitude, mLongitude)
                    val locality = getLocality(center)
                    val countryName = getCountry(center)

                    var address = ""

                    if (!add.isEmpty()) {
                        address = add
                    }
                    if (!locality.isEmpty() && !address.isEmpty() && !address.equals(locality)) {
                        address = address + ", " + locality
                    }
                    if (!addressComponent.name.isEmpty() && !address.isEmpty() && !address.equals(addressComponent.name)) {
                        address = address + ", " + addressComponent.name
                    }
                    if (!countryName.isEmpty() && !address.isEmpty()) {
                        address = address + ", " + countryName
                    }
                    mView!!.et_search_address.setText(address)
                    checkSearchAddressField(LatLng(mLatitude, mLongitude))
                    break
                }
                ard++
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView= inflater.inflate(R.layout.fragment_add_address_bottom_sheet, container, false)
        setUpViews()
        return mView
    }

    private fun setUpViews() {
        if (status == "edit"){
            showAddress()
            mView!!.tv_save.text = requireContext().getString(R.string.update)
        }else{
            mView!!.et_flat_villa.setText("")
            mView!!.et_building_name.setText("")
            mView!!.et_title.setText("")
            mView!!.et_street_area.setText("")
            mView!!.tv_emirate.setText("")
            mView!!.tv_save.text = requireContext().getString(R.string.save)
        }

        getEmirates()

        mView!!.tv_save.setSafeOnClickListener {
            validate(229)
        }

        mView!!.tv_emirate.setSafeOnClickListener {
            getEmirates()
        }

        mView!!.iv_toggle_on.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                is_set_default = false
                is_default = 0
                iv_toggle_off.visibility = View.VISIBLE
                iv_toggle_on.visibility = View.GONE
            }

        })

        mView!!.iv_toggle_off.setOnClickListener {
            is_set_default = true
            is_default = 1
            iv_toggle_off.visibility = View.GONE
            iv_toggle_on.visibility = View.VISIBLE
        }

        mView!!.et_search_address.setOnClickListener {
            mView!!.et_search_address.startAnimation(AlphaAnimation(1f, 0.5f))
            val fields: MutableList<Place.Field> = java.util.ArrayList()
            fields.add(Place.Field.NAME)
            fields.add(Place.Field.ID)
            fields.add(Place.Field.LAT_LNG)
            fields.add(Place.Field.ADDRESS)
            fields.add(Place.Field.ADDRESS_COMPONENTS)

            // Start the autocomplete intent.
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(requireContext())
            resultLauncher.launch(intent)
        }
    }


    private fun checkSearchAddressField(latLng: LatLng) {
        var myLocation = mView!!.et_search_address.text.toString().trim()
        if (myLocation.isEmpty()){
            Utility.showSnackBarValidationError(mView!!.addAddressBottomSheetDialog,requireContext().getString(R.string.please_select_the_address_first),requireContext());
            mView!!.flat_villa_layout.isEnabled = false
            mView!!.building_name_layout.isEnabled = false
            mView!!.title_layout.isEnabled = false
            mView!!.street_area_layout.isEnabled = false
            mView!!.emirate_layout.isEnabled = false
            mView!!.set_as_default_layout.isEnabled = false
        }else{
            mView!!.flat_villa_layout.isEnabled = true
            mView!!.building_name_layout.isEnabled = true
            mView!!.title_layout.isEnabled = true
            mView!!.street_area_layout.isEnabled = true
            mView!!.emirate_layout.isEnabled = true
            mView!!.set_as_default_layout.isEnabled = true

            setAddress(latLng)

        }
    }


    private fun showAddress() {
        val call = apiInterface.showAddress(addressId,
            sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]
        )
        call?.enqueue(object : Callback<ShowAddressResponse?>{
            override fun onResponse(call: Call<ShowAddressResponse?>, response: Response<ShowAddressResponse?>) {
                try {
                    if (response.body() != null) {
                        if (response.body()!!.status==1){
                           val addressItem = response.body()!!.address
                            mView!!.et_flat_villa.setText(addressItem!!.flat)
                            mView!!.et_building_name.setText(addressItem.building_name)
                            mView!!.et_title.setText(addressItem.title)
                            mView!!.et_street_area.setText(addressItem.street)
                            mView!!.tv_emirate.text = addressItem.country!!.name
                            mLatitude = addressItem.lat!!
                            mLongitude = addressItem.langt!!
                            for (i in 0 until countryList.size){
                                if (countryList[i].name!!.equals(addressItem.country.name)||countryList[i].name!!.equals(addressItem.country.name_ar)){
                                    countryId = countryList[i].country_id
                                    break
                                }else{
                                    LogUtils.shortToast(requireContext(), requireContext().getString(R.string.no_country_found))
                                }
                            }
                            if (addressItem.is_default==0){
                                iv_toggle_off.visibility = View.VISIBLE
                                iv_toggle_on.visibility = View.GONE
                            }else{
                                iv_toggle_off.visibility = View.GONE
                                iv_toggle_on.visibility = View.VISIBLE
                            }
                            is_default = addressItem.is_default!!
                        }else{
                            mView!!.cards_country_listing.visibility = View.GONE
                            mView!!.cards_emirates_listing.visibility = View.GONE
                            Utility.showSnackBarOnResponseError(mView!!.addAddressBottomSheetDialog,
                                requireContext().getString(R.string.response_isnt_successful),
                                requireContext())
                        }
                    }else {
                        mView!!.cards_country_listing.visibility = View.GONE
                        mView!!.cards_emirates_listing.visibility = View.GONE
                        Utility.showSnackBarOnResponseError(mView!!.addAddressBottomSheetDialog,
                            requireContext().getString(R.string.response_isnt_successful),
                            requireContext())
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ShowAddressResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(mView!!.addAddressBottomSheetDialog,
                    requireContext().getString(R.string.check_internet),
                    requireContext())
            }

        })
    }

    private fun getEmirates() {
        if (isNetworkAvailable()){
            val call = apiManagerInterface.getEmirates(sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""])
            call!!.enqueue(object : Callback<EmiratesListResponse?> {
                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(call: Call<EmiratesListResponse?>, response: Response<EmiratesListResponse?>) {
                    try {
                        if (response.body() != null) {
                            if (response.body()!!.status==1){
                                if (byDefaultStatus){
                                    mView!!.cards_emirates_listing.visibility = View.GONE
                                }else{
                                    mView!!.cards_emirates_listing.visibility = View.VISIBLE
                                }

                                mView!!.rv_emirates_listing.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                                emiratesList = response.body()!!.emirates as ArrayList<Emirates>
                                countryListingAdapter = CountryListingAdapter(requireContext(),null, emiratesList, object :  ClickInterface.OnRecyclerItemClick{
                                    override fun OnClickAction(position: Int) {
                                        if (sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "").equals("ar")){
                                            mView!!.tv_emirate.text = emiratesList[position].nameAr
                                        }else{
                                            mView!!.tv_emirate.text = emiratesList[position].name
                                        }
                                        selectedEmirates = mView!!.tv_emirate.text.toString().trim()
                                        mView!!.cards_emirates_listing.visibility = View.GONE
                                    }
                                })
                                mView!!.rv_emirates_listing.adapter = countryListingAdapter
                                countryListingAdapter.notifyDataSetChanged()
                            }else{
                                mView!!.cards_emirates_listing.visibility = View.GONE
                                Utility.showSnackBarOnResponseError(mView!!.addAddressBottomSheetDialog,
                                    requireContext().getString(R.string.response_isnt_successful),
                                    requireContext())
                            }
                        }else {
                            mView!!.cards_emirates_listing.visibility = View.GONE
                            Utility.showSnackBarOnResponseError(mView!!.addAddressBottomSheetDialog,
                                requireContext().getString(R.string.response_isnt_successful),
                                requireContext())
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(call: Call<EmiratesListResponse?>, throwable: Throwable) {
                    LogUtils.e("msg", throwable.message)
                    Utility.showSnackBarOnResponseError(mView!!.addAddressBottomSheetDialog,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext())
                }
            })
        }else{
            Utility.showSnackBarOnResponseError(mView!!.addAddressBottomSheetDialog,
                requireContext().getString(R.string.check_internet),
                requireContext())
        }

    }

    private fun validate(countryId: Int?) {
        flat_villa = mView!!.et_flat_villa.text.toString().trim()
        building_name = mView!!.et_building_name.text.toString().trim()
        title = mView!!.et_title.text.toString().trim()
        street_area = mView!!.et_street_area.text.toString().trim()
        selectedEmirates = mView!!.tv_emirate.text.toString().trim()

        when {
            TextUtils.isEmpty(flat_villa) -> {
                Utility.showSnackBarValidationError(mView!!.addAddressBottomSheetDialog,
                    requireContext().getString(R.string.please_enter_valid_flat_villa_name),
                    requireContext())
            }
            TextUtils.isEmpty(building_name) -> {
                Utility.showSnackBarValidationError(mView!!.addAddressBottomSheetDialog,
                    requireContext().getString(R.string.please_enter_valid_building_name),
                    requireContext())
            }
            TextUtils.isEmpty(title) -> {
                Utility.showSnackBarValidationError(mView!!.addAddressBottomSheetDialog,
                    requireContext().getString(R.string.please_enter_valid_title),
                    requireContext())
            }
            TextUtils.isEmpty(street_area) -> {
                Utility.showSnackBarValidationError(mView!!.addAddressBottomSheetDialog,
                    requireContext().getString(R.string.please_enter_valid_street_area),
                    requireContext())
            }
            TextUtils.isEmpty(selectedEmirates) -> {
                Utility.showSnackBarValidationError(mView!!.addAddressBottomSheetDialog,
                    requireContext().getString(R.string.please_enter_valid_emirate),
                    requireContext())
            }
            else -> {
                if(status=="edit"){
                    editAddress(countryId)
                }else{
                    saveAddress(countryId)
                }

            }
        }
    }

    private fun editAddress(countryId: Int?) {
        val builder = createBuilder(arrayOf("flat", "title", "street", "is_default", "country_id", "building_name", "address_id", "lang", "lat", "langt"),
            arrayOf(flat_villa.toString(),
                title.toString(),
                street_area.toString(),
                is_default.toString(),
                countryId.toString(),
                building_name.toString(),
                addressId.toString(),
                sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""],
            mLatitude.toString(),
            mLongitude.toString()))
        val call = apiInterface.editAddress(builder.build())

        call!!.enqueue(object : Callback<MyResponse?>{
            override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                if (response.isSuccessful){
                    if (response.body()!=null){
                        if (response.body()!!.status==1){
                            Utility.showSnackBarOnResponseSuccess(mView!!.addAddressBottomSheetDialog,
                                requireContext().getString(R.string.address_updated_successfully),
                                requireContext())
                            dismiss()
                        }
                    }else{
                        Utility.showSnackBarOnResponseError(mView!!.addAddressBottomSheetDialog,
                            response.message().toString(),
                            requireContext())
                    }
                }else{
                    Utility.showSnackBarOnResponseError(mView!!.addAddressBottomSheetDialog,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext())
                }
            }

            override fun onFailure(call: Call<MyResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(mView!!.addAddressBottomSheetDialog,
                    requireContext().getString(R.string.response_isnt_successful),
                    requireContext())
            }

        })
    }

    private fun saveAddress(country_Id: Int?) {
        val builder = createBuilder(arrayOf("flat", "title", "street", "is_default", "country_id", "building_name", "user_id", "lang", "lat", "langt"),
        arrayOf(flat_villa.toString(),
        title.toString(),
        street_area.toString(),
        is_default.toString(),
            country_Id.toString(),
        building_name.toString(),
        sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0].toString(),
        sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""],
            mLatitude.toString(),
            mLongitude.toString()))

        val call = apiInterface.addAddress(builder.build())
        call!!.enqueue(object : Callback<MyResponse?>{
            override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                if (response.isSuccessful){
                    if (response.body()!=null){
                        if (response.body()!!.status==1){
                            Utility.showSnackBarOnResponseSuccess(mView!!.addAddressBottomSheetDialog,
                                requireContext().getString(R.string.address_added_successfully),
                                requireContext())
                            dismiss()
                            findNavController().popBackStack()
                        }
                    }else{
                        Utility.showSnackBarOnResponseError(mView!!.addAddressBottomSheetDialog,
                            response.message().toString(),
                            requireContext())
                    }
                }else{
                    Utility.showSnackBarOnResponseError(mView!!.addAddressBottomSheetDialog,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext())
                }
            }

            override fun onFailure(call: Call<MyResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(mView!!.addAddressBottomSheetDialog,
                    requireContext().getString(R.string.response_isnt_successful),
                    requireContext())
            }
        })
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }

    companion object {
        const val TAG = "AddAddressBottomSheetFragment"
        private var instance: SharedPreferenceUtility? = null
        private var status = ""
        private var addressId = 0
        fun newInstance(bundle: Bundle?): AddAddressBottomSheetFragment {
            if (bundle!=null){
                status = bundle.getString("status").toString()
                addressId = bundle.getInt("addressId")
            }else{
                Log.e("bundle is ", "null")
            }
            return AddAddressBottomSheetFragment()
        }
        @Synchronized
        fun getInstance(): SharedPreferenceUtility {
            if (instance == null) {
                instance = SharedPreferenceUtility()
            }
            return instance as SharedPreferenceUtility
        }

    }


    private fun getLocality(latLng: LatLng): String {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses: List<Address>
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            var subLocality = ""
            if (!addresses[0].subLocality.isNullOrEmpty()) {
                subLocality = addresses[0].subLocality
            } else {
                if (!addresses[0].locality.isNullOrEmpty()) {
                    subLocality = addresses[0].locality
                }
            }
            return subLocality
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }


    private fun getCountry(latLng: LatLng): String {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses: List<Address>
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            var subLocality = ""
            if (!addresses[0].countryName.isNullOrEmpty()) {
                subLocality = addresses[0].countryName
            }
            return subLocality
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }

    fun setAddress(maplatLng: LatLng?) {
        val geocoder = Geocoder(requireContext())
        try {
            val addressList = geocoder.getFromLocation(maplatLng!!.latitude, maplatLng.longitude, 1)
            if (addressList != null && addressList.size > 0) {
                val locality = addressList[0].getAddressLine(0)
                val country = addressList[0].countryName
                if (locality != null && country != null)
                {
                    current_latitude = addressList[0].latitude
                    current_longitude = addressList[0].longitude
                    Log.e("cureent_lati", current_latitude.toString())
                    Log.e("cureent_longni", current_longitude.toString())
                    strAddress = addressList[0].getAddressLine(0)
                    Log.e("tesr", "" + strAddress)
                }
                if (addressList[0].locality != null) {
                    flat_villa = addressList[0].featureName
                    building_name = addressList[0].featureName
                    val abc = addressList[0].adminArea + addressList[0].subAdminArea
                    val def = addressList[0].premises
                    val ghi = addressList[0].thoroughfare + addressList[0].subThoroughfare
                    val jkl = addressList[0].adminArea
                    street_area = addressList[0].subLocality +  " " + addressList[0].locality
                    countryName = addressList[0].countryName
                    stateName = addressList[0].locality
                    mView!!.et_flat_villa.setText(flat_villa!!)
                    mView!!.et_building_name.setText(flat_villa!!)
                    mView!!.et_street_area.setText(street_area!!)
                    for (i in 0 until emiratesList.size){
                        if (stateName!!.equals(emiratesList[i].name)||countryName!!.equals(emiratesList[i].nameAr)){
                            mView!!.tv_emirate.setText(stateName)
                            emiratesId = emiratesList[i].countryId
                            break
                        }
                    }



                    Log.e("flat_villa", "" + flat_villa)
                    Log.e("country", "" + addressList[0].countryName)
                    Log.e("street_area", "" + street_area)
                    //Log.e("countryId", "" + countryId)
                    Log.e("abc", "" + abc)
                    Log.e("def", "" + def)
                    Log.e("ghi", "" + ghi)
                    Log.e("jkl", "" + jkl)
                    strCity = addressList[0].locality + addressList[0].countryCode + addressList[0].countryName
                    val addList = strAddress.split(",".toRegex()).toTypedArray()
                    Log.e("addList", "" + addList.toString())
                    strAddress = ""
                    for (s in addList) {
                        strAddress = if (strCity.equals(s.trim { it <= ' ' }, ignoreCase = true)) {
                            break
                        } else {
                            strAddress + s
                        }
                    }
                    Log.e("address ", strAddress)

                }
            }else{
                Log.e("err1", "err1")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}