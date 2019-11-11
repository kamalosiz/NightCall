package com.example.kalam_android.util

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.widget.Toast
import com.fxn.pix.Options
import com.fxn.utility.ImageQuality
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

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

fun toast(context: Context?, msg: String) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}

fun getFileBody(path: String, fileName: String): MultipartBody.Part {
    val file = File(path)
    val requestFileProfile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
    return MultipartBody.Part.createFormData(fileName, file.name, requestFileProfile)
}

@SuppressLint("SimpleDateFormat")
fun calculateLocalDate(unixTime: Long): String {
    val date = Date(unixTime * 1000L)
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z")
    sdf.timeZone = TimeZone.getDefault()
    return printDifference(sdf.parse(sdf.format(date)))
}

@SuppressLint("SimpleDateFormat")
fun printDifference(endDate: Date): String {
    val c = Calendar.getInstance().time
    val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z")
    val formattedDate = df.format(c)
    val startDate = df.parse(formattedDate)

    var different = endDate.time - startDate.time
    val secondsInMilli: Long = 1000
    val minutesInMilli = secondsInMilli * 60
    val hoursInMilli = minutesInMilli * 60
    val daysInMilli = hoursInMilli * 24
    val elapsedDays = different / daysInMilli
    different %= daysInMilli
    val elapsedHours = different / hoursInMilli
    different %= hoursInMilli
    val elapsedMinutes = different / minutesInMilli
    different %= minutesInMilli
    val elapsedSeconds = different / secondsInMilli
    var duration = ""
    when {
        elapsedDays.toInt() != 0 -> duration = "${abs(elapsedDays)} days ago"
        elapsedHours.toInt() != 0 -> duration = "${abs(elapsedHours)} hours ago"
        elapsedMinutes.toInt() != 0 -> duration = "${abs(elapsedMinutes)} minutes ago"
        elapsedSeconds.toInt() != 0 -> duration = "${abs(elapsedSeconds)} seconds ago"
    }
    return duration
}

fun showAlertDialoge(context: Context, title: String, message: String) {
    val builder1 = AlertDialog.Builder(context)
    builder1.setTitle(title)
    builder1.setMessage(message)
    builder1.setCancelable(true)
    builder1.setPositiveButton("Okay") { dialog, id -> dialog.cancel() }
    builder1.create().show()
}

