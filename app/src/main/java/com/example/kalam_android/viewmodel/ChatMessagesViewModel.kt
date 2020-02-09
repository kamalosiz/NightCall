package com.example.kalam_android.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kalam_android.localdb.LocalRepo
import com.example.kalam_android.localdb.entities.ChatData
import com.example.kalam_android.repository.Repository
import com.example.kalam_android.repository.model.ChatDetailResponse
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
    private val responsiveLiveData = MutableLiveData<ApiResponse<ChatDetailResponse>>()

    fun allChatResponse(): MutableLiveData<ApiResponse<ChatDetailResponse>> {
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

    fun updateItemToDB(
        unix_time: String,
        message: String,
        uid: Int,
        unReadcount: Int,
        sender_id: Int?,
        isRead: Int?
    ) {
        disposable.add(
            Completable.fromAction {
                localRepo.updateItemToDB(unix_time, message, uid, unReadcount, sender_id, isRead)
            }.subscribeOn(Schedulers.io())
                .subscribe({
                    Debugger.i("testingLocal", "All Chat Items inserted")
                }, {
                    Debugger.i("testingLocal", "Exception while Data insertion: ${it.message}")
                })
        )
    }

    fun updateChatItemDB(uid: Int, unReadcount: Int, isRead: Int?) {
        disposable.add(
            Completable.fromAction {
                localRepo.updateChatItemDB(uid, unReadcount, isRead)
            }.subscribeOn(Schedulers.io())
                .subscribe({
                    Debugger.i("testingLocal", "All Chat Items inserted")
                }, {
                    Debugger.i("testingLocal", "Exception while Data insertion: ${it.message}")
                })
        )
    }

    //Save Messages to DB

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