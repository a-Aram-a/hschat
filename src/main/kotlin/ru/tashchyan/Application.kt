package ru.tashchyan

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import ru.tashchyan.plugins.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureRouting()
        configureSockets()
        configureTemplating()
    }.start(wait = true)
}
