package pl.finitas.data.datasource

import org.litote.kmongo.*
import pl.finitas.configuration.exceptions.ErrorCode
import pl.finitas.configuration.exceptions.NotFoundException
import pl.finitas.data.database.mongoClient
import pl.finitas.data.database.mongoDatabase
import pl.finitas.data.model.Room
import pl.finitas.data.model.RoomMember
import pl.finitas.domain.RoomVersionDto
import java.util.*

object RoomStore {
    private val roomCollection = mongoDatabase.getCollection<Room>()

    suspend fun createRoom(room: Room): Room {
        roomCollection.insertOne(room)
        return room
    }

    suspend fun addUserToRoomWithInvitationLink(idInvitationLink: UUID, idUser: UUID): Room {
        return mongoClient.startSession().use { clientSession ->
            clientSession.startTransaction()
            val room = roomCollection
                .findOne(Room::idInvitationLink eq idInvitationLink)
                ?: throw NotFoundException(
                    "Room with invitation link '$idInvitationLink' not found.",
                    ErrorCode.ROOM_NOT_FOUND,
                )
            roomCollection.updateOne(
                Room::idInvitationLink eq idInvitationLink,
                setValue(Room::members, room.members + RoomMember(idUser))
            )
            val result = roomCollection.findOne(Room::idRoom eq room.idRoom)!!
            clientSession.commitTransaction()
            result
        }
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
}