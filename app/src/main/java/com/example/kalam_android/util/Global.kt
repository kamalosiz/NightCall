package com.example.kalam_android.util

import android.content.Context
import android.graphics.Typeface
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat


object Global {
    var currentChatID = -1
    fun setColor(context: Context, color: Int): Int {
        return ContextCompat.getColor(
            context,
            color
        )
    }

    fun changeText(context: Context, font: Int): Typeface {
        var typeface: Typeface = Typeface.DEFAULT
        if (font == 0) {
            typeface = Typeface.createFromAsset(context.assets, "fonts/amiri_regular.ttf")
        } else if (font == 1) {
            typeface = Typeface.createFromAsset(context.assets, "fonts/roboto_regular.ttf")
        }
        return typeface
    }

    fun showKeyBoard(context: Context, editText: EditText) {
        val imm =
            context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        setFocusCursor(editText)
    }

    private fun setFocusCursor(editText: EditText) {
        editText.isFocusable = true
        editText.isFocusableInTouchMode = true
        editText.requestFocus()
    }

    /* fun turnOFFSpeakers(audioManager: AudioManager) {
         try {
             if (audioManager.isSpeakerphoneOn) {
                 audioManager.isSpeakerphoneOn = false
             }
             audioManager.mode = AudioManager.STREAM_MUSIC
         } catch (e: java.lang.Exception) {
             e.printStackTrace()
         }
     }

     fun turnOnSpeakers(audioManager: AudioManager) {
         try {
             if (!audioManager.isSpeakerphoneOn) {
                 audioManager.isSpeakerphoneOn = true
             }
             audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
         } catch (e: java.lang.Exception) {
             e.printStackTrace()
         }
     }*/

    /*              val fileSize = getFileSizeInBytes(file)
                  if (fileSize < 2000) {
                      logE("File size is less than 2MB : $fileSize")
                      uploadMedia(identifier, file, duration, type)
                  } else {
                      logE("File size is greater than 2MB : $fileSize")
  //                    logE("Before Conversion : ${getReadableFileSize(File(file).length())}")
                      val output =
                          Environment.getExternalStorageDirectory().toString() + File.separator + System.currentTimeMillis() + ".mp4"
                      object : Thread() {
                          override fun run() {
                              super.run()
                              VideoCompressor().compressVideo(file, output)
                              runOnUiThread {
                                  logE("File size after conversion : ${getFileSizeInBytes(output)}")
                                  //                                logE("updatePosts: isVideo = 1")
  //                                logE(
  //                                    "After Conversion : ${getReadableFileSize(File(output).length())}"
  //                                )
                                  uploadMedia(identifier, output, duration, type)
                              }
                          }
                      }.start()
                  }*/
}
/* val params = HashMap<String, RequestBody>()
        params["identifier"] = RequestBody.create(MediaType.parse("text/plain"), identifier)
        params["duration"] = RequestBody.create(MediaType.parse("text/plain"), duration.toString())
        params["type"] = RequestBody.create(MediaType.parse("text/plain"), type)
        viewModel.hitUploadAudioApi(
            sharedPrefsHelper.getUser()?.token, params,
            getFileBody(file, "file")
        )*/

//must implement tha backoff policy to retry the attempt tp upload the image

/*val imageData = workDataOf(
    "identifier" to identifier,
    "file" to file,
    "duration" to duration,
    "type" to type,
    "token" to sharedPrefsHelper.getUser()?.token.toString()
)*/


/* private fun consumeMediaResponse(apiResponse: ApiResponse<MediaResponse>?) {
        when (apiResponse?.status) {

            Status.LOADING -> {
                logE("Loading Audio")
            }
            Status.SUCCESS -> {
                renderMediaResponse(apiResponse.data as MediaResponse)
            }
            Status.ERROR -> {
                toast("Something went wrong please try again")
                logE("consumeResponse ERROR: " + apiResponse.error.toString())
            }
            else -> {
            }
        }
    }

    private fun renderMediaResponse(response: MediaResponse?) {
        logE("socketResponse: $response")
        response?.let {
            it.data?.let { list ->
                emitNewMessageToSocket(
                    "",
                    list[0].type.toString(),
                    list[0].file_id.toString(),
                    list[0].duration.toLong(),
                    list[0].thumbnail,
                    list[0].identifier,
                    list[0].group_id.toString()
                )
            }
        }
    }*/