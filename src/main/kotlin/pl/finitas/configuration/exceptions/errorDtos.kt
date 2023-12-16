package pl.finitas.configuration.exceptions

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val errorCode: ErrorCode,
    val errorMessage: String? = null,
)

enum class ErrorCode {
    GENERIC_ERROR,
    AUTH_ERROR,
    ROOM_NOT_FOUND,
}