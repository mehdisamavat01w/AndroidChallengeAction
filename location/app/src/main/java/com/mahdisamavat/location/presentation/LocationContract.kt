package com.mahdisamavat.location.presentation

import com.mahdisamavat.core.model.Location

data class LocationState(
    val locations: List<Location> = emptyList(),
    val locationCount: Int = 0,
    val isServiceRunning: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class LocationIntent {
    object LoadLocations : LocationIntent()
    object StartService : LocationIntent()
    object StopService : LocationIntent()
    object ClearError : LocationIntent()
}

sealed class LocationEffect {
    data class ShowMessage(val message: String) : LocationEffect()
    data class ShowError(val error: String) : LocationEffect()
}
