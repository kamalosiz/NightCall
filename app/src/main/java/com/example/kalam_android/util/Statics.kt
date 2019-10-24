package com.example.kalam_android.util

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast
import com.fxn.pix.Options
import com.fxn.utility.ImageQuality
import android.content.DialogInterface
import android.text.TextUtils
import okhttp3.FormBody
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.file.Files.delete
import java.nio.file.Files.exists
import android.R.attr.path


fun Context.toast(message: CharSequence) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun pixOptionsSingle(): Options {
    val options = Options.init()
        .setRequestCode(100)                                                 //Request code for activity results
        .setCount(3)                                                         //Number of images to restict selection count
        .setFrontfacing(false)                                                //Front Facing camera on start
        .setImageQuality(ImageQuality.HIGH)                                  //Image Quality
//        .setPreSelectedUrls(returnValue)                                     //Pre selected Image Urls
        .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)           //Orientaion
        .setPath("/pix/images")
    return options
}

fun showAlertDialoge(context: Context, title: String, message: String) {
    val builder1 = AlertDialog.Builder(context)
    builder1.setTitle(title)
    builder1.setMessage(message)
    builder1.setCancelable(true)
    builder1.setPositiveButton("Okay") { dialog, id -> dialog.cancel() }
    builder1.create().show()
}

/*fun getFileBody(path: String, fileName: String): MultipartBody.Part {
    val file = File(path)
    val requestFileProfile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
    return MultipartBody.Part.createFormData(fileName, file.name, requestFileProfile)
}*/

fun isValidEmail(email: String): Boolean {
    return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(
        email
    ).matches()
}

