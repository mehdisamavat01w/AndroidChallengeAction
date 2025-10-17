package com.mahdisamavat.core.common.error



sealed class AppError(
    open val message: String,
    open val cause: Throwable?,
    open val recoverable: Boolean,
    open val context: String
) {


    data class Security(
        override val message: String,
        override val cause: Throwable?,
        override val recoverable: Boolean,
        override val context: String
    ) : AppError(message, cause, recoverable, context)


    data class Data(
        override val message: String,
        override val cause: Throwable?,
        override val recoverable: Boolean = true,
        override val context: String
    ) : AppError(message, cause, recoverable, context)


    data class System(
        override val message: String,
        override val cause: Throwable?,
        override val recoverable: Boolean,
        val fatal: Boolean = false,
        override val context: String
    ) : AppError(message, cause, recoverable, context)


    data class Location(
        override val message: String,
        override val cause: Throwable?,
        override val recoverable: Boolean = true,
        override val context: String
    ) : AppError(message, cause, recoverable, context)

    data class IPC(
        override val message: String,
        override val cause: Throwable?,
        override val recoverable: Boolean,
        override val context: String
    ) : AppError(message, cause, recoverable, context)

    data class Unknown(
        override val message: String,
        override val cause: Throwable?,
        override val recoverable: Boolean = false,
        override val context: String
    ) : AppError(message, cause, recoverable, context)
}