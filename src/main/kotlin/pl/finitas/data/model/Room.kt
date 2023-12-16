package pl.finitas.data.model

import kotlinx.serialization.Serializable
import pl.finitas.configuration.serialization.UUIDSerializer
import java.util.*

@Serializable
data class Room(
    @Serializable(UUIDSerializer::class)
    val idRoom: UUID,
    val name: String,
    @Serializable(UUIDSerializer::class)
    val idInvitationLink: UUID,
    val version: Int,
    val roles: List<RoomRole>,
    val members: List<RoomMember>,
)

@Serializable
data class RoomRole(
    @Serializable(UUIDSerializer::class)
    val idRole: UUID,
    val authorities: List<Authority>,
) {
    companion object {
        val Owner = RoomRole(UUID.randomUUID(), Authority.entries)
    }
}

@Serializable
data class RoomMember(
    @Serializable(UUIDSerializer::class)
    val idUser: UUID,
    val roomRole: RoomRole? = null,
)

enum class Authority {
    READ_USERS_DATA,
    MODIFY_USERS_DATA,
    MODIFY_ROOM,
    MODIFY
}