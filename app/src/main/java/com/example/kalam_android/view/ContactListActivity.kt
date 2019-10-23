package com.example.kalam_android.view

import android.Manifest
import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.databinding.ActivityContactListBinding
import android.provider.ContactsContract
import com.example.kalam_android.repository.model.ContactInfo
import android.database.Cursor
import android.os.Handler
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.permissionHelper.helper.PermissionHelper
import com.example.kalam_android.util.permissionHelper.listeners.MediaPermissionListener
import com.example.kalam_android.view.adapter.AdapterForContacts
import com.fxn.pix.Options
import com.fxn.pix.Pix


class ContactListActivity : BaseActivity() {

    private val TAG = this.javaClass.simpleName
    lateinit var binding: ActivityContactListBinding
    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var adapter: AdapterForContacts

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_contact_list)
        checkPixPermission()
    }

    @SuppressLint("Recycle")
    private fun getAllContact(): MutableList<ContactInfo> {
        val contactInfo: MutableList<ContactInfo> = mutableListOf()
        var phones: Cursor? = null
        phones = getContentResolver().query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        while (phones!!.moveToNext()) {
//            val id =
//                phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PREFERRED_PHONE_ACCOUNT_ID))
            val name =
                phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phoneNumber =
                phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))


            contactInfo.add(ContactInfo("", name, phoneNumber))
        }
        phones.close();
        return contactInfo

    }

    private fun checkPixPermission() {
        Handler().postDelayed(
            {
                PermissionHelper.withActivity(this).addPermissions(
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS
                ).listener(object : MediaPermissionListener {
                    override fun onPermissionGranted() {

                        adapter = AdapterForContacts()
                        linearLayoutManager = LinearLayoutManager(applicationContext)
                        binding.rvForContacts.layoutManager = linearLayoutManager
                        binding.rvForContacts.adapter = adapter
                        adapter.updateList(getAllContact())

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
