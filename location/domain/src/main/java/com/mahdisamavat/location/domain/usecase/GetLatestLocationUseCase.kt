package com.mahdisamavat.location.domain.usecase

import com.mahdisamavat.location.domain.repository.LocationRepository
import com.mahdisamavat.core.model.Location
import javax.inject.Inject
import com.mahdisamavat.core.common.result.Result


class GetLatestLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    suspend operator fun invoke(): Result<Location?> {
        return locationRepository.getLatestLocation()
    }
}
