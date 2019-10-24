package com.example.kalam_android.view.activities

import android.Manifest
import android.annotation.SuppressLint
import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.databinding.ActivityContactListBinding
import com.example.kalam_android.repository.model.ContactInfo
import com.example.kalam_android.repository.model.Contacts
import com.example.kalam_android.repository.net.ApiResponse
import com.example.kalam_android.repository.net.Status
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.permissionHelper.helper.PermissionHelper
import com.example.kalam_android.util.permissionHelper.listeners.MediaPermissionListener
import com.example.kalam_android.view.adapter.AdapterForContacts
import com.example.kalam_android.viewmodel.ContactsViewModel
import com.example.kalam_android.viewmodel.factory.ViewModelFactory
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import javax.inject.Inject


class ContactListActivity : BaseActivity() {

    private val TAG = this.javaClass.simpleName
    lateinit var binding: ActivityContactListBinding
    @Inject
    lateinit var factory: ViewModelFactory
    lateinit var viewModel: ContactsViewModel
    lateinit var contactList: MutableList<ContactInfo>
    private val jsonArray = JsonArray()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_contact_list)
        MyApplication.getAppComponent(this).doInjection(this)
        viewModel = ViewModelProviders.of(this, factory).get(ContactsViewModel::class.java)
        viewModel.contactsResponse().observe(this, Observer {
            consumeResponse(it)
        })
        binding.rvForContacts.layoutManager = LinearLayoutManager(applicationContext)
        binding.rvForContacts.adapter = AdapterForContacts(applicationContext)
        contactList = mutableListOf()
        checkPixPermission()
    }

    private fun consumeResponse(apiResponse: ApiResponse<Contacts>?) {
        when (apiResponse?.status) {

            Status.LOADING -> {
                showProgressDialog(this)
            }
            Status.SUCCESS -> {
                hideProgressDialog()
                renderResponse(apiResponse.data as Contacts)
                logE("+${apiResponse.data}")
            }
            Status.ERROR -> {
                hideProgressDialog()
                logE("consumeResponse ERROR: " + apiResponse.error.toString())
            }
            else -> {
            }
        }
    }

    private fun renderResponse(response: Contacts?) {
        logE("response: $response")
        response?.let {
            logE(it.toString())

            /*  if (it.status) {

              } else {
                  showAlertDialoge(this, "Error", it.message)
              }*/
        }
    }

    @SuppressLint("Recycle")
    private fun getAllContact(): MutableList<ContactInfo> {
        val contactList: MutableList<ContactInfo> = mutableListOf()
        val phones: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        while (phones!!.moveToNext()) {
            val name =
                phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phoneNumber =
                phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            if (!contactList.contains(ContactInfo(name, phoneNumber))) {
                contactList.add(ContactInfo(name, phoneNumber))
            }
        }
        phones.close()
        return contactList
    }

    fun createContactsJson(list: MutableList<ContactInfo>): JsonArray {
        for (x in list.indices) {
            val jsonObject = JsonObject()
            jsonObject.addProperty("number", list[x].number)
            jsonObject.addProperty("status", 0)
            jsonArray.add(jsonObject)
        }
        logE("Json Array : $jsonArray")
        return jsonArray
    }

    private fun checkPixPermission() {
        Handler().postDelayed(
            {
                PermissionHelper.withActivity(this).addPermissions(
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS
                ).listener(object : MediaPermissionListener {
                    override fun onPermissionGranted() {
                        contactList = getAllContact()
                        (binding.rvForContacts.adapter as AdapterForContacts).updateList(contactList)
                        createContactsJson(contactList)
                        val params = HashMap<String, String>()
                        params["contacts"] = createContactsJson(contactList).toString()
                        viewModel.getContacts(params)
                    }

                    override fun onPermissionDenied() {
                        logE("onPermissionDenied")
                    }

                }).build().init()

            }, 100
        )
    }

    private fun logE(message: String) {
        Debugger.e(TAG, message)
    }

}
