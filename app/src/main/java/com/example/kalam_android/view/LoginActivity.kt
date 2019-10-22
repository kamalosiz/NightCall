package com.example.kalam_android.view

import android.content.Intent
import android.content.res.Resources
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
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.databinding.ActivityLoginBinding
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.toast
import javax.inject.Inject

class LoginActivity : BaseActivity(), View.OnClickListener {

    private val TAG = this.javaClass.simpleName
    lateinit var binding: ActivityLoginBinding
    var HIDE_PASSWORD = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        setSpannable()
        binding.hidePassword.setOnClickListener(this)
    }

    fun setSpannable() {
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
        setSpannableButtonText(binding.btnLoginWorlNoor)
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
                    toast(this@LoginActivity, "Terms and Policies")
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

    fun setSpannableButtonText(btn: Button) {
        val fullText = resources.getString(R.string.login_world_noor)
        val subText = resources.getString(R.string.world_noor)
        val color = ContextCompat.getColor(this, R.color.white)
        val spannable = SpannableString(fullText)
        val i = fullText.indexOf(subText)
        spannable.setSpan(
            ForegroundColorSpan(color),
            i,
            i + subText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        btn.text = spannable
        btn.movementMethod = LinkMovementMethod.getInstance()
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
            }
        }
    }

    private fun logE(message: String) {
        Debugger.e(TAG, message)
    }
}
