package com.example.kalam_android.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kalam_android.repository.Repository
import com.example.kalam_android.repository.model.FindFriends
import com.example.kalam_android.repository.model.LoginResponse
import com.example.kalam_android.repository.model.UpdateUserProfile
import com.example.kalam_android.repository.model.UserProfile
import com.example.kalam_android.repository.net.ApiResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Part
import javax.inject.Inject

class EditUserProfileViewModel @Inject constructor(private val repository: Repository): ViewModel() {
    private val disposables = CompositeDisposable()
    private val responseLiveData = MutableLiveData<ApiResponse<UpdateUserProfile>>()

    fun userProfileResponse(): MutableLiveData<ApiResponse<UpdateUserProfile>> {
        return responseLiveData
    }

    fun hitUpdateUserProfile(auth:String,parameters: Map<String, String>) {
        disposables.add(repository.updateProfile(auth,parameters)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { responseLiveData.setValue(ApiResponse.loading()) }
            .subscribe(
                { responseLiveData.setValue(ApiResponse.success(it)) },
                { responseLiveData.setValue(ApiResponse.error(it)) }
            ))
    }
    fun hitUpdateUserProfile(auth:String,parameters: Map<String,@JvmSuppressWildcards RequestBody>,image : MultipartBody.Part) {
        disposables.add(repository.updateProfile(auth,parameters,image)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { responseLiveData.setValue(ApiResponse.loading()) }
                .subscribe(
                        { responseLiveData.setValue(ApiResponse.success(it)) },
                        { responseLiveData.setValue(ApiResponse.error(it)) }
                ))
    }
    fun hitUpdateUserProfile(auth:String,parameters: Map<String,@JvmSuppressWildcards RequestBody>,profileImage : MultipartBody.Part,wallImage : MultipartBody.Part) {
        disposables.add(repository.updateProfile(auth,parameters,profileImage,wallImage)
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