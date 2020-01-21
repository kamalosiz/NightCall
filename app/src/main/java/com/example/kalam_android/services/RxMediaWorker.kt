package com.example.kalam_android.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.RxWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.kalam_android.R
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.repository.Repository
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.wrapper.SocketIO
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject

class RxMediaWorker(
    private val ctx: Context,
    params: WorkerParameters

) : RxWorker(ctx, params) {

    @Inject
    lateinit var repository: Repository

    override fun createWork(): Single<Result> {
        MyApplication.getAppComponent(ctx).doInjection(this)

        val identifier = inputData.getString("identifier")
        val file = inputData.getString("file")
        val duration = inputData.getString("duration")
        val type = inputData.getString("type")
        val token = inputData.getString("token")
        val id = inputData.getString("id")
        val chatId = inputData.getString("chatId")
        val name = inputData.getString("name")
        val language = inputData.getString("language")
        val groupId = inputData.getString("group_id")
        val profileImage = inputData.getString("profile_image")

        Debugger.e("WorkManagerMedia", "file: $file")

        val params = HashMap<String, RequestBody>()

        params["identifier"] = RequestBody.create(
            MediaType.parse("text/plain"),
            identifier.toString()
        )
        params["duration"] = RequestBody.create(
            MediaType.parse("text/plain"),
            duration.toString()
        )
        params["type"] =
            RequestBody.create(MediaType.parse("text/plain"), type.toString())
        params["group_id"] =
            RequestBody.create(MediaType.parse("text/plain"), groupId.toString())

        val imageFileBody: MultipartBody.Part?
        val fileToUpload = File(file.toString())
        val requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), fileToUpload)
        imageFileBody =
            MultipartBody.Part.createFormData("file", fileToUpload.name, requestBody)
        return repository.uploadMedia(token, params, imageFileBody).doOnSuccess {
            showNotification("KalamTime", "Media Uploaded Successfully")
            if (SocketIO.getInstance().socket == null) {
                Debugger.e("WorkManagerMedia", "Socket is not connected")
                SocketIO.getInstance().connectSocket(token)
            } else {
                Debugger.e("WorkManagerMedia", "Socket is connected")
            }
            it.data?.let { list ->
                SocketIO.getInstance().emitNewMessage(
                    id.toString(),
                    chatId.toString(),
                    "",
                    list[0].type.toString(),
                    name.toString(),
                    list[0].file_id.toString(),
                    list[0].duration.toLong(),
                    list[0].thumbnail.toString(),
                    list[0].identifier,
                    language.toString(),
                    list[0].group_id.toString(),
                    profileImage.toString()
                )
            }
        }
            .map { Result.success() }
            .onErrorReturn { Result.retry() }
    }

    private fun showNotification(title: String, task: String) {
        val notificationManager: NotificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "KalamTime",
                "KalamTime",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notification: NotificationCompat.Builder =
            NotificationCompat.Builder(applicationContext, "KalamTime")
                .setContentTitle(title)
                .setContentText(task)
                .setColor(Color.parseColor("#179a63"))
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher)
        notificationManager.notify(1, notification.build())
    }
}