package com.example.kalam_android.view

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.databinding.ActivitySignUpBinding
import com.example.kalam_android.repository.model.LoginResponse
import com.example.kalam_android.repository.net.ApiResponse
import com.example.kalam_android.repository.net.Status
import com.example.kalam_android.util.D
import com.example.kalam_android.viewmodel.SignUpViewModel
import com.example.kalam_android.viewmodel.factory.ViewModelFactory
import com.ybs.countrypicker.CountryPicker
import javax.inject.Inject


class SignUpActivity : BaseActivity() {

    private val TAG = this.javaClass.simpleName
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var viewModel: SignUpViewModel
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)
        MyApplication.getAppComponent(this).doInjection(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SignUpViewModel::class.java)
        binding.tvCountry.setOnClickListener {
            val picker = CountryPicker.newInstance("Select Country")
            picker.setListener { name, code, dialCode, flagID ->
                binding.tvCountry.text = name
                binding.tvDialCode.text = dialCode
                picker.dismiss()
            }
            picker.show(supportFragmentManager, "COUNTRY_PICKER")
        }
        binding.btnNext.setOnClickListener {
            startActivity(Intent(this, VerifyCodeActivity::class.java))
//            hitLogin()
        }

        /*viewModel.loginResponse().observe(this, Observer {
            consumeResponse(it)
        })*/
    }

    private fun consumeResponse(apiResponse: ApiResponse<LoginResponse>?) {
        D.e(TAG, "consumeResponse")
        when (apiResponse?.status) {

            Status.LOADING -> {
            }
            Status.SUCCESS -> {
//                hideProgressDialog()
                logE("consumeResponse SUCCESS")
//                binding.pbSignIn.visibility = View.GONE
//                renderSuccessResponse(apiResponse.data as SigninResponse)
            }
            Status.ERROR -> {
                /* hideProgressDialog()
                 binding.pbSignIn.visibility = View.GONE
                 toast(getString(R.string.errorString))*/
                logE("consumeResponse ERROR: " + apiResponse.error.toString())
            }
            else -> {
            }
        }
    }

    private fun hitLogin() {
        D.d(TAG, "Login Email: " + viewModel.email)
        D.d(TAG, "Login Password: " + viewModel.password)

        val params = HashMap<String, String>()
        params["email"] = viewModel.email
        params["password"] = viewModel.password

        viewModel.hitLoginApi(params)
    }

    private fun logE(message: String) {
        D.e(TAG, message)
    }
}
