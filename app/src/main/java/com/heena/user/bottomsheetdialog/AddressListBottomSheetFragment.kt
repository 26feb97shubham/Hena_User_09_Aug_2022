package com.heena.user.bottomsheetdialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.heena.user.R
import com.heena.user.`interface`.ClickInterface
import com.heena.user.adapters.AddressesAdapter
import com.heena.user.models.AddressListingResponse
import com.heena.user.utils.LogUtils
import com.heena.user.utils.SharedPreferenceUtility
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.models.AddressItem
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.apiInterface
import kotlinx.android.synthetic.main.fragment_address_list_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_my_locations.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddressListBottomSheetFragment : BottomSheetDialogFragment() {
    private var mView : View?= null
    private var addressesList = ArrayList<AddressItem>()
    lateinit var addressesAdapter: AddressesAdapter

    var myaddressItem : AddressItem?= null

    private var setAddressClick : ClickInterface.setAddressClick?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_address_list_bottom_sheet, container, false)
        return mView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getAddressList()
    }

    private fun getAddressList() {
        val call = apiInterface.getAddressList(
            sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0],
            sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]
        )
        call?.enqueue(object : Callback<AddressListingResponse?> {
            override fun onResponse(
                    call: Call<AddressListingResponse?>,
                    response: Response<AddressListingResponse?>
            ) {
                if (response.isSuccessful){
                    mView!!.rv_addresses.visibility = View.VISIBLE
                    if (response.body()!!.status==0){
                        mView!!.noLocationView.visibility = View.VISIBLE
                        mView!!.rv_addresses.visibility = View.GONE
                    }else{
                        mView!!.noLocationView.visibility = View.GONE
                        mView!!.rv_addresses.visibility = View.VISIBLE
                        addressesList = response.body()!!.address as ArrayList<AddressItem>
                        mView!!.rv_addresses.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                        addressesAdapter = AddressesAdapter(requireContext(), addressesList, object : ClickInterface.OnAddAddressClick{
                            override fun OnAddAddress(position: Int, addressItem: AddressItem) {
                                myaddressItem = addressItem
                                SharedPreferenceUtility.myAddressItem = myaddressItem
                                if(myaddressItem!=null){
                                    setAddressClick!!.setAddress(myaddressItem)
                                    dismiss()
                                }else{
                                    Utility.showSnackBarValidationError(mView!!.addressListBottomSheetDialogFragment,
                                        requireContext().getString(R.string.please_select_the_address),
                                        requireContext())
                                }
                                Log.e("myaddressItem", myaddressItem.toString())
                            }
                        })

                        mView!!.rv_addresses.adapter = addressesAdapter
                    }
                }else{
                    Utility.showSnackBarOnResponseError(mView!!.addressListBottomSheetDialogFragment,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext())

                }
            }

            override fun onFailure(call: Call<AddressListingResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(mView!!.addressListBottomSheetDialogFragment,
                    requireContext().getString(R.string.response_isnt_successful),
                    requireContext())
                noLocationView.visibility = View.GONE
                cl_locations.visibility = View.VISIBLE
            }

        })
    }

    fun setAddressCallback(setAddressClick: ClickInterface.setAddressClick){
        this.setAddressClick = setAddressClick
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }

    companion object {
        const val TAG = "AddressListBottomSheetFragment"
        private var instance: SharedPreferenceUtility? = null

        fun newInstance(): AddressListBottomSheetFragment {
            //this.context = context;
            return AddressListBottomSheetFragment()
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