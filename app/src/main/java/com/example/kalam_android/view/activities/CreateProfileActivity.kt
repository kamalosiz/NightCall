package com.example.kalam_android.view.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.databinding.ActivityCreateProfileBinding
import com.example.kalam_android.repository.model.CreateProfileResponse
import com.example.kalam_android.repository.net.ApiResponse
import com.example.kalam_android.repository.net.Status
import com.example.kalam_android.util.*
import com.example.kalam_android.util.permissionHelper.helper.PermissionHelper
import com.example.kalam_android.util.permissionHelper.listeners.MediaPermissionListener
import com.example.kalam_android.viewmodel.CreateProfileViewModel
import com.example.kalam_android.viewmodel.factory.ViewModelFactory
import com.example.kalam_android.wrapper.GlideDownloder
import com.fxn.pix.Options
import com.fxn.pix.Pix
import com.google.firebase.iid.FirebaseInstanceId
import com.yalantis.ucrop.UCrop
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject


class CreateProfileActivity : BaseActivity(), View.OnClickListener {

    private val TAG = this.javaClass.simpleName
    private var profileImagePath: String? = null
    lateinit var viewModel: CreateProfileViewModel
    lateinit var binding: ActivityCreateProfileBinding
    @Inject
    lateinit var factory: ViewModelFactory
    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper
    private var HIDE_PASSWORD = true
    var fcmToken = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_profile)
        MyApplication.getAppComponent(this).doInjection(this)
        viewModel = ViewModelProviders.of(this, factory).get(CreateProfileViewModel::class.java)
        binding.btnBack.setOnClickListener(this)
        binding.ivUploadImage.setOnClickListener(this)
        binding.hidePassword.setOnClickListener(this)
        binding.btnNext.setOnClickListener(this)
        viewModel.createProfileResponse().observe(this, Observer {
            consumeResponse(it)
        })
        getFCMToken()
    }

    private fun getFCMToken() {

        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (task.result != null) {
                    fcmToken = task.result!!.token
                    sharedPrefsHelper.setFCMToken(fcmToken)
                    logE("FCM Token:${fcmToken}")
                }
            } else {
                logE("getInstance Failed:${task.exception}")
            }
        }
    }

    private fun consumeResponse(apiResponse: ApiResponse<CreateProfileResponse>) {
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

    private fun renderResponse(response: CreateProfileResponse?) {
        logE("socketResponse $response")
        response?.let {
            if (it.status) {
//                toast(it.data)
                sharedPrefsHelper.setUser(it.data)
                val intent = Intent(this, WelcomeActivity::class.java)
                intent.putExtra(AppConstants.PROFILE_IMAGE_KEY, profileImagePath.toString())
                intent.putExtra(AppConstants.USER_NAME, binding.etFirstName.text.toString())
                startActivity(intent)
                finish()
            } else {
                showAlertDialoge(this, "Error", it.message)
            }
        }

    }

    private fun checkPixPermission() {
        Handler().postDelayed(
            {
                PermissionHelper.withActivity(this).addPermissions(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ).listener(object : MediaPermissionListener {
                    override fun onPermissionGranted() {
//                        Pix.start(this@CreateProfileActivity, PROFILE_IMAGE_CODE)
                        Pix.start(
                            this@CreateProfileActivity,
                            Options.init().setRequestCode(AppConstants.PROFILE_IMAGE_CODE)
                        )
                    }

                    override fun onPermissionDenied() {
                        logE("onPermissionDenied")
                    }

                }).build().init()

            }, 100
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                AppConstants.PROFILE_IMAGE_CODE -> {
                    val returnValue = data?.getStringArrayListExtra(Pix.IMAGE_RESULTS)
                    logE("onActivityResult returnValue: $returnValue")
                    CropHelper.startCropActivity(
                        sharedPrefsHelper,
                        this,
                        Uri.fromFile(File(returnValue?.get(0).toString()))
                    )
                }
                UCrop.REQUEST_CROP -> {
                    logE("onActivityResult REQUEST_CROP")
                    handleCropResult(data)
                }
            }
        }
    }

    private fun handleCropResult(result: Intent?) {
        profileImagePath = ""
        val resultUri =/* result?.let {  }*/result?.let { UCrop.getOutput(it) }
        if (resultUri == null) {
            Toast.makeText(this, "Error in Image file", Toast.LENGTH_SHORT).show()
            return
        }
        logE("result: ${resultUri.path}")
        profileImagePath = resultUri.path.toString()
        GlideDownloder.load(
            this,
            binding.ivUploadImage,
            resultUri.path,
            R.drawable.dummy_placeholder,
            R.drawable.dummy_placeholder
        )
        binding.ivCameraUpload.visibility = View.GONE
    }

    private fun setData() {
        if (binding.etFirstName.text.toString().isEmpty()) {
            toast("Please enter your first name")
            return
        }
        if (binding.etLastName.text.toString().isEmpty()) {
            toast("Please enter your last name")
            return
        }
        if (binding.etPassword.text.toString().isEmpty()) {
            toast("Please enter password")
            return
        }
        if (binding.etEmail.text.toString().isEmpty()) {
            toast("Please enter your email address")
            return
        } else if (!isEmailValid(binding.etEmail.text.toString())) {
            toast("Email not Valid")
            return
        }


        if (!profileImagePath.isNullOrEmpty()) {
            logE("Profile image is not empty")

            val params = HashMap<String, RequestBody>()
            params["firstname"] = RequestBody.create(
                MediaType.parse("text/plain"),
                binding.etFirstName.text.toString()
            )
            params["lastname"] = RequestBody.create(
                MediaType.parse("text/plain"),
                binding.etLastName.text.toString()
            )
            params["email"] =
                RequestBody.create(MediaType.parse("text/plain"), binding.etEmail.text.toString())
            params["phone"] = RequestBody.create(
                MediaType.parse("text/plain"),
                sharedPrefsHelper.getNumber().toString()
            )
            params["password"] = RequestBody.create(
                MediaType.parse("text/plain"),
                binding.etPassword.text.toString()
            )
            params["country"] = RequestBody.create(
                MediaType.parse("text/plain"),
                sharedPrefsHelper.getPhone()?.countryCode.toString()
            )
            params["country_code"] = RequestBody.create(
                MediaType.parse("text/plain"),
                sharedPrefsHelper.getPhone()?.dialCode.toString()
            )
            params["fcm_token"] = RequestBody.create(MediaType.parse("text/plain"), fcmToken)

            val imageFileBody: MultipartBody.Part?
            val fileToUpload = File(profileImagePath.toString())
            val requestBody = RequestBody.create(MediaType.parse("image/*"), fileToUpload)
            imageFileBody =
                MultipartBody.Part.createFormData("file", fileToUpload.name, requestBody)
            viewModel.hitCreateProfileApi(params, imageFileBody)
        } else {
            logE("Profile image empty")

            val params = HashMap<String, String>()
            params["firstname"] = binding.etFirstName.text.toString()
            params["lastname"] = binding.etLastName.text.toString()
            params["email"] = binding.etEmail.text.toString()
            params["phone"] = sharedPrefsHelper.getNumber().toString()
            params["password"] = binding.etPassword.text.toString()
            params["country"] = sharedPrefsHelper.getPhone()?.countryCode.toString()
            params["country_code"] = sharedPrefsHelper.getPhone()?.dialCode.toString()
            params["fcm_token"] = fcmToken
            logE("Fcm Token ${fcmToken}")

            viewModel.hitCreateProfileApi(params)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnBack -> finish()
            R.id.btnNext -> {
                setData()
            }
            R.id.ivUploadImage -> checkPixPermission()
            R.id.hidePassword -> {
                if (HIDE_PASSWORD) {
                    HIDE_PASSWORD = false
                    binding.etPassword.transformationMethod =
                        HideReturnsTransformationMethod.getInstance()
                    binding.hidePassword.setBackgroundResource(R.drawable.eye_icon)
                } else {
                    HIDE_PASSWORD = true
                    binding.etPassword.transformationMethod =
                        PasswordTransformationMethod.getInstance()
                    binding.hidePassword.setBackgroundResource(R.drawable.hide_eye_icon)
                }
            }
        }
    }

    private fun logE(message: String) {
        Debugger.e(TAG, message)
    }


}
