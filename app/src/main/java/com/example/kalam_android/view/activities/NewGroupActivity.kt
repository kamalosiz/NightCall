package com.example.kalam_android.view.activities

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.databinding.ActivityNewGroupBinding
import com.example.kalam_android.view.adapter.AdapterForNewGroupContact
import android.widget.PopupMenu
import com.example.kalam_android.callbacks.OnClickNewGroupContact
import com.example.kalam_android.repository.model.ContactsData
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.toast

class NewGroupActivity : AppCompatActivity(), OnClickNewGroupContact {
    private lateinit var binding: ActivityNewGroupBinding
    private lateinit var adapterForNewGroupContact: AdapterForNewGroupContact
    private var selectedContactList: ArrayList<ContactsData> = ArrayList()
    private var searchView: SearchView? = null
    private var groupName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_group)

        setAdapter()
        setToolbar()
        getIntentData()
        binding.ivCreateGroup.setOnClickListener {
            if (groupName == "") {
                toast("Enter group name")
            } else if (selectedContactList.size == 0) {
                toast("Select Contact")
            } else {

            }
        }
    }

    private fun getIntentData() {

        if (intent != null) {
            val list = intent.getSerializableExtra(AppConstants.KALAM_CONTACT_LIST) as ArrayList<ContactsData>
            adapterForNewGroupContact.updateList(list)
        }
    }

    private fun setAdapter() {
        adapterForNewGroupContact = AdapterForNewGroupContact(this, this)
        binding.rvForContacts.layoutManager = LinearLayoutManager(this)
        binding.rvForContacts.adapter = adapterForNewGroupContact
    }

    private fun setToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        menu!!.findItem(R.id.item_more)!!.isVisible = false
        searchView = menu?.findItem(R.id.action_search)
                ?.actionView as SearchView
        searchView?.setSearchableInfo(
                searchManager
                        .getSearchableInfo(componentName)
        )
        searchView?.maxWidth = Integer.MAX_VALUE

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                (binding.rvForContacts.adapter as AdapterForNewGroupContact).filter.filter(query)
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                (binding.rvForContacts.adapter as AdapterForNewGroupContact).filter.filter(query)
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> return true
            android.R.id.home -> finish()

        }
        return super.onOptionsItemSelected(item)
    }

    override fun addToList(contact: ContactsData) {
        selectedContactList.add(contact)

    }

    override fun removeToList(contact: ContactsData) {
        selectedContactList.remove(contact)
    }

    override fun groupName(groupTitle: String) {
        groupName = groupTitle
    }


}

