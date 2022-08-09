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
import com.heena.user.models.ManagerItem
import kotlinx.android.synthetic.main.item_featured_naqashat.view.*
import java.util.*
import kotlin.collections.ArrayList

class FeaturedNaqashatAdapter(private val context: Context, private val managersList: ArrayList<ManagerItem>, private val onCLickAction: ClickInterface.OnRecyclerItemClick) : RecyclerView.Adapter<FeaturedNaqashatAdapter.FeaturedNaqashatVH>() {
    inner class FeaturedNaqashatVH(val itemView : View) : RecyclerView.ViewHolder(itemView){
        fun bind(manager: ManagerItem, position: Int) {
            itemView.ratingBar.rating = manager.comment_star!!.toFloat()
            if (manager.name.equals("")){
                itemView.tv_name.text = manager.username
            }else{
                itemView.tv_name.text = manager.name
            }

            if (manager.comment_star.toString().equals("")||manager.comment_star.toString()==null){
                itemView.ratingBar.rating = 0F
                itemView.txtRating.text = String.format( Locale.ENGLISH,"%.1f",0.0)
            }else{
                itemView.ratingBar.rating = manager.comment_star.toFloat()
                itemView.txtRating.text = String.format( Locale.ENGLISH,"%.1f",manager.comment_star.toDouble())
            }
            val managerImage = if (manager.image!!.isEmpty()){
                context.getDrawable(R.drawable.logo_1).toString()
            }else{
                manager.image.toString()
            }

            Glide.with(context).load(managerImage).apply(RequestOptions().error(R.drawable.user)).into(itemView.civ_profile)

            itemView.featured_naqashat_card.setOnClickListener {
                onCLickAction.OnClickAction(position)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeaturedNaqashatVH {
        val view  =LayoutInflater.from(context).inflate(R.layout.item_featured_naqashat, parent, false)
        return FeaturedNaqashatVH(view)
    }

    override fun onBindViewHolder(holder: FeaturedNaqashatVH, position: Int) {
        val manager = managersList[position]
        holder.bind(manager, position)
    }

    override fun getItemCount(): Int {
       return managersList.size
    }
}