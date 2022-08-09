package com.heena.user.fragments

import android.Manifest
import android.app.Activity
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.*
import android.view.animation.AlphaAnimation
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.heena.user.Dialogs.LogoutDialog
import com.heena.user.Dialogs.NearbyDialog
import com.heena.user.Dialogs.NoInternetDialog
import com.heena.user.R
import com.heena.user.`interface`.APIInterface
import com.heena.user.`interface`.ClickInterface
import com.heena.user.activities.LoginActivity
import com.heena.user.adapters.*
import com.heena.user.bottomsheetdialog.BookServiceBottomSheetFragment
import com.heena.user.models.*
import com.heena.user.utils.LogUtils
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.SharedPreferenceUtility.Companion.SelectedLang
import com.heena.user.utils.Utility
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AddressComponent
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.heena.user.`interface`.APIInterface.Companion.createBuilder
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.utils.Utility.apiInterface
import com.heena.user.utils.Utility.apiManagerInterface
import com.heena.user.utils.Utility.loginSignUpAccessAlertDialogBox
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.default_listing_recycler_item.view.*
import kotlinx.android.synthetic.main.fragment_book_service_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.grid_services_recycler_items.view.*
import kotlinx.android.synthetic.main.grid_services_recycler_items.view.iv_fav_added
import kotlinx.android.synthetic.main.side_menu_layout.*
import kotlinx.android.synthetic.main.side_top_view.*
import kotlinx.android.synthetic.main.side_top_view.view.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import android.content.Context.JOB_SCHEDULER_SERVICE

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import android.content.ComponentName
import com.heena.user.services.APIServices
import com.heena.user.tasks.HomeApiTasks.Companion.categoryListResponse
import com.heena.user.tasks.HomeApiTasks.Companion.dashboardManagerListingResponse
import com.heena.user.tasks.HomeApiTasks.Companion.userProfileResponse


class HomeFragment : Fragment() {
    var mView: View? = null
    var layout_resource: Int = 0
    lateinit var featuredNaqashatAdapter: FeaturedNaqashatAdapter
    var servicesListingAdapter: ServicesListingAdapter?=null
    var gridServiceListingAdapter: GridServiceListingAdapter?=null
    var lang = ""
    var mLatitude: Double = 0.0
    var currentLatitude: Double = 0.0
    var mLongitude: Double = 0.0
    var currentLongitude: Double = 0.0
    var selected_category: String = ""
    private var managersList = ArrayList<ManagerItem>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var locationManager: LocationManager? = null
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var countryName: String = ""
    var servicesList = ArrayList<ServiceItem>()
    var service_id: String = ""
    var service = ""
    var mylocation = ""
    var search_keyword = ""
    var f_price = 0
    var categoryList = ArrayList<CategoryItem>()
    var categoryNames = ArrayList<String>()
    lateinit var serviceListingAdapter: ServiceListingAdapter
    private var filter_opt_clicked = false
    private var service_layout_clicked = false
    var myClick = ""
    var profilePicture = ""
    var profilePicture1 = ""
    var profileUsername = ""

    var isGuestLogin = false
    var searching = false
    val requestOption = RequestOptions().centerCrop()
    private var layout_clicked = 0
    private lateinit var jobScheduler: JobScheduler
    private val appPerms = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private var paramsQueryMap = HashMap<String, String>()
    private var activityResultLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            var allAreGranted = true
            for (b in result.values) {
                allAreGranted = allAreGranted && b
            }

