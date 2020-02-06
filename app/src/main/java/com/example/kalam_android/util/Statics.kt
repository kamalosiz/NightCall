package com.example.kalam_android.util

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.loader.content.CursorLoader
import com.example.kalam_android.repository.model.AudioModel
import com.example.kalam_android.repository.model.MediaList
import id.zelory.compressor.Compressor
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
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

/*fun onDisplayPopupPermission(context: Context) {
    if (!isMIUI()) {
        return
    }
    try { // MIUI 8
        val localIntent = Intent("miui.intent.action.APP_PERM_EDITOR")
        localIntent.setClassName(
            "com.miui.securitycenter",
            "com.miui.permcenter.permissions.PermissionsEditorActivity"
        )
        localIntent.putExtra("extra_pkgname", context.packageName)
        context.startActivity(localIntent)
        return
    } catch (ignore: Exception) {
    }
    try { // MIUI 5/6/7
        val localIntent = Intent("miui.intent.action.APP_PERM_EDITOR")
        localIntent.setClassName(
            "com.miui.securitycenter",
            "com.miui.permcenter.permissions.AppPermissionsEditorActivity"
        )
        localIntent.putExtra("extra_pkgname", context.packageName)
        context.startActivity(localIntent)
        return
    } catch (ignore: Exception) {
    }
    // Otherwise jump to application details
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri =
        Uri.fromParts("package", context.packageName, null)
    intent.data = uri
    context.startActivity(intent)
}


fun isMIUI(): Boolean {
    val device = Build.MANUFACTURER
    if (device == "Xiaomi") {
        try {
            val prop = Properties()
            prop.load(FileInputStream(File(Environment.getRootDirectory(), "build.prop")))
            return prop.getProperty(
                "ro.miui.ui.version.code",
                null
            ) != null || prop.getProperty(
                "ro.miui.ui.version.name",
                null
            ) != null || prop.getProperty("ro.miui.internal.storage", null) != null
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    return false
}*/
/*@Throws(IOException::class)
fun splitFile(f: File): List<File>? {
    var partCounter = 1
    val result: MutableList<File> = ArrayList()
    val sizeOfFiles = 1024 * 1024 // 1MB
    val buffer =
        ByteArray(sizeOfFiles) // create a buffer of bytes sized as the one chunk size
    val bis = BufferedInputStream(FileInputStream(f))
    val name: String = f.name
    var tmp: Int
    while (bis.read(buffer).also { tmp = it } > 0) {
        val newFile = File(
            f.parent,
            name + "." + String.format("%03d", partCounter++)
        ) // naming files as <inputFileName>.001, <inputFileName>.002, ...
        val out = FileOutputStream(newFile)
        out.write(
            buffer,
            0,
            tmp
        ) //tmp is chunk size. Need it for the last chunk, which could be less then 1 mb.
        result.add(newFile)
    }
    return result
}*/

/*@SuppressLint("NewApi")
@Throws(IOException::class)
fun mergeFiles(files: List<File>, into: File) {
    val mergingStream = BufferedOutputStream(FileOutputStream(into))
    for (f in files) {
//        val stream: InputStream = FileInputStream(f)
        Files.copy(f.toPath(), mergingStream)
//        stream.close()
    }
    mergingStream.close()
}*/

fun getFileBody(path: String, fileName: String, context: Context): MultipartBody.Part {
    val file = Compressor(context).compressToFile(File(path))
    val requestFileProfile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
    return MultipartBody.Part.createFormData(fileName, file.name, requestFileProfile)
}

/*fun calculateLocalDate(unixTime: Long): String {
    val date = Date(unixTime * 1000L)
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault())
    sdf.timeZone = TimeZone.getDefault()
    return printDifference(sdf.parse(sdf.format(date)))
}*/
/*
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
}*/
fun getTimeStamp(unixTime: Long): String {
    val date = Date(unixTime * 1000L)
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    sdf.timeZone = TimeZone.getDefault()
    return sdf.format(date)
}

fun showAlertDialog(context: Context, title: String, message: String) {
    val builder1 = AlertDialog.Builder(context)
    builder1.setTitle(title)
    builder1.setMessage(message)
    builder1.setCancelable(true)
    builder1.setPositiveButton("Okay") { dialog, id -> dialog.cancel() }
    builder1.create().show()
}

/*fun getReadableFileSize(size: Long): String {
    if (size <= 0) {
        return "0"
    }
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (kotlin.math.log10(size.toDouble()) / kotlin.math.log10(1024.0)).toInt()
    return DecimalFormat("#,##0.#").format(
        size / 1024.0.pow(digitGroups.toDouble())
    ) + " " + units[digitGroups]
}*/

/*fun getFileSizeInBytes(selectedPath: String): Int {
    val file = File(selectedPath)
    return (file.length() / 1024).toString().toInt()
}*/

