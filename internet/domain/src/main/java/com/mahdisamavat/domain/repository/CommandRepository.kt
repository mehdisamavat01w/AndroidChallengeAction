package com.mahdisamavat.domain.repository

import com.mahdisamavat.core.common.result.Result
import com.mahdisamavat.core.ipc.model.Response


interface CommandRepository {

    suspend fun sendCommand(
        commandType: String,
        data: Map<String, String>? = null
    ): Result<Response>
}
