package com.heena.user.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior.getTag
import androidx.recyclerview.widget.RecyclerView
import com.heena.user.R
import com.heena.user.`interface`.ClickInterface
import com.heena.user.models.Cards
import kotlinx.android.synthetic.main.item_cards_list.view.*
import com.heena.user.*
import com.heena.user.utils.Utility.mSelectedItem
import com.heena.user.*
import com.heena.user.*
import kotlin.math.absoluteValue
import com.paypal.android.sdk.cb

import com.heena.user.*

class PaymentCardsAdapter(private val context: Context, private val data:ArrayList<Cards>, private val clickInst: ClickInterface.ClickPosInterface)
    : RecyclerView.Adapter<PaymentCardsAdapter.MyViewHolder>(){
    private var lastCheckedPosition = -1
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_cards_list, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.itemView.tv_card_title.text = data[position].brand
        val card_ending = "Ending in "+data[position].number
        holder.itemView.tv_ending_card_number.text = card_ending

        holder.itemView.iv_selected_unselected.isChecked = position==lastCheckedPosition

        holder.itemView.iv_selected_unselected.setOnClickListener {
           // val copyOfLastCheckedPosition = lastCheckedPosition
            lastCheckedPosition = holder.bindingAdapterPosition

          //  mSelectedItem = lastCheckedPosition
//            notifyItemChanged(copyOfLastCheckedPosition)
//            notifyItemChanged(lastCheckedPosition)
            notifyDataSetChanged()
            clickInst.clickPostion(lastCheckedPosition, "")
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}