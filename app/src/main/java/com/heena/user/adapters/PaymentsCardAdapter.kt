package com.heena.user.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heena.user.R

class PaymentsCardAdapter(private val  context: Context) : RecyclerView.Adapter<PaymentsCardAdapter.PaymentsCardAdapterVH>() {
    inner class PaymentsCardAdapterVH(itemVIew : View) : RecyclerView.ViewHolder(itemVIew){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentsCardAdapterVH {
        val view  =
                LayoutInflater.from(context).inflate(R.layout.item_cards_list, parent, false)
        return PaymentsCardAdapterVH(view)
    }

    override fun onBindViewHolder(holder: PaymentsCardAdapterVH, position: Int) {

    }

    override fun getItemCount(): Int {
        return 3
    }
}