package com.example.kalam_android.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kalam_android.localdb.AllChatEntityClass
import com.example.kalam_android.localdb.LocalRepo
import com.example.kalam_android.repository.Repository
import com.example.kalam_android.repository.model.AllChatListResponse
import com.example.kalam_android.repository.net.ApiResponse
import com.example.kalam_android.util.Debugger
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.http.Body
import javax.inject.Inject

class AllChatListViewModel @Inject constructor(
    private val repository: Repository,
    private val localRepo: LocalRepo
) : ViewModel() {

    private val disposable = CompositeDisposable()
    private val responsiveLiveData = MutableLiveData<ApiResponse<AllChatListResponse>>()
    private val responsiveRoomData = MutableLiveData<ApiResponse<List<AllChatEntityClass>>>()

    fun allChatResponse(): MutableLiveData<ApiResponse<AllChatListResponse>> {
        return responsiveLiveData
    }

    fun allChatLocalResponse(): MutableLiveData<ApiResponse<List<AllChatEntityClass>>> {
        return responsiveRoomData
    }

    fun hitAllChatApi(authorization: String?, @Body parameters: Map<String, String>) {
        disposable.add(repository.getAllChat(authorization, parameters)
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

    fun addAllChatItemsToDB(list: ArrayList<AllChatEntityClass>) {
        disposable.add(
            Completable.fromAction {
                localRepo.insertAllChatListToIntoDB(list)
            }.subscribeOn(Schedulers.io())
                .subscribe({
                    Debugger.i("testingLocal", "All Chat Items inserted")
                }, {
                    Debugger.i("testingLocal", "Exception while Data insertion: ${it.message}")
                })
        )
    }

    fun updateItemToDB(unix_time: String, message: String, uid: Int) {
        disposable.add(
            Completable.fromAction {
                localRepo.updateItemToDB(unix_time, message, uid)
            }.subscribeOn(Schedulers.io())
                .subscribe({
                    Debugger.i("testingLocal", "All Chat Items inserted")
                }, {
                    Debugger.i("testingLocal", "Exception while Data insertion: ${it.message}")
                })
        )
    }

    fun getAllchatItemFromDB() {
        disposable.add(
            localRepo.getAllChatListFromDB()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    responsiveRoomData.value = ApiResponse.success(it)
                }, {
                    responsiveRoomData.value = ApiResponse.error(it)
                })
        )
    }


    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}