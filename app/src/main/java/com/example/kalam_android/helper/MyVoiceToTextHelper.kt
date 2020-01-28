package com.example.kalam_android.helper

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.example.kalam_android.callbacks.ResultVoiceToText
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.toast
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener

class MyVoiceToTextHelper(val context: Activity, private val resultVoiceToText: ResultVoiceToText) :
    RecognitionListener {

    private var speech: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null

    /*    fun checkPermissionForVoiceToText() {
            Dexter.withActivity(context)
                .withPermission(Manifest.permission.RECORD_AUDIO)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                        initVoiceToText()
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest?,
                        token: PermissionToken?
                    ) {
                        token?.continuePermissionRequest()
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                        response?.requestedPermission
                    }

                }).onSameThread().check()
        }*/
    @SuppressLint("CheckResult")
    fun checkPermissionForVoiceToText() {
        Dexter.withActivity(context).withPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    initVoiceToText()
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

    private fun initVoiceToText() {

        speech = SpeechRecognizer.createSpeechRecognizer(context)
        speech?.setRecognitionListener(this)
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        recognizerIntent?.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en")
        recognizerIntent?.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        recognizerIntent?.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
        )
        recognizerIntent?.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)

        recognizerIntent?.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
    }

    override fun onReadyForSpeech(params: Bundle?) {

    }

    override fun onRmsChanged(rmsdB: Float) {
    }

    override fun onBufferReceived(buffer: ByteArray?) {
    }

    override fun onPartialResults(partialResults: Bundle?) {
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
    }

    override fun onBeginningOfSpeech() {
    }

    override fun onEndOfSpeech() {
    }

    override fun onError(error: Int) {
        val errorMessage = getErrorText(error)
        toast(context, errorMessage)
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (matches != null) {
            resultVoiceToText.onResultVoiceToText(matches)
        }
    }

    fun startVoiceToText() {
        speech?.startListening(recognizerIntent)
    }

    fun stopVoiceToText() {
        speech?.stopListening()
    }

    private fun getErrorText(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> "error from server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Didn't understand, please try again."
        }
    }

    fun destroy() {
        if (speech != null) {
            speech?.destroy()
        }
    }

}