package com.example.kalam_android.view.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.PagerAdapter
import com.example.kalam_android.R
import com.example.kalam_android.databinding.ItemForMediaViewerBinding
import com.example.kalam_android.repository.model.MediaList
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.view.activities.OpenMediaActivity
import com.example.kalam_android.wrapper.GlideDownloader
import java.util.*

class AdapterForMediaView(val context: Context, var list: ArrayList<MediaList>) : PagerAdapter() {

    private lateinit var binding: ItemForMediaViewerBinding

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.item_for_media_viewer,
            null,
            false
        )
        when (list[position].type) {
            AppConstants.IMAGE_GALLERY -> {
                binding.viewVideoHolder.rlVideo.visibility = View.GONE
                binding.ivImageViewer.visibility = View.VISIBLE
                binding.viewAudiHolder.rlAudio.visibility = View.GONE
                binding.ivImageViewer.let {
                    GlideDownloader.load(
                        context,
                        it,
                        list[position].file,
                        R.drawable.dummy_placeholder_1,
                        R.drawable.dummy_placeholder_1
                    )
                }
            }
            AppConstants.POST_VIDEO -> {

                binding.viewVideoHolder.rlVideo.visibility = View.VISIBLE
                binding.ivImageViewer.visibility = View.GONE
                binding.viewAudiHolder.rlAudio.visibility = View.GONE
                binding.viewVideoHolder.ivImage.let {
                    GlideDownloader.load(
                        context,
                        it,
                        list[position].file,
                        R.drawable.dummy_placeholder_1,
                        R.drawable.dummy_placeholder_1
                    )
                }
                binding.viewVideoHolder.rlVideo.setOnClickListener {
                    startOpenMediaActivity(list[position].file, list[position].type.toString(), it)
                }

            }
            AppConstants.AUDIO_GALLERY -> {

                binding.viewVideoHolder.rlVideo.visibility = View.GONE
                binding.ivImageViewer.visibility = View.GONE
                binding.viewAudiHolder.rlAudio.visibility = View.VISIBLE
                binding.viewAudiHolder.rlAudio.setOnClickListener {
                    startOpenMediaActivity(list[position].file, list[position].type.toString(), it)
                }

            }
        }
        container.addView(binding.root, 0)
        return binding.root
    }

    fun updateList(list: ArrayList<MediaList>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }


    override fun getCount(): Int {
        return list.size
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {}

    override fun saveState(): Parcelable? {
        return null
    }

    private fun startOpenMediaActivity(file: String, type: String, view: View) {
        val intent = Intent(context, OpenMediaActivity::class.java)
        intent.putExtra(AppConstants.CHAT_FILE, file)
        intent.putExtra(AppConstants.CHAT_TYPE, AppConstants.VIDEO_MESSAGE)
        intent.putExtra(AppConstants.USER_NAME, "")
        intent.putExtra(AppConstants.PROFILE_IMAGE_KEY, "")

        val transitionName = context.getString(R.string.trans_key)
        val options =
            ActivityOptionsCompat.makeSceneTransitionAnimation(
                context as Activity,
                view,
                transitionName
            )
        ActivityCompat.startActivity(context, intent, options.toBundle())
    }
}