package com.heena.user.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heena.user.R
import com.heena.user.`interface`.ClickInterface
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.models.HelpSubCategory
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.content
import kotlinx.android.synthetic.main.sub_category_items.view.*

class SubHelpCategoryAdapter(private val context : Context,
                             private val subCategoryList : ArrayList<HelpSubCategory>,
                             private val subhelpCategoryClicked: ClickInterface.subhelpCategoryClicked)
    : RecyclerView.Adapter<SubHelpCategoryAdapter.SubHelpCategoryAdapterVH>() {
    private var sub_help_category : HelpSubCategory?=null
    inner class SubHelpCategoryAdapterVH(private val itemView : View) : RecyclerView.ViewHolder(itemView){
        fun bind(helpSubCategory: HelpSubCategory, position: Int) {
            val subTitle = if (sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""].equals("ar")){
                helpSubCategory.title_ar
            }else{
                helpSubCategory.title
            }
            itemView.text_sub_category.text = subTitle
            itemView.card_sub_category.setOnClickListener {
                sub_help_category = subCategoryList[position]
                content = sub_help_category!!.content
                Utility.helpSubCategory = sub_help_category
                subhelpCategoryClicked.subHelpCategory(position,sub_help_category!!.content)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubHelpCategoryAdapterVH {
        val view  =
            LayoutInflater.from(context).inflate(R.layout.sub_category_items, parent, false)
        return SubHelpCategoryAdapterVH(view)
    }

    override fun onBindViewHolder(holder: SubHelpCategoryAdapterVH, position: Int) {
        holder.bind(subCategoryList[position], position)
    }

    override fun getItemCount(): Int {
        return subCategoryList.size
    }
}