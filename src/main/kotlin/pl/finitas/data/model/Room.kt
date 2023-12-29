package pl.finitas.data.model

import kotlinx.serialization.Serializable
import pl.finitas.configuration.serialization.SerializableUUID
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
    val name: String,
    val authorities: Set<Authority>,
) {
    companion object {
        val Owner get() = RoomRole(UUID.randomUUID(), "Owner", Authority.entries.toSet())
    }
}

@Serializable
data class RoomMember(
    val idUser: SerializableUUID,
    val idRole: SerializableUUID? = null,// left only idRole
)

enum class Authority {
    READ_USERS_DATA,
    MODIFY_USERS_DATA,
    MODIFY_ROOM,
}