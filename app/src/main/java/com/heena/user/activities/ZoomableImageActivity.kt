package com.heena.user.activities

import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.heena.user.R
import com.heena.user.adapters.ImageViewAdapter
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.extras.StartSnapHelper
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import kotlinx.android.synthetic.main.activity_zoomable_image.*

class ZoomableImageActivity : AppCompatActivity() {
    private var gallery = ArrayList<String>()
    private var imageViewAdapter : ImageViewAdapter?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zoomable_image)
        Utility.changeLanguage(
            this,
            sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]
        )
        if(intent.extras!=null) {
            gallery = intent.extras!!.getStringArrayList("gallery") as ArrayList<String>
        }


        if (gallery.size==0){
            naqashatImagesRecyclerView.visibility = View.GONE
            tv_no_images_found.visibility = View.VISIBLE
        }else{
            naqashatImagesRecyclerView.visibility = View.VISIBLE
            tv_no_images_found.visibility = View.GONE
        }

        naqashatImagesRecyclerView.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL,
            false)

        imageViewAdapter = ImageViewAdapter(this, gallery)
        naqashatImagesRecyclerView.adapter = imageViewAdapter
        StartSnapHelper(this).attachToRecyclerView(naqashatImagesRecyclerView)
        imageViewAdapter!!.notifyDataSetChanged()

        ivBack.setOnClickListener{
            onBackPressed()
        }
    }
}