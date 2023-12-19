package pl.finitas.data.model

import kotlinx.serialization.Serializable
import pl.finitas.configuration.serialization.LocalDateTimeSerializer
import pl.finitas.configuration.serialization.UUIDSerializer
import java.time.LocalDateTime
import java.util.*

@Serializable
data class Message(
    @Serializable(with = UUIDSerializer::class)
    val idMessage: UUID,
    @Serializable(with = UUIDSerializer::class)
    val idUser: UUID,
    @Serializable(with = UUIDSerializer::class)
    val idRoom: UUID,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    val version: Int,
    @Serializable(with = UUIDSerializer::class)
    val idShoppingList: UUID? = null,
    val content: String? = null,
)
