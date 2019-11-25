package com.example.kalam_android.view.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kalam_android.R
import com.example.kalam_android.callbacks.AddMyStatusClickListener
import com.example.kalam_android.databinding.ItemForAddMyStatusBinding
import com.example.kalam_android.databinding.ItemForRecentStoriesBinding
import com.example.kalam_android.util.toast
import com.example.kalam_android.view.activities.StatusDetailActivity

class StoriesAdapter(val context: Context, val addMyStatusClickListener: AddMyStatusClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var MY_STATUS_VIEW = 0
    private var RECENT_STATUS_VIEW = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == MY_STATUS_VIEW) {
            MyStatusHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_for_add_my_status,
                    parent,
                    false
                )
            )
        } else {
            RecentStatusHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_for_recent_stories,
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemCount(): Int {

        return 10 + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) MY_STATUS_VIEW else RECENT_STATUS_VIEW
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {


        when (getItemViewType(position)) {
            MY_STATUS_VIEW -> {
                val myStatusView = holder as MyStatusHolder
                myStatusView.binding.lvMyStatus.setOnClickListener {
                    addMyStatusClickListener.addMyStatus(myStatusView.binding.root, position)
                }
            }
            RECENT_STATUS_VIEW -> {
                val recentStatusView = holder as RecentStatusHolder
                if (position == 1) {
                    recentStatusView.binding.tvStatusTitle.visibility = View.VISIBLE
                } else {
                    recentStatusView.binding.tvStatusTitle.visibility = View.GONE
                }

                recentStatusView.binding.lvRecentStatus.setOnClickListener {
                    context.startActivity(Intent(context, StatusDetailActivity::class.java))
                }
            }

        }

    }

    inner class MyStatusHolder(val binding: ItemForAddMyStatusBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class RecentStatusHolder(val binding: ItemForRecentStoriesBinding) :
        RecyclerView.ViewHolder(binding.root)

}