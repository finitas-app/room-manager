package pl.finitas.data.datasource

import kotlinx.serialization.Serializable
import org.litote.kmongo.combine
import org.litote.kmongo.elemMatch
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import pl.finitas.application.UsersToNotifyResponse
import pl.finitas.configuration.exceptions.BadRequestException
import pl.finitas.configuration.exceptions.ErrorCode
import pl.finitas.configuration.exceptions.NotFoundException
import pl.finitas.configuration.serialization.SerializableUUID
import pl.finitas.data.database.mongoClient
import pl.finitas.data.database.roomCollection
import pl.finitas.data.model.Authority
import pl.finitas.data.model.Room
import pl.finitas.data.model.RoomMember
import java.util.*

suspend fun addUserToRoomWithInvitationLink(idInvitationLink: UUID, idUser: UUID): UsersToNotifyResponse {
    mongoClient.startSession().use { clientSession ->
        clientSession.startTransaction()
        val room = roomCollection
            .findOne(Room::idInvitationLink eq idInvitationLink)
            ?: throw NotFoundException(
                "Room with invitation link '$idInvitationLink' not found.",
                ErrorCode.ROOM_NOT_FOUND,
            )
        if (room.members.find { it.idUser == idUser } != null) throw UserAlreadyExist(idUser)
        val newMembers = room.members + RoomMember(idUser)
        roomCollection.updateOne(
            Room::idInvitationLink eq idInvitationLink,
            combine(
                setValue(Room::members, newMembers),
                setValue(Room::version, room.version + 1),
            )
        )
        clientSession.commitTransaction()
        return UsersToNotifyResponse(newMembers.map { it.idUser })
    }
}

suspend fun deleteUserFromRoom(idUser: UUID, idRoom: UUID): UsersToNotifyResponse {
    mongoClient.startSession().use { clientSession ->
        clientSession.startTransaction()
        val room = getRoomBy(idRoom)
        roomCollection.updateOne(
            Room::idRoom eq idRoom,
            combine(
                setValue(Room::members, room.members.filter { it.idUser != idUser }),
                setValue(Room::version, room.version + 1),
            )
        )
        clientSession.commitTransaction()
        return UsersToNotifyResponse(room.members.map { it.idUser })
    }
}

suspend fun assignRoleToUser(assignRoleToUserDto: AssignRoleToUserDto): UsersToNotifyResponse {
    val (idRoom, idRole, idUser) = assignRoleToUserDto
    mongoClient.startSession().use { clientSession ->
        clientSession.startTransaction()
        val room = getRoomBy(idRoom)
        idRole?.let {
            room.roles.find { role -> role.idRole == it } ?: throw RoleNotFoundException(it)
        }
        roomCollection.updateOne(
            Room::idRoom eq idRoom,
            combine(
                setValue(
                    Room::members,
                    room.members.map { user ->
                        if (user.idUser == idUser) user.copy(idRole = idRole)
                        else user
                    }
                ),
                setValue(Room::version, room.version + 1),
            )
        )
        clientSession.commitTransaction()
        return UsersToNotifyResponse(room.members.map { it.idUser })
    }
}

suspend fun getUsersUnderAuthority(idUser: UUID, authority: Authority):UsersUnderAuthorityResponse  {
    return roomCollection.find(
        Room::members elemMatch (RoomMember::idUser eq idUser)
    )
        .toList()
        .filter { room ->
            val rolesWithAuthority = room.roles.filter { authority in it.authorities }.map { it.idRole }
            room.members.find { it.idUser == idUser }?.idRole in rolesWithAuthority
        }
        .flatMap { room -> room.members.map { it.idUser } }
        .let { UsersUnderAuthorityResponse((it + idUser).toSet()) }
}

@Serializable
data class AssignRoleToUserDto(
    val idRoom: SerializableUUID,
    val idRole: SerializableUUID?,
    val idUser: SerializableUUID,
)

class RoleNotFoundException(idRole: UUID) :
    NotFoundException("Role with id '$idRole' not found!", ErrorCode.ROLE_NOT_FOUND)

class UserAlreadyExist(idUser: UUID) : BadRequestException(
    "User with id '$idUser' already exist",
    ErrorCode.USER_ALREADY_JOINED_ROOM,
)

@Serializable
data class UsersUnderAuthorityResponse(
    val users: Set<SerializableUUID>,
)
