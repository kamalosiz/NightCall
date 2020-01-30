package com.example.kalam_android.view.adapter

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kalam_android.R
import com.example.kalam_android.callbacks.OnClickNewGroupContact
import com.example.kalam_android.databinding.ItemForAddGroupNameBinding
import com.example.kalam_android.databinding.ItemForNewGroupContactBinding
import com.example.kalam_android.databinding.ItemForNewGroupContactListBinding
import com.example.kalam_android.repository.model.ContactsData
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.wrapper.GlideDownloader

class AdapterForNewGroupContact(val context: Context, val onClickNewGroupContact: OnClickNewGroupContact) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    private val VIEW_HEADER = 0
    private val VIEW_ITEM = 1
    private var list: ArrayList<ContactsData>? = null
    private var contactList2: ArrayList<ContactsData>? = null
    private var contactsFilteredList: ArrayList<ContactsData>? = null
    private var groupTitle = ""
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_HEADER) {
            NewGroupNameViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_for_add_group_name, parent, false))
        } else {
            NewGroupContactViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_for_new_group_contact_list, parent, false))
        }
    }

    override fun getItemCount(): Int {
        return (list!!.size.plus(1) ?: 0)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Debugger.e("Contact List For Group : ", "${list}")
        when (getItemViewType(holder.adapterPosition)) {
            VIEW_HEADER -> {
                val itemViewHolder = holder as NewGroupNameViewHolder
                groupTitle = itemViewHolder.binding.etGroupName.text.toString()
                itemViewHolder.binding.etGroupName.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(p0: Editable?) {
                        groupTitle = p0.toString()
                        onClickNewGroupContact.groupName(groupTitle)
                    }

                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        groupTitle = p0.toString()
                        onClickNewGroupContact.groupName(groupTitle)
                    }
                })
            }
            VIEW_ITEM -> {

                val itemViewHolder = holder as NewGroupContactViewHolder
                itemViewHolder.binding.tvContactName.text = list?.get(position - 1)?.name
                itemViewHolder.binding.tvContactPhone.text = list?.get(position - 1)?.number
                GlideDownloader.load(context, itemViewHolder.binding.ivContactImage, list!![position - 1].profile_image, 0, R.drawable.dummy_placeholder)

                itemViewHolder.itemView.setOnClickListener {
                    if (itemViewHolder.binding.ivSelectContact.visibility == View.GONE) {
                        itemViewHolder.binding.ivSelectContact.visibility = View.VISIBLE
                        itemViewHolder.binding.llSelectContact.setBackgroundResource(R.color.audio_send_background)
                        onClickNewGroupContact.addToList(list?.get(position - 1)!!)
                    } else {
                        itemViewHolder.binding.ivSelectContact.visibility = View.GONE
                        itemViewHolder.binding.llSelectContact.setBackgroundResource(R.color.white)
                        onClickNewGroupContact.removeToList(list?.get(position - 1)!!)
                    }
                }
            }
        }
    }

    fun updateList(list: ArrayList<ContactsData>) {
        this.list = list
        contactsFilteredList = list
        contactList2 = list
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            VIEW_HEADER
        } else {
            VIEW_ITEM
        }
    }


    inner class NewGroupNameViewHolder(val binding: ItemForAddGroupNameBinding) : RecyclerView.ViewHolder(binding.root)

    inner class NewGroupContactViewHolder(val binding: ItemForNewGroupContactListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    contactsFilteredList = contactList2
                } else {
                    val filteredList = ArrayList<ContactsData>()
                    contactList2?.let {
                        for (row in it) {
                            if (row.name?.toLowerCase()?.contains(charString.toLowerCase()) == true ||
                                    row.number?.contains(charSequence) == true
                            ) {
                                filteredList.add(row)
                            }
                        }
                    }
                    contactsFilteredList = filteredList
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
                contactsFilteredList?.size?.plus(1)
                list = contactsFilteredList
                notifyDataSetChanged()
            }
        }
    }
}