package com.example.kalam_android.view.adapter

import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kalam_android.R
import com.example.kalam_android.databinding.ItemForContactListBinding
import com.example.kalam_android.databinding.ItemForNewGroupContactBinding
import com.example.kalam_android.repository.model.ContactsList
import com.example.kalam_android.wrapper.GlideDownloder

class AdapterForContacts(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var contactInfo: ArrayList<ContactsList>? = null

    companion object {
        const val VIEW_NEW_GROUP_AND_NUMBER = 0
        const val VIEW_CONTACT_LIST = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {

            VIEW_NEW_GROUP_AND_NUMBER -> {
                NewGroupAndContactViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_for_new_group_contact,
                        parent,
                        false
                    )
                )
            }
            VIEW_CONTACT_LIST -> {
                ContactViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_for_contact_list,
                        parent,
                        false
                    )
                )
            }
            else -> {
                ContactViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_for_contact_list,
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return contactInfo?.size ?: 0
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> {
                VIEW_NEW_GROUP_AND_NUMBER
            }
            1 -> {
                VIEW_CONTACT_LIST
            }
            else -> {
                VIEW_CONTACT_LIST

            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (getItemViewType(position)) {
            VIEW_NEW_GROUP_AND_NUMBER -> {
                val itemHolder = holder as NewGroupAndContactViewHolder
                itemHolder.binding.lvNewGroup.setOnClickListener {
                }
                itemHolder.binding.lvNewContact.setOnClickListener {
                    val intent = Intent(Intent.ACTION_INSERT)
                    intent.type = ContactsContract.Contacts.CONTENT_TYPE
                    context.startActivity(intent)
                }
            }
            VIEW_CONTACT_LIST -> {
                val item = contactInfo?.get(position - 1)
                val itemHolder = holder as ContactViewHolder
                itemHolder.binding.tvContactName.text = item?.name
                itemHolder.binding.tvContactPhone.text = item?.number
                GlideDownloder.load(
                    context,
                    itemHolder.binding.ivContactImage,
                    item?.profile_image,
                    R.drawable.dummy_placeholder,
                    R.drawable.dummy_placeholder
                )
            }

        }
    }

    fun updateList(listOfContact: ArrayList<ContactsList>?) {
        contactInfo?.clear()
        contactInfo = listOfContact
        contactInfo?.size?.plus(1)
        notifyDataSetChanged()
    }
}

class ContactViewHolder(val binding: ItemForContactListBinding) :
    RecyclerView.ViewHolder(binding.root)

class NewGroupAndContactViewHolder(val binding: ItemForNewGroupContactBinding) :
    RecyclerView.ViewHolder(binding.root)
