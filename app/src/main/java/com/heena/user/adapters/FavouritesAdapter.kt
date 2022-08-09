package com.heena.user.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.heena.user.R
import com.heena.user.`interface`.ClickInterface
import com.heena.user.models.Service
import com.heena.user.models.ServiceItem
import com.heena.user.models.ServiceItemY
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.favourite_item_type
import kotlinx.android.synthetic.main.default_listing_recycler_item.view.*
import kotlinx.android.synthetic.main.item_fav_naqashat.view.*
import kotlinx.android.synthetic.main.item_fav_naqashat.view.ratingBar
import kotlinx.android.synthetic.main.item_fav_naqashat.view.txtRating
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class FavouritesAdapter(private val context: Context,
                        private val managerList: ArrayList<Service>?,
                        private val serviceList: ArrayList<ServiceItemY>?,
                        private val onFavItemClick: ClickInterface.onFavItemClick) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    inner class FavouritesAdapterVH(private val favManageritemView : View) : RecyclerView.ViewHolder(favManageritemView) {
        fun bind(service: Service?, position: Int) {
            val favoritesManagerImage = if (service!!.image!!.isEmpty()){
                context.getDrawable(R.drawable.default_henna).toString()
            }else{
                service.image
            }
            Glide.with(context).load(favoritesManagerImage)
                .apply(RequestOptions().error(R.drawable.def_henna))
                .into(favManageritemView.civ_naqashat)
            favManageritemView.tv_naqashat_name.text = service.username
            favManageritemView.tv_address.text = service.address

            if (service.comment_star!!.toString()==null||service.comment_star.toString().equals("")){
                favManageritemView.txtRating.text = String.format( Locale.ENGLISH,"%.1f",0.0)
                favManageritemView.ratingBar.rating = 0F
            }else{
                favManageritemView.txtRating.text = String.format( Locale.ENGLISH,"%.1f",service.comment_star.toDouble())
                favManageritemView.ratingBar.rating = service.comment_star.toFloat()
            }



            favManageritemView.iv_add_delete_fav_manager.visibility = View.GONE
            favManageritemView.iv_added_deleted_fav_manager.visibility = View.VISIBLE

            favManageritemView.iv_added_deleted_fav_manager.setOnClickListener {
                onFavItemClick.delFav(position)
            }

            favManageritemView.setOnClickListener {
                onFavItemClick.openFav(position)
            }
        }
    }

    inner class FavouritesServicesAdapterVH(private val favitemView : View) : RecyclerView.ViewHolder(favitemView) {
        @SuppressLint("SetTextI18n")
        fun bind(serviceItem: ServiceItemY, position: Int) {
            val serviceImage = if (serviceItem.gallery!!.isEmpty()){
                context.getDrawable(R.drawable.def_henna).toString()
            }else{
                serviceItem.gallery[0]
            }
            Glide.with(context).load(serviceImage).apply(RequestOptions().error(R.drawable.def_henna)).into(favitemView.img)
            Glide.with(context).load(serviceItem.user!!.image).apply(RequestOptions().error(R.drawable.user)).into(favitemView.civ_supplierImg_linear)
            favitemView.tv_services.text = serviceItem.name
            favitemView.tv_price.text = "AED ${
                Utility.convertDoubleValueWithCommaSeparator(
                    serviceItem.price!!.total!!.toDouble()
                )
            }"
            favitemView.tv_category_name.text = serviceItem.category!!.name

            favitemView.iv_add_delete_fav.visibility = View.GONE
            favitemView.iv_added_deleted_fav.visibility = View.VISIBLE

            favitemView.iv_added_deleted_fav.setOnClickListener {
                onFavItemClick.delFav(position)
            }

            favitemView.setOnClickListener {
                onFavItemClick.openFav(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (favourite_item_type) {
            1 -> {
                val mView = LayoutInflater.from(context).inflate(R.layout.item_fav_naqashat, parent, false)
                FavouritesAdapterVH(mView)
            }
            2 -> {
                val mView = LayoutInflater.from(context).inflate(R.layout.default_listing_recycler_item, parent, false)
                FavouritesServicesAdapterVH(mView)
            }
            else -> {
                val mView = LayoutInflater.from(context).inflate(R.layout.item_fav_naqashat, parent, false)
                FavouritesAdapterVH(mView)
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val manager = managerList?.get(position)
        val serviceItem = serviceList?.get(position)
        when (favourite_item_type) {
            1 -> {
                val favManagerholder = holder as FavouritesAdapterVH
                favManagerholder.bind(manager, position)
            }
            2 -> {
                val favServicesholder = holder as FavouritesServicesAdapterVH
                if (serviceItem != null) {
                    favServicesholder.bind(serviceItem, position)
                }
            }
            else -> {
                val favManagerholder = holder as FavouritesAdapterVH
                favManagerholder.bind(manager, position)
            }
        }
    }

    override fun getItemCount(): Int {
        return serviceList?.size
            ?: (managerList?.size ?: 0)
    }

    override fun getItemViewType(position: Int): Int {
        return favourite_item_type
    }
}