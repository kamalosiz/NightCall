package com.example.kalam_android.base

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import com.example.kalam_android.R
import com.github.nkzawa.engineio.client.Socket
import com.github.nkzawa.socketio.client.IO


open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

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

    fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(
            email
        ).matches()
    }

    fun showAlertDialoge(context: Context, title: String, message: String) {
        val builder1 = AlertDialog.Builder(context)
        builder1.setTitle(title)
        builder1.setMessage(message)
        builder1.setCancelable(true)
        builder1.setPositiveButton("Okay") { dialog, id -> dialog.cancel() }
        builder1.create().show()
    }

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
}