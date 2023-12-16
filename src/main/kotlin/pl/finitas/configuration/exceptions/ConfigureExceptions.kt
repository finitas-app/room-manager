package pl.finitas.configuration.exceptions

import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*

fun Application.configureExceptions() {
    install(StatusPages) {
        exception(this@configureExceptions::handleException)
    }
}