@SuppressLint("Recycle")
fun getGalleryImagesVideos(context: Context): ArrayList<MediaList> {

    val listOfAllImages = ArrayList<MediaList>()
    val columnIndexData: Int
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
    columnIndexData = cursor!!.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
    while (cursor.moveToNext()) {
        val path = cursor.getString(columnIndexData)
        if (path.contains(".jpg") || path.contains(".png")) {
            listOfAllImages.add(
                MediaList(
                    cursor.getString(columnIndexData),
                    AppConstants.IMAGE_GALLERY
                )
            )
        } else {
            listOfAllImages.add(
                MediaList(
                    cursor.getString(columnIndexData),
                    AppConstants.POST_VIDEO
                )
            )
        }
    }
    cursor.close()
    return listOfAllImages
}

fun getAllAudioFromDevice(context: Context): ArrayList<AudioModel>? {
    val tempAudioList: ArrayList<AudioModel> = ArrayList()
    val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    val projection = arrayOf(
        MediaStore.Audio.AudioColumns.DATA,
        MediaStore.Audio.AudioColumns.ALBUM,
        MediaStore.Audio.ArtistColumns.ARTIST
    )

    val c = context.contentResolver.query(
        uri,
        projection,
        null,
        null,
        null
    )
    if (c != null) {
        while (c.moveToNext()) {
            val path: String = c.getString(0)
            val album: String = c.getString(1)
            val artist: String = c.getString(2)
            val name = path.substring(path.lastIndexOf("/") + 1)
            val file = File(path)
            val duration = getAudioDuration(file)
            val length = getAudioFileSize(file)

            Log.e("Name :$name", " Album :$album")
            Log.e("Path :$path", " Artist :$artist")
            tempAudioList.add(AudioModel(path, name, album, artist, duration!!, length!!))
        }
        c.close()
    }
    return tempAudioList
}

fun getAudioDuration(file: File): String? {
    val mediaMetadataRetriever = MediaMetadataRetriever()
    mediaMetadataRetriever.setDataSource(file.absolutePath)
    val durationStr: String =
        mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
    return formatMilliSecond(durationStr.toLong())
}

fun formatMilliSecond(milliseconds: Long): String? {
    var finalTimerString = ""
    var secondsString = ""
    // Convert total duration into time
    val hours = (milliseconds / (1000 * 60 * 60)).toInt()
    val minutes = (milliseconds % (1000 * 60 * 60)).toInt() / (1000 * 60)
    val seconds = (milliseconds % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()
    // Add hours if there
    if (hours > 0) {
        finalTimerString = "$hours:"
    }
    // Prepending 0 to seconds if it is one digit
    secondsString = if (seconds < 10) {
        "0$seconds"
    } else {
        "" + seconds
    }
    finalTimerString = "$finalTimerString$minutes:$secondsString"
    //      return  String.format("%02d Min, %02d Sec",
//                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
//                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
//                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
// return timer string
    return finalTimerString
}

fun getAudioFileSize(file: File): String? {
    val format = DecimalFormat("##.##")
    val MiB = 1024 * 1024
    val KiB = 1024
    if (!file.isFile) {
        throw  IllegalArgumentException("Expected a file")
    }
    val length = file.length();

    if (length > MiB) {
        return format.format(length / MiB) + " MB"
    }
    if (length > KiB) {
        return format.format(length / KiB) + " KB"
    }
    return format.format(length) + " B"
}

/*@SuppressLint("SetTextI18n")
private fun showMarkerDialog(file: String, context: Context) {
    val dialog = Dialog(context)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    val window = dialog.window
    window?.setBackgroundDrawableResource(android.R.color.transparent)
    dialog.setContentView(R.layout.map_place_dialog_item)

    dialog.findViewById<AppCompatTextView>(R.id.tvTitle).text = name
    dialog.findViewById<AppCompatTextView>(R.id.tvDescription)
        .text = "Distance: $dist KM"

    if (placeID != null) {
        if (placeID.equals("")) {
            dialog.findViewById<AppCompatButton>(R.id.btnProfile).visibility = View.GONE
        } else {
            dialog.findViewById<AppCompatButton>(R.id.btnProfile).visibility = View.VISIBLE
            dialog.findViewById<AppCompatButton>(R.id.btnProfile).setOnClickListener {
                val intent = Intent(activity, ProfileActivity::class.java)
                intent.putExtra(AppConstants.PLACE_ID, placeID)
                activity?.startActivity(intent)
                dialog.dismiss()
            }
        }
    } else {
        dialog.findViewById<AppCompatButton>(R.id.btnProfile).visibility = View.GONE
    }

    dialog.findViewById<AppCompatButton>(R.id.btnNavigate).setOnClickListener {
        val gmmIntentUri = Uri.parse(
            "google.navigation:" +
                    "q=${selectedPoint.lat},${selectedPoint.long}" +
                    "&mode=d"
        )
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent)
        dialog.dismiss()
    }

    dialog.findViewById<AppCompatButton>(R.id.btnDirections).setOnClickListener {
        val origin = "${currentPoint.lat},${currentPoint.long}"
        val destination = "${selectedPoint.lat},${selectedPoint.long}"
        getDirections(origin, destination)
        dialog.dismiss()
    }
    dialog.show()
}*/

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
