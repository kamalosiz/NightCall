package com.example.kalam_android.repository.net

import io.reactivex.annotations.NonNull
import io.reactivex.annotations.Nullable
import com.example.kalam_android.repository.net.Status.*

class ApiResponse<T> private constructor(
    val status: Status, @param:Nullable @field:Nullable
    val data: T?, @param:Nullable @field:Nullable
    val error: Throwable?
) {
    companion object {

        fun <T> loading(): ApiResponse<T> {
            return ApiResponse(LOADING, null, null)
        }

        fun <T> loaded(): ApiResponse<T> {
            return ApiResponse(LOADED, null, null)
        }

        fun <T> success(@NonNull data: T?): ApiResponse<T> {
            return ApiResponse(SUCCESS, data, null)
        }

        fun <T> error(@NonNull error: Throwable): ApiResponse<T> {
            return ApiResponse(ERROR, null, error)
        }

    }
}
