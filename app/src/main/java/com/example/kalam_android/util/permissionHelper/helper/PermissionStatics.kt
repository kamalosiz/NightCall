package com.example.kalam_android.util.permissionHelper.helper

import android.os.Build

import com.example.kalam_android.util.permissionHelper.listeners.MediaPermissionListener

open class PermissionStatics {
    companion object {
        var mediaPermissionListener: MediaPermissionListener? = null
        val PERMISSION_REQUEST_CODE = 171
        var PERMISSION_LIST_KEY = "permission_list_key"

        val isMarshmallow: Boolean
            get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }
}
