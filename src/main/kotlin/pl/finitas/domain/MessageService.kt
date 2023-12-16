package pl.finitas.domain

import pl.finitas.application.SendMessageRequest
import pl.finitas.data.datasource.MessageStore
import pl.finitas.data.model.Message

object MessageService {

    suspend fun addMessage(sendMessageRequest: SendMessageRequest): List<Message> {
        MessageStore.addMessages(sendMessageRequest.messages)
        return MessageStore.readMessagesFromVersion(sendMessageRequest.lastMessagesVersions)
    }
}