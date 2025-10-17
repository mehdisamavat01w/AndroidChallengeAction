package com.mahdisamavat.core.ipc.contract

import android.net.Uri


object IPCContract {


    const val LOCATION_APP_PACKAGE = "com.mahdisamavat.location"
    const val INTERNET_APP_PACKAGE = "com.mahdisamavat.internet"


    const val PERMISSION_IPC = "com.mahdisamavat.PERMISSION_IPC"


    object Provider {
        const val AUTHORITY = "com.mahdisamavat.location.provider"

        val BASE_URI: Uri = Uri.parse("content://$AUTHORITY")
        val LOCATIONS_URI: Uri = Uri.withAppendedPath(BASE_URI, "locations")
        val LATEST_LOCATION_URI: Uri = Uri.withAppendedPath(BASE_URI, "latest")

        const val PATH_LOCATIONS = "locations"
        const val PATH_LATEST = "latest"

        const val CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.$AUTHORITY.location"
        const val CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd.$AUTHORITY.location"

        object Columns {
            const val ID = "id"
            const val LATITUDE = "latitude"
            const val LONGITUDE = "longitude"
            const val ACCURACY = "accuracy"
            const val ALTITUDE = "altitude"
            const val BEARING = "bearing"
            const val SPEED = "speed"
            const val TIMESTAMP = "timestamp"
            const val PROVIDER = "provider"
            const val IS_ENCRYPTED = "is_encrypted"
        }
    }


    object Broadcast {
        const val ACTION_COMMAND = "com.mahdisamavat.location.action.COMMAND"
        const val ACTION_RESPONSE = "com.mahdisamavat.internet.action.RESPONSE"

        const val EXTRA_COMMAND = "extra_command"
        const val EXTRA_COMMAND_TYPE = "extra_command_type"
        const val EXTRA_RESPONSE = "extra_response"
        const val EXTRA_RESPONSE_TYPE = "extra_response_type"
        const val EXTRA_TIMESTAMP = "extra_timestamp"
        const val EXTRA_IS_ENCRYPTED = "extra_is_encrypted"

        const val COMMAND_START_SERVICE = "START_SERVICE"
        const val COMMAND_STOP_SERVICE = "STOP_SERVICE"
        const val COMMAND_GET_ALL_LOCATIONS = "GET_ALL_LOCATIONS"
        const val COMMAND_GET_LATEST_LOCATION = "GET_LATEST_LOCATION"
    }


    object Encryption {
        const val ALGORITHM = "AES/GCM/NoPadding"
        const val KEY_SIZE = 256
        const val IV_SIZE = 12
        const val TAG_SIZE = 128
    }


    object Timeouts {
        const val COMMAND_TIMEOUT_MS = 5000L
        const val QUERY_TIMEOUT_MS = 3000L
        const val RESPONSE_TIMEOUT_MS = 5000L
    }


    object ErrorCodes {
        const val PERMISSION_DENIED = "PERMISSION_DENIED"
        const val SERVICE_NOT_AVAILABLE = "SERVICE_NOT_AVAILABLE"
        const val ENCRYPTION_FAILED = "ENCRYPTION_FAILED"
        const val DECRYPTION_FAILED = "DECRYPTION_FAILED"
        const val INVALID_COMMAND = "INVALID_COMMAND"
        const val TIMEOUT = "TIMEOUT"
        const val UNKNOWN_ERROR = "UNKNOWN_ERROR"
    }
}
