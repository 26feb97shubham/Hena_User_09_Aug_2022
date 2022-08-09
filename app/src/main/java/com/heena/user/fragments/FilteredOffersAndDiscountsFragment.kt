package com.heena.user.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.heena.user.R
import com.heena.user.`interface`.APIInterface.Companion.createBuilder
import com.heena.user.`interface`.ClickInterface
import com.heena.user.adapters.OffersAndDiscountsListingAdapter
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.models.*
import com.heena.user.utils.LogUtils
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.apiInterface
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.filtered_offers_n_disc_recycler_item_listing.view.*
import kotlinx.android.synthetic.main.fragment_filtered_offers_and_discounts.view.*
import kotlinx.android.synthetic.main.fragment_service_payment.view.*
import kotlinx.android.synthetic.main.naqashat_services_recycler_item.view.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class FilteredOffersAndDiscountsFragment : Fragment(), AdapterView.OnItemSelectedListener {
    lateinit var offersAndDiscountsListingAdapter: OffersAndDiscountsListingAdapter
    var mView : View ? = null
    private var offers = arrayOf<String>()
    private var offerList = ArrayList<OfferItemX>()
    private var manager:Manager?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_filtered_offers_and_discounts, container, false)
        mView!!.rv_filtered_offers_n_disc_listing.setHasFixedSize(true)
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
        )
        offers = arrayOf(requireContext().getString(R.string.select_offer),
            requireContext().getString(R.string.ten_percent_offers),
            requireContext().getString(R.string.fifteen_percent_offers),
            requireContext().getString(R.string.thirty_percent_offers),
            requireContext().getString(R.string.fifty_above_percent_offers))
        val arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, offers)
        arrayAdapter.setDropDownViewResource(
            android.R.layout
                .simple_spinner_dropdown_item)
        mView!!.tv_offers_n_disc_name.adapter = arrayAdapter
        requireActivity().tv_title.text = ""
        mView!!.tv_offers_n_disc_name.onItemSelectedListener = this

        requireActivity().tv_title.text = ""

        requireActivity().iv_back.setSafeOnClickListener {
            requireActivity().iv_back.startAnimation(
                AlphaAnimation(1f, 0.5f)
            )
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        requireActivity().iv_notification.setSafeOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F,0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }

        return mView
    }

    private fun showFilteredOfferresults(filter: String) {
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        mView!!.progressBar_offer_result.visibility= View.VISIBLE
        val builder = createBuilder(arrayOf("user_id","filter", "lang"),
            arrayOf(sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0].toString(),
            filter,
            sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]))
        val call = apiInterface.showOffers(builder.build())
        call!!.enqueue(object : Callback<OffersResponse?>{
            override fun onResponse(
                call: Call<OffersResponse?>,
                response: Response<OffersResponse?>
            ) {
                mView!!.progressBar_offer_result.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.isSuccessful){
                        if (response.body()!!.status==1){
                            offerList!!.clear()
                            mView!!.tv_no_offers_found.visibility = View.GONE
                            mView!!.rv_filtered_offers_n_disc_listing.visibility = View.VISIBLE
                            offerList = (response.body()!!.offer as ArrayList<OfferItemX>?)!!
                            mView!!.rv_filtered_offers_n_disc_listing.layoutManager = LinearLayoutManager(requireContext(),  LinearLayoutManager.VERTICAL,
                                false)

                            offersAndDiscountsListingAdapter = OffersAndDiscountsListingAdapter(requireContext(),offerList!!, object : ClickInterface.OnServicesItemClick{
                                override fun OnClickAction(position: Int) {
                                    var bundle = Bundle()
                                    bundle.putSerializable("offerDetails", offerList[position])
                                    findNavController().navigate(R.id.viewOfferFragment, bundle)
                                }

                                override fun onLikeAction(position: Int, itemView: View) {
                                    val offer = offerList[position]
                                    val builder = createBuilder(
                                        arrayOf("lang", "user_id", "service_id"),
                                        arrayOf(
                                            sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""],
                                            sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0]
                                                .toString(),
                                            offer.service_id.toString()
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
                                                    itemView.iv_add_delete_fav.visibility = View.GONE
                                                    itemView.iv_added_deleted_fav.visibility = View.VISIBLE
                                                    LogUtils.shortToast(requireContext(), response.body()!!.message)
                                                    Utility.showSnackBarOnResponseSuccess(mView!!.filteredOffersAndDiscountsFragmentConstraintLayout,
                                                        response.body()!!.message.toString(),
                                                        requireContext())

                                                }
                                            } else {
                                                Utility.showSnackBarOnResponseError(mView!!.filteredOffersAndDiscountsFragmentConstraintLayout,
                                                    requireContext().getString(R.string.response_isnt_successful),
                                                    requireContext())

                                            }
                                        }

                                        override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                                            Utility.showSnackBarOnResponseError(mView!!.filteredOffersAndDiscountsFragmentConstraintLayout,
                                                requireContext().getString(R.string.response_isnt_successful),
                                                requireContext())
                                        }

                                    })
                                }

                                override fun onDislikeAction(position: Int, itemView: View) {
                                    val offer = offerList[position]
                                    val builder = createBuilder(
                                        arrayOf("lang", "user_id", "service_id"),
                                        arrayOf(
                                            sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""],
                                            sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0]
                                                .toString(),
                                            offer.service_id.toString()
                                        )
                                    )
                                    val call = Utility.apiInterface.delServiceFav(builder.build())
                                    call?.enqueue(object : Callback<MyResponse?> {
                                        override fun onResponse(
                                            call: Call<MyResponse?>,
                                            response: Response<MyResponse?>
                                        ) {
                                            if (response.isSuccessful) {
                                                if (response.body()!!.status == 1) {
                                                    itemView.iv_add_delete_fav.visibility = View.VISIBLE
                                                    itemView.iv_added_deleted_fav.visibility = View.GONE
                                                    Utility.showSnackBarOnResponseSuccess(mView!!.filteredOffersAndDiscountsFragmentConstraintLayout,
                                                        response.body()!!.message.toString(),
                                                        requireContext())
                                                }
                                            } else {
                                                Utility.showSnackBarOnResponseError(mView!!.filteredOffersAndDiscountsFragmentConstraintLayout,
                                                    requireContext().getString(R.string.response_isnt_successful),
                                                    requireContext())
                                            }
                                        }

                                        override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                                            Utility.showSnackBarOnResponseError(mView!!.filteredOffersAndDiscountsFragmentConstraintLayout,
                                                requireContext().getString(R.string.response_isnt_successful),
                                                requireContext())
                                        }
                                    })
                                }

                                override fun onProfileClick(position: Int) {
                                }
                            })

                            mView!!.rv_filtered_offers_n_disc_listing.adapter = offersAndDiscountsListingAdapter
                        }else{
                            mView!!.tv_no_offers_found.visibility = View.VISIBLE
                            mView!!.rv_filtered_offers_n_disc_listing.visibility = View.GONE
                        }
                    }else{
                        Utility.showSnackBarOnResponseError(mView!!.filteredOffersAndDiscountsFragmentConstraintLayout,
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

            override fun onFailure(call: Call<OffersResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(mView!!.filteredOffersAndDiscountsFragmentConstraintLayout,
                    requireContext().getString(R.string.response_isnt_successful),
                    requireContext())
                mView!!.progressBar_offer_result.visibility= View.GONE
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

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        when(offers[p2]){
            requireContext().getString(R.string.select_offer) -> {showFilteredOfferresults("")}
            requireContext().getString(R.string.ten_percent_offers) -> { showFilteredOfferresults("1")}
            requireContext().getString(R.string.fifteen_percent_offers) -> { showFilteredOfferresults("2")}
            requireContext().getString(R.string.thirty_percent_offers) -> { showFilteredOfferresults("3")}
            else -> { showFilteredOfferresults("4")}
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        showFilteredOfferresults("")
    }
}