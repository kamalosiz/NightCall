package com.example.kalam_android.view.activities

import android.Manifest
import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.callbacks.OnClickNewGroupContact
import com.example.kalam_android.databinding.ActivityNewGroupBinding
import com.example.kalam_android.localdb.entities.ContactsData
import com.example.kalam_android.repository.model.BasicResponse
import com.example.kalam_android.repository.net.ApiResponse
import com.example.kalam_android.repository.net.Status
import com.example.kalam_android.util.*
import com.example.kalam_android.view.adapter.AdapterForKalamUsers
import com.example.kalam_android.viewmodel.NewGroupViewModel
import com.example.kalam_android.viewmodel.factory.ViewModelFactory
import com.example.kalam_android.wrapper.GlideDownloader
import com.fxn.pix.Options
import com.fxn.pix.Pix
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.yalantis.ucrop.UCrop
import okhttp3.MediaType
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject

class NewGroupActivity : BaseActivity(), OnClickNewGroupContact, View.OnClickListener {
    private lateinit var binding: ActivityNewGroupBinding
    private lateinit var adapterForKalamUsers: AdapterForKalamUsers
    private var selectedContactList: ArrayList<Int?> = ArrayList()
    private var contactsList: ArrayList<ContactsData>? = ArrayList()
    private var searchView: SearchView? = null
    @Inject
    lateinit var factory: ViewModelFactory
    lateinit var viewModel: NewGroupViewModel
    private var profileImagePath: String? = null
    private val TAG = this.javaClass.simpleName
    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_group)
        MyApplication.getAppComponent(this).doInjection(this)
        viewModel = ViewModelProviders.of(this, factory).get(NewGroupViewModel::class.java)
        viewModel.createGroupResponse().observe(this, Observer {
            consumeResponse(it)
        })
        setAdapter()
        setToolbar()
        getIntentData()
        binding.ivCreateGroup.setOnClickListener(this)
        binding.cvGroupCamera.setOnClickListener(this)
    }

    private fun hitCreateGroupApi() {
        if (binding.etGroupName.text.isNullOrEmpty()) {
            toast("Enter group name")
            return
        }
        if (selectedContactList.size < 2) {
            toast("Add atleast 2 contacts")
            return
        }
        if (profileImagePath.isNullOrEmpty())
            hitApi()
        else hitWithImage()
    }

    private fun hitApi() {
        val param = HashMap<String, String>()
        param["group_name"] = binding.etGroupName.text.toString()
        param["users"] = selectedContactList.toString()
        viewModel.hitCreateGroup(sharedPrefsHelper.getUser()?.token.toString(), param)
    }

    private fun hitWithImage() {
        val params = HashMap<String, RequestBody>()
        params["group_name"] = RequestBody.create(
            MediaType.parse("text/plain"),
            binding.etGroupName.text.toString()
        )
        params["users"] = RequestBody.create(
            MediaType.parse("text/plain"),
            selectedContactList.toString()
        )
        viewModel.hitCreateGroup(
            sharedPrefsHelper.getUser()?.token.toString(),
            params,
            getFileBody(profileImagePath.toString(), "file", this)
        )
    }

    private fun consumeResponse(apiResponse: ApiResponse<BasicResponse>?) {
        when (apiResponse?.status) {

            Status.LOADING -> {
                showProgressDialog(this)
            }
            Status.SUCCESS -> {
                hideProgressDialog()
                logE("consumeResponse SUCCESS : ${apiResponse.data}")
            }
            Status.ERROR -> {
                hideProgressDialog()
                logE("consumeResponse ERROR: " + apiResponse.error.toString())
            }
            else -> {
            }
        }
    }

    private fun getIntentData() {
        if (intent != null) {
            contactsList =
                intent.getSerializableExtra(AppConstants.KALAM_CONTACT_LIST) as ArrayList<ContactsData>
            adapterForKalamUsers.updateList(contactsList)
        }
    }

    private fun setAdapter() {
        adapterForKalamUsers = AdapterForKalamUsers(this, this)
        binding.rvForContacts.layoutManager = LinearLayoutManager(this)
        binding.rvForContacts.adapter = adapterForKalamUsers
        ((binding.rvForContacts.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations =
            false
    }

    private fun setToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        menu?.findItem(R.id.refresh)?.isVisible = false
        searchView = menu?.findItem(R.id.action_search)
            ?.actionView as SearchView
        searchView?.setSearchableInfo(
            searchManager
                .getSearchableInfo(componentName)
        )
        searchView?.maxWidth = Integer.MAX_VALUE

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                (binding.rvForContacts.adapter as AdapterForKalamUsers).filter.filter(query)
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                (binding.rvForContacts.adapter as AdapterForKalamUsers).filter.filter(query)
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

    override fun onMyClick(position: Int, list: ArrayList<ContactsData>?) {
        if (list?.get(position)?.is_selected == true) {
            list[position].is_selected = false
            selectedContactList.remove(list[position].id)
        } else {
            list?.get(position)?.is_selected = true
            selectedContactList.add(list?.get(position)?.id)
        }
        adapterForKalamUsers.notifyList(list, position)
    }

    private fun checkPixPermission() {
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report?.areAllPermissionsGranted() == true) {
                    Pix.start(
                        this@NewGroupActivity,
                        Options.init().setRequestCode(AppConstants.GROUP_IMAGE_CODE)
                    )
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                AppConstants.GROUP_IMAGE_CODE -> {
                    val returnValue = data?.getStringArrayListExtra(Pix.IMAGE_RESULTS)
                    CropHelper.startCropActivity(
                        sharedPrefsHelper,
                        this,
                        Uri.fromFile(File(returnValue?.get(0).toString()))
                        , false
                    )
                }
                UCrop.REQUEST_CROP -> {
                    handleCropResult(data)
                }
            }
        }
    }

    private fun handleCropResult(result: Intent?) {
        profileImagePath = ""
        val resultUri = result?.let { UCrop.getOutput(it) }
        if (resultUri == null) {
            Toast.makeText(this, "Error in Image file", Toast.LENGTH_SHORT).show()
            return
        }
        profileImagePath = resultUri.path.toString()
        GlideDownloader.load(
            this,
            binding.cvGroupCamera,
            resultUri.path,
            R.color.grey,
            R.color.grey
        )
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivCreateGroup -> {
                hitCreateGroupApi()
            }
            R.id.cvGroupCamera -> {
                checkPixPermission()
            }
        }
    }

    private fun logE(message: String) {
        Debugger.e(TAG, message)
    }
}

