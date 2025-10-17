package com.mahdisamavat.location.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahdisamavat.core.common.result.Result
import com.mahdisamavat.core.logger.Logger
import com.mahdisamavat.location.domain.usecase.GetAllLocationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val getAllLocationsUseCase: GetAllLocationsUseCase,
    private val logger: Logger
) : ViewModel() {

    private val _state = MutableStateFlow(LocationState())
    val state: StateFlow<LocationState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<LocationEffect>()
    val effect = _effect.asSharedFlow()

    init {
        loadLocations()
    }

    fun handleIntent(intent: LocationIntent) {
        when (intent) {
            is LocationIntent.LoadLocations -> loadLocations()
            is LocationIntent.StartService -> startService()
            is LocationIntent.StopService -> stopService()
            is LocationIntent.ClearError -> _state.value = _state.value.copy(error = null)
        }
    }

    private fun loadLocations() {
        viewModelScope.launch {
            getAllLocationsUseCase().collect { result ->
                when (result) {
                    is Result.Success -> {
                        _state.value = _state.value.copy(
                            locations = result.data,
                            locationCount = result.data.size,
                            isLoading = false
                        )
                    }

                    is Result.Failure -> {
                        logger.e(
                            "LocationViewModel",
                            "Failed to load locations: ${result.error.message}"
                        )
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.error.message
                        )
                    }

                    else -> {}
                }
            }
        }
    }

    private fun startService() {
        viewModelScope.launch {
            _effect.emit(LocationEffect.ShowMessage("Use Internet App to start service"))
        }
    }

    private fun stopService() {
        viewModelScope.launch {
            _effect.emit(LocationEffect.ShowMessage("Use Internet App to stop service"))
        }
    }
}
