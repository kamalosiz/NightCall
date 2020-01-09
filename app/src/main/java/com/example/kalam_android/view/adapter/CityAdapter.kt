package com.example.kalam_android.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.kalam_android.R

class CityAdapter (val pContext: Context, var list: ArrayList<String>, val listener: CityItemClickListener)
    : RecyclerView.Adapter<CityAdapter.ViewHolder>(){


    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val tvItem: AppCompatTextView = itemView.findViewById(R.id.tvItem)
        val loc: ConstraintLayout = itemView.findViewById(R.id.loc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =  LayoutInflater.from(parent.context).inflate(R.layout.item_for_countries, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
    fun updateList(list :ArrayList<String>){
        this.list = list
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvItem.text = list[position]
        holder.loc.setOnClickListener{
            listener.onCityItemClick(list[position], position)
        }
    }

    interface CityItemClickListener {
        fun onCityItemClick(item: String, position: Int)
    }
}