package com.heena.user.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heena.user.R
import com.heena.user.`interface`.ClickInterface
import com.heena.user.models.AddressItem
import kotlinx.android.synthetic.main.addresses_listing.view.*
import kotlinx.android.synthetic.main.saved_address_listing.view.tv_address
import kotlinx.android.synthetic.main.saved_address_listing.view.tv_address_title

class AddressesAdapter(val context: Context, private val addressList: ArrayList<AddressItem>,
                       val OnAddressClick: ClickInterface.OnAddAddressClick)
    : RecyclerView.Adapter<AddressesAdapter.AddressesAdapterVH>() {
    var mSelectedItem = -1
    inner class AddressesAdapterVH(private val addressesitemView : View) : RecyclerView.ViewHolder(addressesitemView) {
        @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
        fun bind(address: AddressItem, position: Int) {
            addressesitemView.tv_address_title.text = address.title
            addressesitemView.tv_address.text = address.flat + "," + address.street

            addressesitemView.rb_address.isChecked = position == mSelectedItem

            addressesitemView.setOnClickListener {
                mSelectedItem = adapterPosition
                notifyDataSetChanged()
                OnAddressClick.OnAddAddress(mSelectedItem, address)
            }

            addressesitemView.rb_address.setOnClickListener {
                mSelectedItem = adapterPosition
                notifyDataSetChanged()
                OnAddressClick.OnAddAddress(mSelectedItem, address)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressesAdapterVH {
        val mView = LayoutInflater.from(context).inflate(R.layout.addresses_listing, parent, false)
        return AddressesAdapterVH(mView)
    }

    override fun onBindViewHolder(holder: AddressesAdapterVH, position: Int) {
        val address = addressList[position]
        holder.bind(address, position)
    }

    override fun getItemCount(): Int {
        return addressList.size
    }
}