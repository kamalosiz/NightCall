package com.example.kalam_android.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kalam_android.repository.Repository
import com.example.kalam_android.repository.model.AudioResponse
import com.example.kalam_android.repository.model.ChatMessagesResponse
import com.example.kalam_android.repository.net.ApiResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Part
import javax.inject.Inject

class ChatMessagesViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    private val disposable = CompositeDisposable()
    private val responsiveLiveData = MutableLiveData<ApiResponse<ChatMessagesResponse>>()
    private val audioLiveData = MutableLiveData<ApiResponse<AudioResponse>>()

    fun allChatResponse(): MutableLiveData<ApiResponse<ChatMessagesResponse>> {
        return responsiveLiveData
    }

    fun audioResponse(): MutableLiveData<ApiResponse<AudioResponse>> {
        return audioLiveData
    }

    fun hitAllChatApi(authorization: String?, @Body parameters: Map<String, String>) {
        disposable.add(repository.getAllMessages(authorization, parameters)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { responsiveLiveData.value = ApiResponse.loading() }
            .subscribe({
                responsiveLiveData.value = ApiResponse.success(it)
            }, {
                responsiveLiveData.value = ApiResponse.error(it)
            })
        )
    }

    fun hitUploadAudioApi(authorization: String?, @Part audio: MultipartBody.Part) {
        disposable.add(repository.uploadAudio(authorization, audio)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { audioLiveData.value = ApiResponse.loading() }
            .subscribe({
                audioLiveData.value = ApiResponse.success(it)
            }, {
                audioLiveData.value = ApiResponse.error(it)
            })
        )
    }
}