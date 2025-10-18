package com.mahdisamavat.internet.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahdisamavat.core.analytics.AnalyticsHelper
import com.mahdisamavat.core.analytics.PerformanceMonitor
import com.mahdisamavat.core.analytics.logCommandSent
import com.mahdisamavat.core.analytics.logDataQuery
import com.mahdisamavat.core.analytics.logError
import com.mahdisamavat.core.common.result.Result
import com.mahdisamavat.core.ipc.model.Response
import com.mahdisamavat.core.logger.Logger
import com.mahdisamavat.core.model.Location
import com.mahdisamavat.domain.repository.CommandRepository
import com.mahdisamavat.domain.repository.LocationQueryRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class MainViewModel(
    private val commandRepository: CommandRepository,
    private val locationQueryRepository: LocationQueryRepository,
    private val logger: Logger,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    companion object {
        private const val TAG = "MainViewModel"
    }

    private val performanceMonitor = PerformanceMonitor(analyticsHelper)

    private val _state = MutableStateFlow(MainContract.State())
    val state: StateFlow<MainContract.State> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<MainContract.Effect>()
    val effect = _effect.asSharedFlow()


    fun handleIntent(intent: MainContract.Intent) {
        logger.d(TAG, "Handling intent: ${intent::class.simpleName}")

        when (intent) {
            is MainContract.Intent.StartService -> startService()
            is MainContract.Intent.StopService -> stopService()
            is MainContract.Intent.GetAllLocations -> getAllLocations()
            is MainContract.Intent.GetLatestLocation -> getLatestLocation()
            is MainContract.Intent.ClearError -> clearError()
        }
    }


    private fun startService() {
        logger.i(TAG, "Starting location service")
        analyticsHelper.logCommandSent("START_SERVICE")
        
        val startTime = System.currentTimeMillis()

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            when (val result = commandRepository.sendCommand("START_SERVICE")) {
                is Result.Success<Response> -> {
                    val response = result.data
                    val latency = System.currentTimeMillis() - startTime
                    logger.i(TAG, "START_SERVICE command successful: ${response.getTypeName()}")
                    
                    performanceMonitor.checkIPCLatency("START_SERVICE", latency)

                    _state.value = _state.value.copy(
                        isLoading = false,
                        lastResponse = response,
                        serviceRunning = when (response) {
                            is Response.ServiceState -> response.isRunning
                            else -> true
                        }
                    )

                    _effect.emit(MainContract.Effect.ShowToast("Service started successfully"))
                }

                is Result.Failure -> {
                    logger.e(TAG, "START_SERVICE command failed: ${result.error.message}")

                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.error.message
                    )

                    _effect.emit(MainContract.Effect.ShowError("Failed to start service"))
                }

                else -> {
                    _state.value = _state.value.copy(isLoading = false)
                }
            }
        }
    }


    private fun stopService() {
        logger.i(TAG, "Stopping location service")
        analyticsHelper.logCommandSent("STOP_SERVICE")

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            when (val result = commandRepository.sendCommand("STOP_SERVICE")) {
                is Result.Success<Response> -> {
                    val response = result.data
                    logger.i(TAG, "STOP_SERVICE command successful: ${response.getTypeName()}")

                    _state.value = _state.value.copy(
                        isLoading = false,
                        lastResponse = response,
                        serviceRunning = when (response) {
                            is Response.ServiceState -> response.isRunning
                            else -> false
                        }
                    )

                    _effect.emit(MainContract.Effect.ShowToast("Service stopped successfully"))
                }

                is Result.Failure -> {
                    logger.e(TAG, "STOP_SERVICE command failed: ${result.error.message}")

                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.error.message
                    )

                    _effect.emit(MainContract.Effect.ShowError("Failed to stop service"))
                }

                else -> {
                    _state.value = _state.value.copy(isLoading = false)
                }
            }
        }
    }


    private fun getAllLocations() {
        logger.i(TAG, "Getting all locations")
        analyticsHelper.logCommandSent("GET_ALL_LOCATIONS")
        
        val startTime = System.currentTimeMillis()

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            when (val result = locationQueryRepository.getAllLocations()) {
                is Result.Success -> {
                    val data = result.data
                    if (data is List<*>) {
                        val locations = data.filterIsInstance<Location>()
                        val duration = System.currentTimeMillis() - startTime
                        logger.i(TAG, "Retrieved ${locations.size} locations")
                        
                        analyticsHelper.logDataQuery(locations.size)
                        performanceMonitor.checkDatabasePerformance("query_all", duration, locations.size)

                        _state.value = _state.value.copy(
                            isLoading = false,
                            locations = locations,
                            latestLocation = null
                        )

                        _effect.emit(MainContract.Effect.ShowToast("Retrieved ${locations.size} locations"))
                    } else {
                        logger.e(TAG, "Unexpected data type in result")
                        _state.value =
                            _state.value.copy(isLoading = false, error = "Invalid data format")
                    }
                }

                is Result.Failure -> {
                    logger.e(TAG, "Failed to get locations: ${result.error.message}")

                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.error.message
                    )

                    _effect.emit(MainContract.Effect.ShowError("Failed to retrieve locations"))
                }

                else -> {
                    _state.value = _state.value.copy(isLoading = false)
                }
            }
        }
    }


    private fun getLatestLocation() {
        logger.i(TAG, "Getting latest location")
        analyticsHelper.logCommandSent("GET_LATEST_LOCATION")

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            when (val result = locationQueryRepository.getLatestLocation()) {
                is Result.Success -> {
                    val location = result.data as? Location
                    logger.i(TAG, "Retrieved latest location: ${location?.id}")
                    analyticsHelper.logDataQuery(if (location != null) 1 else 0)

                    _state.value = _state.value.copy(
                        isLoading = false,
                        latestLocation = location,
                        locations = emptyList()
                    )

                    if (location != null) {
                        _effect.emit(MainContract.Effect.ShowToast("Latest location retrieved"))
                    } else {
                        _effect.emit(MainContract.Effect.ShowToast("No locations found"))
                    }
                }

                is Result.Failure -> {
                    logger.e(TAG, "Failed to get latest location: ${result.error.message}")

                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.error.message
                    )

                    _effect.emit(MainContract.Effect.ShowError("Failed to retrieve latest location"))
                }

                else -> {
                    _state.value = _state.value.copy(isLoading = false)
                }
            }
        }
    }

    private fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
