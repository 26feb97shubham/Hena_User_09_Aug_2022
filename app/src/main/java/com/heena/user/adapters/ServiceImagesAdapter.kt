package com.heena.user.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.heena.user.R
import kotlinx.android.synthetic.main.service_image_list_item.view.*

class ServiceImagesAdapter(
        private val context: Context,
        private val imagesList : ArrayList<String>
) : RecyclerView.Adapter<ServiceImagesAdapter.ServiceImagesAdapterVH>() {
    inner class ServiceImagesAdapterVH(private val serviceImageItemView:View) : RecyclerView.ViewHolder(serviceImageItemView){
        fun bind(image: String) {
            val requestOption1 = RequestOptions().centerCrop().error(R.drawable.def_henna)
            Glide.with(context).load(image).apply(requestOption1).into(serviceImageItemView.img)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceImagesAdapterVH {
        val mView = LayoutInflater.from(context).inflate(R.layout.service_image_list_item, parent, false)
        return ServiceImagesAdapterVH(mView)
    }

    override fun onBindViewHolder(holder: ServiceImagesAdapterVH, position: Int) {
       val image =  if (imagesList.isEmpty()){
           context.getDrawable(R.drawable.def_henna).toString()
       }else{
           imagesList[position]
       }
        holder.bind(image)
    }

    override fun getItemCount(): Int {
        return imagesList.size
    }
}