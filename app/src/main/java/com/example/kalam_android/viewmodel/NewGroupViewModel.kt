package com.example.kalam_android.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kalam_android.localdb.LocalRepo
import com.example.kalam_android.repository.Repository
import com.example.kalam_android.repository.model.BasicResponse
import com.example.kalam_android.repository.net.ApiResponse
import com.example.kalam_android.util.Debugger
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Part
import retrofit2.http.PartMap
import javax.inject.Inject

class NewGroupViewModel @Inject constructor(
    val repository: Repository
) : ViewModel() {

    private val disposables = CompositeDisposable()
    private val responseLiveData = MutableLiveData<ApiResponse<BasicResponse>>()

    fun createGroupResponse(): MutableLiveData<ApiResponse<BasicResponse>> {
        return responseLiveData
    }

    fun hitCreateGroup(authorization: String?, parameters: Map<String, String>) {
        disposables.add(repository.createNewGroup(authorization, parameters)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { responseLiveData.setValue(ApiResponse.loading()) }
            .subscribe(
                { responseLiveData.setValue(ApiResponse.success(it)) },
                { responseLiveData.setValue(ApiResponse.error(it)) }
            ))
    }

    fun hitCreateGroup(authorization: String?, @PartMap params: Map<String, @JvmSuppressWildcards RequestBody>, @Part profilePic: MultipartBody.Part) {
        disposables.add(repository.createNewGroup(authorization, params, profilePic)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { responseLiveData.setValue(ApiResponse.loading()) }
            .subscribe(
                { responseLiveData.setValue(ApiResponse.success(it)) },
                { responseLiveData.setValue(ApiResponse.error(it)) }
            ))
    }


    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}