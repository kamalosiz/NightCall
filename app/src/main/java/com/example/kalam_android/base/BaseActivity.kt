package com.example.kalam_android.base

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.kalam_android.R

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

}