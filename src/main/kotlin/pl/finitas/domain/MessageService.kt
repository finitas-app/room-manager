package pl.finitas.domain

import pl.finitas.application.*
import pl.finitas.data.datasource.MessageStore
import pl.finitas.data.datasource.getRoomsBy
import pl.finitas.data.datasource.hasAnyAuthority
import java.util.*

object MessageService {

    suspend fun addMessage(sendMessageRequest: SendMessageRequest): NewMessagesDto {
        val (messages, unavailableMessages) = sendMessageRequest.messages.partition {
            hasAnyAuthority(sendMessageRequest.idUser, it.idRoom)
        }
        val response = MessageStore.addMessages(messages.map { it.toMessageDto(sendMessageRequest.idUser) })
        return NewMessagesDto(
            messages = getMessagesFromVersionForUsers(
                SyncMessagesFromVersionDto(
                    sendMessageRequest.idUser,
                    response,
                )
            ),
            unavailableRooms = unavailableMessages.map { it.idRoom }
        )
    }

    private suspend fun getMessagesFromVersionForUsers(syncMessagesFromVersionDto: SyncMessagesFromVersionDto): List<MessagesForUsers> {
        val newMessages = MessageStore.readMessagesFromVersion(syncMessagesFromVersionDto.lastMessagesVersions)
        val roomsById =
            getRoomsBy(newMessages.keys.toList())
            .groupBy { it.idRoom }
            .mapValues { it.value[0] }
        return newMessages.map { (idRoom, messages) ->
            MessagesForUsers(
                roomsById[idRoom]!!.members.map { it.idUser },
                messages
            )
        }
    }

    suspend fun getMessagesFromVersion(syncMessagesFromVersionDto: SyncMessagesFromVersionDto): SyncMessageResponse {
        val (lastMessageVersions, unavailableMessages) = syncMessagesFromVersionDto.lastMessagesVersions.partition {
            hasAnyAuthority(syncMessagesFromVersionDto.idUser, it.idRoom)
        }

        return SyncMessageResponse(
            MessageStore.readMessagesFromVersion(lastMessageVersions).flatMap { it.value },
            unavailableMessages.map { it.idRoom }
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