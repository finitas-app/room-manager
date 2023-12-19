package pl.finitas.domain

import pl.finitas.application.*
import pl.finitas.data.datasource.MessageStore
import pl.finitas.data.datasource.RoomStore
import java.util.*

object MessageService {

    suspend fun addMessage(sendMessageRequest: SendMessageRequest): NewMessagesDto {
        val response = MessageStore.addMessages(sendMessageRequest.messages.map { it.toMessageDto(sendMessageRequest.idUser) })
        return getMessagesFromVersion(
            SyncMessagesFromVersionDto(
                sendMessageRequest.idUser,
                response,
            )
        )
    }

    suspend fun getMessagesFromVersion(syncMessagesFromVersionDto: SyncMessagesFromVersionDto): NewMessagesDto {
        val newMessages = MessageStore.readMessagesFromVersion(syncMessagesFromVersionDto.lastMessagesVersions)
        val roomsById = RoomStore
            .getRoomsBy(newMessages.keys.toList())
            .groupBy { it.idRoom }
            .mapValues { it.value[0] }
        return NewMessagesDto(
            newMessages.map { (idRoom, messages) ->
                MessagesForUsers(
                    roomsById[idRoom]!!.members.map { it.idUser },
                    messages
                )
            }
        )
    }
}

fun SingleMessageDto.toMessageDto(idUser: UUID) = MessageDto(
    idMessage = idMessage,
    idUser = idUser,
    idRoom = idRoom,
    idShoppingList = idShoppingList,
    content = content
)