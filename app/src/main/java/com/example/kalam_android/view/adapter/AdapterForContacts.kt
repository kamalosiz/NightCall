package com.example.kalam_android.view.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kalam_android.R
import com.example.kalam_android.databinding.ItemForContactListBinding
import com.example.kalam_android.databinding.ItemForNewGroupContactBinding
import com.example.kalam_android.repository.model.ContactsData
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.view.activities.ChatDetailActivity
import com.example.kalam_android.view.activities.NewGroupActivity
import com.example.kalam_android.wrapper.GlideDownloader

class AdapterForContacts(val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private var contactList: ArrayList<ContactsData>? = null
    private var contactList2: ArrayList<ContactsData>? = null
    private var contactsFilteredList: ArrayList<ContactsData>? = null
    private val TAG = this.javaClass.simpleName
    private val VIEW_HEADER = 0
    private val VIEW_ITEM = 1
    private var tempContactList : ArrayList<ContactsData> = ArrayList()
    private val onClickListener: View.OnClickListener

    init {
        onClickListener = AdapterClickListener()
    }

    fun updateList(listOfContact: ArrayList<ContactsData>?) {
        contactList?.clear()
        contactList = listOfContact
        contactList?.size?.plus(1)
        contactsFilteredList = contactList
        contactList2 = contactList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == VIEW_HEADER) {
            NewGroupAndContactViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_for_new_group_contact,
                    parent,
                    false
                )
            )
        } else {
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

    override fun getItemCount(): Int {
        return (contactList?.size?.plus(1) ?: 0)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_HEADER else VIEW_ITEM
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        for (i in  contactList!!.indices){
            if (contactList!![i].id != 0){
                tempContactList.clear()
                tempContactList.add(contactList!![i])
            }
        }
        when (getItemViewType(position)) {
            VIEW_HEADER -> {
            }
            VIEW_ITEM -> {
                val item = contactList?.get(position - 1)
                val itemHolder = holder as ContactViewHolder
                itemHolder.binding.tvContactName.text = item?.name
                itemHolder.binding.tvContactPhone.text = item?.number
                GlideDownloader.load(
                    context,
                    itemHolder.binding.ivContactImage,
                    item?.profile_image,
                    R.drawable.dummy_placeholder,
                    R.drawable.dummy_placeholder
                )
                if (item?.id == 0) {
                    itemHolder.binding.btnInvite.visibility = View.VISIBLE
                    itemHolder.binding.btnInvite.tag = position - 1
                    itemHolder.binding.rlItem.setOnClickListener(null)
                } else {
                    itemHolder.binding.btnInvite.visibility = View.GONE
                    itemHolder.binding.rlItem.tag = position - 1
                    itemHolder.binding.rlItem.setOnClickListener(onClickListener)
                }

            }

        }
    }

    inner class NewGroupAndContactViewHolder(val binding: ItemForNewGroupContactBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.lvNewContact.setOnClickListener(onClickListener)
            binding.lvNewGroup.setOnClickListener(onClickListener)
        }
    }

    inner class ContactViewHolder(val binding: ItemForContactListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.btnInvite.setOnClickListener(onClickListener)
            binding.rlItem.setOnClickListener(onClickListener)
        }
    }

    private inner class AdapterClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            when (v.id) {
                R.id.lvNewGroup -> {
                    val intent = Intent(context,NewGroupActivity::class.java)
                    intent.putExtra(AppConstants.KALAM_CONTACT_LIST,tempContactList)
                    context.startActivity(intent)
                }
                R.id.lvNewContact -> {
                    val intent = Intent(Intent.ACTION_INSERT)
                    intent.type = ContactsContract.Contacts.CONTENT_TYPE
                    context.startActivity(intent)
                }
                R.id.btnInvite -> {
                    val feedIndex: Int = Integer.parseInt(v.tag.toString())
                    val item = contactList?.get(feedIndex)
                    val intent = Intent(Intent.ACTION_SENDTO)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.data = Uri.parse("smsto:${item?.number}")
                    intent.putExtra("sms_body", context.getString(R.string.invite_text))
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    }
                }
                R.id.rlItem -> {
                    val feedIndex: Int = Integer.parseInt(v.tag.toString())
                    val item = contactList?.get(feedIndex)
                    if (item?.id != 0) {
                        val name: String = if (item?.name.isNullOrEmpty()) {
                            item?.kalam_name.toString()
                        } else {
                            item?.name.toString()
                        }
                        val intent = Intent(context, ChatDetailActivity::class.java)
                        intent.putExtra(AppConstants.CALLER_USER_ID, item?.id?.toLong())
                        intent.putExtra(AppConstants.IS_CHATID_AVAILABLE, false)
                        intent.putExtra(AppConstants.CHAT_USER_NAME, name)
                        intent.putExtra(
                            AppConstants.CHAT_USER_PICTURE,
                            item?.profile_image.toString()
                        )
                        context.startActivity(intent)
                        (context as Activity).finish()
                    } else {
                        logE("Receiver Id in else: ${item.id}")
                    }
                }
            }
        }
    }

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
                contactList = contactsFilteredList
                notifyDataSetChanged()
            }
        }
    }


    private fun logE(msg: String) {
        Debugger.e(TAG, msg)
    }
}
