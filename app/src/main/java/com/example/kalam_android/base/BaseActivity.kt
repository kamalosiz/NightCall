package com.example.kalam_android.base

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.example.kalam_android.R
import com.example.kalam_android.util.Debugger
import com.fxn.pix.Options
import com.fxn.pix.Pix
import com.tbruyelle.rxpermissions2.RxPermissions


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

    @SuppressLint("CheckResult")
    fun checkPixPermission(context: FragmentActivity, requestCode: Int) {
        RxPermissions(context)
            .request(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .subscribe { granted ->
                if (granted) {
                    Pix.start(
                        context,
                        Options.init().setRequestCode(requestCode)
                    )
                } else {
                    Debugger.e("Capturing Image", "onPermissionDenied")
                }
            }
    }
}