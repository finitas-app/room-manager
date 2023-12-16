package pl.finitas.application

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import pl.finitas.configuration.serialization.UUIDSerializer
import pl.finitas.domain.RoomService
import java.util.*

fun Route.roomRouter() {
    route("/rooms") {
        post {
            call.receive<CreateRoomRequest>()
                .let { RoomService.createRoom(it) }
                .let { call.respond(HttpStatusCode.Created, it) }
        }
        post("/users") {
            call.receive<JoinRoomWithInvitationRequest>()
                .let { RoomService.joinRoomWithInvitationLinkId(it) }
                .let { call.respond(HttpStatusCode.OK, it) }
        }
    }
}

@Serializable
data class CreateRoomRequest(
    @Serializable(UUIDSerializer::class)
    val creator: UUID,
    val roomName: String,
)

@Serializable
data class JoinRoomWithInvitationRequest(
    @Serializable(UUIDSerializer::class)
    val idUser: UUID,
    @Serializable(UUIDSerializer::class)
    val idInvitationLink: UUID,
)
