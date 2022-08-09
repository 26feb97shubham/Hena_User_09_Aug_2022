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

class GridFeaturedNaqashatAdapter(private val context: Context, private val managersList: ArrayList<ManagerItem>, private val onCLickAction: ClickInterface.OnRecyclerItemClick):
        RecyclerView.Adapter<GridFeaturedNaqashatAdapter.GridFeaturedNaqashatAdapterVH>() {
    inner class GridFeaturedNaqashatAdapterVH(val itemView : View) : RecyclerView.ViewHolder(itemView){
        fun bind(manager: ManagerItem, position: Int) {
            val requestOption = RequestOptions().centerCrop().error(R.drawable.user)
            itemView.ratingBar.rating = manager.comment_star!!.toFloat()
           /* if (manager.name.equals("")){
                itemView.tv_name.text = manager.username
            }else{
                itemView.tv_name.text = manager.name
            }*/

            itemView.tv_name.text = manager.username

            if (manager.comment_star.toString().equals("")||manager.comment_star.toString()==null){
                itemView.ratingBar.rating = 0F
                itemView.txtRating.text = String.format( Locale.ENGLISH,"%.1f",0.0)
            }else{
                itemView.ratingBar.rating = manager.comment_star.toFloat()
                itemView.txtRating.text = String.format( Locale.ENGLISH,"%.1f",manager.comment_star.toDouble())
            }

            if (manager.image!!.isEmpty()){
                Glide.with(context).load(R.drawable.logo_1).apply(requestOption).into(itemView.civ_profile)
            }else{
                Glide.with(context).load(manager.image).apply(requestOption).into(itemView.civ_profile)
            }

            itemView.featured_naqashat_card.setOnClickListener {
                onCLickAction.OnClickAction(position)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridFeaturedNaqashatAdapterVH {
        val view  = LayoutInflater.from(context).inflate(R.layout.grid_item_featured_naqashat, parent, false)
        return GridFeaturedNaqashatAdapterVH(view)
    }

    override fun onBindViewHolder(holder: GridFeaturedNaqashatAdapterVH, position: Int) {
        val manager = managersList[position]
        holder.bind(manager, position)
    }

    override fun getItemCount(): Int {
        return managersList.size
    }
}