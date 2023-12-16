package pl.finitas.configuration

import io.ktor.server.application.*
import io.ktor.server.routing.*
import pl.finitas.application.messageRouter
import pl.finitas.application.roomRouter

fun Application.configureRouting() {
    routing {
        roomRouter()
        messageRouter()
    }
}