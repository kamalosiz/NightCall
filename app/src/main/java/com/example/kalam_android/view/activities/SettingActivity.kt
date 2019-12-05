package com.example.kalam_android.view.activities

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.databinding.ActivitySelectLanguageBinding
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.SharedPrefsHelper
import com.example.kalam_android.util.toast
import com.example.kalam_android.wrapper.SocketIO
import javax.inject.Inject

class SettingActivity : BaseActivity(), AdapterView.OnItemSelectedListener, View.OnClickListener {

    var item = ""
    var position = -1
    private var autoTranslate: Int? = null
    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper
    lateinit var binding: ActivitySelectLanguageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_select_language)
        MyApplication.getAppComponent(this).doInjection(this)
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
                    sharedPrefsHelper.put(AppConstants.CONTACTS_SYNCED, false)
                    sharedPrefsHelper.put(AppConstants.KEY_IS_LOGIN, false)
                    sharedPrefsHelper.put(AppConstants.KEY_USER_OBJECT, AppConstants.DUMMY_STRING)
                    sharedPrefsHelper.put(AppConstants.FCM_TOKEN, AppConstants.DUMMY_STRING)
                    sharedPrefsHelper.put(AppConstants.PHONE, AppConstants.DUMMY_STRING)
                    toast("Logout Successfully")
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                builder1.setNegativeButton("No") { dialog, id ->
                    dialog.cancel()
                }
                builder1.create().show()
            }
        }
    }
}
