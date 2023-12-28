package pl.finitas.data.datasource

import org.litote.kmongo.and
import org.litote.kmongo.descending
import org.litote.kmongo.eq
import org.litote.kmongo.gt
import pl.finitas.configuration.exceptions.BadRequestException
import pl.finitas.configuration.exceptions.ErrorCode
import pl.finitas.data.database.mongoClient
import pl.finitas.data.database.mongoDatabase
import pl.finitas.data.model.Message
import pl.finitas.domain.MessageDto
import pl.finitas.domain.MessagesVersionDto
import java.time.LocalDateTime
import java.util.*

private val messageCollection = mongoDatabase.getCollection<Message>()

object MessageStore {
    suspend fun addMessages(messages: List<MessageDto>): List<MessagesVersionDto> {
        return messages.groupBy { it.idRoom }.map { (idRoom, messages) ->
            mongoClient.startSession().use { clientSession ->
                clientSession.startTransaction()
                val lastVersion = messageCollection
                    .find(Message::idRoom eq idRoom)
                    .sort(descending(Message::version))
                    .limit(1)
                    .first()?.version ?: 0

                var currentVersion = lastVersion

                messageCollection.insertMany(messages.map { it.toMessage(version = ++currentVersion) })

                clientSession.commitTransaction()
                MessagesVersionDto(idRoom, lastVersion)
            }
        }
    }

    suspend fun readMessagesFromVersion(roomIds: List<MessagesVersionDto>): Map<UUID, List<Message>> {
        if (roomIds.distinctBy { it.idRoom }.size != roomIds.size)
            throw BadRequestException(errorCode = ErrorCode.SYNCHRONIZATION_ERROR)
        return roomIds.associate { (idRoom, version) ->
            idRoom to messageCollection.find(
                and(
                    Message::idRoom eq idRoom,
                    Message::version gt version,
                ),
            ).toList()
        }.filter { it.value.isNotEmpty() }
    }
}

private fun MessageDto.toMessage(version: Int) = Message(
    idMessage = idMessage,
    idUser = idUser,
    idRoom = idRoom,
    createdAt = LocalDateTime.now(),
    version = version,
    content = content,
    idShoppingList = idShoppingList,
)