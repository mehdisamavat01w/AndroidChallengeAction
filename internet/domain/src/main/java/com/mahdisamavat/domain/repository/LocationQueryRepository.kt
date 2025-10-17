package com.mahdisamavat.domain.repository

import com.mahdisamavat.core.common.result.Result
import com.mahdisamavat.core.model.Location


interface LocationQueryRepository {

    suspend fun getAllLocations(): Result<List<Location>>


    suspend fun getLatestLocation(): Result<Location?>
}
