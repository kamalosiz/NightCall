package com.example.kalam_android.view.adapter

import android.content.Context
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.PagerAdapter
import com.example.kalam_android.R
import com.example.kalam_android.callbacks.MyClickListener
import com.example.kalam_android.databinding.ItemForStatusDetailBinding
import com.example.kalam_android.repository.model.ImageModel

class StatusPagerAdapter(
    private val context: Context,
    private val imageModelArrayList: ArrayList<ImageModel>
) : PagerAdapter() {
    private var myClickListener : MyClickListener?=null
    private lateinit var binding: ItemForStatusDetailBinding

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.item_for_status_detail,
            container,
            false
        )
        binding.ivStatusImage.setImageResource(imageModelArrayList[position].image)
        binding.ivUpArrow.setOnClickListener {

            myClickListener?.myOnClick(binding.root,position)
        }
        container.addView(binding.root, 0)
        return binding.root
    }

    public  fun setClickListener(myClickListener : MyClickListener){
        this.myClickListener = myClickListener
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return imageModelArrayList.size
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {}

    override fun saveState(): Parcelable? {
        return null
    }
}