package com.mahdisamavat.core.ipc.model

import kotlinx.serialization.Serializable


@Serializable
sealed class Response {


    @Serializable
    data class Success(
        val message: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : Response()


    @Serializable
    data class Error(
        val message: String,
        val errorCode: String? = null,
        val timestamp: Long = System.currentTimeMillis()
    ) : Response()


    @Serializable
    data class ServiceState(
        val isRunning: Boolean,
        val message: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : Response()


    fun getTypeName(): String = when (this) {
        is Success -> "SUCCESS"
        is Error -> "ERROR"
        is ServiceState -> "SERVICE_STATE"
    }
}
