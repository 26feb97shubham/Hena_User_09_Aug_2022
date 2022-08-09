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
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.models.ServiceItem
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility.convertDoubleValueWithCommaSeparator
import com.heena.user.utils.Utility.loginSignUpAccessAlertDialogBox
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.default_listing_recycler_item.view.*
import kotlinx.android.synthetic.main.default_listing_recycler_item.view.ratingBar
import kotlinx.android.synthetic.main.default_listing_recycler_item.view.tv_services
import kotlinx.android.synthetic.main.default_listing_recycler_item.view.txtRating
import kotlinx.android.synthetic.main.grid_services_recycler_items.view.*
import java.util.*
import kotlin.collections.ArrayList

class ServicesListingAdapter(private val context: Context,
                             private val servicesList: ArrayList<ServiceItem>,
                             private val onServicesItemClick: ClickInterface.OnServicesItemClick) :
    RecyclerView.Adapter<ServicesListingAdapter.FeaturedDefaultListingAdapterVH>() {

    inner class FeaturedDefaultListingAdapterVH(itemView : View) : RecyclerView.ViewHolder(itemView){
        @SuppressLint("SetTextI18n")
        fun bind(service: ServiceItem, position: Int) {
            val galleryList : ArrayList<String> = servicesList[position].gallery as ArrayList<String>
            val requestOption1 = RequestOptions().centerCrop().error(R.drawable.def_henna)
            val requestOption2 = RequestOptions().centerCrop().error(R.drawable.user)
            if (galleryList.size==0){
                itemView.img.setImageResource(R.drawable.default_background)
            }else{
                Glide.with(context).load(galleryList[0]).apply(requestOption1).into(itemView.img)
            }
            itemView.tv_services.text = service.name

            var cat_name = if(sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""].equals("ar")){
                service.category!!.name_ar
            }else{
                service.category!!.name
            }
            itemView.tv_category_name.text = cat_name
            if (service.user!!.avg_star!!.toString().equals("")||service.user!!.avg_star!!.toString()==null){
                itemView.ratingBar.rating = 0F
                itemView.txtRating.text = String.format( Locale.ENGLISH,"%.1f", 0)
            }else{
                itemView.ratingBar.rating = service.user.avg_star!!.toFloat()
                itemView.txtRating.text = String.format( Locale.ENGLISH,"%.1f", service.user.avg_star.toDouble())
            }
            val userImage = if (service.user.image!!.isEmpty()){
                context.getDrawable(R.drawable.logo_1).toString()
            }else{
                service.user.image
            }

            itemView.tv_price.text =  "AED ${convertDoubleValueWithCommaSeparator(service.price!!.main!!.toDouble())}"
            if (service.offer==null){
                itemView.tv_offer_price_linear.visibility = View.GONE
                itemView.tv_price.foreground = null
            }else{
                itemView.tv_offer_price_linear.visibility = View.VISIBLE
                itemView.tv_offer_price_linear.text = "AED ${convertDoubleValueWithCommaSeparator(service.offer.price!!)}"
                itemView.tv_price.foreground = context.resources.getDrawable(R.drawable.strike_through_text_drawable)
            }

            Glide.with(context).load(userImage).apply(requestOption2).into(itemView.civ_supplierImg_linear)
            if (service.is_favorite==0){
                itemView.iv_add_delete_fav.visibility = View.VISIBLE
                itemView.iv_added_deleted_fav.visibility = View.GONE
            }else{
                itemView.iv_add_delete_fav.visibility = View.GONE
                itemView.iv_added_deleted_fav.visibility = View.VISIBLE
            }
            itemView.iv_add_delete_fav.setOnClickListener {
                if (sharedPreferenceInstance!![SharedPreferenceUtility.IsGuestUserLogin, false]) {
                    loginSignUpAccessAlertDialogBox(context)
                }else{
                    onServicesItemClick.onLikeAction(position, itemView)
                }

            }

            itemView.iv_added_deleted_fav.setOnClickListener {
                if (sharedPreferenceInstance!![SharedPreferenceUtility.IsGuestUserLogin, false]) {
                    loginSignUpAccessAlertDialogBox(context)
                }else{
                    onServicesItemClick.onDislikeAction(position, itemView)
                }

            }

            itemView.setSafeOnClickListener {
                onServicesItemClick.OnClickAction(position)
            }

            itemView.civ_supplierImg_linear.setSafeOnClickListener {
                onServicesItemClick.onProfileClick(position)
            }
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FeaturedDefaultListingAdapterVH {
        val view  =
            LayoutInflater.from(context).inflate(R.layout.default_listing_recycler_item, parent, false)
        return FeaturedDefaultListingAdapterVH(view)
    }

    override fun onBindViewHolder(holder: FeaturedDefaultListingAdapterVH, position: Int) {
        val serviceItem = servicesList[position]
        holder.bind(serviceItem, position)
    }

    override fun getItemCount(): Int {
        return servicesList.size
    }
}