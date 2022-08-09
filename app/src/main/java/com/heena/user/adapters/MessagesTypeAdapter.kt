package com.heena.user.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.heena.user.R
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.models.Message
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.booking_item_type
import com.heena.user.utils.Utility.sender_admin
import kotlinx.android.synthetic.main.row_chat_partner.view.*
import kotlinx.android.synthetic.main.row_chat_user.view.*
import kotlinx.android.synthetic.main.row_chat_user.view.message

class MessagesTypeAdapter(
    private val context : Context,
    private val messageList : ArrayList<Message>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (sender_admin) {
            1 -> {
                val view  =
                    LayoutInflater.from(context).inflate(R.layout.row_chat_user, parent, false)
                UserChatAdapterVH(view)
            }
            2 -> {
                val view  =
                    LayoutInflater.from(context).inflate(R.layout.row_chat_partner, parent, false)
                AdminChatAdapterVH(view)
            }
            else -> {
                val view  =
                    LayoutInflater.from(context).inflate(R.layout.row_chat_user, parent, false)
                UserChatAdapterVH(view)
            }
        }
    }

    override fun getItemCount(): Int {
       return messageList.size
    }

    inner class UserChatAdapterVH(private val itemView : View) : RecyclerView.ViewHolder(itemView){
        fun bind(message: Message, position: Int, context: Context) {
            val requestOption = RequestOptions().centerCrop().error(R.drawable.def_henna)
            if (message.message_type.equals("1")){
                itemView.card_image.visibility = View.GONE
                itemView.message.visibility = View.VISIBLE
                itemView.message.text=message.message
            }else{
                itemView.card_image.visibility = View.VISIBLE
                itemView.message.visibility = View.GONE
                Glide.with(context).load(message.message).apply(requestOption).into(itemView.post_image)
            }
        }

    }

    inner class AdminChatAdapterVH(private val itemView : View) : RecyclerView.ViewHolder(itemView){
        fun bind(message: Message, position: Int, context: Context) {
            val requestOption = RequestOptions().centerCrop().error(R.drawable.def_henna)
            if (message.message_type.equals("1")){
                itemView.card_image_admin.visibility = View.GONE
                itemView.message_admin.visibility = View.VISIBLE
                itemView.message_admin.text=message.message
            }else{
                itemView.card_image_admin.visibility = View.VISIBLE
                itemView.message_admin.visibility = View.GONE
                Glide.with(context).load(message.message).apply(requestOption).into(itemView.post_image_admin)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (messageList[position].sender_id==sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0]){
            sender_admin = 1
        }else{
            sender_admin = 2
        }
        return sender_admin
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position]
        if (messageList[position].sender_id==sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0]){
            booking_item_type=1
        }else{
            booking_item_type=2
        }
        when (booking_item_type) {
            1 -> {
                val userchatholder = holder as UserChatAdapterVH
                userchatholder.bind(message, position, context)
            }
            2 -> {
                val adminchatholder = holder as AdminChatAdapterVH
                adminchatholder.bind(message, position, context)
            }
            else -> {
                val userchatholder = holder as UserChatAdapterVH
                userchatholder.bind(message, position, context)
            }
        }
    }
}