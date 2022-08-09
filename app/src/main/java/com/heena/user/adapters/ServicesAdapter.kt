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
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.loginSignUpAccessAlertDialogBox
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.grid_services_recycler_items.view.*
import kotlinx.android.synthetic.main.naqashat_services_recycler_item.view.*
import kotlinx.android.synthetic.main.naqashat_services_recycler_item.view.img
import kotlinx.android.synthetic.main.naqashat_services_recycler_item.view.iv_add_to_fav
import kotlinx.android.synthetic.main.naqashat_services_recycler_item.view.iv_fav_added
import kotlinx.android.synthetic.main.naqashat_services_recycler_item.view.tv_category_name
import kotlinx.android.synthetic.main.naqashat_services_recycler_item.view.tv_original_price
import kotlinx.android.synthetic.main.naqashat_services_recycler_item.view.tv_services
import kotlin.collections.ArrayList

class ServicesAdapter(
    private val context: Context,
    private val serviceList: ArrayList<ServiceItem>,
    private val onCLickAction: ClickInterface.OnServicesItemClick
) :
    RecyclerView.Adapter<ServicesAdapter.ServiceAdapterVH>() {
    inner class ServiceAdapterVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bind(serviceItem: ServiceItem, position: Int) {
            val requestOption1 = RequestOptions().centerCrop().error(R.drawable.def_henna)
            if (serviceItem.gallery!!.isEmpty()){
                Glide.with(context).load(R.drawable.def_henna).into(itemView.img)
            }else{
                Glide.with(context).load(serviceItem.gallery[0]).apply(requestOption1).into(itemView.img)
            }
            itemView.tv_services.text = serviceItem.name
            itemView.tv_original_price.text = "AED" + " " + Utility.convertDoubleValueWithCommaSeparator(serviceItem.price!!.total!!.toDouble())
            var cat_name = if(sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""].equals("ar")){
                serviceItem.category!!.name_ar
            }else{
                serviceItem.category!!.name
            }
            itemView.tv_category_name.text = cat_name

            itemView.setSafeOnClickListener {
                onCLickAction.OnClickAction(position)
            }

            if (serviceItem.is_favorite == 1) {
                itemView.iv_add_to_fav.visibility = View.GONE
                itemView.iv_fav_added.visibility = View.VISIBLE
            } else {
                itemView.iv_add_to_fav.visibility = View.VISIBLE
                itemView.iv_fav_added.visibility = View.GONE
            }

            itemView.iv_add_to_fav.setOnClickListener {
                if (sharedPreferenceInstance!![SharedPreferenceUtility.IsGuestUserLogin, false]) {
                    loginSignUpAccessAlertDialogBox(context)
                }else{
                    onCLickAction.onLikeAction(position, itemView)
                }

            }

            itemView.iv_fav_added.setOnClickListener {
                if (sharedPreferenceInstance!![SharedPreferenceUtility.IsGuestUserLogin, false]) {
                    loginSignUpAccessAlertDialogBox(context)
                }else{
                    onCLickAction.onDislikeAction(position, itemView)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceAdapterVH {
        val view =
            LayoutInflater.from(context)
                .inflate(R.layout.naqashat_services_recycler_item, parent, false)
        return ServiceAdapterVH(view)
    }

    override fun onBindViewHolder(holder: ServiceAdapterVH, position: Int) {
        val serviceItem = serviceList[position]
        holder.bind(serviceItem, position)
    }

    override fun getItemCount(): Int {
        return serviceList.size
    }
}