package com.example.kalam_android.util

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.widget.Toast
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.pow

fun Context.toast(message: CharSequence) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

/*fun pixOptionsSingle(): Options {
    val options = Options.init()
        .setRequestCode(100)                                                 //Request code for activity results
        .setCount(3)                                                         //Number of images to restict selection count
        .setFrontfacing(false)                                                //Front Facing camera on start
        .setImageQuality(ImageQuality.HIGH)                                  //Image Quality
//        .setPreSelectedUrls(returnValue)                                     //Pre selected Image Urls
        .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)           //Orientaion
        .setPath("/pix/images")
    return options
}*/

fun toast(context: Context?, msg: String) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}

fun getFileBody(path: String, fileName: String): MultipartBody.Part {
    val file = File(path)
    val requestFileProfile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
    return MultipartBody.Part.createFormData(fileName, file.name, requestFileProfile)
}

fun calculateLocalDate(unixTime: Long): String {
    val date = Date(unixTime * 1000L)
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault())
    sdf.timeZone = TimeZone.getDefault()
    return printDifference(sdf.parse(sdf.format(date)))
}

fun printDifference(endDate: Date): String {
    val c = Calendar.getInstance().time
    val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault())
    val formattedDate = df.format(c)
    val startDate = df.parse(formattedDate)

    var difference = startDate?.time!! - endDate.time
    if (difference.toInt() == 0) {
        difference = 1000
    }
    val secondsInMilli: Long = 1000
    val minutesInMilli = secondsInMilli * 60
    val hoursInMilli = minutesInMilli * 60
    val daysInMilli = hoursInMilli * 24
    val elapsedDays = difference / daysInMilli
    difference %= daysInMilli
    val elapsedHours = difference / hoursInMilli
    difference %= hoursInMilli
    val elapsedMinutes = difference / minutesInMilli
    difference %= minutesInMilli
    val elapsedSeconds = difference / secondsInMilli
    var duration = ""
    when {
        elapsedDays.toInt() != 0 -> duration = "$elapsedDays days ago"
        elapsedHours.toInt() != 0 -> duration = "$elapsedHours hours ago"
        elapsedMinutes.toInt() != 0 -> duration = "$elapsedMinutes minutes ago"
        elapsedSeconds.toInt() != 0 -> duration = "$elapsedSeconds seconds ago"
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

fun getReadableFileSize(size: Long): String {
    if (size <= 0) {
        return "0"
    }
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
    return DecimalFormat("#,##0.#").format(
        size / 1024.0.pow(digitGroups.toDouble())
    ) + " " + units[digitGroups]
}

