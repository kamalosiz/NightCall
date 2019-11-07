package com.example.kalam_android.view.activities

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

class SelectLanguage : BaseActivity(), AdapterView.OnItemSelectedListener, View.OnClickListener {

    var item = ""
    var position = -1
    var autoTranslate = -1
    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper
    lateinit var binding: ActivitySelectLanguageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_select_language)
        MyApplication.getAppComponent(this).doInjection(this)
        binding.btnUpdateChanges.setOnClickListener(this)
        binding.header.btnRight.visibility = View.GONE
        Debugger.e(
            "SelectLanguage",
            "Language: ${sharedPrefsHelper.getLanguage()} , isCheck: ${sharedPrefsHelper.getTransState()}"
        )
        binding.checkBox.isChecked = sharedPrefsHelper.getTransState() != 0
        applySpinner()
        checkBoxListener()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (position) {
            0 -> {
                item = "ar"
                this.position = position
            }
            1 -> {
                item = "en"
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
        languages.addAll(arrayOf("Arabic", "English"))
        binding.spinner1.onItemSelectedListener = this
        val aa =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner1.adapter = aa
        binding.spinner1.setSelection(sharedPrefsHelper.getLanguage() ?: 0)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnUpdateChanges -> {
                SocketIO.updateSettings(
                    AppConstants.UPDATE_SETTINGS, autoTranslate.toString()
                    , item, sharedPrefsHelper.getUser()?.id.toString()
                )
                toast("Language successfully updated")
                sharedPrefsHelper.saveLanguage(position)
                sharedPrefsHelper.saveTranslateState(autoTranslate)
            }
        }
    }
}
