package com.heena.user.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heena.user.R
import com.heena.user.models.BookingSlots
import com.heena.user.models.Slots
import kotlinx.android.synthetic.main.layout_slots.view.*
import org.json.JSONArray

class SlotsAdapter(
    private val context: Context,
    private val slotsList: ArrayList<Slots>,
    private val myBookingSlotsList: ArrayList<BookingSlots>,
    private val slotsClicked: slotsCallback
):
    RecyclerView.Adapter<SlotsAdapter.SlotsAdapterVH>() {
    inner class SlotsAdapterVH(private val itemView : View) : RecyclerView.ViewHolder(itemView){
        fun bind(slots: Slots, position: Int){
            itemView.tvSlots.text = slots.fromTimeToTime
            if (!myBookingSlotsList.isEmpty()){
                for (i in 0 until myBookingSlotsList.size){
                    if (slots.fromTime?.equals(myBookingSlotsList[i].fromTime)!!){
                        itemView.isEnabled = false
                        itemView.setBackgroundResource(R.drawable.gold_solid_box)
                    }else{
                        itemView.isEnabled = true
                        itemView.setBackgroundResource(R.drawable.gray_solid_box)
                    }
                }
            }else{
                itemView.isEnabled = true
                itemView.setBackgroundResource(R.drawable.gray_solid_box)
            }

            itemView.setOnClickListener {
                slotsClicked.slotsClicked(position = position, itemView)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotsAdapterVH {
        val view =
            LayoutInflater.from(context).inflate(R.layout.layout_slots, parent, false)
        return SlotsAdapterVH(view)
    }

    override fun onBindViewHolder(holder: SlotsAdapterVH, position: Int) {
        val slots = slotsList[position]
        holder.bind(slots, position)
    }

    override fun getItemCount(): Int {
        return slotsList.size
    }

    interface slotsCallback{
        fun slotsClicked(position: Int, view: View)
    }

    public fun getAllSelectedSlots(slotsList : ArrayList<Slots>) : JSONArray {
        var slotsJson = JSONArray()
        for (i in 0 until slotsList.size){
            if(slotsList[i].isChecked){
                slotsJson.put(slotsList[i].fromTimeToTime)
            }
        }
        return slotsJson
    }
}