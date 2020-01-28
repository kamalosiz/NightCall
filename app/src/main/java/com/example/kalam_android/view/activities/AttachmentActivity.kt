package com.example.kalam_android.view.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kalam_android.R
import com.example.kalam_android.callbacks.MyClickListener
import com.example.kalam_android.callbacks.OnStartDragListener
import com.example.kalam_android.callbacks.RemoveItemCallBack
import com.example.kalam_android.callbacks.SelectedItemCallBack
import com.example.kalam_android.databinding.ActivityAttachmentBinding
import com.example.kalam_android.helper.EditItemTouchHelperCallback
import com.example.kalam_android.repository.model.MediaList
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.toast
import com.example.kalam_android.view.adapter.AdapterForMediaView
import com.example.kalam_android.view.adapter.AdapterSelectedMedia
import java.util.*

class AttachmentActivity : AppCompatActivity(), MyClickListener, OnStartDragListener,
    SelectedItemCallBack, View.OnClickListener, RemoveItemCallBack {

    private lateinit var binding: ActivityAttachmentBinding
    private var list: ArrayList<MediaList>? = null
    private lateinit var adapterForMediaView: AdapterForMediaView
    private lateinit var adapterForSelectedMedia: AdapterSelectedMedia
    private var mItemTouchHelper: ItemTouchHelper? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_attachment)

        getIntentData()
        initViewPagerAdapter()
        initRecyclerViewAdapter()
        dragRecyclerViewItem()
        onClick()
    }

    private fun onClick() {
        binding.ivBack.setOnClickListener(this)
        binding.llGallery.setOnClickListener(this)
        binding.tvDone.setOnClickListener(this)
        binding.llAudio.setOnClickListener(this)
    }

    private fun initViewPagerAdapter() {

        adapterForMediaView = AdapterForMediaView(this, list!!)
        binding.viewPagerForList.adapter = adapterForMediaView
        binding.viewPagerForList.isSaveEnabled = false
        adapterForMediaView.notifyDataSetChanged()

    }

    private fun initRecyclerViewAdapter() {

        adapterForSelectedMedia = AdapterSelectedMedia(this, list!!)
        binding.rvSelectedMedia.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvSelectedMedia.adapter = adapterForSelectedMedia
        adapterForSelectedMedia.setMyClickListener(this)
        adapterForSelectedMedia.setOnStartDragListener(this)
        adapterForSelectedMedia.setSelectedItemCallBack(this)
        adapterForSelectedMedia.setRemoveItemCallBack(this)
        adapterForSelectedMedia.notifyDataSetChanged()

    }

    private fun getIntentData() {
        if (intent != null) {
            list =
                intent.getSerializableExtra(AppConstants.SELECTED_IMAGES_VIDEOS) as ArrayList<MediaList>?
            Debugger.w("Gallery list ", list.toString())
        }
    }

    private fun dragRecyclerViewItem() {
        val itemAnimator: RecyclerView.ItemAnimator = DefaultItemAnimator()
        itemAnimator.addDuration = 1000
        itemAnimator.removeDuration = 1000
        binding.rvSelectedMedia.itemAnimator = itemAnimator

        val callback: ItemTouchHelper.Callback =
            EditItemTouchHelperCallback(adapterForSelectedMedia)
        mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper?.attachToRecyclerView(binding.rvSelectedMedia)
    }

    override fun myOnClick(view: View, position: Int) {
        this.list = list
        adapterForMediaView.updateList(this.list as ArrayList<MediaList>)
        binding.viewPagerForList.currentItem = position
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder?) {
        mItemTouchHelper?.startDrag(viewHolder!!)

    }

    override fun selectedItem(list: ArrayList<MediaList>, position: Int) {
        this.list = list
        adapterForMediaView.notifyDataSetChanged()
        binding.viewPagerForList.currentItem = position
    }

    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.ivBack -> {
                onBackPressed()
            }
            R.id.llGallery -> {
                startActivityForResult(
                    Intent(this, GalleryPostActivity::class.java),
                    AppConstants.SELECTED_IMAGES
                )
            }
            R.id.tvDone -> {
                val intent = Intent()
                intent.putExtra(AppConstants.SELECTED_IMAGES_VIDEOS, list)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
            R.id.llAudio -> {
                showDialog()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                AppConstants.SELECTED_IMAGES -> {
                    if (data != null) {
                        val listData =
                            data.getSerializableExtra(AppConstants.SELECTED_IMAGES_VIDEOS) as ArrayList<MediaList>
                        list?.addAll(listData)
                        adapterForMediaView.updateList(list!!)
                        adapterForSelectedMedia.notifyDataSetChanged()
                    }
                }
                AppConstants.SELECT_AUDIO -> {
                    if (data != null) {
                        val listData =
                            data.getSerializableExtra(AppConstants.SELECTED_IMAGES_VIDEOS) as ArrayList<MediaList>
                        list?.addAll(listData)
                        adapterForMediaView.updateList(list!!)
                        adapterForSelectedMedia.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    override fun onRemoveItem(mediaList: MediaList) {
        list!!.remove(mediaList)
        adapterForSelectedMedia.notifyDataSetChanged()
        adapterForMediaView.notifyDataSetChanged()
    }

    private fun showDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems = arrayOf("Select Audio", "Voice Recording")
        pictureDialog.setItems(pictureDialogItems
        ) { dialog, which ->
            when (which) {
                0 -> {
                    startActivityForResult(
                            Intent(this, AudioFileActivity::class.java),
                            AppConstants.SELECT_AUDIO
                    )
                }
                1 -> {
                    startActivityForResult(
                            Intent(this, VoiceRecordingActivity::class.java),
                            AppConstants.SELECT_AUDIO
                    )
                }
            }
        }
        pictureDialog.show()
    }


}
