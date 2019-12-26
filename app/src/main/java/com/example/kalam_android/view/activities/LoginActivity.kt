package com.example.kalam_android.view.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.HideReturnsTransformationMethod
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.databinding.ActivityLoginBinding
import com.example.kalam_android.repository.model.LoginResponse
import com.example.kalam_android.repository.net.ApiResponse
import com.example.kalam_android.repository.net.Status
import com.example.kalam_android.util.*
import com.example.kalam_android.viewmodel.LoginViewModel
import com.example.kalam_android.viewmodel.factory.ViewModelFactory
import com.google.firebase.iid.FirebaseInstanceId
import javax.inject.Inject

class LoginActivity : BaseActivity(), View.OnClickListener {


    private val TAG = this.javaClass.simpleName
    lateinit var binding: ActivityLoginBinding
    private var HIDE_PASSWORD = true
    @Inject
    lateinit var factory: ViewModelFactory
    lateinit var viewModel: LoginViewModel
    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper
    private var fcmToken = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        MyApplication.getAppComponent(this).doInjection(this)
        viewModel = ViewModelProviders.of(this, factory).get(LoginViewModel::class.java)
        viewModel.loginResponse().observe(this, Observer {
            consumeResponse(it)
        })
        setSpannable()
        binding.hidePassword.setOnClickListener(this)
        binding.btnLogin.setOnClickListener(this)
        binding.llForgotPass.setOnClickListener(this)
//        binding.etUsername.setText("waqarmustafa18@gmail.com")
//        binding.etPass.setText("123")
        fcmToken = sharedPrefsHelper.getFCMToken().toString()
        if (fcmToken == "") {
            getFCMToken()
        }
    }

    private fun getFCMToken() {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (task.result != null) {
                    fcmToken = task.result!!.token
                    sharedPrefsHelper.saveFCMToken(fcmToken)
                    logE("FCM Token:${fcmToken}")
                }
            } else {
                logE("getInstance Failed:${task.exception}")
            }
        }
    }

    private fun consumeResponse(apiResponse: ApiResponse<LoginResponse>?) {
        when (apiResponse?.status) {

            Status.LOADING -> {
                showProgressDialog(this)
            }
            Status.SUCCESS -> {
                hideProgressDialog()
                renderResponse(apiResponse.data as LoginResponse)
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

    private fun renderResponse(response: LoginResponse?) {
        logE("socketResponse: $response")
        response?.let { res ->
            logE(res.toString())
            if (res.status) {
                sharedPrefsHelper.setUser(res.data)
                res.data?.auto_translate?.let {
                    sharedPrefsHelper.saveTranslateState(it)
                }
                sharedPrefsHelper.saveLanguage(res.data?.language.toString())
                hideProgressDialog()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                showAlertDialoge(this, "Error", res.message.toString())
            }
        }
    }

    private fun setSpannable() {
        setSpannableTextView(
            resources.getString(R.string.tap_login),
            resources.getString(R.string.kalam_terms),
            binding.tvTermsPolicy,
            false
        )
        setSpannableTextView(
            resources.getString(R.string.signup),
            resources.getString(R.string.signup_sub),
            binding.tvSignUp,
            true
        )
    }

    private fun setSpannableTextView(
        fullText: String,
        subText: String,
        view: TextView,
        SIGN_UP: Boolean
    ) {
        val color = ContextCompat.getColor(this, R.color.theme_color)
        val spannable = SpannableString(fullText)
        val i = fullText.indexOf(subText)
        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                if (SIGN_UP)
                    startActivity(Intent(this@LoginActivity, SignUpActivity::class.java))
                else
                    toast("Terms and Policies")
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }

        }, i, i + subText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(
            ForegroundColorSpan(color),
            i,
            i + subText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            i,
            i + subText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        view.text = spannable
        view.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.hidePassword -> {
                if (HIDE_PASSWORD) {
                    HIDE_PASSWORD = false
                    binding.etPass.transformationMethod =
                        HideReturnsTransformationMethod.getInstance()
                    binding.hidePassword.setBackgroundResource(R.drawable.eye_icon)
                } else {
                    HIDE_PASSWORD = true
                    binding.etPass.transformationMethod = PasswordTransformationMethod.getInstance()
                    binding.hidePassword.setBackgroundResource(R.drawable.hide_eye_icon)
                }
                if (binding.etPass.text.toString().isNotEmpty()) {
                    binding.etPass.setSelection(binding.etPass.text.toString().length)
                }
            }
            R.id.btnLogin -> {
                val params = HashMap<String, String>()
                params["login"] = binding.etUsername.text.toString()
                params["password"] = binding.etPass.text.toString()
                params["fcm_token"] = fcmToken
                viewModel.hitLogin(params)
            }
            R.id.llForgotPass -> {
                startActivity(Intent(this, ResetPasswordActivity::class.java))
            }
        }
    }

    private fun logE(message: String) {
        Debugger.e(TAG, message)
    }
}
