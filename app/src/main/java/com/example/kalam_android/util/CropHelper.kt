package com.example.kalam_android.util

import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.kalam_android.R
import com.yalantis.ucrop.UCrop
import java.io.File


object CropHelper {

    private val SAMPLE_CROPPED_IMAGE_NAME = "KalamImage"
    private var index: Int? = null

    fun startCropActivity(
        sharedPrefsHelper: SharedPrefsHelper,
        context: Context,
        uri: Uri
    ) {
        index = sharedPrefsHelper.getImageIndex()?.plus(1)
        val destinationFileName = "$SAMPLE_CROPPED_IMAGE_NAME$index.jpg"
        index?.let { sharedPrefsHelper.setImageIndex(it) }
        var uCrop = UCrop.of(uri, Uri.fromFile(File(context.cacheDir, destinationFileName)))
        uCrop.withAspectRatio(1f, 1f)  // for profile: 3:4  a kind of square
        uCrop = advancedConfig(context, uCrop)
        uCrop.start(context as AppCompatActivity)
    }

    private fun advancedConfig(context: Context, uCrop: UCrop): UCrop {
        val options = UCrop.Options()
        //        options.setCompressionQuality(mSeekBarQuality.getProgress());
//        options.setCompressionQuality(60)
        options.setCompressionQuality(95)
        options.setHideBottomControls(true)

        options.setToolbarTitle("Adjust Image")

        options.setToolbarColor(ContextCompat.getColor(context, R.color.theme_color))
        options.setStatusBarColor(ContextCompat.getColor(context, R.color.theme_color))
//        options.setColorStatusBarColor(ContextCompat.getColor(context, R.color.white))
        return uCrop.withOptions(options)
    }
}
