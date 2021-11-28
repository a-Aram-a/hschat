package ru.tashchyan.plugins

import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import java.time.*
import io.ktor.application.*
import io.ktor.routing.*
import ru.tashchyan.Auth

/*Обрабатывает Ws соединения.
WS соединения нужны ТОЛЬКО ДЛЯ ОТПРАВКИ НОВЫХ СООБЩЕНИЙ ПОЛЬЗОВАТЕЛЯМ!
В будущем можно сделать и другие события вроде изменения имени комнаты или то что вас кикнули из комнаты.
В ДАННОМ ОБРАБОТЧИКЕ ПРОИСХОДИТ ТОЛЬКО(!) поддерживание ws соединений и их представление в Auth.ws_connections
НИКАКИХ ДАННЫХ МЫ ТУТ НЕ ПРИНИМАЕМ, НЕ ОТПРАВЛЯЕМ - ЕДИНСТВЕННОЕ ЗАЧЕМ ЭТО НУЖНО, ЧТОБЫ ПРИ ОБРАЩЕНИИ
К /api/message_send, http обработчик помимо записи в БД смог получить все активные ws_connections соответствующие нужным пользователям
и отправить им новое сообщение через WS. ВСЁ! Больше нигде ws не используется.

Ws соединение привязывается к активному http сеансу по . Причём к одному сеансу разрешено открывать несколько ws соединений
*/
fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        webSocket("/api/events") { // websocketSession
            val auth_token = call.request.queryParameters["auth_token"]
            if(auth_token != null && Auth.getUserByToken(auth_token) != null) {
                println("Adding user!")
                val thisConnection = Auth.WsConnection(auth_token, this)
                Auth.ws_connections += thisConnection
                try {
                    for (frame in incoming) {
                        frame as? Frame.Text ?: continue
                        val receivedText = frame.readText()
                        //onMessage
                    }
                } catch (e: Exception) {
                    println(e.localizedMessage)
                } finally {
                    println("Removing $thisConnection!")
                    Auth.ws_connections -= thisConnection
                }
            } else {
                close(CloseReason(CloseReason.Codes.NORMAL, "Invalid auth_token"))
            }
        }
    }
}
