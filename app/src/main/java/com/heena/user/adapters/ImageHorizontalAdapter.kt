package com.heena.user.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.heena.user.R
import kotlinx.android.synthetic.main.heena_designs_layout_item.view.*

class ImageHorizontalAdapter(
        private val context: Context,
        private val ImageList: ArrayList<String>
) : RecyclerView.Adapter<ImageHorizontalAdapter.ImageHorizontalAdapterVH>() {
    inner class ImageHorizontalAdapterVH(itemView : View) : RecyclerView.ViewHolder(itemView) {
        fun bind(image: String) {
            val requestOption = RequestOptions().centerCrop().error(R.drawable.def_henna)
            Glide.with(context).load(image).apply(requestOption).into(itemView.iv_heena)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHorizontalAdapterVH {
        val view = LayoutInflater.from(context).inflate(R.layout.heena_designs_layout_item, parent, false)
        return ImageHorizontalAdapterVH(view)
    }

    override fun onBindViewHolder(holder: ImageHorizontalAdapterVH, position: Int) {
        if (ImageList.isEmpty()){
            holder.bind(context.getDrawable(R.drawable.def_henna).toString())
        }else{
            val image = ImageList[position]
            holder.bind(image)
        }
    }

    override fun getItemCount(): Int {
        return  ImageList.size
    }
}