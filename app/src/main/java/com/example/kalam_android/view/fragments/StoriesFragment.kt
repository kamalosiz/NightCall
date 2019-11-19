package com.example.kalam_android.view.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kalam_android.R
import com.example.kalam_android.callbacks.AddMyStatusClickListener
import com.example.kalam_android.databinding.StoriesFragmentBinding
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.view.adapter.StoriesAdapter
import com.fxn.pix.Options
import com.fxn.pix.Pix
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_create_profile.view.*

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
        binding.rvStories.adapter = StoriesAdapter(activity!!.applicationContext, this)

        return binding.root
    }

    override fun addMyStatus(view: View, position: Int) {

        checkPixPermission(
            activity!!,
            AppConstants.STATUS_IMAGE_CODE
        )
    }

    @SuppressLint("CheckResult")
    fun checkPixPermission(context: FragmentActivity, requestCode: Int) {
        RxPermissions(this)
            .request(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .subscribe { granted ->
                if (granted) {
                    Pix.start(
                        context,
                        Options.init().setRequestCode(requestCode)
                    )
                } else {
                    Debugger.e("Capturing Image", "onPermissionDenied")
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

}