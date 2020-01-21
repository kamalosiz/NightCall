package com.example.kalam_android.view.activities

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.databinding.ActivitySignUpBinding
import com.example.kalam_android.repository.model.PhoneModel
import com.example.kalam_android.repository.model.SignUpResponse
import com.example.kalam_android.repository.net.ApiResponse
import com.example.kalam_android.repository.net.Status
import com.example.kalam_android.util.*
import com.example.kalam_android.viewmodel.SignUpViewModel
import com.example.kalam_android.viewmodel.factory.ViewModelFactory
import com.ybs.countrypicker.CountryPicker
import java.lang.StringBuilder
import javax.inject.Inject


class SignUpActivity : BaseActivity() {

    private val TAG = this.javaClass.simpleName
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var viewModel: SignUpViewModel
    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper
    var phone = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)
        MyApplication.getAppComponent(this).doInjection(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SignUpViewModel::class.java)
        binding.tvCountry.setOnClickListener { pickerDialoge() }
        binding.btnBack.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        binding.btnNext.setOnClickListener { hitSignUp() }
        viewModel.signupResponse().observe(this, Observer<ApiResponse<SignUpResponse>> {
            consumeResponse(it)
        })
    }


    private fun pickerDialoge() {
        val picker = CountryPicker.newInstance("Select Country")
        picker.setListener { name, code, dialCode, flagID ->
            binding.tvCountry.text = name
            binding.tvDialCode.text = dialCode
            sharedPrefsHelper.setPhone(PhoneModel(name, code, dialCode.substring(1)))
            logE("phone number: ${sharedPrefsHelper.getPhone()}")
            picker.dismiss()
        }
        picker.show(supportFragmentManager, "COUNTRY_PICKER")
    }

    private fun consumeResponse(apiResponse: ApiResponse<SignUpResponse>?) {
        when (apiResponse?.status) {

            Status.LOADING -> {
                showProgressDialog(this@SignUpActivity)
            }
            Status.SUCCESS -> {
                hideProgressDialog()
                renderResponse(apiResponse.data as SignUpResponse)
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

    private fun renderResponse(response: SignUpResponse?) {
        logE("socketResponse: $response")
        response?.let {
            logE(it.toString())
            if (it.status) {
                logE("status true: ${it.data?.verification_code}")
                logE("Phone number on sucess : $phone")

                sharedPrefsHelper.setNumber(phone)
                val intent = Intent(this@SignUpActivity, VerifyCodeActivity::class.java)
                intent.putExtra(AppConstants.VERIFICATION_CODE, it.data?.verification_code)
                startActivity(intent)
                finish()
            } else {
                showAlertDialog(this, "Error", it.message)
            }
        }
    }

    private fun hitSignUp() {
        if (binding.tvCountry.text.isEmpty()) {
            toast("Please select your Country")
            return
        }
        if (binding.etNumber.text.toString().isEmpty()) {
            toast("Please enter your Number")
            return
        }
        phone = StringBuilder(binding.tvDialCode.text).append(binding.etNumber.text.toString())
            .toString()
        logE("Phone number : $phone")
        val params = HashMap<String, String>()
        params["number"] = phone
        viewModel.hitSignUpApi(params)
    }

    private fun logE(message: String) {
        Debugger.e(TAG, message)
    }
}
