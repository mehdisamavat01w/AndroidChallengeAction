package com.mahdisamavat.core.ipc.model

import kotlinx.serialization.Serializable


@Serializable
sealed class Command {

    @Serializable
    data object StartService : Command()


    @Serializable
    data object StopService : Command()


    @Serializable
    data object GetAllLocations : Command()


    @Serializable
    data object GetLatestLocation : Command()

    fun getName(): String = when (this) {
        is StartService -> "START_SERVICE"
        is StopService -> "STOP_SERVICE"
        is GetAllLocations -> "GET_ALL_LOCATIONS"
        is GetLatestLocation -> "GET_LATEST_LOCATION"
    }
}
