package com.example.kalam_android.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kalam_android.localdb.ContactsEntityClass
import com.example.kalam_android.repository.LocalRepo
import com.example.kalam_android.repository.Repository
import com.example.kalam_android.repository.model.Contacts
import com.example.kalam_android.repository.net.ApiResponse
import com.example.kalam_android.util.Debugger
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ContactsViewModel @Inject constructor(
    private val repository: Repository,
    private val localRepo: LocalRepo
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val responseLiveData = MutableLiveData<ApiResponse<Contacts>>()
    private val responsiveRoomData = MutableLiveData<ApiResponse<List<ContactsEntityClass>>>()

    fun contactsResponse(): MutableLiveData<ApiResponse<Contacts>> {
        return responseLiveData
    }

    fun contactsFromRoomResponse(): MutableLiveData<ApiResponse<List<ContactsEntityClass>>> {
        return responsiveRoomData
    }

    fun getContacts(parameters: Map<String, String>) {
        disposables.add(repository.getContacts(parameters)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { responseLiveData.setValue(ApiResponse.loading()) }
            .subscribe(
                { responseLiveData.setValue(ApiResponse.success(it)) },
                { responseLiveData.setValue(ApiResponse.error(it)) }
            ))
    }

    fun addContactsToLocal(list: ArrayList<ContactsEntityClass>) {
        disposables.add(
            Completable.fromAction {
                localRepo.insertIntoDB(list)
            }.subscribeOn(Schedulers.io())
                .subscribe({
                    Debugger.i("testingLocal", "Data inserted")
                }, {
                    Debugger.i("testingLocal", "Exception while Data insertion: ${it.message}")
                })
        )
    }

    fun getContactsFromLocal() {
        disposables.add(
            localRepo.getContactsFromLocal()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    responsiveRoomData.value = ApiResponse.success(it)
                }, {
                    responsiveRoomData.value = ApiResponse.error(it)
                })
        )
    }

    fun deleteAllLocalContacts() {
        disposables.add(
            Completable.fromAction {
                localRepo.removeContacts()
            }.subscribeOn(Schedulers.io())
                .subscribe({
                    Debugger.i("testingLocal", "All Items Deleted")
                }, {
                    Debugger.i("testingLocal", "Exception while Data insertion: ${it.message}")
                })
        )
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}