package com.heena.user.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.heena.user.R
import com.heena.user.`interface`.ClickInterface
import com.heena.user.models.BookingItem
import com.heena.user.utils.Utility.booking_item_type
import com.heena.user.utils.Utility.convertDoubleValueWithCommaSeparator
import kotlinx.android.synthetic.main.booking_history_recycler_items_listing.view.*
import kotlinx.android.synthetic.main.current_booking_recycler_items_listing.view.*
import kotlinx.android.synthetic.main.current_booking_recycler_items_listing.view.ratingBar
import kotlinx.android.synthetic.main.current_booking_recycler_items_listing.view.supplierImg
import kotlinx.android.synthetic.main.current_booking_recycler_items_listing.view.tv_bookingId
import kotlinx.android.synthetic.main.current_booking_recycler_items_listing.view.tv_price
import kotlinx.android.synthetic.main.current_booking_recycler_items_listing.view.tv_service
import kotlinx.android.synthetic.main.current_booking_recycler_items_listing.view.txtRating
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class BookingAdapter(
    private val context: Context,
    private val bookingList: ArrayList<BookingItem>,
    private val onRecyclerItemClick: ClickInterface.OnRecyclerItemClick
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemViewType(position: Int): Int {
        return booking_item_type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (booking_item_type) {
            1 -> {
                val view  =
                    LayoutInflater.from(context).inflate(R.layout.current_booking_recycler_items_listing, parent, false)
                CurrentBookingServicesAdapterVH(view)
            }
            2 -> {
                val view  =
                    LayoutInflater.from(context).inflate(R.layout.booking_history_recycler_items_listing, parent, false)
                BookingHistoryAdapterVH(view)
            }
            else -> {
                val view  =
                    LayoutInflater.from(context).inflate(R.layout.current_booking_recycler_items_listing, parent, false)
                CurrentBookingServicesAdapterVH(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val bookingItem = bookingList[position]
        when (booking_item_type) {
            1 -> {
                val currentBookingHolder = holder as CurrentBookingServicesAdapterVH
                currentBookingHolder.bind(bookingItem, position, context)
            }
            2 -> {
                val historyBookingHolder = holder as BookingHistoryAdapterVH
                historyBookingHolder.bind(bookingItem, position)
            }
            else -> {
                val currentBookingHolder = holder as CurrentBookingServicesAdapterVH
                currentBookingHolder.bind(bookingItem, position, context)
            }
        }
    }

    override fun getItemCount(): Int {
        return bookingList.size
    }

    inner class CurrentBookingServicesAdapterVH(private val bookingItemView : View) : RecyclerView.ViewHolder(bookingItemView) {
        fun bind(bookingItem: BookingItem, position: Int, context: Context) {
            val bookingId = "BOOK#" + bookingItem.booking_id
            bookingItemView.tv_bookingId.text = bookingId
            bookingItemView.tv_service.text = bookingItem.service!!.name
            bookingItemView.tv_price.text = "AED ${convertDoubleValueWithCommaSeparator(bookingItem.price!!)}"
            val managerImage = if (bookingItem.manager!!.image!!.isEmpty()){
                context.getDrawable(R.drawable.user).toString()
            }else{
                bookingItem.manager.image
            }
            if(managerImage.isNullOrEmpty()){
                Glide.with(context).load(R.drawable.logo_1)
                    .apply(RequestOptions().error(R.drawable.user))
                    .into(bookingItemView.supplierImg)
            }else{
                Glide.with(context).load(managerImage)
                    .apply(RequestOptions().error(R.drawable.user))
                    .into(bookingItemView.supplierImg)
            }

            if (bookingItem.manager.avg_star.equals("")||bookingItem.manager.avg_star.equals(null)){
                bookingItemView.ratingBar.rating = 0F
                bookingItemView.txtRating.text = "0"
            }else{
                bookingItemView.ratingBar.rating = bookingItem.manager.avg_star!!.toFloat()
                bookingItemView.txtRating.text = String.format( Locale.ENGLISH,"%.1f",bookingItem.manager.avg_star.toDouble())
            }


            when(bookingItem.status){
                1 -> bookingItemView.tv_pending.apply {
                    this.text = context.getString(R.string.accepted)
                    this.isAllCaps = true
                    this.setTextColor(Color.parseColor("#37CC37"))
                }
                2 -> bookingItemView.tv_pending.apply {
                    this.text = context.getString(R.string.cancelled)
                    this.isAllCaps = true
                    this.setTextColor(Color.parseColor("#FF0909"))
                }
                3 -> bookingItemView.tv_pending.apply {
                    this.text = context.getString(R.string.completed)
                    this.isAllCaps = true
                    this.setTextColor(Color.parseColor("#37CC37"))
                }
                4 -> bookingItemView.tv_pending.apply {
                    this.text = context.getString(R.string.pending)
                    this.isAllCaps = true
                    this.setTextColor(Color.parseColor("#FF9F54"))
                }
            }

            bookingItemView.setOnClickListener {
                onRecyclerItemClick.OnClickAction(position)
            }
        }
    }

    inner class BookingHistoryAdapterVH(private val bookingHistoryItemView : View) : RecyclerView.ViewHolder(bookingHistoryItemView) {
        fun bind(bookingItem: BookingItem, position: Int) {
            val bookingId = "BOOK#" + bookingItem.booking_id
            bookingHistoryItemView.tv_bookingId.text = bookingId
            bookingHistoryItemView.tv_service.text = bookingItem.service!!.name
            bookingHistoryItemView.tv_date.text = bookingItem.booking_date
            bookingHistoryItemView.tv_price.text = "AED ${convertDoubleValueWithCommaSeparator(bookingItem.price!!)}"
            val managerImage = if (bookingItem.manager!!.image!!.isEmpty()){
                context.getDrawable(R.drawable.logo_1).toString()
            }else{
                bookingItem.manager.image
            }
            Glide.with(context).load(managerImage).apply(RequestOptions().error(R.drawable.user)).into(bookingHistoryItemView.supplierImg)
            if (bookingItem.manager.avg_star.equals("")||bookingItem.manager.avg_star.equals(null)){
                bookingHistoryItemView.ratingBar.rating = 0F
                bookingHistoryItemView.txtRating.text = "0"
            }else{
                bookingHistoryItemView.ratingBar.rating = bookingItem.manager.avg_star!!.toFloat()
                bookingHistoryItemView.txtRating.text = String.format(Locale.ENGLISH,"%.1f",bookingItem.manager.avg_star.toDouble())
            }

            when(bookingItem.status){
                1 -> bookingHistoryItemView.tv_pending_1.apply {
                    this.text = context.getString(R.string.accepted)
                    this.isAllCaps = true
                    this.setTextColor(Color.parseColor("#37CC37"))
                }
                2 -> bookingHistoryItemView.tv_pending_1.apply {
                    this.text = context.getString(R.string.cancelled)
                    this.isAllCaps = true
                    this.setTextColor(Color.parseColor("#FF0909"))
                }
                3 -> bookingHistoryItemView.tv_pending_1.apply {
                    this.text = context.getString(R.string.completed)
                    this.isAllCaps = true
                    this.setTextColor(Color.parseColor("#37CC37"))
                }
                4 -> bookingHistoryItemView.tv_pending_1.apply {
                    this.text = context.getString(R.string.pending)
                    this.isAllCaps = true
                    this.setTextColor(Color.parseColor("#FF9F54"))
                }
            }

            bookingHistoryItemView.setOnClickListener {
                onRecyclerItemClick.OnClickAction(position)
            }
        }
    }
}