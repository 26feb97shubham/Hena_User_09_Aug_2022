package com.heena.user.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.heena.user.R
import com.heena.user.models.CommentsItem
import kotlinx.android.synthetic.main.naqashat_reviews_recycler_item.view.*
import java.util.*
import kotlin.collections.ArrayList

class ReviewsAdapter(private val context: Context, private val commentsListing: ArrayList<CommentsItem>) : RecyclerView.Adapter<ReviewsAdapter.ReviewsAdapterVH>() {
    inner class ReviewsAdapterVH(itemView : View) : RecyclerView.ViewHolder(itemView) {
        fun bind(commentsItem: CommentsItem) {
            val requestOption1 = RequestOptions().centerCrop().error(R.drawable.user)
            itemView.tv_date.text = commentsItem.create_at
            itemView.tv_naqashat_desc.text = commentsItem.message
            if (commentsItem.star!!.toString().equals("")|| commentsItem.star.toString()==null){
                itemView.txtRating.text = String.format( Locale.ENGLISH,"%.1f",0.0)
                itemView.ratingBar.rating = 0F
            }else{
                itemView.txtRating.text = String.format( Locale.ENGLISH,"%.1f",commentsItem.star.toDouble())
                itemView.ratingBar.rating = commentsItem.star.toFloat()
            }

            itemView.tv_naqashat_name.text = commentsItem.owner!!.name
            if(commentsItem.owner.name.isNullOrBlank()){
                itemView.tv_naqashat_name.text = commentsItem.owner.username
            }else{
                itemView.tv_naqashat_name.text = commentsItem.owner.name
            }

            if (commentsItem.owner.image.isNullOrEmpty()){
                Glide.with(context).load(R.drawable.logo_1).apply(requestOption1).into(itemView.civ_profile)
            }else{
                Glide.with(context).load(commentsItem.owner.image).apply(requestOption1).into(itemView.civ_profile)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewsAdapterVH {
        val view = LayoutInflater.from(context).inflate(R.layout.naqashat_reviews_recycler_item, parent, false)
        return ReviewsAdapterVH(view)
    }

    override fun onBindViewHolder(holder: ReviewsAdapterVH, position: Int) {
        val commentsItem = commentsListing[position]
        holder.bind(commentsItem)
    }

    override fun getItemCount(): Int {
       return commentsListing.size
    }
}