package com.example.kalam_android.services

import android.content.Context
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.repository.Repository
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.wrapper.SocketIO
import com.github.nkzawa.socketio.client.Ack
import com.google.gson.JsonObject
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File
import javax.inject.Inject

class RxMediaWorker(
    private val ctx: Context,
    params: WorkerParameters

) : RxWorker(ctx, params) {

    @Inject
    lateinit var repository: Repository
    var isSocketConnected = false

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
            if (SocketIO.getInstance().socket == null) {
                Debugger.e("WorkManagerMedia", "Socket is not connected")
                SocketIO.getInstance().connectSocket(token)
                isSocketConnected = false
            } else {
                isSocketConnected = true
                Debugger.e("WorkManagerMedia", "Socket is connected")
            }
            it.data?.let { list ->
                emitNewMessage(
                    id.toString(),
                    chatId.toString(),
                    list[0].type.toString(),
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

    private fun emitNewMessage(
        id: String,
        chatID: String,
        message: String,
        type: String,
        senderName: String,
        fileID: String,
        duration: Long,
        thumbnail: String,
        identifier: String,
        language: String,
        groupID: String,
        profileImage: String
    ) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("user_id", id)
        jsonObject.addProperty("chat_id", chatID)
        jsonObject.addProperty("message", message)
        jsonObject.addProperty("mType", type)
        jsonObject.addProperty("sender_name", senderName)
        jsonObject.addProperty("file_id", fileID)
        jsonObject.addProperty("duration", duration)
        jsonObject.addProperty("thumbnail", thumbnail)
        jsonObject.addProperty("identifier", identifier)
        jsonObject.addProperty("language", language)
        jsonObject.addProperty("group_id", groupID)
        jsonObject.addProperty("profile_image", profileImage)
        jsonObject.addProperty("is_group", 0)
        SocketIO.getInstance().socket?.emit(AppConstants.SEND_MESSAGE, jsonObject, Ack {
            val json = it[0] as JSONObject
            if (isSocketConnected)
                SocketIO.getInstance().socketCallback?.socketResponse(
                    json,
                    AppConstants.SEND_MESSAGE
                )
            else SocketIO.getInstance().socket?.disconnect()
        })
    }
}