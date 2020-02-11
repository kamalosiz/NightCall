package com.example.kalam_android.view.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.callbacks.SelectAudioCallBack
import com.example.kalam_android.databinding.ActivityAudioFileBinding
import com.example.kalam_android.repository.model.AudioModel
import com.example.kalam_android.repository.model.MediaList
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.AudioFetcher
import com.example.kalam_android.util.ImageFetcher
import com.example.kalam_android.view.adapter.AdapterForAudio
import com.example.kalam_android.view.adapter.GalleryImageAdapter
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_gallery_post.*
import kotlinx.android.synthetic.main.item_chat.*
import kotlinx.android.synthetic.main.item_for_audio.view.*

class AudioFileActivity : BaseActivity(), SelectAudioCallBack, View.OnClickListener {

    private lateinit var binding: ActivityAudioFileBinding
    private lateinit var adapterForAudio: AdapterForAudio
    private var mediaList: ArrayList<MediaList> = ArrayList()
    private var isLoaded = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_audio_file)
        askPermissions()
        onClickListener()
    }

    private fun onClickListener() {

        binding.ivBack.setOnClickListener(this)
        binding.tvDone.setOnClickListener(this)
    }

    @SuppressLint("CheckResult")
    private fun askPermissions() {
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    binding.progressBar.visibility = View.VISIBLE
                    initAudioFetcher()
                } else {
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
                token?.continuePermissionRequest()
            }

        }).check()
    }

    private fun initAudioFetcher() {

        val audio = @SuppressLint("StaticFieldLeak")
        object : AudioFetcher(this) {
            override fun onPostExecute(paths: ArrayList<AudioModel>) {
                super.onPostExecute(paths)
                setList(paths)
            }
        }
        audio.execute()
    }

    private fun setList(list: ArrayList<AudioModel>) {

        adapterForAudio = AdapterForAudio(this, list)
        binding.rvAudio.layoutManager = LinearLayoutManager(this)
        binding.rvAudio.adapter = adapterForAudio
        adapterForAudio.setSelectAudioCallBack(this)
        adapterForAudio.notifyDataSetChanged()
        binding.progressBar.visibility = View.GONE
    }

    override fun selectAudio(view: View, audioModel: AudioModel, position: Int) {

        if (view.ivAudioSelect.visibility == View.GONE) {
            view.ivAudioSelect.visibility = View.VISIBLE
            mediaList.add(MediaList(audioModel.aPath, AppConstants.AUDIO_GALLERY))
        } else {
            view.ivAudioSelect.visibility = View.GONE
            mediaList.remove(MediaList(audioModel.aPath, AppConstants.AUDIO_GALLERY))

        }

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBack -> {
                onBackPressed()
            }
            R.id.tvDone -> {
                val intent = Intent()
                if (mediaList.isNullOrEmpty()) {
                    finish()
                } else {
                    intent.putExtra(AppConstants.SELECTED_IMAGES_VIDEOS, mediaList)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        }
    }
}
