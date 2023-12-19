package pl.finitas.domain

import kotlinx.serialization.Serializable
import pl.finitas.configuration.serialization.UUIDSerializer
import java.util.*

@Serializable
data class MessagesVersionDto(
    @Serializable(UUIDSerializer::class)
    val idRoom: UUID,
    val version: Int,
)

@Serializable
data class RoomVersionDto(
    @Serializable(UUIDSerializer::class)
    val idRoom: UUID,
    val version: Int,
)

@Serializable
data class MessageDto(
    @Serializable(UUIDSerializer::class)
    val idMessage: UUID,
    @Serializable(UUIDSerializer::class)
    val idUser: UUID,
    @Serializable(UUIDSerializer::class)
    val idRoom: UUID,
    @Serializable(UUIDSerializer::class)
    val idShoppingList: UUID? = null,
    val content: String? = null,
)