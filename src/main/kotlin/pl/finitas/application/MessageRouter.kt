package pl.finitas.application

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import pl.finitas.domain.MessageDto
import pl.finitas.domain.MessageService
import pl.finitas.domain.MessagesVersionDto

fun Route.messageRouter() {
    route("/messages") {
        post {
            call.receive<SendMessageRequest>()
                .let { MessageService.addMessage(it) }
                .let { call.respond(HttpStatusCode.Created, it) }
        }
    }
}

@Serializable
data class SendMessageRequest(
    val messages: List<MessageDto>,
    val lastMessagesVersions: List<MessagesVersionDto>,
)

