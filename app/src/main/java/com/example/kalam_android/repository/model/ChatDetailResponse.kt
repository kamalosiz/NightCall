package com.example.kalam_android.repository.model

import com.example.kalam_android.localdb.entities.ChatData
import java.io.Serializable

data class ChatDetailResponse(
    val action: String,
    val code: Int,
    val status: Boolean,
    val data: DataObject,
    val message: String
)

data class DataObject(
    val is_first_page: Int,
    val is_last_page: Int,
    val swipe_up: Int,
    val chats: ArrayList<ChatData>?
)

data class MediaResponse(
    val action: String,
    val code: Int,
    val status: Boolean,
    val data: ArrayList<DataResponse>?,
    val message: String
)

data class DataResponse(
    val file_url: String,
    val duration: Double,
    val message: String?,
    val type: String?,
    val identifier: String,
    val thumbnail: String?,
    val file_id: Long,
    val group_id: String?
)

data class MediaList(val file: String, val type: Int) : Serializable

data class AudioModel(
    val aPath: String,
    val aName: String,
    val aAlbum: String,
    val aArtist: String,
    val duration: String,
    val audioLength: String
)

data class Point(var lat: Double, var long: Double)
