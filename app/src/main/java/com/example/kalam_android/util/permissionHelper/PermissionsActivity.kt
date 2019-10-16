package com.example.kalam_android.util.permissionHelper

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.kalam_android.util.permissionHelper.helper.PermissionStatics
import com.example.kalam_android.util.permissionHelper.helper.PermissionStatics.Companion.PERMISSION_LIST_KEY
import com.example.kalam_android.util.permissionHelper.helper.PermissionStatics.Companion.PERMISSION_REQUEST_CODE
import com.example.kalam_android.util.permissionHelper.helper.PermissionStatics.Companion.mediaPermissionListener
import java.util.*

class PermissionsActivity : Activity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private val TAG = "PermissionsActivity"

    private var permissionsList: ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        permissionsList = intent.getStringArrayListExtra(PERMISSION_LIST_KEY)
        checkPermissions()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            var allPermissionGranted = true

            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    allPermissionGranted = false
                    break
                }
            }

            if (allPermissionGranted) {
                mediaPermissionListener?.let {
                    it.onPermissionGranted()
                }
            } else {
                mediaPermissionListener?.onPermissionDenied()
            }
            finish()
        }
    }


    private fun checkPermissions() {
        val tempList = ArrayList<String>()
        if (PermissionStatics.isMarshmallow) {
            permissionsList?.forEach {
                if (isNotGranted(it)) {
                    tempList.add(it)
                }
            }
            if (tempList.isEmpty()) {
                callGranted()
            } else {
                requestPermissions(
                    tempList.toTypedArray(),
                    PERMISSION_REQUEST_CODE
                )
            }
        } else {
            callGranted()
        }
    }

    private fun callGranted() {
        mediaPermissionListener?.onPermissionGranted()
        finish()
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun isNotGranted(permission: String): Boolean {
        return this.checkSelfPermission(
            permission
        ) != PackageManager.PERMISSION_GRANTED
    }

}
