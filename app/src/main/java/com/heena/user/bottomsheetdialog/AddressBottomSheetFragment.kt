package com.heena.user.bottomsheetdialog

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.heena.user.*
import com.heena.user.`interface`.ClickInterface
import com.heena.user.models.Country
import com.heena.user.models.CountryListResponse
import com.heena.user.utils.LogUtils
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.widget.Autocomplete
import com.heena.user.`interface`.APIInterface.Companion.createBuilder
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.models.MyResponse
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_add_address_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_address_bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_address_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_address_bottom_sheet.view.et_title
import kotlinx.android.synthetic.main.fragment_address_bottom_sheet.view.iv_toggle_off
import kotlinx.android.synthetic.main.fragment_address_bottom_sheet.view.iv_toggle_on
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap


class AddressBottomSheetFragment : Fragment(), OnMapReadyCallback,
        GoogleMap.OnCameraIdleListener{
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private var current_latitude = 0.0
    private var current_longitude = 0.0
    private var strAddress = ""
    private var strCity = ""
    private var flat_villa: String ?=null
    private var building_name : String?=null
    private var title : String?=null
    private var street_area : String?=null
    private var is_default : Int = 0
    private var countryList = ArrayList<Country>()
    private var countryId : Int?=null
    private var countryName : String?=null
    private var is_set_default = false
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0
    private var placeClick = false
    private var addressMap = HashMap<String, String>()
    private var direction : String = ""
    private var stateName : String?=null
    private var mView : View ?=null
    private var placeAPIresultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            val place = Autocomplete.getPlaceFromIntent(data!!)
            gMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(place.latLng!!, 15F))
            mLatitude = place.latLng!!.latitude
            mLongitude = place.latLng!!.longitude
            sharedPreferenceInstance!!.save(SharedPreferenceUtility.SavedLat,mLatitude.toString())
            sharedPreferenceInstance!!.save(SharedPreferenceUtility.SavedLng,mLongitude.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      arguments?.let {
          direction = it.getString("direction").toString()
      }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_address_bottom_sheet, container, false)
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
        )
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())


        requireActivity().tv_title.text = ""

        requireActivity().iv_back.setSafeOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F, 0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        requireActivity().iv_notification.setSafeOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F, 0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }

        var mapViewBundle : Bundle? = null
        if (savedInstanceState!=null){
            mapViewBundle = savedInstanceState.getBundle(getString(R.string.google_maps_api_key))
        }

        getCountires()

        mView!!.iv_toggle_off.setOnClickListener {
            is_set_default = true
            is_default = 0
            mView!!.iv_toggle_off.visibility = View.GONE
            mView!!.iv_toggle_on.visibility = View.VISIBLE
        }

        mView!!.iv_toggle_on.setOnClickListener {
            is_set_default = false
            is_default = 1
            mView!!.iv_toggle_off.visibility = View.VISIBLE
            mView!!.iv_toggle_on.visibility = View.GONE
        }
        placeClick = sharedPreferenceInstance!![SharedPreferenceUtility.PLACECLICK, false]

        mView!!.mapView.onCreate(mapViewBundle)
        mView!!.mapView.getMapAsync(this)


        /*mView!!.tv_location.setSafeOnClickListener {
            placeClick = true
            sharedPreferenceInstance!!.save(SharedPreferenceUtility.PLACECLICK, placeClick)
            val fields: MutableList<Place.Field> = ArrayList()
            fields.add(Place.Field.NAME)
            fields.add(Place.Field.ID)
            fields.add(Place.Field.LAT_LNG)
            fields.add(Place.Field.ADDRESS)
            fields.add(Place.Field.ADDRESS_COMPONENTS)

            // Start the autocomplete intent.
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(requireContext())
            placeAPIresultLauncher.launch(intent)
        }*/

        mView!!.tv_enter_manually.setSafeOnClickListener {
//            dismiss()
            val bundle = Bundle()
            bundle.putString("status", "add")
            bundle.putInt("addressId", 0)
            val addAddressBottomSheetFragment = AddAddressBottomSheetFragment.newInstance(bundle)
            addAddressBottomSheetFragment.show(requireActivity().supportFragmentManager, AddAddressBottomSheetFragment.TAG)
        }

        mView!!.tv_submit.setSafeOnClickListener {
            for (i in 0 until countryList.size){
                if (countryName!!.equals(countryList[i].name)||countryName!!.equals(countryList[i].name_ar)){
                    countryId = countryList[i].country_id
                    break
                }
            }
            Log.e("Address", tv_location.text.toString())
            Log.e("Lat", mLatitude.toString())
            Log.e("Lng", mLongitude.toString())
            validateAndSave(countryId)
        }
    }

    private fun validateAndSave(countryId: Int?) {
        title = mView!!.et_title.text.toString().trim()
        if(TextUtils.isEmpty(title)){
            Utility.showSnackBarValidationError(mView!!.addressBottomSheetLinearLayout,
                requireContext().getString(R.string.please_enter_valid_title),
                requireContext())
        }else if (countryId==null || countryId==0){
            Utility.showSnackBarValidationError(mView!!.addressBottomSheetLinearLayout,
                requireContext().getString(R.string.no_country_found),
                requireContext())
        } else{
            saveAddress(countryId)
        }
    }


    private fun saveAddress(countryId: Int?) {
        val builder = createBuilder(arrayOf("flat", "title", "street", "is_default", "country_id", "building_name", "user_id", "lat", "langt"),
                arrayOf(flat_villa.toString(),
                        title.toString(),
                        street_area.toString(),
                        is_default.toString(),
                        countryId.toString(),
                        building_name.toString(),
                        sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0].toString(),
                mLatitude.toString(),
                mLongitude.toString()))
        val call = Utility.apiInterface.addAddress(builder.build())
        call!!.enqueue(object : Callback<MyResponse?> {
            override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        if (response.body()!!.status == 1) {
                            Utility.showSnackBarOnResponseSuccess(mView!!.addressBottomSheetLinearLayout,
                                response.body()!!.message.toString(),
                                requireContext())
                            if (direction.equals("Payment Fragment", true)){
                                sharedPreferenceInstance!!.save(SharedPreferenceUtility.SavedAddress,tv_location.text.toString())
                                findNavController().popBackStack()
                            }else{
                                findNavController().popBackStack()
                            }
                        }
                    } else {
                        Utility.showSnackBarOnResponseError(mView!!.addressBottomSheetLinearLayout,
                            response.message().toString(),
                            requireContext())
                    }
                } else {
                    Utility.showSnackBarOnResponseError(mView!!.addressBottomSheetLinearLayout,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext())
                }
            }

            override fun onFailure(call: Call<MyResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(mView!!.addressBottomSheetLinearLayout,
                    requireContext().getString(R.string.check_internet),
                    requireContext())
            }

        })
    }

    private fun getCountires() {
        if (Utility.isNetworkAvailable()){
            val call = Utility.apiManagerInterface.getCountries(sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""])
            call!!.enqueue(object : Callback<CountryListResponse?> {
                override fun onResponse(call: Call<CountryListResponse?>, response: Response<CountryListResponse?>) {
                    try {
                        if (response.body() != null) {
                            if (response.body()!!.status == 1) {
                                countryList.clear()
                                countryList = response.body()!!.country as ArrayList<Country>
                            } else {
                                Utility.showSnackBarOnResponseError(mView!!.addressBottomSheetLinearLayout,
                                    requireContext().getString(R.string.response_isnt_successful),
                                    requireContext())
                            }
                        } else {
                            Utility.showSnackBarOnResponseError(mView!!.addressBottomSheetLinearLayout,
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

                override fun onFailure(call: Call<CountryListResponse?>, throwable: Throwable) {
                    LogUtils.e("msg", throwable.message)
                    Utility.showSnackBarOnResponseError(mView!!.addressBottomSheetLinearLayout,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext())
                }
            })
        }else{
            Utility.showSnackBarOnResponseError(mView!!.addressBottomSheetLinearLayout,
                requireContext().getString(R.string.check_internet),
                requireContext())
        }

    }


    companion object {
        const val TAG = "AddressBottomSheetFragment"
        private var onAddressCallback : ClickInterface.OnAddressClick?=null
        private var instance: SharedPreferenceUtility? = null
        private var gMap : GoogleMap?=null
        fun newInstance(onAddressCallback: ClickInterface.OnAddressClick): AddressBottomSheetFragment {
            this.onAddressCallback = onAddressCallback
            return AddressBottomSheetFragment()
        }
        @Synchronized
        fun getInstance(): SharedPreferenceUtility {
            if (instance == null) {
                instance = SharedPreferenceUtility()
            }
            return instance as SharedPreferenceUtility
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        Locale.setDefault(Locale.ENGLISH)
        gMap = googleMap
        gMap!!.mapType = GoogleMap.MAP_TYPE_NORMAL
        gMap!!.uiSettings.isCompassEnabled = true
        gMap!!.uiSettings.isMapToolbarEnabled = true
        gMap!!.uiSettings.isMyLocationButtonEnabled = true
        gMap!!.uiSettings.isScrollGesturesEnabled = true
        gMap!!.uiSettings.isScrollGesturesEnabledDuringRotateOrZoom = true
        fetchCurrentLocation()
    }

    private fun fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }else{
            fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->
                lastLocation = location
                val currentLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                mLongitude = lastLocation.longitude
                mLatitude = lastLocation.latitude
                gMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                mView!!.mapView.onResume()
                gMap!!.setOnCameraIdleListener(this)
            }
        }
    }

    override fun onCameraIdle() {
        Log.v("Onmove Idle", "Onmove ")
        val mapLatLng = gMap!!.cameraPosition.target
        setAddress(mapLatLng)
    }

    private fun returnCountryId(selectedCountry: String, countryList: ArrayList<Country>): Int? {
        for (country : Country in countryList) {
            if (country.name.equals(selectedCountry, true)) {
                return country.country_id
            }
        }
        return null
    }

    private fun setAddress(maplatLng: LatLng?) {
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
                    mLatitude = addressList[0].latitude
                    mLongitude = addressList[0].longitude
                    sharedPreferenceInstance!!.save(SharedPreferenceUtility.SavedLat,current_latitude.toString())
                    sharedPreferenceInstance!!.save(SharedPreferenceUtility.SavedLng,current_longitude.toString())
                    Log.e("cureent_lati", current_latitude.toString())
                    Log.e("cureent_longni", current_longitude.toString())
                    strAddress = addressList[0].getAddressLine(0)
//                    flat_villa = addressList[0].getAddressLine(1)
                    Log.e("tesr", "" + strAddress)
//                    Log.e("flat_villa", "" + flat_villa)
                }
                if (addressList[0].locality != null) {
                    flat_villa = addressList[0].featureName
                    building_name = addressList[0].featureName
                    val abc = addressList[0].adminArea + addressList[0].subAdminArea
                    val def = addressList[0].premises
                    val ghi = addressList[0].thoroughfare + addressList[0].subThoroughfare
                    street_area = addressList[0].subLocality +  " " + addressList[0].locality
                    countryName = addressList[0].countryName
                    stateName = addressList[0].adminArea
                   /* countryId = returnCountryId(addressList[0].countryName, countryList)*/
                   /* for (i in 0 until countryList.size){
                        if (countryName!!.equals(countryList[i].name)||countryName!!.equals(countryList[i].name_ar)){
                            countryId = countryList[i].country_id
                            break
                        }
                    }*/
                    Log.e("flat_villa", "" + flat_villa)
                    Log.e("country", "" + addressList[0].countryName)
                    Log.e("street_area", "" + street_area)
                    Log.e("countryId", "" + countryId)
                    Log.e("abc", "" + abc)
                    Log.e("def", "" + def)
                    Log.e("ghi", "" + ghi)
                    strCity = addressList[0].locality + addressList[0].countryCode + addressList[0].countryName
                    val addList = strAddress.split(",".toRegex()).toTypedArray()
//                    val addList = strAddress.split(",".toRegex(), 5)
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
                    if(addressList[0].postalCode!=null) {
                        addressMap["flat_villa"] = flat_villa!!
                        addressMap["street_area"] = street_area!!
                        addressMap["country"] = countryName!!
//                        addressMap["countryId"] = returnCountryId(countryName!!, countryList).toString()
                    }
                    else
                    {
                        addressMap["flat_villa"] = flat_villa!!
                        addressMap["street_area"] = street_area!!
                        addressMap["country"] = countryName!!
                    }
                    tv_location.text = strAddress
                }else{
                    strAddress = ""
                    tv_location.text = strAddress
                }
            }else{
                Log.e("err1", "err1")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}