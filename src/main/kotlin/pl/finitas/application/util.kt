package pl.finitas.application

import io.ktor.server.application.*
import io.ktor.util.pipeline.*
import pl.finitas.configuration.exceptions.ForbiddenException
import pl.finitas.data.datasource.hasAllAuthority
import pl.finitas.data.datasource.hasAnyAuthority
import pl.finitas.data.model.Authority
import java.util.*


suspend fun PipelineContext<Unit, ApplicationCall>.hasAnyAuthority(vararg authorities: Authority) {
    val (idUser, idRoom) = getDataForAuthorization()
    if (!hasAnyAuthority(idUser, idRoom, authorities.toSet()))
        userDoesNotHaveAnyAuthority(authorities.toSet())
}

suspend fun PipelineContext<Unit, ApplicationCall>.hasAnyAuthority(authorities: Set<Authority>) {
    val (idUser, idRoom) = getDataForAuthorization()
    if (!hasAnyAuthority(idUser, idRoom, authorities))
        userDoesNotHaveAnyAuthority(authorities)
}

suspend fun PipelineContext<Unit, ApplicationCall>.hasAllAuthority(vararg authorities: Authority) {
    val (idUser, idRoom) = getDataForAuthorization()
    if (!hasAllAuthority(idUser, idRoom, authorities.toSet()))
        userDoesNotHaveAllAuthorities(authorities.toSet())
}

suspend fun PipelineContext<Unit, ApplicationCall>.hasAllAuthority(authorities: Set<Authority>) {
    val (idUser, idRoom) = getDataForAuthorization()
    if (!hasAllAuthority(idUser, idRoom, authorities))
        userDoesNotHaveAllAuthorities(authorities)
}

fun PipelineContext<Unit, ApplicationCall>.getRequester() =
    call.request.queryParameters["requester"]?.let(UUID::fromString) ?: idUserNotProvided()

fun PipelineContext<Unit, ApplicationCall>.getIdRoomContext() =
    call.request.queryParameters["idRoomContext"]?.let(UUID::fromString) ?: idRoomNotProvided()

private fun PipelineContext<Unit, ApplicationCall>.getDataForAuthorization() =
    getRequester() to getIdRoomContext()

fun idUserNotProvided(): Nothing = throw ForbiddenException("Id user not provided for authorization")
fun idRoomNotProvided(): Nothing = throw ForbiddenException("Id room not provided for authorization")


private fun userDoesNotHaveAnyAuthority(authorities: Set<Authority>): Nothing =
    throw ForbiddenException("The user does not have any authority: $authorities")

private fun userDoesNotHaveAllAuthorities(authorities: Set<Authority>): Nothing =
    throw ForbiddenException("The user does not have all authority: $authorities")