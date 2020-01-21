package com.example.kalam_android.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kalam_android.localdb.LocalRepo
import com.example.kalam_android.localdb.entities.ChatData
import com.example.kalam_android.repository.Repository
import com.example.kalam_android.repository.model.ChatMessagesResponse
import com.example.kalam_android.repository.net.ApiResponse
import com.example.kalam_android.util.Debugger
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.http.Body
import javax.inject.Inject

class ChatMessagesViewModel @Inject constructor(
    private val repository: Repository,
    private val localRepo: LocalRepo
) : ViewModel() {

    private val disposable = CompositeDisposable()
    private val responsiveLiveData = MutableLiveData<ApiResponse<ChatMessagesResponse>>()

    fun allChatResponse(): MutableLiveData<ApiResponse<ChatMessagesResponse>> {
        return responsiveLiveData
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

    fun addMessageToDB(list: ArrayList<ChatData>) {
        disposable.add(
            Completable.fromAction {
                localRepo.insertChatsIntoDB(list)
            }.subscribeOn(Schedulers.io())
                .subscribe({
                    Debugger.i("testingLocal", "All Chat messages inserted")
                }, {
                    Debugger.i("testingLocal", "Exception while Data insertion: ${it.message}")
                })
        )
    }

    fun addMessageToDB(msg: ChatData) {
        disposable.add(
            Completable.fromAction {
                localRepo.insertChatsIntoDB(msg)
            }.subscribeOn(Schedulers.io())
                .subscribe({
                    Debugger.i("testingLocal", "All Chat messages inserted")
                }, {
                    Debugger.i("testingLocal", "Exception while Data insertion: ${it.message}")
                })
        )
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}