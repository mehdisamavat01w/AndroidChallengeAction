package com.mahdisamavat.core.ipc.serializer

import com.mahdisamavat.core.common.error.AppError
import com.mahdisamavat.core.common.result.Result
import com.mahdisamavat.core.ipc.model.Command
import com.mahdisamavat.core.ipc.model.Response
import com.mahdisamavat.core.logger.Logger
import kotlinx.serialization.json.Json


class MessageSerializer(
    private val logger: Logger
) {

    companion object {
        private const val TAG = "MessageSerializer"
    }

    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
        encodeDefaults = true
    }


    fun serializeCommand(command: Command): Result<String> {
        return try {
            logger.d(TAG, "Serializing command: ${command.getName()}")
            val jsonString = json.encodeToString(command)
            logger.d(TAG, "Command serialized successfully (${jsonString.length} bytes)")
            Result.success(jsonString)
        } catch (e: Exception) {
            logger.e(TAG, "Failed to serialize command", e)
            Result.failure(
                AppError.IPC(
                    "Failed to serialize command: ${e.message}",
                    e,
                    true,
                    "MessageSerializer"
                )
            )
        }
    }


    fun deserializeCommand(jsonString: String): Result<Command> {
        return try {
            logger.d(TAG, "Deserializing command (${jsonString.length} bytes)")
            val command = json.decodeFromString<Command>(jsonString)
            logger.d(TAG, "Command deserialized successfully: ${command.getName()}")
            Result.success(command)
        } catch (e: Exception) {
            logger.e(TAG, "Failed to deserialize command", e)
            Result.failure(
                AppError.IPC(
                    "Failed to deserialize command: ${e.message}",
                    e,
                    true,
                    "MessageSerializer"
                )
            )
        }
    }


    fun serializeResponse(response: Response): Result<String> {
        return try {
            logger.d(TAG, "Serializing response: ${response.getTypeName()}")
            val jsonString = json.encodeToString(response)
            logger.d(TAG, "Response serialized successfully (${jsonString.length} bytes)")
            Result.success(jsonString)
        } catch (e: Exception) {
            logger.e(TAG, "Failed to serialize response", e)
            Result.failure(
                AppError.IPC(
                    "Failed to serialize response: ${e.message}",
                    e,
                    true,
                    "MessageSerializer"
                )
            )
        }
    }


    fun deserializeResponse(jsonString: String): Result<Response> {
        return try {
            logger.d(TAG, "Deserializing response (${jsonString.length} bytes)")
            val response = json.decodeFromString<Response>(jsonString)
            logger.d(TAG, "Response deserialized successfully: ${response.getTypeName()}")
            Result.success(response)
        } catch (e: Exception) {
            logger.e(TAG, "Failed to deserialize response", e)
            Result.failure(
                AppError.IPC(
                    "Failed to deserialize response: ${e.message}",
                    e,
                    true,
                    "MessageSerializer"
                )
            )
        }
    }


    fun prepareCommandForTransmission(command: Command): Result<String> {
        logger.i(TAG, "Preparing command for transmission: ${command.getName()}")
        return serializeCommand(command)
    }


    fun parseCommandFromTransmission(data: String): Result<Command> {
        logger.i(TAG, "Parsing command from transmission")
        return deserializeCommand(data)
    }


    fun prepareResponseForTransmission(response: Response): Result<String> {
        logger.i(TAG, "Preparing response for transmission: ${response.getTypeName()}")
        return serializeResponse(response)
    }


    fun parseResponseFromTransmission(data: String): Result<Response> {
        logger.i(TAG, "Parsing response from transmission")
        return deserializeResponse(data)
    }
}
