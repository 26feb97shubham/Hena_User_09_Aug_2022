package com.heena.user.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heena.user.R
import com.heena.user.models.Notification
import com.heena.user.utils.SharedPreferenceUtility
import kotlinx.android.synthetic.main.item_notifications_list.view.*

class NotificationsAdapter (private val context: Context, private val data:ArrayList<Notification>): RecyclerView.Adapter<NotificationsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_notifications_list, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang,"").equals("ar")){
            holder.itemView.notificationTitle.text = data[position].titleAr
            holder.itemView.notificationsDetails.text = data[position].messageAr
        }else{
            holder.itemView.notificationTitle.text = data[position].title
            holder.itemView.notificationsDetails.text = data[position].message
        }

        holder.itemView.notificationCreatedTime.text = data[position].createAt
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}