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
import com.heena.user.application.MyApp
import com.heena.user.models.OfferItemX
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.loginSignUpAccessAlertDialogBox
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.default_listing_recycler_item.view.*
import kotlinx.android.synthetic.main.filtered_offers_n_disc_recycler_item_listing.view.*
import kotlinx.android.synthetic.main.filtered_offers_n_disc_recycler_item_listing.view.img
import kotlinx.android.synthetic.main.filtered_offers_n_disc_recycler_item_listing.view.iv_add_delete_fav
import kotlinx.android.synthetic.main.filtered_offers_n_disc_recycler_item_listing.view.iv_added_deleted_fav
import kotlinx.android.synthetic.main.filtered_offers_n_disc_recycler_item_listing.view.tv_category_name
import kotlinx.android.synthetic.main.filtered_offers_n_disc_recycler_item_listing.view.tv_original_price
import kotlinx.android.synthetic.main.filtered_offers_n_disc_recycler_item_listing.view.tv_services
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class OffersAndDiscountsListingAdapter(
    private val context: Context,
    private val offerList: ArrayList<OfferItemX>,
    private val onServicesItemClick: ClickInterface.OnServicesItemClick
) :
    RecyclerView.Adapter<OffersAndDiscountsListingAdapter.OffersAndDiscountsListingAdapterVH>() {
    inner class OffersAndDiscountsListingAdapterVH(private val itemView : View) : RecyclerView.ViewHolder(itemView){
        fun bind(offer : OfferItemX, position:Int){
            val requestOption = RequestOptions().centerCrop().error(R.drawable.def_henna)
            val requestOption1 = RequestOptions().centerCrop().error(R.drawable.user)
            itemView.tv_services.text = offer.name
            itemView.tv_original_price.text = "AED ${Utility.convertDoubleValueWithCommaSeparator(offer.price!!.total!!.toDouble())}"
            itemView.tv_discounted_price.text = "AED ${Utility.convertDoubleValueWithCommaSeparator(offer.offer!!.price!!.total!!.toDouble())}"
            if (offer.gallery!!.isEmpty()){
                Glide.with(context).load(R.drawable.def_henna).into(itemView.img)
            }else{
                Glide.with(context).load(offer.gallery[0]).apply(requestOption).into(itemView.img)
            }
            if (offer.user?.image.isNullOrEmpty()){
                Glide.with(context).load(R.drawable.logo_1).apply(requestOption1).into(itemView.supplierImg)
            }else{
                Glide.with(context).load(offer.user?.image).apply(requestOption1).into(itemView.supplierImg)
            }

            if (offer.user!!.avg_star.equals("")||offer.user.avg_star==null){
                itemView.ratingBar_offer.rating = 0F
                itemView.txtRating_offer.text = "0"
            }else{
                itemView.ratingBar_offer.rating = offer.user.avg_star.toFloat()
                itemView.txtRating_offer.text = String.format( Locale.ENGLISH,"%.1f",offer.user.avg_star.toDouble())
            }
            itemView.tv_category_name.text = offer.category!!.name
            if (offer.is_favorite==0){
                itemView.iv_add_delete_fav.visibility = View.VISIBLE
                itemView.iv_added_deleted_fav.visibility = View.GONE
            }else{
                itemView.iv_add_delete_fav.visibility = View.GONE
                itemView.iv_added_deleted_fav.visibility = View.VISIBLE
            }

            itemView.iv_add_delete_fav.setOnClickListener {
                if (MyApp.sharedPreferenceInstance!![SharedPreferenceUtility.IsGuestUserLogin]) {
                    loginSignUpAccessAlertDialogBox(context)
                }else{
                    onServicesItemClick.onLikeAction(position, itemView)
                }

            }

            itemView.iv_added_deleted_fav.setOnClickListener {
                if (MyApp.sharedPreferenceInstance!![SharedPreferenceUtility.IsGuestUserLogin]) {
                    loginSignUpAccessAlertDialogBox(context)
                }else{
                    onServicesItemClick.onDislikeAction(position, itemView)
                }
            }


            itemView.setSafeOnClickListener {
                onServicesItemClick.OnClickAction(position)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OffersAndDiscountsListingAdapterVH {
        val view  =
            LayoutInflater.from(context).inflate(R.layout.filtered_offers_n_disc_recycler_item_listing, parent, false)
        return OffersAndDiscountsListingAdapterVH(view)
    }

    override fun onBindViewHolder(holder: OffersAndDiscountsListingAdapterVH, position: Int) {
        val offer = offerList[position]
        holder.bind(offer, position)
    }

    override fun getItemCount(): Int {
       return offerList.size
    }
}