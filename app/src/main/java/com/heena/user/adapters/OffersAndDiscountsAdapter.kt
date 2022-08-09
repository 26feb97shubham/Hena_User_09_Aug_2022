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
import com.heena.user.models.OfferItem
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.loginSignUpAccessAlertDialogBox
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.naqashat_offers_recycler_item.view.*
import kotlinx.android.synthetic.main.naqashat_offers_recycler_item.view.tv_original_price
import kotlinx.android.synthetic.main.naqashat_offers_recycler_item.view.tv_services
import kotlin.collections.ArrayList

class OffersAndDiscountsAdapter(
    private val context: Context,
    private val offerList: ArrayList<OfferItem>,
    private val onCLickAction: ClickInterface.onOfferItemClick
) :
    RecyclerView.Adapter<OffersAndDiscountsAdapter.OffersAndDiscountsAdapterVH>() {

    inner class OffersAndDiscountsAdapterVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bind(offer: OfferItem, position: Int) {
            val requestOption = RequestOptions().centerCrop().error(R.drawable.def_henna)
            val offerServiceImage = if (offer.gallery!!.isEmpty()){
                context.getDrawable(R.drawable.def_henna).toString()
            }else{
                offer.gallery[0]
            }
            Glide.with(context).load(offerServiceImage).apply(requestOption).into(itemView.offer_image)
            itemView.tv_services.text = offer.name
            itemView.tv_original_price.text = "AED" + " " + Utility.convertDoubleValueWithCommaSeparator(offer.price!!.main!!.toDouble())
            itemView.tv_discounted_price.text = "AED" + " " + Utility.convertDoubleValueWithCommaSeparator(offer.offer!!.price!!.main!!.toDouble())
            itemView.setSafeOnClickListener {
                onCLickAction.OnClickAction(position)
            }
            if (offer.is_favorite == 1) {
                itemView.iv_add_to_fav_offer.visibility = View.GONE
                itemView.iv_fav_added_offer.visibility = View.VISIBLE
            } else {
                itemView.iv_add_to_fav_offer.visibility = View.VISIBLE
                itemView.iv_fav_added_offer.visibility = View.GONE
            }

            itemView.iv_add_to_fav_offer.setOnClickListener {
                if (sharedPreferenceInstance!![SharedPreferenceUtility.IsGuestUserLogin, false]){
                    loginSignUpAccessAlertDialogBox(context)
                }else{
                    onCLickAction.onLikeAction(position, itemView)
                }
            }

            itemView.iv_fav_added_offer.setOnClickListener {
                if (sharedPreferenceInstance!![SharedPreferenceUtility.IsGuestUserLogin, false]){
                    loginSignUpAccessAlertDialogBox(context)
                }else{
                    onCLickAction.onDislikeAction(position, itemView)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OffersAndDiscountsAdapterVH {
        val view =
            LayoutInflater.from(context)
                .inflate(R.layout.naqashat_offers_recycler_item, parent, false)
        return OffersAndDiscountsAdapterVH(view)
    }

    override fun onBindViewHolder(holder: OffersAndDiscountsAdapterVH, position: Int) {
        val offer = offerList[position]
        holder.bind(offer, position)
    }

    override fun getItemCount(): Int {
        return offerList.size
    }
}