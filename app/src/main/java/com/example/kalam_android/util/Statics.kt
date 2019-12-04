package com.example.kalam_android.util

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.loader.content.CursorLoader
import com.example.kalam_android.repository.model.MediaList
import com.koushikdutta.ion.builder.BitmapBuilder
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
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
        elapsedDays.toInt() != 0 -> {
            duration = if (elapsedDays.toInt() == 1) {
                "$elapsedDays day ago"
            } else {
                "$elapsedDays days ago"
            }
        }
        elapsedHours.toInt() != 0 -> {
            duration = if (elapsedHours.toInt() == 1) {
                "$elapsedHours hour ago"
            } else {
                "$elapsedHours hours ago"
            }
        }
        elapsedMinutes.toInt() != 0 -> {
            duration = if (elapsedMinutes.toInt() == 1) {
                "$elapsedMinutes minute ago"
            } else {
                "$elapsedMinutes minutes ago"
            }
        }
        elapsedSeconds.toInt() != 0 -> {
            duration = if (elapsedSeconds.toInt() == 1) {
                "$elapsedSeconds second ago"
            } else {
                "$elapsedSeconds seconds ago"
            }
        }
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

fun getFileSizeInBytes(selectedPath: String): Int {
    val file = File(selectedPath)
    return (file.length() / 1024).toString().toInt()
}

@SuppressLint("Recycle")
fun getGalleryImagesVideos(context: Context): ArrayList<MediaList> {

    val listOfAllImages = ArrayList<MediaList>()
    val column_index_data: Int
    val projection = arrayOf(
        MediaStore.Files.FileColumns._ID,
        MediaStore.Files.FileColumns.DATA,
        MediaStore.Files.FileColumns.DATE_ADDED,
        MediaStore.Files.FileColumns.MEDIA_TYPE,
        MediaStore.Files.FileColumns.MIME_TYPE,
        MediaStore.Files.FileColumns.TITLE
    )

// Return only video and image metadata.
    val selection = (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
            + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
            + " OR "
            + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
            + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)

    val queryUri = MediaStore.Files.getContentUri("external")

    val cursorLoader = CursorLoader(
        context,
        queryUri,
        projection,
        selection,
        null, // Selection args (none).
        MediaStore.Files.FileColumns.DATE_ADDED + " DESC" // Sort order.
    )

    val cursor = cursorLoader.loadInBackground()
    column_index_data = cursor!!.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
    while (cursor.moveToNext()) {
        val path = cursor.getString(column_index_data)
        if (path.contains(".jpg") || path.contains(".png")) {
            listOfAllImages.add(
                MediaList(
                    cursor.getString(column_index_data),
                    AppConstants.IMAGE_GALLERY
                )
            )
        } else {
            listOfAllImages.add(
                MediaList(
                    cursor.getString(column_index_data),
                    AppConstants.POST_VIDEO
                )
            )
        }
    }
    cursor.close()
    return listOfAllImages
}

/*fun getAllShownImagesPath(context: Context): ArrayList<MediaList> {
    val listOfAllImages = ArrayList<MediaList>()
    val uri: Uri
    val cursor: Cursor?
    val column_index_data: Int
    val column_index_folder_name: Int
    var absolutePathOfImage: String? = null
    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    try {
        val projection =
            arrayOf(MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

        cursor = context.contentResolver.query(uri, projection, null, null, null)

        column_index_data = cursor!!.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
        column_index_folder_name = cursor
            .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data)
            listOfAllImages.add(MediaList(absolutePathOfImage, AppConstants.IMAGE_GALLERY))
//            D.e("getAllShownImagesPath", "Gallery Image path: " + absolutePathOfImage!!)
        }
        cursor.close()
    } catch (e: Exception) {

    }

    return listOfAllImages
}

fun getAllShownVideosPath(context: Context): ArrayList<MediaList> {
    val listOfAllImages = ArrayList<MediaList>()
    val uri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    val cursor: Cursor?
    val column_index_data: Int
    val column_index_folder_name: Int
    var absolutePathOfImage: String? = null

    try {
        val projection =
            arrayOf(MediaStore.MediaColumns.DATA, MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
        cursor = context.contentResolver.query(uri, projection, null, null, null)
        column_index_data = cursor!!.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
        column_index_folder_name = cursor
            .getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data)
            listOfAllImages.add(MediaList(absolutePathOfImage, AppConstants.POST_VIDEO))
//            D.e("getAllShownVideosPath", "Gallery Video path: " + absolutePathOfImage!!)
        }
        cursor.close()
    } catch (e: Exception) {

    }
    return listOfAllImages
}

fun getEmptyMultipartList(): ArrayList<MultipartBody.Part> {
    var multiPartList: ArrayList<MultipartBody.Part> = ArrayList()
    return multiPartList
}*/