            if (allAreGranted) {
                Log.e("Granted", "Permissions")
                getCurrentLocation()
            } else {
                mView!!.ll_no_service_found_home.visibility = View.VISIBLE
                mView!!.rv_default_listing.visibility = View.GONE
                Utility.showSnackBarValidationError(
                    mView!!.homeFragmentConstraintlayout,
                    requireContext().getString(R.string.please_allow_permissions),
                    requireContext()
                )

            }
        }

    private var placeAPIresultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val place = Autocomplete.getPlaceFromIntent(data!!)
                val latlng = place.latLng
                latitude = latlng!!.latitude
                longitude = latlng.longitude
                Log.e("latitude1", latitude.toString())
                Log.e("longitude1", longitude.toString())
                val addressComponents: MutableList<AddressComponent> =
                    place.addressComponents!!.asList()
                var ard = 0
                var add = ""
                for (addressComponent in addressComponents) {
                    Log.e("addressss--", addressComponent.name)
                    countryName = addressComponent.name
                    if (ard == 0) {
                        add = addressComponent.name
                    }

                    var flag: Boolean = false
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
                        val center = LatLng(latitude, longitude)
                        val locality = getLocality(center)
                        val countryName = getCountry(center)

                        var address = ""

                        if (!add.isEmpty()) {
                            address = add
                        }
                        if (!locality.isEmpty() && !address.isEmpty() && !address.equals(locality)) {
                            address = address + ", " + locality
                        }
                        if (!addressComponent.name.isEmpty() && !address.isEmpty() && !address.equals(
                                addressComponent.name
                            )
                        ) {
                            address = address + ", " + addressComponent.name
                        }
                        if (!countryName.isEmpty() && !address.isEmpty()) {
                            address = address + ", " + countryName
                        }
                        mView!!.et_location.setText(address)
                        break
                    }
                    ard++
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_home, container, false)
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SelectedLang, "")
        )
        locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        activityResultLauncher.launch(appPerms)
        mView!!.iv_filter.setOnClickListener {
            if (!filter_opt_clicked) {
                filter_opt_clicked = true
                mView!!.filterlayout.visibility = View.VISIBLE
            } else {
                filter_opt_clicked = false
                mView!!.filterlayout.visibility = View.GONE
            }
        }
        setUpViews()

        return mView
    }

    private fun getFilteredServicesList() {
        service = mView!!.tv_services_filter.text.toString().trim()
        mylocation = mView!!.et_location.text.toString().trim()
        Log.e("mylocation: ", "" + mylocation)
        Log.e("service: ", "" + service)

        if (service.equals("") || service.equals(null)) {
            mView!!.tv_filtered_services.visibility = View.GONE
        } else {
            mView!!.tv_filtered_services.visibility = View.VISIBLE
        }

        if (f_price == 0) {
            mView!!.tv_price_range.text = getString(R.string.low_high)
        } else {
            mView!!.tv_price_range.text = getString(R.string.high_low)
        }
        if (mylocation.equals("") || mylocation.equals("null")) {
            mLatitude = latitude
            mLongitude = longitude
        } else {
            mLatitude = latitude
            mLongitude = longitude
        }
        getAllServicesListing(mLatitude, mLongitude, f_price, service_id, "", 1, "50")
    }


    private fun setUpViews() {
        mView!!.cards_service_categories_listing.visibility = View.GONE
        instance = sharedPreferenceInstance!!
        lang = instance!!.get(SelectedLang, "").toString()
        isGuestLogin = instance!!.get(SharedPreferenceUtility.IsGuestUserLogin, false)
        Log.e("User Id", instance!!.get(SharedPreferenceUtility.UserId, 0).toString())
        Utility.setLanguage(requireContext(), lang)
        jobScheduler = requireContext()
            .getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler

        val componentName = ComponentName(requireContext(), APIServices::class.java)
        val jobInfo = JobInfo.Builder(1, componentName)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPersisted(true)
            .build()
        jobScheduler.schedule(jobInfo)


        if (isGuestLogin) {
            requireActivity().llLogout.visibility = View.GONE
            requireActivity().helpPageDivider.visibility = View.GONE
            requireActivity().logOutDivider.visibility = View.GONE
            requireActivity().iv_notification.visibility = View.GONE
            requireActivity().tv_name.text = requireActivity().getString(R.string.guest_user)
            requireActivity().userIcon.borderColor = context!!.getResources().getColor(R.color.black)
            requireActivity().userIcon.borderWidth = 3
            Glide.with(requireContext()).load(R.drawable.dark_logo).into(requireActivity().userIcon)
            Glide.with(requireContext()).load(R.drawable.golden_logo).into(requireActivity().img)
            if (!Utility.hasConnection(requireContext())) {
                val noInternetDialog = NoInternetDialog()
                noInternetDialog.isCancelable = false
                noInternetDialog.setRetryCallback(object : NoInternetDialog.RetryInterface {
                    override fun retry() {
                        noInternetDialog.dismiss()
                        getHomes()
                    }

                })
                noInternetDialog.show(requireActivity().supportFragmentManager, "Home Fragment")
            } else {
                getHomes()
            }
        } else {
            requireActivity().llLogout.visibility = View.VISIBLE
            requireActivity().helpPageDivider.visibility = View.VISIBLE
            requireActivity().logOutDivider.visibility = View.VISIBLE
            requireActivity().iv_notification.visibility = View.VISIBLE
            if (!Utility.hasConnection(requireContext())) {
                val noInternetDialog = NoInternetDialog()
                noInternetDialog.isCancelable = false
                noInternetDialog.setRetryCallback(object : NoInternetDialog.RetryInterface {
                    override fun retry() {
                        noInternetDialog.dismiss()
                        getProfile()
                        getHomes()
                    }

                })
                noInternetDialog.show(requireActivity().supportFragmentManager, "Home Fragment")
            } else {
                getProfile()
                getHomes()
            }
        }

        clickOnDrawer()
        setBottomView()


        mView!!.filtered_result_layout.visibility = View.GONE
        requireActivity().itemHome.setImageResource(R.drawable.home_icon_active)

        mView!!.radioGroup.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
            override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
                f_price = when {
                    mView!!.rb_low_to_high.isChecked -> {
                        0
                    }
                    mView!!.rb_high_to_low.isChecked -> {
                        1
                    }
                    else -> {
                        0
                    }
                }
            }
        })

        mView!!.tv_apply_filter.setSafeOnClickListener {
            mView!!.filterlayout.visibility = View.GONE
            mView!!.filtered_result_layout.visibility = View.VISIBLE
            layout_resource = R.layout.filtered_results_listing_item
            getFilteredServicesList()
        }

        mView!!.tv_reset_filter.setSafeOnClickListener {
            mView!!.tv_services_filter.text = ""
            mView!!.et_location.text = ""
            f_price = 0
            service_id = ""
            search_keyword = ""
            mView!!.filterlayout.visibility = View.GONE
            mView!!.filtered_result_layout.visibility = View.GONE
            getAllServicesListing(
                currentLatitude,
                longitude = currentLongitude,
                0,
                "",
                "",
                0,
                "50"
            )
        }
        setOnClickBottomItemView()
        setByDefault()

        requireActivity().iv_notification.setSafeOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F, 0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(
                requireContext(),
                requireActivity().iv_notification
            )
            findNavController().navigate(R.id.notificationsFragment)
        }

        mView!!.tv_view_all.setSafeOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_allManagerListFragment)
        }

        mView!!.locationlayout.setSafeOnClickListener {
            val fields: MutableList<Place.Field> = java.util.ArrayList()
            fields.add(Place.Field.NAME)
            fields.add(Place.Field.ID)
            fields.add(Place.Field.LAT_LNG)
            fields.add(Place.Field.ADDRESS)
            fields.add(Place.Field.ADDRESS_COMPONENTS)
            val intent =
                Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(
                    requireContext()
                )
            placeAPIresultLauncher.launch(intent)
        }

        mView!!.serviceslayout.setSafeOnClickListener {
            if (!service_layout_clicked) {
                service_layout_clicked = true
                mView!!.cards_service_categories_listing.visibility = View.VISIBLE
            } else {
                service_layout_clicked = false
                mView!!.cards_service_categories_listing.visibility = View.GONE
            }
        }

        mView!!.et_search_keyword.setOnEditorActionListener(object :
            TextView.OnEditorActionListener {
            override fun onEditorAction(
                textView: TextView?,
                actionId: Int,
                keyEvent: KeyEvent?
            ): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search_keyword = mView!!.et_search_keyword.text.toString()
                    sharedPreferenceInstance!!.hideSoftKeyBoard(
                        requireContext(),
                        mView!!.et_search_keyword
                    )
                    searching = true
                    getAllServicesListing(
                        latitude,
                        longitude,
                        0,
                        "",
                        search_keyword,
                        1,
                        "50"
                    )
                    return true
                }
                return false
            }
        })

        mView!!.iv_grid_view.setSafeOnClickListener {
            mView!!.iv_grid_view.setImageResource(R.drawable.grid_selected)
            mView!!.iv_linear_view.setImageResource(R.drawable.linear_unselected)
            layout_clicked = 1
            gridViewLayout()
        }

        mView!!.iv_linear_view.setSafeOnClickListener {
            mView!!.iv_grid_view.setImageResource(R.drawable.grid_unselected)
            mView!!.iv_linear_view.setImageResource(R.drawable.linear_selected)
            layout_clicked = 2
            linearViewLayout()
        }

        mView!!.tv_filtered_locations.setSafeOnClickListener {
            val nearbyDialog = NearbyDialog()
            nearbyDialog.isCancelable = false
            nearbyDialog.setDataCompletionCallback(object : NearbyDialog.NearByInterface {
                override fun nearby(progressChangedValue: Int) {
                    myClick = "NearBy"
                    getAllServicesListing(
                        currentLatitude,
                        currentLongitude,
                        0,
                        "",
                        "",
                        1,
                        progressChangedValue.toString()
                    )
                }

            })
            nearbyDialog.show(requireActivity().supportFragmentManager, "HomeFragment")
        }
    }

    private fun getHomes() {
        getCategories()
        getDashboardManagerList()
    }

    private fun setProfile() {
        if (userProfileResponse.status == 1) {
            profileUsername =
                if (userProfileResponse.profile!!.name.toString().equals("")) {
                    userProfileResponse.profile!!.username.toString()
                } else {
                    userProfileResponse.profile!!.name.toString()
                }
            Glide.with(requireContext()).load(profilePicture)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        p0: GlideException?,
                        p1: Any?,
                        p2: com.bumptech.glide.request.target.Target<Drawable>?,
                        p3: Boolean
                    ): Boolean {
                        Log.e("err", p0?.message.toString())
                        requireActivity().headerView.user_icon_progress_bar_side_top_view.visibility =
                            View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        p0: Drawable?,
                        p1: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        dataSource: com.bumptech.glide.load.DataSource?,
                        p4: Boolean
                    ): Boolean {
                        requireActivity().headerView.user_icon_progress_bar_side_top_view.visibility =
                            View.GONE
                        return false
                    }
                }).placeholder(R.drawable.user)
                .apply(requestOption).into(requireActivity().headerView.userIcon)

            Glide.with(requireContext()).load(profilePicture)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        p0: GlideException?,
                        p1: Any?,
                        p2: com.bumptech.glide.request.target.Target<Drawable>?,
                        p3: Boolean
                    ): Boolean {
                        Log.e("err", p0?.message.toString())
                        return false
                    }

                    override fun onResourceReady(
                        p0: Drawable?,
                        p1: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        dataSource: com.bumptech.glide.load.DataSource?,
                        p4: Boolean
                    ): Boolean {
                        return false
                    }
                }).placeholder(R.drawable.user)
                .apply(requestOption).into(requireActivity().img)
            requireActivity().headerView.tv_name.text = profileUsername
        } else {
            Utility.showSnackBarOnResponseError(
                mView!!.homeFragmentConstraintlayout,
                userProfileResponse.message.toString(),
                requireContext()
            )
        }
    }

    private fun getProfile() {
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        mView!!.fragment_home_progressBar.visibility = View.VISIBLE
        val call = apiInterface.getLoggedInUserProfile(
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.UserId, 0),
            sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]
        )
        call?.enqueue(object : Callback<UserProfileResponse?> {
            override fun onResponse(
                call: Call<UserProfileResponse?>,
                response: Response<UserProfileResponse?>
            ) {
                mView!!.fragment_home_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful) {
                    if (response.body()!!.status == 1) {
                        if (response.body()!!.profile!!.image.toString().contains("default", false)) {
                            Glide.with(requireContext()).load(R.drawable.dark_logo)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .listener(object : RequestListener<Drawable> {
                                    override fun onLoadFailed(
                                        p0: GlideException?,
                                        p1: Any?,
                                        p2: com.bumptech.glide.request.target.Target<Drawable>?,
                                        p3: Boolean
                                    ): Boolean {
                                        Log.e("err", p0?.message.toString())
                                        requireActivity().headerView.user_icon_progress_bar_side_top_view.visibility =
                                            View.GONE
                                        return false
                                    }

                                    override fun onResourceReady(
                                        p0: Drawable?,
                                        p1: Any?,
                                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                                        dataSource: com.bumptech.glide.load.DataSource?,
                                        p4: Boolean
                                    ): Boolean {
                                        requireActivity().headerView.user_icon_progress_bar_side_top_view.visibility =
                                            View.GONE
                                        return false
                                    }
                                }).placeholder(R.drawable.user)
                                .apply(requestOption).into(requireActivity().headerView.userIcon)
                            requireActivity().userIcon.borderWidth = 5
                        } else {
                            Glide.with(requireContext()).load(response.body()!!.profile!!.image.toString())
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .listener(object : RequestListener<Drawable> {
                                    override fun onLoadFailed(
                                        p0: GlideException?,
                                        p1: Any?,
                                        p2: com.bumptech.glide.request.target.Target<Drawable>?,
                                        p3: Boolean
                                    ): Boolean {
                                        Log.e("err", p0?.message.toString())
                                        requireActivity().headerView.user_icon_progress_bar_side_top_view.visibility =
                                            View.GONE
                                        return false
                                    }

                                    override fun onResourceReady(
                                        p0: Drawable?,
                                        p1: Any?,
                                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                                        dataSource: com.bumptech.glide.load.DataSource?,
                                        p4: Boolean
                                    ): Boolean {
                                        requireActivity().headerView.user_icon_progress_bar_side_top_view.visibility =
                                            View.GONE
                                        return false
                                    }
                                }).placeholder(R.drawable.user)
                                .apply(requestOption).into(requireActivity().headerView.userIcon)
                        }

                        if (response.body()!!.profile!!.image.toString().contains("default", false)) {
                            Glide.with(requireContext()).load(R.drawable.golden_logo)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .listener(object : RequestListener<Drawable> {
                                    override fun onLoadFailed(
                                        p0: GlideException?,
                                        p1: Any?,
                                        p2: com.bumptech.glide.request.target.Target<Drawable>?,
                                        p3: Boolean
                                    ): Boolean {
                                        Log.e("err", p0?.message.toString())
                                        requireActivity().headerView.user_icon_progress_bar_side_top_view.visibility =
                                            View.GONE
                                        return false
                                    }

                                    override fun onResourceReady(
                                        p0: Drawable?,
                                        p1: Any?,
                                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                                        dataSource: com.bumptech.glide.load.DataSource?,
                                        p4: Boolean
                                    ): Boolean {
                                        requireActivity().headerView.user_icon_progress_bar_side_top_view.visibility =
                                            View.GONE
                                        return false
                                    }
                                }).placeholder(R.drawable.user)
                                .apply(requestOption).into(requireActivity().img)
                        } else {
                            Glide.with(requireContext()).load(response.body()!!.profile!!.image.toString())
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .listener(object : RequestListener<Drawable> {
                                    override fun onLoadFailed(
                                        p0: GlideException?,
                                        p1: Any?,
                                        p2: com.bumptech.glide.request.target.Target<Drawable>?,
                                        p3: Boolean
                                    ): Boolean {
                                        Log.e("err", p0?.message.toString())
                                        requireActivity().headerView.user_icon_progress_bar_side_top_view.visibility =
                                            View.GONE
                                        return false
                                    }

                                    override fun onResourceReady(
                                        p0: Drawable?,
                                        p1: Any?,
                                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                                        dataSource: com.bumptech.glide.load.DataSource?,
                                        p4: Boolean
                                    ): Boolean {
                                        requireActivity().headerView.user_icon_progress_bar_side_top_view.visibility =
                                            View.GONE
                                        return false
                                    }
                                }).placeholder(R.drawable.user)
                                .apply(requestOption).into(requireActivity().img)
                        }

                        profileUsername =
                            if (response.body()!!.profile!!.name.toString().equals("")) {
                                response.body()!!.profile!!.username.toString()
                            } else {
                                response.body()!!.profile!!.name.toString()
                            }

                        requireActivity().headerView.tv_name.text = profileUsername
                    } else {
                        Utility.showSnackBarOnResponseError(
                            mView!!.homeFragmentConstraintlayout,
                            response.body()!!.message.toString(),
                            requireContext()
                        )
                    }
                } else {
                    Utility.showSnackBarOnResponseError(
                        mView!!.homeFragmentConstraintlayout,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext()
                    )
                }
            }

            override fun onFailure(call: Call<UserProfileResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(
                    mView!!.homeFragmentConstraintlayout,
                    requireContext().getString(R.string.response_isnt_successful),
                    requireContext()
                )
                mView!!.fragment_home_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        })
    }

    private fun getAllServicesListing(
        latitude: Double,
        longitude: Double,
        f_price: Int,
        category_id: String,
        search_keyword: String,
        searching_status: Int,
        radius: String
    ) {
        mView!!.fragment_home_progressBar.visibility = View.VISIBLE
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        mView!!.ll_no_service_found_home.visibility = View.GONE
        paramsQueryMap = HashMap<String, String>()
        paramsQueryMap["lang"] =
            sharedPreferenceInstance!![SelectedLang, ""]
        paramsQueryMap["lat"] = latitude.toString()
        paramsQueryMap["long"] = longitude.toString()
        paramsQueryMap["category_id"] = category_id.toString()
        paramsQueryMap["radius"] = radius
        paramsQueryMap["f_price"] = f_price.toString()
        paramsQueryMap["search"] = search_keyword

        var call : Call<ServicesListingResponse?>?=null

        if(isGuestLogin){
            call = apiInterface.servicesGuestListing(
                paramsQueryMap
            )
        }else{
            call = apiInterface.servicesListing(
                sharedPreferenceInstance!!.get(
                    SharedPreferenceUtility.UserId,
                    0
                ),
                paramsQueryMap
            )
        }

        call?.enqueue(object : Callback<ServicesListingResponse?> {
            override fun onResponse(
                call: Call<ServicesListingResponse?>,
                response: Response<ServicesListingResponse?>
            ) {
                mView!!.fragment_home_progressBar.visibility = View.GONE
                mView!!.ll_no_service_found_home.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        if (response.body()!!.status != 0) {
                            servicesList.clear()
                            servicesList = response.body()!!.service as ArrayList<ServiceItem>
                            Log.e("size", servicesList.size.toString())
                            if (servicesList.size == 0) {
                                mView!!.ll_no_service_found_home.visibility = View.VISIBLE
                                mView!!.rv_default_listing.visibility = View.GONE
                            } else {
                                mView!!.ll_no_service_found_home.visibility = View.GONE
                                mView!!.rv_default_listing.visibility = View.VISIBLE
                                if (searching) {
                                    searching = false
                                    if (searching_status == 0) {
                                        mView!!.filtered_result_layout.visibility = View.GONE
                                    } else {
                                        mView!!.filtered_result_layout.visibility = View.VISIBLE
                                        mView!!.tv_price_range.text = search_keyword

                                        if (myClick.equals("NearBy")) {
                                            mView!!.ll_service_price_range.visibility = View.GONE
                                        } else {
                                            mView!!.ll_service_price_range.visibility = View.VISIBLE
                                            if (category_id.equals("") || service.equals("")) {
                                                mView!!.tv_filtered_services.visibility = View.GONE
                                            } else {
                                                mView!!.tv_filtered_services.visibility =
                                                    View.VISIBLE
                                                mView!!.tv_filtered_services.text =
                                                    selected_category
                                            }
                                        }
                                    }
                                } else {
                                    if (searching_status == 0) {
                                        mView!!.filtered_result_layout.visibility = View.GONE
                                    } else {
                                        mView!!.filtered_result_layout.visibility = View.VISIBLE
                                        if (f_price == 0) {
                                            mView!!.tv_price_range.text =
                                                getString(R.string.low_high)
                                        } else {
                                            mView!!.tv_price_range.text =
                                                getString(R.string.high_low)
                                        }
                                        if (myClick.equals("NearBy")) {
                                            mView!!.ll_service_price_range.visibility = View.GONE
                                        } else {
                                            mView!!.ll_service_price_range.visibility = View.VISIBLE
                                            if (category_id.equals("") || service.equals("")) {
                                                mView!!.tv_filtered_services.visibility = View.GONE
                                            } else {
                                                mView!!.tv_filtered_services.visibility =
                                                    View.VISIBLE
                                                mView!!.tv_filtered_services.text =
                                                    selected_category
                                            }
                                        }
                                    }
                                }
                                setServicesAdapter()
                            }
                        } else {
                            mView!!.ll_no_service_found_home.visibility = View.VISIBLE
                            mView!!.rv_default_listing.visibility = View.GONE
                            mView!!.filtered_result_layout.visibility = View.GONE
                        }
                    }
                } else {
                    Utility.showSnackBarOnResponseError(
                        mView!!.homeFragmentConstraintlayout,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext()
                    )
                }
            }

            override fun onFailure(call: Call<ServicesListingResponse?>, throwable: Throwable) {
                mView!!.fragment_home_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                Utility.showSnackBarOnResponseError(
                    mView!!.homeFragmentConstraintlayout,
                    requireContext().getString(R.string.response_isnt_successful),
                    requireContext()
                )
            }

        })
    }

    private fun returnServiceId(
        selectedCategory: String,
        servicesList: ArrayList<CategoryItem>
    ): Int? {
        for (service: CategoryItem in servicesList) {
            if (service.name.equals(selectedCategory)) {
                return service.category_id
            }
        }
        return null
    }

    private fun setCategories(){
        if (categoryListResponse.status == 1) {
            categoryList.clear()
            categoryList = categoryListResponse.category as ArrayList<CategoryItem>
            for (i in 0 until categoryList.size) {
                categoryNames.add(categoryList.get(i).name.toString())
            }
            mView!!.rv_services_list.layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                false
            )
            serviceListingAdapter = ServiceListingAdapter(
                requireContext(),
                categoryNames,
                object : ClickInterface.OnRecyclerItemClick {
                    override fun OnClickAction(position: Int) {
                        mView!!.tv_services_filter.text = categoryNames[position]
                        selected_category =
                            tv_services_filter.text.toString().trim()
                        mView!!.cards_service_categories_listing.visibility =
                            View.GONE
                        service_id = returnServiceId(
                            selected_category,
                            categoryList
                        ).toString()
                        Log.e("service_id", service_id.toString())
                    }
                })
            mView!!.rv_services_list.adapter = serviceListingAdapter
        } else {
            LogUtils.longToast(requireContext(), categoryListResponse.message)
        }
    }

    private fun getCategories() {
        val call =
            apiManagerInterface.categoryList(sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""])
        call!!.enqueue(object : Callback<CategoryListResponse?> {
            override fun onResponse(
                call: Call<CategoryListResponse?>,
                response: Response<CategoryListResponse?>
            ) {
                try {
                    if (response.isSuccessful) {
                        if (response.body()!!.status == 1) {
                            categoryList.clear()
                            categoryList = response.body()!!.category as ArrayList<CategoryItem>
                            for (i in 0 until categoryList.size) {
                                categoryNames.add(categoryList.get(i).name.toString())
                            }
                            mView!!.rv_services_list.layoutManager = LinearLayoutManager(
                                requireContext(),
                                LinearLayoutManager.VERTICAL,
                                false
                            )
                            serviceListingAdapter = ServiceListingAdapter(
                                requireContext(),
                                categoryNames,
                                object : ClickInterface.OnRecyclerItemClick {
                                    override fun OnClickAction(position: Int) {
                                        mView!!.tv_services_filter.text = categoryNames[position]
                                        selected_category =
                                            tv_services_filter.text.toString().trim()
                                        mView!!.cards_service_categories_listing.visibility =
                                            View.GONE
                                        service_id = returnServiceId(
                                            selected_category,
                                            categoryList
                                        ).toString()
                                        Log.e("service_id", service_id.toString())
                                    }
                                })
                            mView!!.rv_services_list.adapter = serviceListingAdapter
                        } else {
                            LogUtils.longToast(requireContext(), response.body()!!.message)
                        }
                    } else {
                        Utility.showSnackBarOnResponseError(
                            mView!!.homeFragmentConstraintlayout,
                            requireContext().getString(R.string.response_isnt_successful),
                            requireContext()
                        )
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<CategoryListResponse?>, throwable: Throwable) {
                Utility.showSnackBarOnResponseError(
                    mView!!.homeFragmentConstraintlayout,
                    requireContext().getString(R.string.response_isnt_successful),
                    requireContext()
                )
            }
        })
    }

    private fun setServicesAdapter() {
        if (layout_clicked == 1) {
            gridViewLayout()
        } else {
            linearViewLayout()
        }
    }

    fun linearViewLayout() {
        mView!!.rv_default_listing.layoutManager = GridLayoutManager(
            requireContext(),
            1
        )
        servicesListingAdapter = ServicesListingAdapter(requireContext(), servicesList,
            object : ClickInterface.OnServicesItemClick {
                override fun OnClickAction(position: Int) {
                    val serviceItem = servicesList[position]
                    val bundle = Bundle()
                    bundle.putSerializable("service", serviceItem)
                    val bookServiceBottomSheetFragment = BookServiceBottomSheetFragment.newInstance(
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
                            if (isGuestLogin) {
                                loginSignUpAccessAlertDialogBox(requireContext())
                            } else {
                                val bundle = Bundle()
                                bundle.putSerializable("service", serviceItem)
                                findNavController().navigate(
                                    R.id.action_homeFragment_to_servicvePaymentFragment,
                                    bundle
                                )
                            }
                        }

                        override fun onLikeAction(position: Int, likeStatus: Boolean,  itemView: View) {
                            likeService(itemView, position, servicesListingAdapter, null)
                        }

                        override fun onDislikeAction(position: Int, unLikeStatus: Boolean,  itemView: View) {
                            disLikeService(itemView, position, servicesListingAdapter, null)
                        }
                    })
                    serviceListingAdapter.notifyDataSetChanged()
                }

                override fun onLikeAction(position: Int, itemView: View) {
                    likeService(itemView, position, servicesListingAdapter, null)
                }

                override fun onDislikeAction(position: Int, itemView: View) {
                    disLikeService(itemView, position, servicesListingAdapter, null)
                }

                override fun onProfileClick(position: Int) {
                    if (isGuestLogin) {
                        loginSignUpAccessAlertDialogBox(requireContext())
                    } else {
                        val serviceItem = servicesList[position]
                        val bundle = Bundle()
                        bundle.putString("managerName", serviceItem.user!!.name)
                        bundle.putInt("managerId", serviceItem.user.user_id!!)
                        findNavController().navigate(
                            R.id.action_homeFragment_to_featuredNaqashatProfileFragment,
                            bundle
                        )
                    }
                }

            })

        mView!!.rv_default_listing.adapter = servicesListingAdapter
    }

    private fun gridViewLayout() {
        mView!!.rv_default_listing.layoutManager = GridLayoutManager(
            requireContext(),
            2
        )
        gridServiceListingAdapter = GridServiceListingAdapter(requireContext(), servicesList,
            object : ClickInterface.OnServicesItemClick {
                override fun OnClickAction(position: Int) {
                    val serviceItem = servicesList[position]
                    val bundle = Bundle()
                    bundle.putSerializable("service", serviceItem)
                    val bookServiceBottomSheetFragment =
                        BookServiceBottomSheetFragment.newInstance(
                            bundle, position
                        )
                    bookServiceBottomSheetFragment.show(
                        requireActivity().supportFragmentManager,
                        BookServiceBottomSheetFragment.TAG
                    )
                    bookServiceBottomSheetFragment.setSubscribeClickListenerCallback(object :
                        ClickInterface.OnBookServiceClick {
                        override fun OnBookService() {
                            if (isGuestLogin) {
                                loginSignUpAccessAlertDialogBox(requireContext())
                            }else{
                                val bundle = Bundle()
                                bundle.putSerializable("service", serviceItem)
                                findNavController().navigate(
                                    R.id.action_homeFragment_to_servicvePaymentFragment,
                                    bundle
                                )
                            }

                        }

                        override fun onLikeAction(position: Int, likeStatus: Boolean,  itemView: View) {
                            likeService(itemView, position, null, gridServiceListingAdapter)
                        }

                        override fun onDislikeAction(position: Int, unLikeStatus: Boolean, itemView: View) {
                            disLikeService(itemView, position, null, gridServiceListingAdapter)
                        }

                    })
                }

                override fun onLikeAction(position: Int, itemView: View) {
                    likeService(itemView, position, null, gridServiceListingAdapter)
                }

                override fun onDislikeAction(position: Int, itemView: View) {
                    disLikeService(itemView, position, null, gridServiceListingAdapter)
                }

                override fun onProfileClick(position: Int) {
                    val serviceItem = servicesList[position]
                    val bundle = Bundle()
                    bundle.putString("managerName", serviceItem.user!!.name)
                    bundle.putInt("managerId", serviceItem.user.user_id!!)
                    findNavController().navigate(
                        R.id.action_homeFragment_to_featuredNaqashatProfileFragment,
                        bundle
                    )
                }

            })
        mView!!.rv_default_listing.adapter = gridServiceListingAdapter
        gridServiceListingAdapter!!.notifyDataSetChanged()
    }

    private fun setDashboardManagerList(){
        if (dashboardManagerListingResponse != null) {
            if (dashboardManagerListingResponse.status == 1) {
                managersList.clear()
                managersList = dashboardManagerListingResponse.manager as ArrayList<ManagerItem>
                mView!!.ll_no_manager_found_home.visibility = View.GONE
                mView!!.rv_featured_naqashat.visibility = View.VISIBLE
                setFeaturedNaqashatAdapter(managersList)
            } else {
                mView!!.ll_no_manager_found_home.visibility = View.VISIBLE
                mView!!.rv_featured_naqashat.visibility = View.GONE
            }
        }
    }

    private fun getDashboardManagerList() {
        mView!!.fragment_home_progressBar.visibility = View.VISIBLE
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        val call = apiInterface.dashboardManagersListing(
            sharedPreferenceInstance!!.get(
                SelectedLang,
                ""
            )
        )
        call?.enqueue(object : Callback<ManagerListingResponse?> {
            override fun onResponse(
                call: Call<ManagerListingResponse?>,
                response: Response<ManagerListingResponse?>
            ) {
                mView!!.fragment_home_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        if (response.body()!!.status == 1) {
                            managersList.clear()
                            managersList = response.body()!!.manager as ArrayList<ManagerItem>
                            mView!!.ll_no_manager_found_home.visibility = View.GONE
                            mView!!.rv_featured_naqashat.visibility = View.VISIBLE
                            setFeaturedNaqashatAdapter(managersList)
                        } else {
                            mView!!.ll_no_manager_found_home.visibility = View.VISIBLE
                            mView!!.rv_featured_naqashat.visibility = View.GONE
                        }
                    }
                } else {
                    Utility.showSnackBarOnResponseError(
                        mView!!.homeFragmentConstraintlayout,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext()
                    )
                }
            }

            override fun onFailure(call: Call<ManagerListingResponse?>, throwable: Throwable) {
                Utility.showSnackBarOnResponseError(
                    mView!!.homeFragmentConstraintlayout,
                    requireContext().getString(R.string.response_isnt_successful),
                    requireContext()
                )
                mView!!.fragment_home_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        })
    }

    private fun setByDefault() {
        layout_clicked = 1
        mView!!.filterlayout.visibility = View.GONE
        mView!!.filtered_result_layout.visibility = View.GONE
        mView!!.rv_default_listing.layoutManager = GridLayoutManager(
            requireContext(),
            1
        )
        layout_resource = R.layout.default_listing_recycler_item
    }

    private fun setFeaturedNaqashatAdapter(managersList: ArrayList<ManagerItem>) {
        mView!!.rv_featured_naqashat.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )

        featuredNaqashatAdapter = FeaturedNaqashatAdapter(
            requireContext(),
            managersList,
            object : ClickInterface.OnRecyclerItemClick {
                override fun OnClickAction(position: Int) {
                    if (isGuestLogin) {
                        loginSignUpAccessAlertDialogBox(requireContext())
                    } else {
                        val manager_id = managersList[position].mangaer_id
                        sharedPreferenceInstance!!.save(
                            SharedPreferenceUtility.ManagerId,
                            manager_id
                        )
                        val bundle = Bundle()
                        bundle.putString("managerName", managersList[position].name)
                        managersList[position].mangaer_id?.let { bundle.putInt("managerId", it) }
                        findNavController().navigate(
                            R.id.action_homeFragment_to_featuredNaqashatProfileFragment,
                            bundle
                        )
                    }
                }

            })

        mView!!.rv_featured_naqashat.adapter = featuredNaqashatAdapter
    }

    private fun openCloseDrawer() {
        if (requireActivity().drawerLayout.isDrawerOpen(GravityCompat.START)) {
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            requireActivity().drawerLayout.openDrawer(GravityCompat.START)
        }
    }


    private fun setOnClickBottomItemView() {
        requireActivity().itemAppointment.setOnClickListener {
            if (findNavController().currentDestination?.id != R.id.appointmentFragment) {
                requireActivity().itemAppointment.startAnimation(AlphaAnimation(1f, 0.5f))
                setBottomView()
                if (isGuestLogin) {
                    loginSignUpAccessAlertDialogBox(requireContext())
                } else {
                    requireActivity().itemAppointment.setImageResource(R.drawable.appointment_active)
                    findNavController().navigate(R.id.appointmentFragment)
                }
            }

        }
        requireActivity().itemFavourites.setOnClickListener {
            if (findNavController().currentDestination?.id != R.id.favouritesFragment) {
                requireActivity().itemFavourites.startAnimation(AlphaAnimation(1f, 0.5f))
                setBottomView()
                if (isGuestLogin) {
                    loginSignUpAccessAlertDialogBox(requireContext())
                } else {
                    requireActivity().itemFavourites.setImageResource(R.drawable.favourites_icon_active)
                    findNavController().navigate(R.id.favouritesFragment)
                }
            }

        }
        requireActivity().itemHome.setOnClickListener {
            if (findNavController().currentDestination?.id != R.id.homeFragment) {
                requireActivity().itemHome.startAnimation(AlphaAnimation(1f, 0.5f))
                setBottomView()
                requireActivity().itemHome.setImageResource(R.drawable.home_icon_active)
                findNavController().navigate(R.id.homeFragment)
            }
        }

        requireActivity().itemCategories.setOnClickListener {
            requireActivity().itemCategories.startAnimation(AlphaAnimation(1f, 0.5f))
            openCloseDrawer()
        }

        requireActivity().itemSettings.setOnClickListener {
            if (findNavController().currentDestination?.id != R.id.settingsFragment) {
                requireActivity().itemSettings.startAnimation(AlphaAnimation(1f, 0.5f))
                setBottomView()
                if (isGuestLogin) {
                    loginSignUpAccessAlertDialogBox(requireContext())
                } else {
                    requireActivity().itemSettings.setImageResource(R.drawable.settings_active)
                    findNavController().navigate(R.id.settingsFragment)
                }
            }
        }
    }

    private fun setBottomView() {
        requireActivity().itemCategories.setImageResource(R.drawable.symbols_inactive)
        requireActivity().itemAppointment.setImageResource(R.drawable.appointment_icon_inactive)
        requireActivity().itemHome.setImageResource(R.drawable.icon_home_inactive)
        requireActivity().itemFavourites.setImageResource(R.drawable.favourites_icon_inactive)
        requireActivity().itemSettings.setImageResource(R.drawable.settings_icon_inactive)
    }


    private fun clickOnDrawer() {
        requireActivity().llAccount.setOnClickListener {
            requireActivity().llAccount.startAnimation(AlphaAnimation(1f, 0.5f))
            if (isGuestLogin) {
                loginSignUpAccessAlertDialogBox(requireContext())
            } else {
                requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
                findNavController().navigate(R.id.userProfileFragment)
            }
        }

        requireActivity().llNotifications.setOnClickListener {
            requireActivity().llNotifications.startAnimation(AlphaAnimation(1f, 0.5f))
            if (isGuestLogin) {
                loginSignUpAccessAlertDialogBox(requireContext())
            } else {
                requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
                findNavController().navigate(R.id.notificationsFragment)
            }
        }

        requireActivity().llMyOffers.setOnClickListener {
            requireActivity().llMyOffers.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            findNavController().navigate(R.id.filteredoffersAndDiscountsFragment)
        }

        requireActivity().llSettings.setOnClickListener {
            requireActivity().llSettings.startAnimation(AlphaAnimation(1f, 0.5f))
            if (isGuestLogin) {
                loginSignUpAccessAlertDialogBox(requireContext())
            } else {
                requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
                findNavController().navigate(R.id.settingsFragment)
            }
        }

        requireActivity().llLanguages.setOnClickListener {
            requireActivity().llLanguages.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            findNavController().navigate(R.id.languageFragment)
        }

        requireActivity().llMyCards.setOnClickListener {
            requireActivity().llMyCards.startAnimation(AlphaAnimation(1f, 0.5f))
            if (isGuestLogin) {
                loginSignUpAccessAlertDialogBox(requireContext())
            } else {
                requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
                findNavController().navigate(R.id.mycardsFragment)
            }
        }

        requireActivity().llLocations.setOnClickListener {
            requireActivity().llLocations.startAnimation(AlphaAnimation(1f, 0.5f))
            if (isGuestLogin) {
                loginSignUpAccessAlertDialogBox(requireContext())
            } else {
                requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
                findNavController().navigate(R.id.mylocationsFragment)
            }
        }

        requireActivity().llAboutUs.setOnClickListener {
            requireActivity().llAboutUs.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            val bundle = Bundle()
            bundle.putString("title", getString(R.string.about_us))
            findNavController().navigate(R.id.cmsFragment, bundle)
        }

        requireActivity().llContactUs.setOnClickListener {
            requireActivity().llContactUs.startAnimation(AlphaAnimation(1f, 0.5f))
            if (isGuestLogin) {
                loginSignUpAccessAlertDialogBox(requireContext())
            } else {
                requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
                findNavController().navigate(R.id.contactusFragment)
            }
        }

        requireActivity().llTermsAndConditions.setOnClickListener {
            requireActivity().llTermsAndConditions.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            val bundle = Bundle()
            bundle.putString("title", getString(R.string.terms_and_conditions))
            findNavController().navigate(R.id.cmsFragment, bundle)
        }

        requireActivity().llPrivacyAndPolicy.setOnClickListener {
            requireActivity().llPrivacyAndPolicy.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            val bundle = Bundle()
            bundle.putString("title", getString(R.string.privacy_and_policy))
            findNavController().navigate(R.id.cmsFragment, bundle)
        }

        requireActivity().llFAQ.setOnClickListener {
            requireActivity().llFAQ.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            val bundle = Bundle()
            bundle.putString("title", getString(R.string.frequently_asked_questions))
            findNavController().navigate(R.id.cmsFragment, bundle)
        }

        requireActivity().llHelpPage.setOnClickListener {
            requireActivity().llHelpPage.startAnimation(AlphaAnimation(1f, 0.5f))
            if (isGuestLogin) {
                loginSignUpAccessAlertDialogBox(requireContext())
            } else {
                requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
                findNavController().navigate(R.id.helpFragment)
            }
        }


        requireActivity().llLogout.setOnClickListener {
            requireActivity().llLogout.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)

            val logoutDialog = LogoutDialog()
            logoutDialog.isCancelable = false
            logoutDialog.setDataCompletionCallback(object : LogoutDialog.LogoutInterface {
                override fun complete() {
                    logoutAPI()
                }
            })
            logoutDialog.show(requireActivity().supportFragmentManager, "HomeFragment")
        }

    }

    private fun logoutAPI() {
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        val call = apiInterface.logout(
            sharedPreferenceInstance!!.get(
                SharedPreferenceUtility.UserId,
                0
            ),
            sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]
        )
        call!!.enqueue(object : Callback<LogoutResponse?> {
            override fun onResponse(
                call: Call<LogoutResponse?>,
                response: Response<LogoutResponse?>
            ) {
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.isSuccessful) {
                        if (response.body()!!.status == 1) {
                            sharedPreferenceInstance!!
                                .delete(SharedPreferenceUtility.IsLogin)
                            startActivity(
                                Intent(requireContext(), LoginActivity::class.java).addFlags(
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                                )
                            )
                            requireActivity().finishAffinity()
                        } else {
                            Utility.showSnackBarOnResponseError(
                                mView!!.homeFragmentConstraintlayout,
                                requireContext().getString(R.string.response_isnt_successful),
                                requireContext()
                            )
                        }
                    } else {
                        Utility.showSnackBarOnResponseError(
                            mView!!.homeFragmentConstraintlayout,
                            requireContext().getString(R.string.response_isnt_successful),
                            requireContext()
                        )
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<LogoutResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(
                    mView!!.homeFragmentConstraintlayout,
                    requireContext().getString(R.string.response_isnt_successful),
                    requireContext()
                )
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        })
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

    override fun onStart() {
        super.onStart()
        locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    fun getCurrentLocation() {
        if (locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        ) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            } else {
                fusedLocationClient.lastLocation.addOnCompleteListener {
                    val location = it.result
                    if (location != null) {
                        latitude = location.latitude
                        currentLatitude = location.latitude
                        longitude = location.longitude
                        currentLongitude = location.longitude
                        Log.e("latitude", latitude.toString())
                        Log.e("longitude", longitude.toString())

                        if (!Utility.hasConnection(requireContext())) {
                            val noInternetDialog = NoInternetDialog()
                            noInternetDialog.isCancelable = false
                            noInternetDialog.setRetryCallback(object :
                                NoInternetDialog.RetryInterface {
                                override fun retry() {
                                    noInternetDialog.dismiss()
                                    latitude = location.latitude
                                    longitude - location.longitude
                                    getAllServicesListing(
                                        latitude,
                                        longitude,
                                        0,
                                        service_id,
                                        "",
                                        0,
                                        "50"
                                    )
                                }

                            })
                            noInternetDialog.show(
                                requireActivity().supportFragmentManager,
                                "Home Fragment"
                            )
                        } else {
                            getAllServicesListing(
                                location.latitude,
                                longitude = location.longitude,
                                0,
                                service_id,
                                "",
                                0,
                                "50"
                            )
                        }

                    } else {
                        val locationRequest = LocationRequest.create().apply {
                            interval = 10000
                            fastestInterval = 1000
                            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                            numUpdates = 1
                        }
                        val locationCallback = object : LocationCallback() {
                            override fun onLocationResult(locationResult: LocationResult) {
                                val location1 = locationResult.lastLocation
                                latitude = location1!!.latitude
                                longitude = location1.longitude
                                Log.e("latitude", latitude.toString())
                                Log.e("longitude", longitude.toString())
                            }
                        }
                        fusedLocationClient.requestLocationUpdates(
                            locationRequest,
                            locationCallback,
                            Looper.myLooper()!!
                        )
                    }
                }
            }
        } else {
            requireContext().startActivity(
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().iv_back.visibility = View.GONE
        requireActivity().tv_title.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().tv_title.visibility = View.VISIBLE
        requireActivity().iv_back.visibility = View.VISIBLE
    }

    override fun onStop() {
        super.onStop()
        requireActivity().tv_title.visibility = View.VISIBLE
        requireActivity().iv_back.visibility = View.VISIBLE
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


    private fun likeService(itemView: View, position:Int,
                            servicesListingAdapter1: ServicesListingAdapter?,
                            gridServiceListingAdapter1: GridServiceListingAdapter?) {
        if (isGuestLogin) {
            loginSignUpAccessAlertDialogBox(requireContext())
        } else {
            val serviceItem = servicesList[position]
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
                                    mView!!.homeFragmentConstraintlayout,
                                    response.body()!!.message.toString(),
                                    requireContext()
                                )
                            }
                            if(servicesListingAdapter1==null){
                                gridServiceListingAdapter1!!.notifyDataSetChanged()
                            }else{
                                servicesListingAdapter1.notifyDataSetChanged()
                            }
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
                                mView!!.homeFragmentConstraintlayout,
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
                            mView!!.homeFragmentConstraintlayout,
                            t.message.toString(),
                            requireContext()
                        )
                    }
                }

            })
        }
    }

    private fun disLikeService(itemView: View, position:Int,
                               servicesListingAdapter1: ServicesListingAdapter?,
                               gridServiceListingAdapter1: GridServiceListingAdapter?){
        if (isGuestLogin) {
            loginSignUpAccessAlertDialogBox(requireContext())
        } else {
            val serviceItem = servicesList[position]
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
                                    mView!!.homeFragmentConstraintlayout,
                                    response.body()!!.message.toString(),
                                    requireContext()
                                )
                            }


                            serviceItem.is_favorite = 0
                            if(servicesListingAdapter1==null){
                                gridServiceListingAdapter1!!.notifyDataSetChanged()
                            }else{
                                servicesListingAdapter1.notifyDataSetChanged()
                            }

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
                                mView!!.homeFragmentConstraintlayout,
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
                            mView!!.homeFragmentConstraintlayout,
                            t.message.toString(),
                            requireContext()
                        )
                    }
                }

            })
        }
    }
}