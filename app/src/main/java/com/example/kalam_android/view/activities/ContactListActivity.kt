package com.example.kalam_android.view.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.databinding.ActivityContactListBinding
import com.example.kalam_android.localdb.entities.ContactsEntityClass
import com.example.kalam_android.repository.model.ContactInfo
import com.example.kalam_android.repository.model.Contacts
import com.example.kalam_android.repository.model.ContactsData
import com.example.kalam_android.repository.net.ApiResponse
import com.example.kalam_android.repository.net.Status
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.SharedPrefsHelper
import com.example.kalam_android.util.toast
import com.example.kalam_android.view.adapter.AdapterForContacts
import com.example.kalam_android.viewmodel.ContactsViewModel
import com.example.kalam_android.viewmodel.factory.ViewModelFactory
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.layout_content_of_chat.view.*
import javax.inject.Inject


class ContactListActivity : BaseActivity(), PopupMenu.OnMenuItemClickListener {

    private val TAG = this.javaClass.simpleName
    lateinit var binding: ActivityContactListBinding
    @Inject
    lateinit var factory: ViewModelFactory
    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper
    lateinit var viewModel: ContactsViewModel
    lateinit var contactList: ArrayList<ContactsData>
    private val jsonArray = JsonArray()
    private var searchView: SearchView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_contact_list
        )
        MyApplication.getAppComponent(this).doInjection(this)
        viewModel = ViewModelProviders.of(this, factory).get(ContactsViewModel::class.java)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Contacts"

        if (sharedPrefsHelper.isContactsSynced()) {
            viewModel.getContactsFromLocal()
            logE("loaded from local db")
        } else {
            checkPermissions()
            logE("loaded from Server")
        }
        viewModel.contactsFromRoomResponse().observe(this, Observer {
            consumeLocalResponse(it)
        })
        viewModel.contactsResponse().observe(this, Observer {
            consumeResponse(it)
        })
        binding.rvForContacts.adapter =
            AdapterForContacts(this)
        contactList = ArrayList()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = menu?.findItem(R.id.action_search)
            ?.actionView as SearchView
        searchView?.setSearchableInfo(
            searchManager
                .getSearchableInfo(componentName)
        )
        searchView?.maxWidth = Integer.MAX_VALUE

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                (binding.rvForContacts.adapter as AdapterForContacts).filter.filter(query)
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                (binding.rvForContacts.adapter as AdapterForContacts).filter.filter(query)
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> return true
            R.id.item_more -> popUpMenu(this.findViewById(item.itemId), R.menu.menu, this)
            android.R.id.home -> finish()

        }
        return super.onOptionsItemSelected(item)
    }

    private fun consumeResponse(apiResponse: ApiResponse<Contacts>?) {
        when (apiResponse?.status) {

            Status.LOADING -> {
            }
            Status.SUCCESS -> {
                binding.pbCenter.visibility = View.GONE
                binding.rvForContacts.visibility = View.VISIBLE
                toast("Contacts Synced Successfully")
                renderResponse(apiResponse.data as Contacts)
                logE("+${apiResponse.data}")
            }
            Status.ERROR -> {
                binding.pbCenter.visibility = View.GONE
                binding.rvForContacts.visibility = View.VISIBLE
                toast("Something went wrong please try again")
                logE("consumeResponse ERROR: " + apiResponse.error.toString())
            }
            else -> {
            }
        }
    }

    private fun renderResponse(response: Contacts?) {
        logE("socketResponse: $response")
        response?.let {
            contactList = it.data.contacts_list
            (binding.rvForContacts.adapter as AdapterForContacts).updateList(contactList)
            val entityList = ArrayList<ContactsEntityClass>()
            for (i in contactList) {
                entityList.add(
                    ContactsEntityClass(
                        0,
                        i.number,
                        i.id,
                        i.name,
                        i.profile_image,
                        i.kalam_number,
                        i.kalam_name
                    )
                )
            }
            viewModel.addContactsToLocal(entityList)
            sharedPrefsHelper.contactsSynced()
        }
    }

    private fun consumeLocalResponse(apiResponse: ApiResponse<List<ContactsEntityClass>>?) {

        when (apiResponse?.status) {

            Status.LOADING -> {
            }
            Status.SUCCESS -> {
                binding.pbCenter.visibility = View.GONE
                renderLocalResponse(apiResponse.data)
                logE("local socketResponse +${apiResponse.data}")
            }
            Status.ERROR -> {
                binding.pbCenter.visibility = View.GONE
                toast("Something went wrong please try again")
                logE("consumeResponse ERROR: " + apiResponse.error.toString())
            }
            else -> {
            }
        }
    }

    private fun renderLocalResponse(list: List<ContactsEntityClass>?) {
        logE("renderLocalResponse: $list")
        if (list?.isNotEmpty() == true) {
            for (item in list) {
                logE("Added to list")
                contactList.add(
                    ContactsData(
                        item.number,
                        item.contact_id,
                        item.name,
                        item.profile_image,
                        item.kalam_number,
                        item.kalam_name
                    )
                )
            }
            logE("Local List Size: ${contactList.size}")
        }
        (binding.rvForContacts.adapter as AdapterForContacts).updateList(contactList)
    }

    private fun getAllContact(): ArrayList<ContactInfo> {
        val list: ArrayList<ContactInfo> = ArrayList()
        val cursor: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        while (cursor!!.moveToNext()) {
            val name =
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phoneNumber =
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    .trim()

            if (!list.contains(ContactInfo(name, phoneNumber))) {
                list.add(ContactInfo(name, phoneNumber))
            }

        }
        cursor.close()
        return list
    }

    private fun createContactsJson(list: MutableList<ContactInfo>): JsonArray {
        for (x in list.indices) {
            val jsonObject = JsonObject()
            jsonObject.addProperty("name", list[x].name)
            jsonObject.addProperty("number", list[x].number)
            jsonObject.addProperty("image", "")
            jsonObject.addProperty("id", 0)
            jsonArray.add(jsonObject)
        }
        logE("Json Array : $jsonArray")
        return jsonArray
    }

    private fun checkPermissions() {
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report?.areAllPermissionsGranted() == true) {
                    binding.pbCenter.visibility = View.VISIBLE
                    binding.rvForContacts.visibility = View.GONE
                    val params = HashMap<String, String>()
                    params["contacts"] = createContactsJson(getAllContact()).toString()
                    viewModel.getContacts(sharedPrefsHelper.getUser()?.token,params)
                } else {
                    Debugger.e("Capturing Image", "onPermissionDenied")
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                token?.continuePermissionRequest()
            }

        }).check()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sync -> {
                viewModel.deleteAllLocalContacts()
                checkPermissions()
                true
            }
            else -> false
        }
    }

    override fun onBackPressed() {
        // close search view on back button pressed
        /*if (searchView?.isIconified == true) {
            searchView?.isIconified = true
            return
        }*/
        finish()
    }


    private fun logE(message: String) {
        Debugger.e(TAG, message)
    }

}
