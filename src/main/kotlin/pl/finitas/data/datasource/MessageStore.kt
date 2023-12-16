package pl.finitas.data.datasource

import org.litote.kmongo.and
import org.litote.kmongo.descending
import org.litote.kmongo.eq
import org.litote.kmongo.gt
import pl.finitas.data.database.mongoClient
import pl.finitas.data.database.mongoDatabase
import pl.finitas.data.model.Message
import pl.finitas.domain.MessageDto
import pl.finitas.domain.MessagesVersionDto
import java.time.LocalDateTime

private val messageCollection = mongoDatabase.getCollection<Message>()

object MessageStore {

    suspend fun addMessages(messages: List<MessageDto>) {
        messages.groupBy { it.idRoom }.forEach { (idRoom, messages) ->
            mongoClient.startSession().use { clientSession ->
                clientSession.startTransaction()
                var lastVersion = messageCollection
                    .find(Message::idRoom eq idRoom)
                    .sort(descending(Message::version))
                    .limit(1)
                    .first()?.version ?: 0

                messageCollection.insertMany(messages.map { it.toMessage(version = lastVersion++) })

                clientSession.commitTransaction()
            }
        }
    }

    suspend fun readMessagesFromVersion(roomIds: List<MessagesVersionDto>): List<Message> {
        return roomIds.flatMap { (idRoom, version) ->
            messageCollection.find(
                and(
                    Message::idRoom eq idRoom,
                    Message::version gt version,
                ),
            ).toList()
        }
    }
}

private fun MessageDto.toMessage(version: Int) = Message(
    idMessage = idMessage,
    idUser = idUser,
    idRoom = idRoom,
    messageType = messageType,
    createdAt = LocalDateTime.now(),
    version = version,
)