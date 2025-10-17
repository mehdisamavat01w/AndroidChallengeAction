package com.mahdisamavat.domain.usecase

import com.mahdisamavat.core.common.result.Result
import com.mahdisamavat.core.model.Location
import com.mahdisamavat.domain.repository.LocationQueryRepository


class GetAllLocationsUseCase(
    private val locationQueryRepository: LocationQueryRepository
) {
    suspend operator fun invoke(): Result<List<Location>> {
        return locationQueryRepository.getAllLocations()
    }
}
