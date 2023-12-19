package pl.finitas.application

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import pl.finitas.configuration.serialization.UUIDSerializer
import pl.finitas.domain.RoomService
import pl.finitas.domain.RoomVersionDto
import java.util.*

fun Route.roomRouter() {
    route("/rooms") {
        post {
            RoomService.createRoom(call.receive())
                .let { call.respond(HttpStatusCode.Created, it) }
        }
        post("/users") {
            RoomService.joinRoomWithInvitationLinkId(call.receive())
                .let { call.respond(HttpStatusCode.OK, it) }
        }
        post("/sync") {
            RoomService.getChangedRooms(call.receive())
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

@Serializable
data class GetChangedRoomsDto(
    @Serializable(UUIDSerializer::class)
    val idUser: UUID,
    val roomVersions: List<RoomVersionDto>,
)
