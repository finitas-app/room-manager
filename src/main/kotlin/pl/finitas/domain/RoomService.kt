package pl.finitas.domain

import pl.finitas.application.CreateRoomRequest
import pl.finitas.application.JoinRoomWithInvitationRequest
import pl.finitas.data.datasource.RoomStore
import pl.finitas.data.model.Room
import pl.finitas.data.model.RoomMember
import pl.finitas.data.model.RoomRole
import java.util.*

object RoomService {
    suspend fun createRoom(createRoomRequest: CreateRoomRequest): Room {
        return RoomStore.createRoom(
            Room(
                idRoom = UUID.randomUUID(),
                name = createRoomRequest.roomName,
                idInvitationLink = UUID.randomUUID(),
                version = 0,
                roles = listOf(RoomRole.Owner),
                members = listOf(RoomMember(createRoomRequest.creator, RoomRole.Owner))
            )
        )
    }

    suspend fun joinRoomWithInvitationLinkId(
        joinRoomWithInvitationRequest: JoinRoomWithInvitationRequest
    ) = RoomStore.addUserToRoomWithInvitationLink(
        joinRoomWithInvitationRequest.idInvitationLink,
        joinRoomWithInvitationRequest.idUser,
    )
}