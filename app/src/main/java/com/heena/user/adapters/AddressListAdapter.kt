package com.heena.user.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heena.user.R
import com.heena.user.`interface`.ClickInterface
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.models.AddressItem
import com.heena.user.utils.SharedPreferenceUtility
import kotlinx.android.synthetic.main.saved_address_listing.view.*

class AddressListAdapter(private val context: Context, private val addressList : ArrayList<AddressItem>,
                         private val OnAddressItemClick : ClickInterface.OnAddressItemClick
) : RecyclerView.Adapter<AddressListAdapter.AddressListAdapterVH>() {
    inner class AddressListAdapterVH(val itemView : View) : RecyclerView.ViewHolder(itemView){
        @SuppressLint("SetTextI18n")
        fun bind(addressItem: AddressItem, position: Int) {
            if(sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""].toString() == "ar"){
                itemView.tv_address_title.gravity = Gravity.END
                itemView.tv_address.gravity = Gravity.END
            }
            itemView.tv_address_title.text = addressItem.title
            itemView.tv_address.text = addressItem.flat + "," + addressItem.street

            if (addressItem.is_default==0){
                itemView.tv_is_default.visibility = View.GONE
            }else{
                itemView.tv_is_default.visibility = View.VISIBLE
            }

            itemView.tv_address_edit.setOnClickListener {
                OnAddressItemClick.editAdddress(position)
            }

            itemView.tv_address_delete.setOnClickListener {
                OnAddressItemClick.deleteAddress(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressListAdapterVH {
        val mView = LayoutInflater.from(context).inflate(R.layout.saved_address_listing, parent, false)
        return AddressListAdapterVH(mView)
    }

    override fun onBindViewHolder(holder: AddressListAdapterVH, position: Int) {
        holder.bind(addressList[position], position)
    }

    override fun getItemCount(): Int {
       return addressList.size
    }
}