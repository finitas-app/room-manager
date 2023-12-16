package pl.finitas.configuration.exceptions

import io.ktor.http.*

open class BaseException(
    message: String,
    val errorCode: ErrorCode,
    cause: Exception? = null,
    val statusCode: HttpStatusCode = HttpStatusCode.InternalServerError,
) : Exception(message, cause)

class NotFoundException(
    message: String = "Not found",
    errorCode: ErrorCode,
    cause: Exception? = null,
) : BaseException(message, errorCode, cause, HttpStatusCode.NotFound)

class BadRequestException(
    message: String = "Bad request",
    errorCode: ErrorCode,
    cause: Exception? = null,
) : BaseException(message, errorCode, cause, HttpStatusCode.BadRequest)

class UnauthorizedException(
    message: String = "Unauthorized",
    errorCode: ErrorCode = ErrorCode.AUTH_ERROR,
    cause: Exception? = null,
) : BaseException(message, errorCode, cause, HttpStatusCode.Unauthorized)

class ConflictException(
    errorCode: ErrorCode,
    message: String = "Conflict",
    cause: Exception? = null,
) : BaseException(message, errorCode, cause, HttpStatusCode.Conflict)

class InternalServerException(
    message: String = "Internal Server Error",
    cause: Exception? = null,
    errorCode: ErrorCode = ErrorCode.GENERIC_ERROR,
) : BaseException(message, errorCode, cause)

class ExternalErrorException(
    errorResponse: ErrorResponse,
    statusCode: HttpStatusCode,
) : BaseException(
    message = errorResponse.errorMessage ?: errorResponse.errorCode.name,
    errorCode = errorResponse.errorCode,
    statusCode = statusCode,
)
