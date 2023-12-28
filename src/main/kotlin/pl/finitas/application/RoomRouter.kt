package pl.finitas.application

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import pl.finitas.configuration.exceptions.BadRequestException
import pl.finitas.configuration.exceptions.ErrorCode
import pl.finitas.configuration.serialization.SerializableUUID
import pl.finitas.data.datasource.*
import pl.finitas.data.model.Authority
import pl.finitas.data.model.Room
import pl.finitas.data.model.RoomMember
import pl.finitas.data.model.RoomRole
import pl.finitas.domain.RoomVersionDto
import java.util.*

fun Route.roomRouter() {
    route("/rooms") {
        post {
            val room: CreateRoomRequest = call.receive()
            createRoom(room.toModel())
                .let { call.respond(HttpStatusCode.Created, it) }
        }
        post("/sync") {
            val request: GetChangedRoomsDto = call.receive()
            val (roomVersions, unavailableRooms) = request.roomVersions.partition { (idRoom) ->
                hasAnyAuthority(request.idUser, idRoom)
            }
            getChangedRooms(request.idUser, roomVersions)
                .let { rooms ->
                    call.respond(
                        HttpStatusCode.OK,
                        SyncRoomResponse(
                            rooms,
                            unavailableRooms.map { it.idRoom },
                        )
                    )
                }
        }
        usersRouter()
        rolesRouter()
    }
}

fun Route.usersRouter() {
    route("/users") {
        get {
            val idRoom = call.parameters["idRoom"]?.let { UUID.fromString(it) }
            val idUser  = call.parameters["idUser"]?.let { UUID.fromString(it) } ?: throw UserNotProvidedException()
            getReachableUsersForUser(idUser, idRoom)
                .let { call.respond(HttpStatusCode.OK, it) }
        }
        post {
            val request: JoinRoomWithInvitationRequest = call.receive()
            addUserToRoomWithInvitationLink(request.idInvitationLink, request.idUser)
                .let { call.respond(HttpStatusCode.OK, it) }
        }
        delete {
            hasAllAuthority(Authority.MODIFY_ROOM)
            val request: DeleteUserRequest = call.receive()
            deleteUserFromRoom(request.idUser, getIdRoomContext())
                .let { call.respond(HttpStatusCode.OK, it) }
        }
        put("/roles") {
            hasAllAuthority(Authority.MODIFY_ROOM)
            val request: AssignRoleToUserRequest = call.receive()
            assignRoleToUser(request.toDto(getIdRoomContext()))
                .let { call.respond(HttpStatusCode.OK, it) }
        }
    }
}

fun Route.rolesRouter() {
    route("/roles") {
        post {
            hasAllAuthority(Authority.MODIFY_ROOM)

            val request: AddRoleRequest = call.receive()
            addRole(request.toDto(idRoom = getIdRoomContext()))
                .let { call.respond(HttpStatusCode.Created, it) }
        }
        put {
            hasAllAuthority(Authority.MODIFY_ROOM)

            val request: UpdateRoleRequest = call.receive()
            updateRole(request.toDto(idRoom = getIdRoomContext()))
                .let { call.respond(HttpStatusCode.OK, it) }
        }
        delete {
            hasAllAuthority(Authority.MODIFY_ROOM)

            val request: DeleteRoleRequest = call.receive()
            deleteRole(request.toDto(idRoom = getIdRoomContext()))
                .let { call.respond(HttpStatusCode.OK, it) }
        }
    }
}

@Serializable
data class CreateRoomRequest(
    val creator: SerializableUUID,
    val roomName: String,
) {
    fun toModel() = Room(
        idRoom = UUID.randomUUID(),
        name = roomName,
        idInvitationLink = UUID.randomUUID(),
        version = 0,
        roles = listOf(RoomRole.Owner),
        members = listOf(RoomMember(creator, RoomRole.Owner))
    )
}

@Serializable
data class SyncRoomResponse(
    val rooms: List<Room>,
    val unavailableRooms: List<SerializableUUID>,
)

@Serializable
data class JoinRoomWithInvitationRequest(
    val idUser: SerializableUUID,
    val idInvitationLink: SerializableUUID,
)

@Serializable
data class GetChangedRoomsDto(
    val idUser: SerializableUUID,
    val roomVersions: List<RoomVersionDto>,
)

@Serializable
data class AssignRoleToUserRequest(
    val idRole: SerializableUUID?,
    val idUser: SerializableUUID,
) {
    fun toDto(idRoom: UUID) = AssignRoleToUserDto(idRoom, idRole, idUser)
}

class UserNotProvidedException: BadRequestException("idUser not provided", ErrorCode.ID_USER_NOT_PROVIDED)

@Serializable
data class AddRoleRequest(
    val name: String,
    val authorities: Set<Authority>,
) {
    fun toDto(idRoom: UUID) = AddRoleDto(idRoom, name, authorities)
}

@Serializable
data class AddRoleResponse(
    val roomRole: RoomRole,
    val usersToNotify: List<SerializableUUID>,
)

@Serializable
data class UpdateRoleRequest(
    val idRole: SerializableUUID,
    val name: String,
    val authorities: Set<Authority>,
) {
    fun toDto(idRoom: UUID) = UpdateRoleDto(idRoom, idRole, name, authorities)
}

@Serializable
data class UpdateRoleResponse(
    val roomRole: RoomRole,
    val usersToNotify: List<SerializableUUID>,
)

@Serializable
data class DeleteRoleRequest(
    val idRole: SerializableUUID,
) {
    fun toDto(idRoom: UUID) = DeleteRoleDto(idRoom, idRole)
}

@Serializable
data class DeleteUserRequest(
    val idUser: SerializableUUID,
)

@Serializable
data class UsersToNotifyResponse(
    val usersToNotify: List<SerializableUUID>,
)
