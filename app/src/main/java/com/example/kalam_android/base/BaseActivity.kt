package com.example.kalam_android.base

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import com.example.kalam_android.R
import com.example.kalam_android.util.Debugger


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

    fun popUpMenu(view: View?, menu: Int, listener: PopupMenu.OnMenuItemClickListener) {
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
}