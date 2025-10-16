package com.mahdisamavat.location.domain.usecase

import com.mahdisamavat.core.model.Location
import com.mahdisamavat.location.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import com.mahdisamavat.core.common.result.Result


class GetAllLocationsUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    operator fun invoke(): Flow<Result<List<Location>>> {
        return locationRepository.getAllLocationsFlow()
    }
}
