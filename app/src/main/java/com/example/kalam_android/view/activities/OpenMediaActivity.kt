package com.example.kalam_android.view.activities

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.DataBindingUtil
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.databinding.ActivityOpenMediaBinding
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.wrapper.GlideDownloader
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.*
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.util.Util

class OpenMediaActivity : BaseActivity(), View.OnClickListener, Player.EventListener {

    lateinit var binding: ActivityOpenMediaBinding
    private val TAG = this.javaClass.simpleName
    private var type: String? = ""
    private var file: String? = ""
    private var name: String? = ""
    private var profile: String? = ""
    var player: SimpleExoPlayer? = null
    private var mHandler: Handler? = null
    var mRunnable: Runnable? = null
    private val MIN_BUFFER_DURATION = 3000
    private val MAX_BUFFER_DURATION = 5000
    private val MIN_PLAYBACK_START_BUFFER = 1500
    private val MIN_PLAYBACK_RESUME_BUFFER = 5000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_open_media)
        initialization()
        checkMediaType()
    }

    private fun initialization() {
        type = intent.getStringExtra(AppConstants.CHAT_TYPE)
        file = intent.getStringExtra(AppConstants.CHAT_FILE)
        name = intent.getStringExtra(AppConstants.USER_NAME)
        profile = intent.getStringExtra(AppConstants.PROFILE_IMAGE_KEY)
        binding.header.rlBack.setOnClickListener(this)
        binding.header.llProfile.setOnClickListener(this)
        binding.header.ivAudio.visibility = View.GONE
        binding.header.ivMore.visibility = View.GONE
        binding.header.ivVideo.visibility = View.GONE
        GlideDownloader.load(
            this,
            binding.header.ivProfileImage,
            profile,
            R.color.grey,
            R.color.grey
        )
        binding.header.tvName.text = name
    }

    private fun checkMediaType() {
        if (type == AppConstants.IMAGE_MESSAGE) {
            logE("Image Received")
            binding.image.visibility = View.VISIBLE
            binding.rlVideo.visibility = View.GONE
            GlideDownloader.load(
                this,
                binding.image,
                file,
                R.color.grey,
                R.color.grey
            )
        } else if (type == AppConstants.VIDEO_MESSAGE) {
            logE("Video Received")
            binding.image.visibility = View.GONE
            binding.rlVideo.visibility = View.VISIBLE
            setUpPlayer(file)
        }
    }

    private fun setUpPlayer(videoUri: String?) {
        initializePlayer()
        if (videoUri == null) {
            return
        }
        buildMediaSource(Uri.parse(videoUri))
    }

    private fun initializePlayer() {
        if (player == null) {
            val loadControl: LoadControl = DefaultLoadControl(
                DefaultAllocator(true, 16),
                MIN_BUFFER_DURATION,
                MAX_BUFFER_DURATION,
                MIN_PLAYBACK_START_BUFFER,
                MIN_PLAYBACK_RESUME_BUFFER, -1, true
            )
            val bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter()
            val videoTrackSelectionFactory: TrackSelection.Factory =
                AdaptiveTrackSelection.Factory(bandwidthMeter)
            val trackSelector: TrackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
            player = ExoPlayerFactory.newSimpleInstance(
                DefaultRenderersFactory(this),
                trackSelector,
                loadControl
            )
            binding.videoFullScreenPlayer.player = player
        }
    }

    private fun buildMediaSource(mUri: Uri) {
        logE("Uri : $mUri")
        val bandwidthMeter = DefaultBandwidthMeter()
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
            this,
            Util.getUserAgent(this, getString(R.string.app_name)), bandwidthMeter
        )
        val videoSource: MediaSource = ExtractorMediaSource.Factory(dataSourceFactory)
            .createMediaSource(mUri)
        player?.prepare(videoSource)
        player?.playWhenReady = true
        player?.addListener(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.header.header.visibility = View.GONE
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            binding.header.header.visibility = View.VISIBLE
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    private fun logE(msg: String) {
        Debugger.e(TAG, msg)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.rlBack -> {
                onBackPressed()
            }
            R.id.llProfile -> {
                val intent = Intent(this@OpenMediaActivity, UserProfileActivity::class.java)
                intent.putExtra(AppConstants.CHAT_USER_NAME, name)
                intent.putExtra(AppConstants.CHAT_USER_PICTURE, profile)
                val transitionName = getString(R.string.profile_trans)
                val options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this,
                        binding.header.ivProfileImage,
                        transitionName
                    )
                ActivityCompat.startActivity(this, intent, options.toBundle())
            }
        }
    }


    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
    }

    override fun onSeekProcessed() {
    }

    override fun onTracksChanged(
        trackGroups: TrackGroupArray?,
        trackSelections: TrackSelectionArray?
    ) {
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
    }

    override fun onLoadingChanged(isLoading: Boolean) {
    }

    override fun onPositionDiscontinuity(reason: Int) {
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
    }

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {

    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            Player.STATE_BUFFERING -> binding.spinnerVideoDetails.visibility = View.VISIBLE
            Player.STATE_ENDED -> {
            }
            Player.STATE_IDLE -> {
            }
            Player.STATE_READY -> binding.spinnerVideoDetails.visibility = View.GONE
            else -> {
            }
        }
    }

    private fun releasePlayer() {
        if (player != null) {
            player?.release()
            player = null
        }
    }

    private fun pausePlayer() {
        if (player != null) {
            player?.playWhenReady = false
            player?.playbackState
        }
    }

    private fun resumePlayer() {
        if (player != null) {
            player?.playWhenReady = true
            player?.playbackState
        }
    }

    override fun onPause() {
        super.onPause()
        pausePlayer()
        if (mRunnable != null) {
            mHandler?.removeCallbacks(mRunnable)
        }
    }

    override fun onRestart() {
        super.onRestart()
        resumePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }
}
