package com.example.kalam_android.view.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kalam_android.R
import com.example.kalam_android.callbacks.OnGalleryItemClickedListener
import com.example.kalam_android.repository.model.MediaList
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.ImageFetcher
import com.example.kalam_android.view.adapter.GalleryImageAdapter
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_gallery_post.*
import kotlinx.android.synthetic.main.gallery_image_item.view.*

class GalleryPostActivity : AppCompatActivity(), OnGalleryItemClickedListener {

    var allPermissionsGranted = false
    var pictureCounter = 0
    private val TAG = this.javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery_post)
        initPermissions()
    }

    private fun initPermissions() {
        askPermissions()
    }

    @SuppressLint("CheckResult")
    private fun askPermissions() {
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    initImageFetcher()
                }else{
                    Toast.makeText(
                        applicationContext,
                        "Permission required to use gallery storage",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
            }

        })
//        allPermissionsGranted = false
//        RxPermissions(this@GalleryPostActivity)
//            .request(
//                Manifest.permission.READ_EXTERNAL_STORAGE,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE
//            )
//            .subscribe { granted ->
//                if (granted) {
//                    allPermissionsGranted = true
//                    initImageFetcher()
//                } else {
//                    Toast.makeText(
//                        this,
//                        "Permission required to use gallery storage",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    finish()
//                }
//            }
    }

//    lateinit var mediaOptions: ArrayList<GalleryImageModel>

    private fun initImageFetcher() {

        val imageFetcher = @SuppressLint("StaticFieldLeak")
        object : ImageFetcher(this) {
            override fun onPostExecute(paths: ArrayList<MediaList>) {
                super.onPostExecute(paths)
                var mediaOptions = ArrayList<MediaList>()

                mediaOptions.addAll(paths)
                setList(mediaOptions)
            }
        }
        imageFetcher.execute()
    }

    lateinit var highLightList: ArrayList<Int>

    private fun setList(list: ArrayList<MediaList>) {
        highLightList = ArrayList()
        tempList = ArrayList()
        val gridLayoutManager = GridLayoutManager(
            applicationContext,
            3,
            LinearLayoutManager.VERTICAL,
            false
        )
        rvGalleryImages.layoutManager = gridLayoutManager
        rvGalleryImages.adapter = GalleryImageAdapter(this, list, this)
    }

    lateinit var tempList: ArrayList<MediaList>
    override fun onGalleryItemClicked(
        list: ArrayList<MediaList>,
        view: View,
        position: Int
    ) {

        logE("onGalleryItemClicked : position: $position")

        if (!tempList.contains(MediaList(list[position].file, list[position].type))) {
            view.setBackgroundColor(resources.getColor(R.color.grey))
            if (pictureCounter < list.size) {
                pictureCounter++
            }
            view.rlSelectedImageCounter?.visibility = View.VISIBLE
//            view.rlSelectedImageCounter?.visibility = pictureCounter.toString()
            highLightList.add(position)
            logE("onGalleryItzemClicked : highlight position: ${highLightList[highLightList.size - 1]}")
            tempList.add(MediaList(list[position].file, list[position].type))
        } else {
            highLightList.remove(position)
            view.setBackgroundColor(0)
            if (pictureCounter > 0) {
                pictureCounter--
            }
            view.rlSelectedImageCounter?.visibility = View.GONE
//            view.tvSelectedImageCounter?.text = ""
            highLightList.remove(position)
            tempList.remove(MediaList(list[position].file, list[position].type))
        }
        if (tempList.size == 0) {
            tvHeaderTitle.text = "Gallery"
            rlDone.isClickable = false
//            tvDone.setTextColor(Color.parseColor("#999999"))
        } else {
            tvHeaderTitle.text = "Gallery (${tempList.size})"
            rlDone.isClickable = true
//            tvDone.setTextColor(Color.BLUE)
        }
    }

    fun rlCameraAction(view: View) {

    }

    fun rlBackAction(view: View) {

        onBackPressed()
//        finish()
//        ChatDetailActivity.setGallery = false
    }

    fun rlDoneAction(view: View) {
        if (tempList.size > 0) {
//            ChatDetailActivity.itemList.addAll(tempList)
//            ChatDetailActivity.setGallery = true
            val intent = Intent()
            intent.putExtra(AppConstants.SELECTED_IMAGES_VIDEOS, tempList)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        finish()
//        ChatDetailActivity.setGallery = false
    }

    private fun logE(msg: String) {
        Debugger.e(TAG, msg)
    }
}