package com.heena.user.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class CardSliderAdapter(fa: FragmentActivity, var fragments: ArrayList<Fragment>) : FragmentStateAdapter(fa)  {
    override fun getItemCount(): Int {
        return this.fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return this.fragments[position]
    }
}