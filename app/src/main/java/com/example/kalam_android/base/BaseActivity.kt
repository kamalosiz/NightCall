package com.example.kalam_android.base

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.example.kalam_android.R
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.permissionHelper.helper.PermissionHelper
import com.example.kalam_android.util.permissionHelper.listeners.MediaPermissionListener
import com.fxn.pix.Options
import com.fxn.pix.Pix
import com.github.nkzawa.engineio.client.Socket
import com.github.nkzawa.socketio.client.IO


open class BaseActivity : AppCompatActivity() {

    private var progressDialog: AlertDialog? = null

    fun showProgressDialog(context: Context) {
        progressDialog = AlertDialog.Builder(context)
            .setView(R.layout.progress_dialoge)
            .setCancelable(false)
            .show()
    }

    fun hideProgressDialog() {
        if (progressDialog!!.isShowing) {
            progressDialog!!.dismiss()
        }
    }

    val EMAIL_REGEX = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
    fun isEmailValid(email: String): Boolean {
        return EMAIL_REGEX.toRegex().matches(email)
    }
    /*fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(
            email
        ).matches()
    }*/

    fun popUpMenu(view: View, menu: Int, listener: PopupMenu.OnMenuItemClickListener) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(menu)
        popupMenu.setOnMenuItemClickListener(listener)
        /* val tempMenu = popupMenu.getMenu()
         for (i in 0 until tempMenu.size()) {
             val mi = tempMenu.getItem(i)
             applyFontToMenuItem(mi)
         }*/
        popupMenu.show()
    }

    fun checkPixPermission(context: FragmentActivity, requestCode: Int) {
        Handler().postDelayed(
            {
                PermissionHelper.withActivity(this).addPermissions(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ).listener(object : MediaPermissionListener {
                    override fun onPermissionGranted() {
                        Pix.start(
                            context,
                            Options.init().setRequestCode(requestCode)
                        )
                    }

                    override fun onPermissionDenied() {
                        Debugger.e("Capturing Image", "onPermissionDenied")
                    }

                }).build().init()

            }, 100
        )
    }
}