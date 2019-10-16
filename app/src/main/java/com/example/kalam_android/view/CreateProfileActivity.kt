package com.example.kalam_android.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.databinding.DataBindingUtil
import com.example.kalam_android.R
import com.example.kalam_android.databinding.ActivityCreateProfileBinding
import com.example.kalam_android.util.D
import com.example.kalam_android.util.permissionHelper.helper.PermissionHelper
import com.example.kalam_android.util.permissionHelper.listeners.MediaPermissionListener
import com.fxn.pix.Pix
import android.net.Uri
import android.widget.Toast
import com.example.kalam_android.util.CropHelper
import com.example.kalam_android.wrapper.GlideDownloder
import com.fxn.pix.Options
import com.yalantis.ucrop.UCrop
import java.io.File


class CreateProfileActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = this.javaClass.simpleName
    private val IMAGE_REQUEST_CODE = 111
    private var profileImagePath: String? = null
    lateinit var binding: ActivityCreateProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_profile)
        binding.btnBack.setOnClickListener(this)
        binding.ivUploadImage.setOnClickListener(this)
        binding.btnNext.setOnClickListener(this)
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
//                        Pix.start(this@CreateProfileActivity, IMAGE_REQUEST_CODE)
                        Pix.start(
                            this@CreateProfileActivity,
                            Options.init().setRequestCode(IMAGE_REQUEST_CODE)
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
                IMAGE_REQUEST_CODE -> {
                    val returnValue = data?.getStringArrayListExtra(Pix.IMAGE_RESULTS)
                    logE("onActivityResult returnValue: $returnValue")
                    CropHelper.startCropActivity(
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
        val resultUri = result?.let { UCrop.getOutput(it) }
        if (resultUri == null) {
            Toast.makeText(this, "Error in Image file", Toast.LENGTH_SHORT).show()
            return
        }
        profileImagePath = resultUri.path
        GlideDownloder.load(
            this,
            binding.ivUploadImage,
            profileImagePath ?: "",
            R.drawable.user_placeholder,
            R.drawable.user_placeholder
        )
        binding.ivCameraUpload.visibility = View.GONE
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnBack -> finish()
            R.id.btnNext -> {
                startActivity(Intent(this, WelcomeActivity::class.java))
            }
            R.id.ivUploadImage -> checkPixPermission()
        }
    }

    private fun logE(message: String) {
        D.e(TAG, message)
    }
}
