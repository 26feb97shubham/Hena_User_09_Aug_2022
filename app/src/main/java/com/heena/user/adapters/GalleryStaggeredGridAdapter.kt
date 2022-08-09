package com.heena.user.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.heena.user.R
import com.heena.user.`interface`.ClickInterface
import com.heena.user.models.GalleryItem
import kotlinx.android.synthetic.main.gallery_posts.view.*


class GalleryStaggeredGridAdapter(
    private val context: Context,
    private val ImageUriList: ArrayList<GalleryItem>,
    private val onRecyclerItemClick: ClickInterface.OnRecyclerItemClick
) :
    RecyclerView.Adapter<GalleryStaggeredGridAdapter.PostViewHolder>() {
    inner class PostViewHolder(private val v: View) : RecyclerView.ViewHolder(v) {
        fun bind(imagePath: String, position: Int) {
            val requestOption = RequestOptions().centerCrop().error(R.drawable.def_henna)
            Glide.with(context).load(imagePath)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(p0: GlideException?, p1: Any?, p2: com.bumptech.glide.request.target.Target<Drawable>?, p3: Boolean): Boolean {
                         Log.e("err", p0?.message.toString())
                            v.gallery_image_progress_bar.visibility = View.GONE
                            return false
                        }

                        override fun onResourceReady(p0: Drawable?, p1: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, dataSource: com.bumptech.glide.load.DataSource?, p4: Boolean): Boolean {
                            v.gallery_image_progress_bar.visibility = View.GONE
                            return false
                        }
                    }).apply(requestOption).into(v.gallery_images)

            v.gallery_images.setOnClickListener {
                onRecyclerItemClick.OnClickAction(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
       val view = LayoutInflater.from(context).inflate(R.layout.gallery_posts, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        if (ImageUriList.isEmpty()){
            holder.bind(context.getDrawable(R.drawable.user).toString(), position)
        }else{
            val galleryItem = ImageUriList[position]
            holder.bind(galleryItem.image!!, position)
        }
    }

    override fun getItemCount(): Int {
        return ImageUriList.size
    }
}