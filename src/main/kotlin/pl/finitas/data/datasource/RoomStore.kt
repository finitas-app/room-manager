package pl.finitas.data.datasource

import kotlinx.serialization.Serializable
import org.litote.kmongo.*
import pl.finitas.configuration.exceptions.ErrorCode
import pl.finitas.configuration.exceptions.NotFoundException
import pl.finitas.configuration.serialization.SerializableUUID
import pl.finitas.data.database.roomCollection
import pl.finitas.data.model.Room
import pl.finitas.data.model.RoomMember
import pl.finitas.domain.RoomVersionDto
import java.util.*

suspend fun createRoom(room: Room): Room {
    roomCollection.insertOne(room)
    return room
}

suspend fun getChangedRooms(idUser: UUID, roomVersions: List<RoomVersionDto>): List<Room> {
    return roomVersions
        .flatMap { (idRoom, version) ->
            roomCollection.find(
                and(
                    Room::idRoom eq idRoom,
                    Room::version gt version,
                    Room::members elemMatch (RoomMember::idUser eq idUser)
                ),
            ).toList()
        } +
            roomCollection.find(
                and(
                    not(Room::idRoom `in` roomVersions.map { it.idRoom }),
                    Room::members elemMatch (RoomMember::idUser eq idUser)
                )
            ).toList()
}

suspend fun getRoomsBy(roomIds: List<UUID>): List<Room> {
    return roomCollection.find(Room::idRoom `in` roomIds).toList()
}

suspend fun findRoomBy(idRoom: UUID) = roomCollection.findOne(Room::idRoom eq idRoom)

suspend fun getRoomBy(idRoom: UUID) = findRoomBy(idRoom) ?: throw NotFoundException(
    "Room with id '${idRoom}' not found.",
    ErrorCode.ROOM_NOT_FOUND,
)

suspend fun getReachableUsersForUser(idUser: UUID, idRoom: UUID?): ReachableUsersDto {
    val idUserContains = Room::members elemMatch (RoomMember::idUser eq idUser)
    return roomCollection
        .find(
            if (idRoom == null)
                idUserContains
            else
                and(
                    idUserContains,
                    (Room::idRoom eq idRoom),
                )
        )
        .toList()
        .flatMap { room -> room.members.map { it.idUser } }
        .let { ReachableUsersDto(it.distinct()) }
}

@Serializable
data class ReachableUsersDto(
    val reachableUsers: List<SerializableUUID>,
)
