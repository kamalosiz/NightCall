package com.example.kalam_android.view.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.databinding.ActivityVerifyCodeBinding
import com.example.kalam_android.repository.model.BasicResponse
import com.example.kalam_android.repository.net.ApiResponse
import com.example.kalam_android.repository.net.Status
import com.example.kalam_android.util.*
import com.example.kalam_android.viewmodel.VerifyCodeViewModel
import com.example.kalam_android.viewmodel.factory.ViewModelFactory
import javax.inject.Inject

class VerifyCodeActivity : BaseActivity(), View.OnClickListener {

    private val TAG = this.javaClass.simpleName
    lateinit var binding: ActivityVerifyCodeBinding
    lateinit var viewModel: VerifyCodeViewModel
    @Inject
    lateinit var factory: ViewModelFactory
    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper
    private lateinit var code: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_verify_code)
        MyApplication.getAppComponent(this@VerifyCodeActivity).doInjection(this)
        viewModel = ViewModelProviders.of(this, factory).get(VerifyCodeViewModel::class.java)
        binding.btnBack.setOnClickListener(this)
        binding.btnVerify.setOnClickListener(this)
        binding.tvResendCode.setOnClickListener(this)
        code = intent.getIntExtra(AppConstants.VERIFICATION_CODE, 0).toString()
        binding.etCode.setText(code)
        viewModel.verificationResponse().observe(this, Observer {
            consumeResponse(it)
        })
    }

    private fun consumeResponse(apiResponse: ApiResponse<BasicResponse>) {
        when (apiResponse.status) {
            Status.LOADING -> showProgressDialog(this)
            Status.SUCCESS -> {
                hideProgressDialog()
                renderResponse(apiResponse.data)
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

    private fun renderResponse(response: BasicResponse?) {
        logE("socketResponse $response")
        response?.let {
            if (it.status) {
                toast(it.data)
                startActivity(Intent(this, CreateProfileActivity::class.java))
                finish()
            } else {
                showAlertDialoge(this, "Error", it.message)
            }

        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnBack -> {
                startActivity(Intent(this, SignUpActivity::class.java))
                finish()
            }
            R.id.btnVerify -> {
                if (binding.etCode.text.toString().isEmpty()) {
                    toast("Please enter Verification Code")
                    return
                } else if (code != binding.etCode.text.toString()) {
                    toast("Verification code invalid")
                    return
                }
                val params = HashMap<String, String>()
                params["number"] = sharedPrefsHelper.getNumber().toString()
                params["verification_code"] = binding.etCode.text.toString()
                viewModel.hitVerificationApi(params)
            }
            R.id.tvResendCode -> {
                toast("Code resend successfully")
            }
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, SignUpActivity::class.java))
        finish()
    }

    private fun logE(message: String) {
        Debugger.e(TAG, message)
    }
}
