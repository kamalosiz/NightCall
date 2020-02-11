package com.example.kalam_android.view.activities

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.databinding.ActivityResetPasswordBinding
import com.example.kalam_android.repository.model.BasicResponse
import com.example.kalam_android.repository.net.ApiResponse
import com.example.kalam_android.repository.net.Status
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.showAlertDialog
import com.example.kalam_android.util.toast
import com.example.kalam_android.viewmodel.ForgetPasswordViewModel
import com.example.kalam_android.viewmodel.factory.ViewModelFactory
import javax.inject.Inject

class ResetPasswordActivity : BaseActivity(), View.OnClickListener {

    lateinit var binding: ActivityResetPasswordBinding
    @Inject
    lateinit var factory: ViewModelFactory
    lateinit var viewModel: ForgetPasswordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reset_password)

        MyApplication.getAppComponent(this).doInjection(this)
        viewModel = ViewModelProviders.of(this, factory).get(ForgetPasswordViewModel::class.java)
        viewModel.forgetPasswordResponse().observe(this, Observer {
            consumeResponse(it)
        })
        onClick()
    }

    private fun onClick() {
        binding.btnBack.setOnClickListener(this)
        binding.btnSubmit.setOnClickListener(this)
    }

    private fun consumeResponse(apiResponse: ApiResponse<BasicResponse>?) {
        Debugger.e("Forget Password Response : ", "${apiResponse?.data}")
        when (apiResponse?.status) {
            Status.LOADING -> {
                showProgressDialog(this)
            }
            Status.SUCCESS -> {
                hideProgressDialog()
                showAlertDialog(this, "Send email", apiResponse.data!!.data)
            }
            Status.ERROR -> {
                hideProgressDialog()
                Debugger.e("Error : ", apiResponse.error.toString())
            }
            else -> {

            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnSubmit -> {

                if (binding.etEmail.text.toString() == "") {

                    toast("Please enter your email address")

                } else if (!isEmailValid(binding.etEmail.text.toString())) {

                    toast("Email not valid")

                } else {

                    val params = HashMap<String, String>()
                    params["email"] = binding.etEmail.text.toString()
                    viewModel.hitForgetPassword(params)
                }
            }
            R.id.btnBack -> {
                finish()
            }
        }
    }
}
