package pl.finitas

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.slf4j.LoggerFactory
import pl.finitas.configuration.configureRouting
import pl.finitas.configuration.exceptions.configureExceptions
import pl.finitas.configuration.serialization.configureSerialization

private val logger = LoggerFactory.getLogger("ConfigureMongoDb")

fun main() {
    val profile = System.getProperty("profile")
    logger.info("Profile: $profile")
    logger.info("Props: ${System.getProperties()}")
    embeddedServer(Netty, port = 8083, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureExceptions()
    configureRouting()
}
