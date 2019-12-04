package com.example.kalam_android.view.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kalam_android.R
import com.example.kalam_android.callbacks.AddMyStatusClickListener
import com.example.kalam_android.databinding.StoriesFragmentBinding
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.toast
import com.example.kalam_android.view.adapter.StoriesAdapter
import com.fxn.pix.Options
import com.fxn.pix.Pix
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.sandrios.sandriosCamera.internal.SandriosCamera
import com.sandrios.sandriosCamera.internal.configuration.CameraConfiguration
import kotlinx.android.synthetic.main.layout_content_of_chat.view.*

class StoriesFragment : Fragment(), AddMyStatusClickListener {


    private lateinit var binding: StoriesFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.stories_fragment, container, false
        )

        binding.rvStories.layoutManager = LinearLayoutManager(activity)
        binding.rvStories.adapter = StoriesAdapter(activity!!, this)

        return binding.root
    }

    override fun addMyStatus(view: View, position: Int) {

        checkPixPermission()
    }

    private fun checkPixPermission() {
        Dexter.withActivity(activity).withPermissions(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report?.areAllPermissionsGranted() == true) {
                    /*SandriosCamera
                        .with()
                        .setShowPicker(true)
                        .setVideoFileSize(20)
                        .setMediaAction(CameraConfiguration.MEDIA_ACTION_BOTH)
                        .enableImageCropping(true)
                        .launchCamera(activity)*/
                    toast(activity,"My Status")
                } else {
                    Debugger.e("Capturing Image", "onPermissionDenied")
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

}