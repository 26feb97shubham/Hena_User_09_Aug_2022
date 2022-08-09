package com.heena.user.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heena.user.R
import com.heena.user.`interface`.ClickInterface
import kotlinx.android.synthetic.main.services_categories_items.view.*

class ServiceListingAdapter(
        private val context: Context, private val serviceListing : ArrayList<String>,
        private val onRecyclerItemClick: ClickInterface.OnRecyclerItemClick
) : RecyclerView.Adapter<ServiceListingAdapter.ServicesListingAdapterVH>()  {
    inner class ServicesListingAdapterVH(itemView : View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicesListingAdapterVH {
        val view = LayoutInflater.from(context).inflate(R.layout.services_categories_items, parent, false)
        return ServicesListingAdapterVH(view)
    }

    override fun onBindViewHolder(holder: ServicesListingAdapterVH, position: Int) {
        holder.itemView.tv_services_categories.text = serviceListing[position]

        holder.itemView.setOnClickListener {
            onRecyclerItemClick.OnClickAction(position)
        }
    }

    override fun getItemCount(): Int {
        return serviceListing.size
    }
}