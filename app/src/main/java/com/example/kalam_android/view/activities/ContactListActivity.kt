package com.example.kalam_android.view.activities

import android.Manifest
import android.app.SearchManager
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.callbacks.OnClickNewGroupContact
import com.example.kalam_android.databinding.ActivityContactListBinding
import com.example.kalam_android.localdb.entities.ContactsData
import com.example.kalam_android.repository.model.ContactInfo
import com.example.kalam_android.repository.model.Contacts
import com.example.kalam_android.repository.net.ApiResponse
import com.example.kalam_android.repository.net.Status
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.SharedPrefsHelper
import com.example.kalam_android.util.toast
import com.example.kalam_android.view.adapter.AdapterForContacts
import com.example.kalam_android.view.adapter.AdapterForKalamUsers
import com.example.kalam_android.viewmodel.ContactsViewModel
import com.example.kalam_android.viewmodel.factory.ViewModelFactory
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import javax.inject.Inject

class ContactListActivity : BaseActivity(), OnClickNewGroupContact {

    private val TAG = this.javaClass.simpleName
    lateinit var binding: ActivityContactListBinding
    @Inject
    lateinit var factory: ViewModelFactory
    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper
    lateinit var viewModel: ContactsViewModel
    lateinit var contactList: ArrayList<ContactsData>
    private var searchView: SearchView? = null
    private var isFromForward = false
    private var selectedMsgsIds: String? = null
    private var selectedContactList: ArrayList<Int?> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_contact_list
        )
        MyApplication.getAppComponent(this).doInjection(this)
        isFromForward = intent.getBooleanExtra(AppConstants.IS_FORWARD_MESSAGE, false)
        viewModel = ViewModelProviders.of(this, factory).get(ContactsViewModel::class.java)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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
        if (isFromForward) {
            supportActionBar?.title = "Forward To"
            binding.ivForward.visibility = View.VISIBLE
            contactList = ArrayList()
            binding.rvForContacts.adapter =
                AdapterForKalamUsers(this, this)
            ((binding.rvForContacts.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations =
                false
            selectedMsgsIds = intent.getStringExtra(AppConstants.SELECTED_MSGS_IDS)
            logE("chatMessagesList :$selectedMsgsIds")
        } else {
            supportActionBar?.title = "Contacts"
            binding.ivForward.visibility = View.GONE
            binding.rvForContacts.adapter =
                AdapterForContacts(this)
            contactList = ArrayList()
        }
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
                if (isFromForward)
                    (binding.rvForContacts.adapter as AdapterForKalamUsers).filter.filter(query)
                else (binding.rvForContacts.adapter as AdapterForContacts).filter.filter(query)
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                if (isFromForward)
                    (binding.rvForContacts.adapter as AdapterForKalamUsers).filter.filter(query)
                else (binding.rvForContacts.adapter as AdapterForContacts).filter.filter(query)
//                (binding.rvForContacts.adapter as AdapterForContacts).filter.filter(query)
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> return true
//            R.id.refresh -> popUpMenu(this.findViewById(item.itemId), R.menu.menu, this)
            R.id.refresh -> {
                viewModel.deleteAllLocalContacts()
                checkPermissions()
            }
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
            contactList.clear()
            contactList = it.data.contacts_list
            if (isFromForward) {
                val list = ArrayList<ContactsData>()
                for (i in 0 until (contactList.size)) {
                    if (contactList[i].id != 0) {
                        list.add(contactList[i])
                    }
                }
                (binding.rvForContacts.adapter as AdapterForKalamUsers).updateList(list)
            } else {
                (binding.rvForContacts.adapter as AdapterForContacts).updateList(contactList)
            }
            viewModel.addContactsToLocal(contactList)
            sharedPrefsHelper.contactsSynced()
        }
    }

    private fun consumeLocalResponse(apiResponse: ApiResponse<List<ContactsData>>) {

        when (apiResponse.status) {

            Status.LOADING -> {
            }
            Status.SUCCESS -> {
                binding.pbCenter.visibility = View.GONE
                renderLocalResponse(apiResponse.data!!)
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

    private fun renderLocalResponse(list: List<ContactsData>) {
        logE("renderLocalResponse: $list")
        contactList.clear()
        contactList.addAll(list)
        if (isFromForward) {
            val newList = ArrayList<ContactsData>()
            for (i in 0 until (contactList.size)) {
                if (contactList[i].id != 0) {
                    newList.add(contactList[i])
                }
            }
            (binding.rvForContacts.adapter as AdapterForKalamUsers).updateList(newList)
        } else {
            (binding.rvForContacts.adapter as AdapterForContacts).updateList(contactList)
        }
    }

    private fun getAllContact(): ArrayList<ContactInfo> {
        val list: ArrayList<ContactInfo> = ArrayList()
        val nameList: ArrayList<String> = ArrayList()
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

            if (!nameList.contains(name)) {
                nameList.add(name)
                list.add(ContactInfo(name, phoneNumber))
            }
        }
        cursor.close()
        return list
    }

    private fun createContactsJson(list: MutableList<ContactInfo>): JsonArray {
        val jsonArray = JsonArray()
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
                    viewModel.getContacts(sharedPrefsHelper.getUser()?.token.toString(), params)
                } else {
                    Debugger.e("checkPermissions", "onPermissionDenied")
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

    override fun onMyClick(position: Int, list: ArrayList<ContactsData>?) {
        if (list?.get(position)?.is_selected == true) {
            list[position].is_selected = false
            selectedContactList.remove(list[position].id)
        } else {
            list?.get(position)?.is_selected = true
            selectedContactList.add(list?.get(position)?.id)
        }
        (binding.rvForContacts.adapter as AdapterForKalamUsers).notifyList(list, position)
    }

}
