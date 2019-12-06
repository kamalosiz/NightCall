package com.example.kalam_android.view.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.databinding.ActivitySelectLanguageBinding
import com.example.kalam_android.repository.model.Logout
import com.example.kalam_android.repository.net.ApiResponse
import com.example.kalam_android.repository.net.Status
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.SharedPrefsHelper
import com.example.kalam_android.util.toast
import com.example.kalam_android.viewmodel.LogoutViewModel
import com.example.kalam_android.viewmodel.factory.ViewModelFactory
import com.example.kalam_android.wrapper.SocketIO
import javax.inject.Inject

class SettingActivity : BaseActivity(), AdapterView.OnItemSelectedListener, View.OnClickListener {

    var item = ""
    var position = -1
    private var autoTranslate: Int? = null
    private var userId = ""
    @Inject
    lateinit var factory: ViewModelFactory
    lateinit var viewModel: LogoutViewModel
    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper
    lateinit var binding: ActivitySelectLanguageBinding
    private val TAG = this.javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_select_language)
        MyApplication.getAppComponent(this).doInjection(this)
        viewModel = ViewModelProviders.of(this, factory).get(LogoutViewModel::class.java)
        viewModel.logoutResponse().observe(this, Observer {
            consumeResponse(it)
        })
        binding.btnUpdateChanges.setOnClickListener(this)
        binding.logout.setOnClickListener(this)
        binding.header.btnRight.visibility = View.GONE
        /*Debugger.e(
            "SettingActivity",
            "Language from login: ${sharedPrefsHelper.getUser()?.language} " +
                    ", state from login: ${sharedPrefsHelper.getUser()?.auto_translate}"
        )
        Debugger.e(
            "SettingActivity",
            "Language: ${sharedPrefsHelper.getLanguage()} , isCheck: ${sharedPrefsHelper.getTransState()}"
        )*/
        autoTranslate = sharedPrefsHelper.getTransState()
        binding.checkBox.isChecked = sharedPrefsHelper.getTransState() != 0
        userId = sharedPrefsHelper.getUser()?.id.toString()
        applySpinner()
        checkBoxListener()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (position) {
            0 -> {
                item = "en"
                this.position = position
            }
            1 -> {
                item = "ar"
                this.position = position
            }
        }
    }

    private fun checkBoxListener() {
        binding.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            autoTranslate = if (isChecked) {
                1
            } else {
                0
            }
        }
    }

    private fun applySpinner() {
        val languages: ArrayList<String> = ArrayList()
        languages.addAll(arrayOf("English", "Arabic"))
        binding.spinner1.onItemSelectedListener = this
        val aa =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner1.adapter = aa
        if (sharedPrefsHelper.getLanguage() == "en") {
            binding.spinner1.setSelection(0)
        } else if (sharedPrefsHelper.getLanguage() == "ar") {
            binding.spinner1.setSelection(1)
        }
//        binding.spinner1.setSelection(sharedPrefsHelper.getLanguage() ?: 0)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnUpdateChanges -> {
                val builder1 = AlertDialog.Builder(this)
                builder1.setTitle("Update Language")
                builder1.setMessage("Do you really want update changes?")
                builder1.setCancelable(true)
                builder1.setPositiveButton("Yes") { dialog, id ->
                    SocketIO.updateSettings(
                        AppConstants.UPDATE_SETTINGS, autoTranslate.toString()
                        , item, sharedPrefsHelper.getUser()?.id.toString()
                    )
                    toast("Language successfully updated")
                    sharedPrefsHelper.saveLanguage(item)
                    autoTranslate?.let { sharedPrefsHelper.saveTranslateState(it) }
                }
                builder1.setNegativeButton("No") { dialog, id ->
                    dialog.cancel()
                }
                builder1.create().show()
            }
            R.id.logout -> {
                val builder1 = AlertDialog.Builder(this)
                builder1.setTitle("Logout")
                builder1.setMessage("Are you sure you want to logout?")
                builder1.setCancelable(true)
                builder1.setPositiveButton("Yes") { dialog, id ->

                    val params = HashMap<String, String>()
                    params["user_id"] = userId
                    viewModel.hitLogoutApi(sharedPrefsHelper.getUser()?.token.toString(), params)

                }
                builder1.setNegativeButton("No") { dialog, id ->
                    dialog.cancel()
                }
                builder1.create().show()
            }
        }
    }

    private fun consumeResponse(apiResponse: ApiResponse<Logout>) {
        when (apiResponse?.status) {

            Status.LOADING -> {
                showProgressDialog(this)
            }
            Status.SUCCESS -> {
                hideProgressDialog()
                sharedPrefsHelper.put(AppConstants.CONTACTS_SYNCED, false)
                sharedPrefsHelper.put(AppConstants.KEY_IS_LOGIN, false)
//                    sharedPrefsHelper.put(AppConstants.KEY_USER_OBJECT, AppConstants.DUMMY_STRING)
                sharedPrefsHelper.setUser(null)
//                    sharedPrefsHelper.put(AppConstants.FCM_TOKEN, AppConstants.DUMMY_STRING)
                sharedPrefsHelper.setFCMToken("")
//                    sharedPrefsHelper.put(AppConstants.PHONE, AppConstants.DUMMY_STRING)
                sharedPrefsHelper.setNumber("")
                sharedPrefsHelper.put(AppConstants.KEY_IS_LOGIN,false)
                toast("Logout Successfully")
                setResult(Activity.RESULT_OK)
                finish()
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

    private fun logE(message: String) {
        Debugger.e(TAG, message)
    }
}
