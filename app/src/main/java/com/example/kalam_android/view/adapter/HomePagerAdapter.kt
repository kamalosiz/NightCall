package com.example.kalam_android.view.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.kalam_android.view.fragments.*

class HomePagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        var f: Fragment = ChatsFragment()
        when (position) {
            0 -> f = ChatsFragment()
            1 -> f = CallsFragment()
            2 -> f = StoriesFragment()
            3 -> f = ProfileFragment()
            4 -> f = SettingFragment()
        }
        /*  val args = Bundle()
          args.putBoolean(Constants.KEY_IS_FROM_ACTIVITY, false)
          f.arguments = args*/
        return f
    }

    override fun getCount(): Int {
        return 5           // As there are only 5 Tabs
    }
}