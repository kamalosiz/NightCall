package com.example.kalam_android.view.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.kalam_android.repository.model.ProfileData
import com.example.kalam_android.view.fragments.ProfileImagesFragment
import com.example.kalam_android.view.fragments.ProfileOverviewFragment
import com.example.kalam_android.view.fragments.ProfileVideosFragment

class ViewPagerAdapterFragment(fm: FragmentManager, val list: ArrayList<ProfileData>) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> ProfileOverviewFragment.newInstance(list)
            1 -> ProfileImagesFragment()
            else -> {
                return ProfileVideosFragment()
            }
        }
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getItemPosition(`object`: Any): Int {
        if (`object` is ProfileOverviewFragment){

        }
        return super.getItemPosition(`object`)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Overview"
            1 -> "Photos"
            else -> {
                return "Videos"
            }
        }
    }

}