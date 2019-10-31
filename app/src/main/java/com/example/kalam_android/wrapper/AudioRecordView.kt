package com.example.kalam_android.wrapper

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.Timer
import java.util.TimerTask
import android.os.Handler
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import androidx.databinding.adapters.FrameLayoutBindingAdapter
import com.example.kalam_android.R
import com.example.kalam_android.databinding.LayoutForChatScreenBinding
import kotlinx.android.synthetic.main.layout_for_chat_screen.view.*

/**
 * Created by Varun John on 4 Dec, 2018
 * Github : https://github.com/varunjohn
 */
class AudioRecordView : FrameLayout {

    private var animBlink: Animation? = null
    private var animJump: Animation? = null
    private var animJumpFast: Animation? = null

    private var isDeleting: Boolean = false
    private var stopTrackingAction: Boolean = false

    object Message {
        private var handler: Handler = Handler(Looper.getMainLooper())
    }

    private var audioTotalTime: Int = 0
    private var timerTask: TimerTask? = null
    private var audioTimer: Timer? = null
    private val timeFormatter = SimpleDateFormat("mm:ss", Locale.getDefault())

    private var lastX: Float = 0.toFloat()
    private var lastY: Float = 0.toFloat()
    private var firstX: Float = 0.toFloat()
    private var firstY: Float = 0.toFloat()

    private val directionOffset: Float = 0.toFloat()
    private var cancelOffset: Float = 0.toFloat()
    private var lockOffset: Float = 0.toFloat()
    private var dp = 0f
    private var isLocked = false

    private var userBehaviour = UserBehaviour.NONE
    private lateinit var binding: LayoutForChatScreenBinding
    private lateinit var layoutInflater: LayoutInflater
    var recordingListener: RecordingListener? = null
        get() = field
        set(value) {
            field = value
        }

    enum class UserBehaviour {
        CANCELING,
        LOCKING,
        NONE
    }

    enum class RecordingBehaviour {
        CANCELED,
        LOCKED,
        LOCK_DONE,
        RELEASED
    }

    interface RecordingListener {

        fun onRecordingStarted()

        fun onRecordingLocked()

        fun onRecordingCompleted()

        fun onRecordingCanceled()

    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView()
    }

    constructor(context: Context) : super(context) {
        initView()
    }

    private fun initView() {

        layoutInflater = LayoutInflater.from(context)
        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.layout_for_chat_screen, null, true)
        addView(binding.root)

