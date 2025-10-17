package com.mahdisamavat.internet.presentation.main

import com.mahdisamavat.core.ipc.model.Response
import com.mahdisamavat.core.model.Location


object MainContract {


    data class State(
        val isLoading: Boolean = false,
        val locations: List<Location> = emptyList(),
        val latestLocation: Location? = null,
        val lastResponse: Response? = null,
        val error: String? = null,
        val serviceRunning: Boolean? = null
    )


    sealed class Intent {

        data object StartService : Intent()


        data object StopService : Intent()


        data object GetAllLocations : Intent()


        data object GetLatestLocation : Intent()

        data object ClearError : Intent()
    }


    sealed class Effect {
        data class ShowToast(val message: String) : Effect()
        data class ShowError(val message: String) : Effect()
    }
}
