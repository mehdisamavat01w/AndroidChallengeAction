package com.mahdisamavat.domain.usecase

import com.mahdisamavat.core.common.result.Result
import com.mahdisamavat.core.ipc.model.Response
import com.mahdisamavat.domain.repository.CommandRepository


class StartLocationCollectionUseCase(
    private val commandRepository: CommandRepository
) {
    suspend operator fun invoke(): Result<Response> {
        return commandRepository.sendCommand("START_COLLECTION")
    }
}
