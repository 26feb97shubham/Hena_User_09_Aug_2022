package com.heena.user.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heena.user.R
import com.heena.user.`interface`.ClickInterface
import com.heena.user.models.ManagerItem
import kotlinx.android.synthetic.main.available_naqasha_listing_item.view.*
import kotlinx.android.synthetic.main.available_naqasha_listing_item.view.ratingBar
import kotlinx.android.synthetic.main.available_naqasha_listing_item.view.txtRating
import kotlinx.android.synthetic.main.grid_services_recycler_items.view.*
import java.util.*
import kotlin.collections.ArrayList

class AvailableNaqashatAdapter(
        private val context: Context, private val managersList: ArrayList<ManagerItem>,
        private val onCLickAction: ClickInterface.OnRecyclerItemClick,
        private val onCalendarClick: ClickInterface.OnCalendarClick
) : RecyclerView.Adapter<AvailableNaqashatAdapter.AvailableNaqashatAdapterVH>() {
    inner class AvailableNaqashatAdapterVH(private val availableNaqashatView : View) : RecyclerView.ViewHolder(availableNaqashatView){
        fun bind(managerItem: ManagerItem) {
            availableNaqashatView.tv_name.text = managerItem.name
            availableNaqashatView.tv_address.text = managerItem.address

            if (managerItem.comment_star!!.toString()==null || managerItem.comment_star.toString().equals("")){
                availableNaqashatView.ratingBar.rating = 0F
                availableNaqashatView.txtRating.text = String.format("%.1f", 0.0)
            }else{
                availableNaqashatView.ratingBar.rating = managerItem.comment_star.toFloat()
                availableNaqashatView.txtRating.text = String.format(Locale.ENGLISH,"%.1f",managerItem.comment_star.toDouble())
            }

            availableNaqashatView.calendar_layout.setOnClickListener {
                onCalendarClick.OnCalendar()
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvailableNaqashatAdapterVH {
        val view  = LayoutInflater.from(context).inflate(R.layout.available_naqasha_listing_item, parent, false)
        return AvailableNaqashatAdapterVH(view)
    }

    override fun onBindViewHolder(holder: AvailableNaqashatAdapterVH, position: Int) {
        val managerItem = managersList[position]
        holder.bind(managerItem)
    }

    override fun getItemCount(): Int {
        return managersList.size
    }
}