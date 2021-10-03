package ir.shojaei.picmaker

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MViewPagerAdapter(fa:FragmentActivity):FragmentStateAdapter(fa) {
    override fun getItemCount()=2

    override fun createFragment(position: Int)=
        if(position==0)TextFragment() else ImageFragment()
}