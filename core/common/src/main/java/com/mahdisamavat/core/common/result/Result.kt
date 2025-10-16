package com.mahdisamavat.core.common.result

import com.mahdisamavat.core.common.error.AppError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart


sealed class Result<out T> {

    data class Success<T>(val data: T) : Result<T>()
    data class Failure(val error: AppError) : Result<Nothing>()

    object Loading : Result<Nothing>()

    companion object {

        fun <T> success(data: T): Result<T> = Success(data)


        fun <T> failure(error: AppError): Result<T> = Failure(error)


        fun <T> failure(throwable: Throwable): Result<T> = Failure(
            AppError.Unknown(throwable.message ?: "Unknown error", throwable, context = "Unknown")
        )

        fun <T> loading(): Result<T> = Loading
    }
}

