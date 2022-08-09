package com.heena.user.fragments

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.heena.user.Dialogs.NoInternetDialog
import com.heena.user.R
import com.heena.user.`interface`.ClickInterface
import com.heena.user.adapters.AddressListAdapter
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.bottomsheetdialog.AddAddressBottomSheetFragment
import com.heena.user.models.AddressItem
import com.heena.user.models.AddressListingResponse
import com.heena.user.models.MyResponse
import com.heena.user.utils.LogUtils
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.apiInterface
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_add_address_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_address_bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_appointments.view.*
import kotlinx.android.synthetic.main.fragment_my_cards.view.*
import kotlinx.android.synthetic.main.fragment_my_locations.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MyLocationsFragment : Fragment() {
    private var addressesList = ArrayList<AddressItem>()
    private var addressListAdapter: AddressListAdapter?=null
    private var address_id = 0
    private val appPerms = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private var activityResultLauncher: ActivityResultLauncher<Array<String>> =
            registerForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()) { result ->
                var allAreGranted = true
                for(b in result.values) {
                    allAreGranted = allAreGranted && b
                }

                if(allAreGranted) {
                    Log.e("Granted", "Permissions")
                    val bundle = Bundle()
                    bundle.putString("direction", "Navigation")
                    findNavController().navigate(R.id.action_mylocationsFragment_to_addressBottomSheetFragment, bundle)
                    true
                }else{
                    Utility.showSnackBarValidationError(myLocationsFragmentConstraintLayout,
                        requireContext().getString(R.string.please_allow_permissions),
                        requireContext())
                }
            }



    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =inflater.inflate(R.layout.fragment_my_locations, container, false)
            Utility.changeLanguage(
                requireContext(),
                sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
            )
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().tv_title.text = ""

        requireActivity().iv_back.setSafeOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F, 0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().navigate(R.id.action_mylocationsFragment_to_homeFragment)
        }

        requireActivity().iv_notification.setSafeOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F,0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }


        if (!Utility.hasConnection(requireContext())){
            val noInternetDialog = NoInternetDialog()
            noInternetDialog.isCancelable = false
            noInternetDialog.setRetryCallback(object : NoInternetDialog.RetryInterface{
                override fun retry() {
                    noInternetDialog.dismiss()
                    getAddressList()
                }

            })
            noInternetDialog.show(requireActivity().supportFragmentManager, "MyLocations Fragment")
        }

        getAddressList()

        tv_add_new_address.setSafeOnClickListener {
            activityResultLauncher.launch(appPerms)
        }
    }

    private fun getAddressList() {
        frag_my_locations_progressBar.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val call = apiInterface.getAddressList(sharedPreferenceInstance!!.get(SharedPreferenceUtility.UserId, 0),
                sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, ""))
        call?.enqueue(object : Callback<AddressListingResponse?> {
            override fun onResponse(
                    call: Call<AddressListingResponse?>,
                    response: Response<AddressListingResponse?>
            ) {
                frag_my_locations_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful) {
                    if (response.body()!!.status == 0) {
                        noLocationView.visibility = View.VISIBLE
                        cl_locations.visibility = View.GONE
                    } else {
                        noLocationView.visibility = View.GONE
                        cl_locations.visibility = View.VISIBLE
                        addressesList.clear()
                        addressesList = response.body()!!.address as ArrayList<AddressItem>
                        rv_address_list.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                        addressListAdapter = AddressListAdapter(requireContext(), addressesList, object : ClickInterface.OnAddressItemClick {
                            override fun editAdddress(position: Int) {
                                val updateAddressDialog = AlertDialog.Builder(requireContext())
                                updateAddressDialog.setCancelable(false)
                                updateAddressDialog.setTitle(requireContext().getString(R.string.update_address))
                                updateAddressDialog.setMessage(requireContext().getString(R.string.would_you_like_to_update_your_address))
                                updateAddressDialog.setPositiveButton(requireContext().getString(R.string.yes)){ dialog, _ ->
                                    address_id = addressesList[position].address_id!!
                                    val bundle = Bundle()
                                    bundle.putString("status", "edit")
                                    bundle.putInt("addressId", address_id)
                                    val addressBottomSheetFragment = AddAddressBottomSheetFragment.newInstance(bundle)
                                    addressBottomSheetFragment.show(requireActivity().supportFragmentManager, AddAddressBottomSheetFragment.TAG)
                                    dialog.dismiss()
                                }
                                updateAddressDialog.setNegativeButton(requireContext().getString(R.string.no)){ dialog, _ ->
                                    dialog!!.cancel()
                                }
                                updateAddressDialog.show()
                            }

                            override fun deleteAddress(position: Int) {
                                val address_id = addressesList.get(position).address_id!!.toInt()
                                val deleteServiceDialog = AlertDialog.Builder(requireContext())
                                deleteServiceDialog.setCancelable(false)
                                deleteServiceDialog.setTitle(requireContext().getString(R.string.delete_address))
                                deleteServiceDialog.setMessage(requireContext().getString(R.string.are_you_sure_you_want_to_delete_the_address))
                                deleteServiceDialog.setPositiveButton(requireContext().getString(R.string.delete)
                                ) { dialog, _ ->
                                    delete_Address(address_id, position)
                                    dialog!!.dismiss()
                                }
                                deleteServiceDialog.setNegativeButton(requireContext().getString(R.string.cancel)
                                ) { dialog, _ -> dialog!!.cancel() }
                                deleteServiceDialog.show()
                            }

                        })
                        rv_address_list.adapter = addressListAdapter
                        addressListAdapter!!.notifyDataSetChanged()
                    }
                } else {
                    Utility.showSnackBarOnResponseError(myLocationsFragmentConstraintLayout,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext())

                    noLocationView.visibility = View.GONE
                    cl_locations.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<AddressListingResponse?>, throwable: Throwable) {
                frag_my_locations_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(myLocationsFragmentConstraintLayout,
                    requireContext().getString(R.string.response_isnt_successful),
                    requireContext())
                noLocationView.visibility = View.GONE
                cl_locations.visibility = View.VISIBLE
            }

        })
    }

    private fun delete_Address(addressId: Int, position: Int) {
        val call = apiInterface.deleteAddress(addressId,
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, ""))
        call?.enqueue(object : Callback<MyResponse?> {
            override fun onResponse(
                call: Call<MyResponse?>,
                response: Response<MyResponse?>
            ) {
                if (response.isSuccessful) {
                    if (response.body()!!.status == 1) {
                        addressesList.removeAt(position)
                        if (rv_address_list.adapter != null) {
                            rv_address_list.adapter!!.notifyDataSetChanged()
                            if (addressesList.size == 0) {
                                noLocationView.visibility = View.GONE
                                cl_locations.visibility = View.VISIBLE
                            }
                            Utility.showSnackBarOnResponseSuccess(myLocationsFragmentConstraintLayout,
                                response.body()!!.message.toString(),
                                requireContext())
                        } else {
                            Utility.showSnackBarOnResponseError(myLocationsFragmentConstraintLayout,
                                response.body()!!.message.toString(),
                                requireContext())
                        }
                        //LogUtils.longToast(requireContext(), response.body()!!.message)
                    } else {
                        Utility.showSnackBarOnResponseError(myLocationsFragmentConstraintLayout,
                            response.body()!!.message.toString(),
                            requireContext())
                    }
                } else {
                    Utility.showSnackBarOnResponseError(myLocationsFragmentConstraintLayout,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext())
                }
            }

            override fun onFailure(
                call: Call<MyResponse?>,
                throwable: Throwable
            ) {
                Utility.showSnackBarOnResponseError(myLocationsFragmentConstraintLayout,
                    requireContext().getString(R.string.response_isnt_successful),
                    requireContext())
            }

        })
    }

    override fun onResume() {
        super.onResume()
        getAddressList()
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