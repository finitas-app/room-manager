package pl.finitas

import pl.finitas.configuration.configureRouting
import pl.finitas.configuration.serialization.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import pl.finitas.configuration.exceptions.configureExceptions

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureExceptions()
    configureRouting()
}
