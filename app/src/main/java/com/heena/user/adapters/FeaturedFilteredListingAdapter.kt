package com.heena.user.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heena.user.R

class FeaturedFilteredListingAdapter(private val context: Context)
    : RecyclerView.Adapter<FeaturedFilteredListingAdapter.FeaturedFilteredListingAdapterVH>() {
    inner class FeaturedFilteredListingAdapterVH(private val itemView : View) : RecyclerView.ViewHolder(itemView){
        fun bind() {

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeaturedFilteredListingAdapter.FeaturedFilteredListingAdapterVH {
        val view = LayoutInflater.from(context).inflate(R.layout.filtered_results_listing_item, parent, false)
        return FeaturedFilteredListingAdapterVH(view)
    }

    override fun getItemCount(): Int {
        return 5
    }

    override fun onBindViewHolder(holder: FeaturedFilteredListingAdapterVH, position: Int) {
        holder.bind()
    }
}