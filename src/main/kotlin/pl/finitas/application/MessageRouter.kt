package pl.finitas.application

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import pl.finitas.configuration.serialization.SerializableUUID
import pl.finitas.data.model.Message
import pl.finitas.domain.MessageService
import pl.finitas.domain.MessagesVersionDto

fun Route.messageRouter() {
    route("/messages") {
        post {
            call.receive<SendMessageRequest>()
                .let { MessageService.addMessage(it) }
                .let { call.respond(HttpStatusCode.Created, it) }
        }
        post("/sync") {
            MessageService.getMessagesFromVersion(call.receive())
                .let { call.respond(HttpStatusCode.OK, it) }
        }
    }
}

@Serializable
data class SyncMessagesFromVersionDto(
    val idUser: SerializableUUID,
    val lastMessagesVersions: List<MessagesVersionDto>,
)

@Serializable
data class SendMessageRequest(
    val idUser: SerializableUUID,
    val messages: List<SingleMessageDto>,
)

@Serializable
data class SingleMessageDto(
    val idMessage: SerializableUUID,
    val idRoom: SerializableUUID,
    val idShoppingList: SerializableUUID? = null,
    val content: String? = null,
)

@Serializable
data class NewMessagesDto(
    val messages: List<MessagesForUsers>,
)


@Serializable
data class MessagesForUsers(
    val users: List<SerializableUUID>,
    val messages: List<Message>
)

