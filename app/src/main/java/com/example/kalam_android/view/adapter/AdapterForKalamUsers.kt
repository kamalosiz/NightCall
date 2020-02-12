package com.example.kalam_android.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kalam_android.R
import com.example.kalam_android.callbacks.OnClickNewGroupContact
import com.example.kalam_android.databinding.ItemForNewGroupContactListBinding
import com.example.kalam_android.localdb.entities.ContactsData
import com.example.kalam_android.wrapper.GlideDownloader

class AdapterForKalamUsers(
    val context: Context,
    private val onClickNewGroupContact: OnClickNewGroupContact
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private var list: ArrayList<ContactsData>? = null
    private var contactsFilteredList: ArrayList<ContactsData>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return NewGroupContactViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_for_new_group_contact_list,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return contactsFilteredList?.size ?: 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemViewHolder = holder as NewGroupContactViewHolder
        val contact = contactsFilteredList?.get(position)
        itemViewHolder.binding.tvContactName.text = contact?.name
        itemViewHolder.binding.tvContactPhone.text = contact?.number
        GlideDownloader.load(
            context,
            itemViewHolder.binding.ivContactImage,
            contact?.profile_image.toString(),
            0,
            R.drawable.dummy_placeholder
        )
        if (contact?.is_selected == true) {
            itemViewHolder.binding.ivSelectContact.visibility = View.VISIBLE
            itemViewHolder.binding.llSelectContact.setBackgroundResource(R.color.audio_send_background)
        } else {
            itemViewHolder.binding.ivSelectContact.visibility = View.GONE
            itemViewHolder.binding.llSelectContact.setBackgroundResource(R.color.white)
        }
    }

    fun updateList(list: ArrayList<ContactsData>?) {
        this.list = list
        contactsFilteredList = list
        notifyDataSetChanged()
    }

    fun notifyList(list: ArrayList<ContactsData>?, position: Int) {
        contactsFilteredList = list
        notifyItemChanged(position)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    inner class NewGroupContactViewHolder(val binding: ItemForNewGroupContactListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val llSelectContact: LinearLayout = binding.llSelectContact

        init {
            llSelectContact.setOnClickListener {
                onClickNewGroupContact.onMyClick(adapterPosition, contactsFilteredList)
            }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                contactsFilteredList = if (charString.isEmpty()) {
                    list
                } else {
                    val filteredList = ArrayList<ContactsData>()
                    list?.let {
                        for (row in it) {
                            if (row.name?.toLowerCase()?.contains(charString.toLowerCase()) == true ||
                                row.number?.contains(charSequence) == true
                            ) {
                                filteredList.add(row)
                            }
                        }
                    }
                    filteredList
                }

                val filterResults = FilterResults()
                filterResults.values = contactsFilteredList
                return filterResults
            }

            override fun publishResults(
                charSequence: CharSequence,
                filterResults: FilterResults
            ) {
                contactsFilteredList = filterResults.values as ArrayList<ContactsData>?
                notifyDataSetChanged()
            }
        }
    }
}