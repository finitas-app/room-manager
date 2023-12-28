package pl.finitas.data.datasource

import kotlinx.serialization.Serializable
import org.litote.kmongo.combine
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import pl.finitas.application.AddRoleResponse
import pl.finitas.application.UpdateRoleResponse
import pl.finitas.application.UsersToNotifyResponse
import pl.finitas.configuration.serialization.SerializableUUID
import pl.finitas.data.database.mongoClient
import pl.finitas.data.database.roomCollection
import pl.finitas.data.model.Authority
import pl.finitas.data.model.Room
import pl.finitas.data.model.RoomRole
import java.util.*

suspend fun addRole(addRoleDto: AddRoleDto): AddRoleResponse {
    mongoClient.startSession().use { clientSession ->
        clientSession.startTransaction()
        val room = getRoomBy(addRoleDto.idRoom)
        val roleToSave = addRoleDto.toModel()
        roomCollection.updateOne(
            Room::idRoom eq addRoleDto.idRoom,
            combine(
                setValue(Room::roles, room.roles + roleToSave),
                setValue(Room::version, room.version + 1),
            )
        )
        clientSession.commitTransaction()
        return AddRoleResponse(
            roomRole = roleToSave,
            usersToNotify = room.members.map { it.idUser },
        )
    }
}

suspend fun updateRole(updateRoleDto: UpdateRoleDto): UpdateRoleResponse {
    mongoClient.startSession().use { clientSession ->
        clientSession.startTransaction()
        val room = getRoomBy(updateRoleDto.idRoom)
        val roleToSave = updateRoleDto.toModel()
        roomCollection.updateOne(
            Room::idRoom eq updateRoleDto.idRoom,
            combine(
                setValue(Room::roles, room.roles.filter { it.idRole != updateRoleDto.idRole } + roleToSave),
                setValue(Room::version, room.version + 1),
            )
        )
        clientSession.commitTransaction()
        return UpdateRoleResponse(
            roomRole = roleToSave,
            usersToNotify = room.members.map { it.idUser },
        )
    }
}

suspend fun deleteRole(deleteRoleDto: DeleteRoleDto): UsersToNotifyResponse {
    mongoClient.startSession().use { clientSession ->
        clientSession.startTransaction()
        val room = getRoomBy(deleteRoleDto.idRoom)
        roomCollection.updateOne(
            Room::idRoom eq deleteRoleDto.idRoom,
            combine(
                setValue(Room::roles, room.roles.filter { it.idRole != deleteRoleDto.idRole }),
                setValue(Room::version, room.version + 1),
            )
        )
        clientSession.commitTransaction()
        return UsersToNotifyResponse(
            room.members.map { it.idUser }
        )
    }
}

suspend fun hasAnyAuthority(idUser: UUID, idRoom: UUID, authorities: Set<Authority> = setOf()): Boolean {
    val member = findRoomMember(idUser, idRoom) ?: return false
    val memberAuthorities = member.roomRole?.authorities ?: setOf()
    return !(authorities.isNotEmpty() && !memberAuthorities.any { it in authorities })
}

suspend fun hasAllAuthority(idUser: UUID, idRoom: UUID, authorities: Set<Authority> = setOf()): Boolean {
    val member = findRoomMember(idUser, idRoom) ?: return false
    val memberAuthorities = member.roomRole?.authorities ?: setOf()
    return !(authorities.isNotEmpty() && !memberAuthorities.containsAll(authorities))
}

private suspend fun findRoomMember(idUser: UUID, idRoom: UUID) = findRoomBy(idRoom)
    ?.members
    ?.find { it.idUser == idUser }

@Serializable
data class AddRoleDto(
    val idRoom: SerializableUUID,
    val name: String,
    val authorities: Set<Authority>,
) {
    fun toModel() = RoomRole(UUID.randomUUID(), name, authorities)
}

@Serializable
data class UpdateRoleDto(
    val idRoom: SerializableUUID,
    val idRole: SerializableUUID,
    val name: String,
    val authorities: Set<Authority>,
) {
    fun toModel() = RoomRole(idRole, name, authorities)
}

@Serializable
data class DeleteRoleDto(
    val idRoom: SerializableUUID,
    val idRole: SerializableUUID,
)