        dp = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            1f,
            context.resources.displayMetrics
        )

        animBlink = AnimationUtils.loadAnimation(
            context,
            R.anim.blink
        )
        animJump = AnimationUtils.loadAnimation(
            context,
            R.anim.jump
        )
        animJumpFast = AnimationUtils.loadAnimation(
            context,
            R.anim.jump_fast
        )

        setupRecording()
    }

    fun setAudioRecordButtonImage(imageResource: Int) {
        binding.imageAudio.setImageResource(imageResource)
    }

    fun setStopButtonImage(imageResource: Int) {
        binding.imageStop.setImageResource(imageResource)
    }

    fun setSendButtonImage(imageResource: Int) {
        binding.imageSend.setImageResource(imageResource)
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setupRecording() {

        binding.imageViewSend.animate().scaleX(0f).scaleY(0f).setDuration(100)
            .setInterpolator(LinearInterpolator()).start()

        binding.editTextMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                if (s.toString().isEmpty()) {
                    if (binding.imageViewSend.visibility != View.GONE) {
                        binding.imageViewSend.visibility = View.GONE
                        binding.imageViewSend.animate().scaleX(0f).scaleY(0f).setDuration(100)
                            .setInterpolator(LinearInterpolator()).start()
                    }
                } else {
                    if (binding.imageViewSend.visibility != View.VISIBLE && !isLocked) {
                        binding.imageViewSend.visibility = View.VISIBLE
                        binding.imageViewSend.animate().scaleX(1f).scaleY(1f).setDuration(100)
                            .setInterpolator(LinearInterpolator()).start()
                    }
                }
            }
        })

        binding.imageViewAudio.setOnTouchListener(OnTouchListener { view, motionEvent ->
            if (isDeleting) {
                return@OnTouchListener true
            }

            if (motionEvent.action == MotionEvent.ACTION_DOWN) {

                cancelOffset = (binding.imageViewAudio.x / 2.8).toFloat()
                lockOffset = (binding.imageViewAudio.x / 2.5).toFloat()

                if (firstX == 0f) {
                    firstX = motionEvent.rawX
                }

                if (firstY == 0f) {
                    firstY = motionEvent.rawY
                }

                startRecord()

            } else if (motionEvent.action == MotionEvent.ACTION_UP || motionEvent.action == MotionEvent.ACTION_CANCEL) {

                if (motionEvent.action == MotionEvent.ACTION_UP) {
                    stopRecording(RecordingBehaviour.RELEASED)
                }

            } else if (motionEvent.action == MotionEvent.ACTION_MOVE) {

                if (stopTrackingAction) {
                    return@OnTouchListener true
                }

                var direction = UserBehaviour.NONE

                val motionX = Math.abs(firstX - motionEvent.rawX)
                val motionY = Math.abs(firstY - motionEvent.rawY)

                if (motionX > directionOffset &&
                    motionX > directionOffset &&
                    lastX < firstX && lastY < firstY
                ) {

                    if (motionX > motionY && lastX < firstX) {
                        direction = UserBehaviour.CANCELING

                    } else if (motionY > motionX && lastY < firstY) {
                        direction = UserBehaviour.LOCKING
                    }

                } else if (motionX > motionY && motionX > directionOffset && lastX < firstX) {
                    direction = UserBehaviour.CANCELING
                } else if (motionY > motionX && motionY > directionOffset && lastY < firstY) {
                    direction = UserBehaviour.LOCKING
                }

                if (direction == UserBehaviour.CANCELING) {
                    if (userBehaviour == UserBehaviour.NONE || motionEvent.rawY + binding.imageViewAudio.width / 2 > firstY) {
                        userBehaviour = UserBehaviour.CANCELING
                    }

                    if (userBehaviour == UserBehaviour.CANCELING) {
                        translateX(-(firstX - motionEvent.rawX))
                    }
                } else if (direction == UserBehaviour.LOCKING) {
                    if (userBehaviour == UserBehaviour.NONE || motionEvent.rawX + binding.imageViewAudio.width / 2 > firstX) {
                        userBehaviour = UserBehaviour.LOCKING
                    }

                    if (userBehaviour == UserBehaviour.LOCKING) {
                        translateY(-(firstY - motionEvent.rawY))
                    }
                }

                lastX = motionEvent.rawX
                lastY = motionEvent.rawY
            }
            view.onTouchEvent(motionEvent)
            true
        })

        binding.imageViewStop.setOnClickListener {
            isLocked = false
            stopRecording(RecordingBehaviour.LOCK_DONE)
        }
    }

    private fun translateY(y: Float) {
        if (y < -lockOffset) {
            locked()
            binding.imageViewAudio.translationY = 0f
            return
        }

        if (binding.layoutLock.visibility != View.VISIBLE) {
            binding.layoutLock.visibility = View.VISIBLE
        }

        binding.imageViewAudio.translationY = y
        binding.layoutLock.translationY = y / 2
        binding.imageViewAudio.translationX = 0f
    }

    private fun translateX(x: Float) {
        if (x < -cancelOffset) {
            canceled()
            binding.imageViewAudio.translationX = 0f
            binding.layoutSlideCancel.translationX = 0f
            return
        }

        binding.imageViewAudio.translationX = x
        binding.layoutSlideCancel.translationX = x
        binding.layoutLock.translationY = 0f
        binding.imageViewAudio.translationY = 0f

        if (Math.abs(x) < binding.imageViewMic.width / 2) {
            if (binding.layoutLock.visibility != View.VISIBLE) {
                binding.layoutLock.visibility = View.VISIBLE
            }
        } else {
            if (binding.layoutLock.visibility != View.GONE) {
                binding.layoutLock.visibility = View.GONE
            }
        }
    }

    private fun locked() {
        stopTrackingAction = true
        stopRecording(RecordingBehaviour.LOCKED)
        isLocked = true
        //        Animator animator = ViewAnimationUtils.createCircularReveal()
    }

    private fun canceled() {
        stopTrackingAction = true
        stopRecording(RecordingBehaviour.CANCELED)
    }

    private fun stopRecording(recordingBehaviour: RecordingBehaviour) {

        stopTrackingAction = true
        firstX = 0f
        firstY = 0f
        lastX = 0f
        lastY = 0f

        userBehaviour = UserBehaviour.NONE

        binding.imageViewAudio.animate().scaleX(1f).scaleY(1f).translationX(0f).translationY(0f)
            .setDuration(100).setInterpolator(LinearInterpolator()).start()
        binding.layoutSlideCancel.translationX = 0f
        binding.layoutSlideCancel.visibility = View.GONE

        binding.layoutLock.visibility = View.GONE
        binding.layoutLock.translationY = 0f
        binding.imageViewLockArrow.clearAnimation()
        binding.imageViewLock.clearAnimation()

        if (isLocked) {
            return
        }

        if (recordingBehaviour == RecordingBehaviour.LOCKED) {
            binding.imageViewStop.visibility = View.VISIBLE

            if (recordingListener != null)
                recordingListener!!.onRecordingLocked()

        } else if (recordingBehaviour == RecordingBehaviour.CANCELED) {
            binding.textViewTime.clearAnimation()
            binding.textViewTime.visibility = View.INVISIBLE
            binding.imageViewMic.visibility = View.INVISIBLE
            binding.imageViewStop.visibility = View.GONE

            timerTask!!.cancel()
            delete()

            if (recordingListener != null)
                recordingListener!!.onRecordingCanceled()

        } else if (recordingBehaviour == RecordingBehaviour.RELEASED || recordingBehaviour == RecordingBehaviour.LOCK_DONE) {
            binding.textViewTime.clearAnimation()
            binding.textViewTime.visibility = View.INVISIBLE
            binding.imageViewMic.visibility = View.INVISIBLE
            binding.layoutMessage.visibility = View.VISIBLE
            binding.imageViewAttachment.visibility = View.VISIBLE
            binding.imageViewStop.visibility = View.GONE

            timerTask!!.cancel()

            if (recordingListener != null)
                recordingListener!!.onRecordingCompleted()
        }
    }

    private fun startRecord() {
        if (recordingListener != null)
            recordingListener!!.onRecordingStarted()

        stopTrackingAction = false
        binding.layoutMessage.visibility = View.GONE
        binding.imageViewAttachment.visibility = View.INVISIBLE
        binding.imageViewAudio.animate().scaleXBy(1f).scaleYBy(1f).setDuration(200)
            .setInterpolator(OvershootInterpolator()).start()
        binding.textViewTime.visibility = View.VISIBLE
        binding.layoutLock.visibility = View.VISIBLE
        binding.layoutSlideCancel.visibility = View.VISIBLE
        binding.imageViewMic.visibility = View.VISIBLE
        binding.textViewTime.startAnimation(animBlink)
        binding.imageViewLockArrow.clearAnimation()
        binding.imageViewLock.clearAnimation()
        binding.imageViewLockArrow.startAnimation(animJumpFast)
        binding.imageViewLock.startAnimation(animJump)

        if (audioTimer == null) {
            audioTimer = Timer()
            timeFormatter.timeZone = TimeZone.getTimeZone("UTC")
        }


        timerTask = object : TimerTask() {
            override fun run() {
                handler.post {
                    binding.textViewTime.text =
                        timeFormatter.format(Date((audioTotalTime * 1000).toLong()))
                    audioTotalTime++
                }
            }
        }


        audioTotalTime = 0
        audioTimer!!.schedule(timerTask, 0, 1000)
    }

    private fun delete() {
        binding.imageViewMic.visibility = View.VISIBLE
        binding.imageViewMic.rotation = 0f
        isDeleting = true
        binding.imageViewAudio.isEnabled = false

        handler.postDelayed({
            isDeleting = false
            binding.imageViewAudio.isEnabled = true
            binding.imageViewAttachment.visibility = View.VISIBLE
        }, 1250)

        binding.imageViewMic.animate().translationY(-dp * 150).rotation(180f).scaleXBy(0.6f)
            .scaleYBy(0.6f).setDuration(500).setInterpolator(DecelerateInterpolator())
            .setListener(object : Animator.AnimatorListener {

                override fun onAnimationStart(animation: Animator) {
                    binding.dustin.translationX = -dp * 40
                    binding.dustinCover.translationX = -dp * 40

                    binding.dustinCover.animate().translationX(0f).rotation(-120f).setDuration(350)
                        .setInterpolator(DecelerateInterpolator()).start()

                    binding.dustin.animate().translationX(0f).setDuration(350)
                        .setInterpolator(DecelerateInterpolator())
                        .setListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator) {
                                binding.dustin.visibility = View.VISIBLE
                                binding.dustinCover.visibility = View.VISIBLE
                            }

                            override fun onAnimationEnd(animation: Animator) {

                            }

                            override fun onAnimationCancel(animation: Animator) {

                            }

                            override fun onAnimationRepeat(animation: Animator) {

                            }
                        }).start()
                }

                override fun onAnimationEnd(animation: Animator) {
                    binding.imageViewMic.animate().translationY(0f).scaleX(1f).scaleY(1f)
                        .setDuration(350)
                        .setInterpolator(LinearInterpolator()).setListener(
                            object : Animator.AnimatorListener {
                                override fun onAnimationStart(animation: Animator) {

                                }

                                override fun onAnimationEnd(animation: Animator) {
                                    binding.imageViewMic.visibility = View.INVISIBLE
                                    binding.imageViewMic.rotation = 0f

                                    binding.dustinCover.animate().rotation(0f).setDuration(150)
                                        .setStartDelay(50).start()
                                    binding.dustin.animate().translationX(-dp * 40).setDuration(200)
                                        .setStartDelay(250)
                                        .setInterpolator(DecelerateInterpolator())
                                        .start()
                                    binding.dustinCover.animate().translationX(-dp * 40)
                                        .setDuration(200)
                                        .setStartDelay(250)
                                        .setInterpolator(DecelerateInterpolator())
                                        .setListener(object : Animator.AnimatorListener {
                                            override fun onAnimationStart(animation: Animator) {

                                            }

                                            override fun onAnimationEnd(animation: Animator) {
                                                binding.layoutMessage.visibility = View.VISIBLE
                                            }

                                            override fun onAnimationCancel(animation: Animator) {

                                            }

                                            override fun onAnimationRepeat(animation: Animator) {

                                            }
                                        }).start()
                                }

                                override fun onAnimationCancel(animation: Animator) {

                                }

                                override fun onAnimationRepeat(animation: Animator) {

                                }
                            }
                        ).start()
                }

                override fun onAnimationCancel(animation: Animator) {

                }

                override fun onAnimationRepeat(animation: Animator) {

                }
            }).start()
    }
}
