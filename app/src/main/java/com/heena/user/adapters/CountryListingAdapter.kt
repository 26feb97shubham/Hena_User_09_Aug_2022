package com.heena.user.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heena.user.R
import com.heena.user.`interface`.ClickInterface
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.models.Country
import com.heena.user.models.Emirates
import com.heena.user.utils.SharedPreferenceUtility
import kotlinx.android.synthetic.main.country_list_items.view.*

class CountryListingAdapter(
    private val context: Context, private val countryListing: ArrayList<Country>?,
    private val emiratesListing: ArrayList<Emirates>?,
    private val onRecyclerItemClick: ClickInterface.OnRecyclerItemClick
): RecyclerView.Adapter<CountryListingAdapter.CountryListingAdapterVH>()  {
    inner class CountryListingAdapterVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(country: Country?,emirate : Emirates?, position: Int) {
            if (sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""] == "ar"){
                if (country!=null){
                    itemView.tv_country_name.text = country!!.name_ar
                }else{
                    itemView.tv_country_name.text = emirate!!.nameAr
                }

            }else{
                if (country!=null){
                    itemView.tv_country_name.text = country!!.name
                }else{
                    itemView.tv_country_name.text = emirate!!.name
                }
            }
            itemView.setOnClickListener {
                onRecyclerItemClick.OnClickAction(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryListingAdapterVH {
        val view = LayoutInflater.from(context).inflate(R.layout.country_list_items, parent, false)
        return CountryListingAdapterVH(view)
    }

    override fun onBindViewHolder(holder: CountryListingAdapterVH, position: Int) {
        var emirate : Emirates?= null
        var country : Country?= null
        if(countryListing==null && emiratesListing!=null){
            emirate = emiratesListing?.get(position)!!
        }else{
            country = countryListing?.get(position)!!
        }
        if (country!=null && emirate==null){
            holder.bind(country,null,position)
        }else if (country==null && emirate!=null){
            holder.bind(null,emirate,position)
        }else{
            holder.bind(null,null,position)
        }

    }

    override fun getItemCount(): Int {
        if(countryListing!=null){
            return countryListing.size
        }else if(emiratesListing!=null){
            return emiratesListing.size
        }else{
            return 0
        }

    }
}