package com.example.kalam_android.util.permissionHelper.helper

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.kalam_android.util.permissionHelper.PermissionsActivity
import com.example.kalam_android.util.permissionHelper.listeners.MediaPermissionListener
import java.util.*

class PermissionHelper(permissionBuilder: PermissionBuilder) : MediaPermissionListener {
//    private val TAG = "PermissionHelper"
    private var mediaPermissionListener: MediaPermissionListener? = null
    private val permissionsList: ArrayList<String>?
    var context: Context? = null

    init {
        this.mediaPermissionListener = permissionBuilder.mediaPermissionListener
        this.context = permissionBuilder.context
        this.permissionsList = permissionBuilder.permissionsList
    }

    fun init() {
        PermissionStatics.mediaPermissionListener = this.mediaPermissionListener
        val intent = Intent(context, PermissionsActivity::class.java)
        intent.putStringArrayListExtra(PermissionStatics.PERMISSION_LIST_KEY, permissionsList)
        this.context?.startActivity(intent)
    }

    override fun onPermissionGranted() {
        mediaPermissionListener?.onPermissionGranted()
    }

    override fun onPermissionDenied() {
        mediaPermissionListener?.onPermissionDenied()
    }

    class PermissionBuilder(internal var context: Context?) {
        internal var mediaPermissionListener: MediaPermissionListener?= null
        internal var permissionsList: ArrayList<String>? = null

        init {
            this.permissionsList = ArrayList()
        }

        /*fun with(context: Context): PermissionBuilder {
            this.context = context
            this.permissionsList = ArrayList()
            return this
        }*/

        /*fun addPermission(permission: String): PermissionBuilder {
            if (permissionsList == null) {
                permissionsList = ArrayList()
            }
//            permissionsList!!.add(permission)
            permissionsList?.add(permission)
            return this
        }*/

        fun addPermissions(vararg permission: String): PermissionBuilder {
            if (permissionsList == null) {
                permissionsList = ArrayList()
            }
            permissionsList?.addAll(listOf(*permission))
            return this
        }

        fun listener(mediaPermissionListener: MediaPermissionListener): PermissionBuilder {
            this.mediaPermissionListener = mediaPermissionListener
            return this
        }

        fun build(): PermissionHelper {
            return PermissionHelper(this)
        }
    }

    companion object {
        fun withActivity(context: Context?): PermissionBuilder {
            return PermissionBuilder(context)
        }

        /*fun showAlert(context: Context) {
            Toast.makeText(context, "Please Allow Permissions to proceed.", Toast.LENGTH_SHORT).show()
        }*/
    }
}
