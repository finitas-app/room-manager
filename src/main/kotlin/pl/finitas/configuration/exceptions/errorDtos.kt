package pl.finitas.configuration.exceptions

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val errorCode: ErrorCode,
    val errorMessage: String? = null,
)

enum class ErrorCode {
    GENERIC_ERROR,
    ROOM_NOT_FOUND,
    ROLE_NOT_FOUND,
    SYNCHRONIZATION_ERROR,
    ID_USER_NOT_PROVIDED,
    WRONG_AUTHORITY,
}