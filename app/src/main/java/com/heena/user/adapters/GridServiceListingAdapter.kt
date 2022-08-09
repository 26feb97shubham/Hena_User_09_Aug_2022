package com.heena.user.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.heena.user.R
import com.heena.user.`interface`.ClickInterface
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.models.ServiceItem
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility.convertDoubleValueWithCommaSeparator
import com.heena.user.utils.Utility.loginSignUpAccessAlertDialogBox
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.default_listing_recycler_item.view.tv_services
import kotlinx.android.synthetic.main.grid_services_recycler_items.view.*
import java.util.*
import kotlin.collections.ArrayList

class GridServiceListingAdapter (
        private val context: Context,
        private val servicesList: ArrayList<ServiceItem>,
        private val onServicesItemClick: ClickInterface.OnServicesItemClick
) : RecyclerView.Adapter<GridServiceListingAdapter.GridServiceListingAdapterVH>() {
    inner class GridServiceListingAdapterVH(private val griditemView : View) : RecyclerView.ViewHolder(griditemView){
        fun bind(service: ServiceItem, position: Int) {
            val requestOption = RequestOptions().centerCrop().error(R.drawable.user)
            val requestOption1 = RequestOptions().centerCrop().error(R.drawable.def_henna)
            val galleryList : ArrayList<String> = servicesList[position].gallery as ArrayList<String>
            if (galleryList.size==0){
                griditemView.offer_image.setImageResource(R.drawable.default_background)
            }else{
                Glide.with(context).load(galleryList[0]).apply(requestOption1).into(griditemView.offer_image)
            }

            griditemView.tv_services.text = service.name

            griditemView.tv_original_price.text =  "AED ${convertDoubleValueWithCommaSeparator(service.price!!.main!!.toDouble())}"
            if (service.offer==null){
                griditemView.tv_offer_price.visibility = View.GONE
                griditemView.tv_original_price.foreground = null
            }else{
                griditemView.tv_offer_price.visibility = View.VISIBLE
                griditemView.tv_offer_price.text = "AED ${convertDoubleValueWithCommaSeparator(service.offer.price!!)}"
                griditemView.tv_original_price.foreground = context.resources.getDrawable(R.drawable.strike_through_text_drawable)
            }


            var cat_name = if(sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""].equals("ar")){
                service.category!!.name_ar
            }else{
                service.category!!.name
            }
            griditemView.tv_category.text = cat_name
            val userImage = if (service.user!!.image!!.isEmpty()){
                context.getDrawable(R.drawable.logo_1).toString()
            }else{
                service.user.image!!
            }
            Glide.with(context).load(userImage).apply(requestOption).into(griditemView.civ_supplierImg)
            if (service.user.avg_star!!.toString().equals(null) || service.user.avg_star.toString().equals("")){
                griditemView.ratingBar.rating = 0F
                griditemView.txtRating.text = String.format( Locale.ENGLISH,"%.1f", 0.0)
            }else{
                griditemView.ratingBar.rating = service.user.avg_star.toFloat()
                griditemView.txtRating.text = String.format( Locale.ENGLISH,"%.1f", service.user.avg_star.toDouble())
            }


            if (service.is_favorite==0){
                griditemView.iv_add_to_fav.visibility = View.VISIBLE
                griditemView.iv_fav_added.visibility = View.GONE
            }else{
                griditemView.iv_add_to_fav.visibility = View.GONE
                griditemView.iv_fav_added.visibility = View.VISIBLE
            }
            griditemView.iv_add_to_fav.setSafeOnClickListener {
                if (sharedPreferenceInstance!![SharedPreferenceUtility.IsGuestUserLogin, false]) {
                    loginSignUpAccessAlertDialogBox(context)
                }else{
                    onServicesItemClick.onLikeAction(position, itemView)
                }

            }

            griditemView.iv_fav_added.setSafeOnClickListener {
                if (sharedPreferenceInstance!![SharedPreferenceUtility.IsGuestUserLogin, false]) {
                    loginSignUpAccessAlertDialogBox(context)
                }else{
                    onServicesItemClick.onDislikeAction(position, itemView)
                }
            }

            griditemView.setSafeOnClickListener {
                onServicesItemClick.OnClickAction(position)
            }

            griditemView.civ_supplierImg.setSafeOnClickListener {
                onServicesItemClick.onProfileClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridServiceListingAdapterVH {
        val view  =
                LayoutInflater.from(context).inflate(R.layout.grid_services_recycler_items, parent, false)
        return GridServiceListingAdapterVH(view)
    }

    override fun onBindViewHolder(holder: GridServiceListingAdapterVH, position: Int) {
        val serviceItem = servicesList[position]
        holder.bind(serviceItem, position)
    }

    override fun getItemCount(): Int {
        return servicesList.size
    }
}