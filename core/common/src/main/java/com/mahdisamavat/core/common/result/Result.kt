package com.mahdisamavat.core.common.result

import com.mahdisamavat.core.common.error.AppError


/**
 * Sealed class representing operation result states.
 * 
 * Provides type-safe error handling and loading states for
 * Clean Architecture. Used across all layers for consistent
 * error propagation and UI state management.
 * 
 * @param T Success data type
 */
sealed class Result<out T> {

    /**
     * Successful operation with data.
     * @property data Result payload
     */
    data class Success<T>(val data: T) : Result<T>()
    /**
     * Failed operation with error details.
     * @property error Structured error information
     */
    data class Failure(val error: AppError) : Result<Nothing>()

    /**
     * Operation in progress state for UI loading indicators.
     */
    object Loading : Result<Nothing>()

    companion object {

        /**
         * Creates success result with data.
         * @param data Payload to wrap
         * @return Success result
         */
        fun <T> success(data: T): Result<T> = Success(data)


        /**
         * Creates failure result from AppError.
         * @param error Structured error
         * @return Failure result
         */
        fun <T> failure(error: AppError): Result<T> = Failure(error)


        /**
         * Creates failure result from Throwable.
         * Wraps exception in AppError.Unknown.
         * 
         * @param throwable Exception to wrap
         * @return Failure result
         */
        fun <T> failure(throwable: Throwable): Result<T> = Failure(
            AppError.Unknown(throwable.message ?: "Unknown error", throwable, context = "Unknown")
        )

        /**
         * Creates loading result for operation in progress.
         * @return Loading result
         */
        fun <T> loading(): Result<T> = Loading
    }
}

