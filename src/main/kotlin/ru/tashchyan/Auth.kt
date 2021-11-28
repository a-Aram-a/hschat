package ru.tashchyan

import io.ktor.http.cio.websocket.*
import ru.tashchyan.entities.UserClient
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

object Auth {
    //Набор всех открытых http сеансов пользователей. С каждым сеансом связан пользователь UserClient{userID, name}
    private val sessions = Collections.synchronizedSet<Session?>(LinkedHashSet())
    data class Session(val token: String, val userClient: UserClient)

    //Набор Ws соединений. Модифицируется исключительно в обработчике ws (в Socket.kt)
    //Используется в методе /api/messages_send где помимо добавления сообщения в БД
    //происходит отправка текущего сообщения активным ws подключениям, связанными с участинками комнаты, куда отправилось сообщение.
    val ws_connections = Collections.synchronizedSet<WsConnection?>(LinkedHashSet())
    class WsConnection(val token: String, val session: DefaultWebSocketSession) {
        val user: UserClient = Auth.getUserByToken(token) ?: throw Exception("Invalid auth_token")
        companion object {
            var lastId = AtomicInteger(0)
        }
        val connectionId = lastId.getAndIncrement()
    }

    //генерируем случайный токен
    fun generateToken(): String {
        return java.util.UUID.randomUUID().toString().replace("-", "")
    }
    //добавляем сеанс по токену и ассоциируем его с пользователем
    fun addNewSession(token: String, user: UserClient) {
        sessions += Session(token, user)
    }
    //выполняем логаут сеанса с закрытием всех ws соединний связанных с ним
    suspend fun removeSession(token: String) {
        sessions.removeIf { it.token == token }
        ws_connections.forEach { if(it.token == token) it.session.close(CloseReason(CloseReason.Codes.NORMAL, "User logout")) }
        ws_connections.removeIf { it.token == token }
    }
    //получаем пользователя по токену сеанса (именно по нему методы api понимают залогинел ли пользователь)
    fun getUserByToken(token: String): UserClient? {
        if (!sessions.any { it.token == token }) return null
        return sessions.single { it.token == token }.userClient
    }

    //На будущее. функция для хеширования паролей (соль нужно брать отдельно)
    fun hashString(type: String, input: String): String { //type="MD5","SHA-1",...
        fun ByteArray.toHexString() = asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }
        val bytes = MessageDigest
            .getInstance(type)
            .digest(input.toByteArray())
        return bytes.toHexString()
    }
}
