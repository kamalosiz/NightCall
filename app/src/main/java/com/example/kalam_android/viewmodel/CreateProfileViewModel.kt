package com.example.kalam_android.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kalam_android.repository.Repository
import com.example.kalam_android.repository.model.CreateProfileResponse
import com.example.kalam_android.repository.net.ApiResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Part
import retrofit2.http.PartMap
import javax.inject.Inject

class CreateProfileViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    private val disposable = CompositeDisposable()
    private val responsiveLiveData = MutableLiveData<ApiResponse<CreateProfileResponse>>()

    fun createProfileResponse(): MutableLiveData<ApiResponse<CreateProfileResponse>> {
        return responsiveLiveData
    }

    fun hitCreateProfileApi(@Body parameters: Map<String, String>) {
        disposable.add(repository.createProfile(parameters)
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

    fun hitCreateProfileApi(@PartMap params: Map<String, @JvmSuppressWildcards RequestBody>, @Part profilePic: MultipartBody.Part) {
        disposable.add(repository.createProfile(params, profilePic)
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

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}