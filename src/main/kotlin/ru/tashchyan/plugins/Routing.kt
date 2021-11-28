package ru.tashchyan.plugins

import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.request.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import ru.tashchyan.DbManipulator
import ru.tashchyan.Auth
import ru.tashchyan.entities.*
import ru.tashchyan.serverResponse.*
import ru.tashchyan.userRequest.*
import java.util.*

fun Application.configureRouting() {
    /*API реализуется через HTTP посредством 14 методов, каждый из которых прослушивает свой маршрут.
    * Каждый из них получает на вход строковой параметр request, содержащий в себе json с параметрами описанными
    * в соответствующих классах package ru.tashchyan.userRequest. Скрипт обрабатывает запрос и возвращает json
    * сериализованный объект ответа. Все такие объекты ответа каждого метода определены в классах package ru.tashchyan.serverResponse
    *
    * Взаимодействие с БД осуществляется через object DbManipulator, который посредством jdbc взаимодействует с бд.
    * В этом классе зашито все необходимое для работы api методов.
    *
    * При работе с api и DbManipulator используется 3 Типа сущностей UserRow, RoomRow, MessageRow которые полностью соответствуют
    * строкам в БД. Также есть ещё 3 дополнительные сущности UserClient, RoomClient, MessageClient в которые предназначены для
    * отправки на клиент! В нашем случае у UserClient и RoomClient нету поля password( в отличие от UserRow, RoomRow).
    * Таким образом на клиент мы отправляем сущности UserClient, RoomClient, MessageClient(фактически это те же урезанные строки бд), а с DbManipulator-ом
    * работаем с UserRow, RoomRow, MessageRow
    *
    * Websockets используются только в методе /api/messages_send (см подробнее описание в файле Sockets.kt)
    *
    * Методы авторизации
    * ->/api/auth_login - получает name, password; В случае успеха возвращает auth_token, предварительо сохранив его в Auth.sessions
    *   вместе с только что полученной из бд UserRow (чтобы потом получать этот объект не из бд, а по токену)
    * ->/api/auth_register - получает name, password; вносит в бд нового пользователя
    * ->/api/auth_logout - аннулирует auth_token в  Auth.sessions так что по нему больше нельзя получить UserRow
    *
    * Методы комнат
    * ->/api/room_create - создаёт комнату с указанем названия и пароля (пустой пароль - значит открытая комната)
    * ->/api/room_change - заменяет создателя комнаты, название и пароль (доступно для выполнения только текущему создателю)
    * ->/api/room_join - присоединяется к комнате по roomID и паролю
    * ->/api/room_leave - покидание комнаты (в БД уничтожается запись в связующей таблице users_rooms)
    * ->/api/room_kick - изгнание из комнаты пользователя по userID (доступно только для текущего создателя комнаты)
    * ->/api/room_getusers - получения списка всех участников комнаты
    * ->/api/room_get - получения всех своих комнат
    * ->/api/room_getpassword - получение пароля комнаты (доступно только для текущего создателя комнаты)
    *
    * Методы сообщений
    * ->/api/messages_send - отправляет сообщение в комнату, записывая её в бд, а также отсылая событие об этом всем активным
    *   ws_connections участников комнаты
    * ->/api/messages_get - получение всех сообщений комнаты после некоторого id (по умолчанию 0, то есть получаем все сообщения)
    *
    * Метод пользователя
    * ->/api/user_getById - получение пользователя по id (например, если чела кикнут, то его имя нельзя будет получить через /api/room_getusers
    *   поэтому чтобы получать имена покинувших комнату людей но оставивших там сообщения, можем использовать этот метод)
    * */

    //setting spa html path
    install(StatusPages) {
        statusFile(HttpStatusCode.NotFound, filePattern = "files\\index.html")
    }

    routing {
        //static content
        static("/static") {
            resources("files")
        }

        //api methods
        post("/api/auth_login") {
            try {
                val requestJson = call.receiveParameters()["request"] ?: throw Exception("Invalid request")
                val request = Json.decodeFromString<Auth_login_request>(requestJson)

                val currUserID = DbManipulator.selectUserIDByNameAndPassword(request.name, request.password)
                    ?: throw Exception("Wrong name or password")
                val currUserName = DbManipulator.selectUserById(currUserID)!!.name
                val authToken = Auth.generateToken()
                Auth.addNewSession(authToken, UserClient(currUserID, currUserName))
                call.respondText(Json.encodeToString(
                    Auth_login_response(true, authToken, UserClient(currUserID, currUserName), "")
                ))
            } catch(e :Exception) {
                e.printStackTrace()
                call.respondText(Json.encodeToString(
                    Auth_login_response(false, "", UserClient(0, "Unknown"), e.message.toString())
                ))
            }
        }

        post("/api/auth_register") {
            try {
                val requestJson = call.receiveParameters()["request"] ?: throw Exception("Invalid request")
                val request = Json.decodeFromString<Auth_register_request>(requestJson)

                if (request.name.length !in 1..50) {
                    throw Exception("Name should contain from 1 to 50 symbols")
                }
                if (request.password.length !in 4..255) {
                    throw Exception("Password should contain from 4 to 255 symbols")
                }
                val findUserID = DbManipulator.selectUserIDByName(request.name)
                if (findUserID != null) {
                    throw Exception("User with this name has already registered")
                }
                DbManipulator.insertUser(UserRow(0, request.name, request.password))
                call.respondText(Json.encodeToString(
                    Auth_register_response(true, "")
                ))
            } catch(e :Exception) {
                e.printStackTrace()
                call.respondText(Json.encodeToString(
                    Auth_register_response(false, e.message.toString())
                ))
            }
        }

        post("/api/auth_logout") {
            try {
                val requestJson = call.receiveParameters()["request"] ?: throw Exception("Invalid request")
                val request = Json.decodeFromString<Auth_logout_request>(requestJson)

                val authToken = request.auth_token
                val currUser = Auth.getUserByToken(authToken) ?: throw Exception("Invalid auth_token")

                Auth.removeSession(authToken)
                call.respondText(Json.encodeToString(
                    Auth_logout_response(true, "")
                ))
            } catch(e :Exception) {
                e.printStackTrace()
                call.respondText(Json.encodeToString(
                    Auth_logout_response(false, e.message.toString())
                ))
            }
        }

        post("/api/room_create") {
            try {
                val requestJson = call.receiveParameters()["request"] ?: throw Exception("Invalid request")
                val request = Json.decodeFromString<Room_create_request>(requestJson)
                val authToken = request.auth_token
                val currUser = Auth.getUserByToken(authToken) ?: throw Exception("Invalid auth_token")
                if (request.title.length !in 1..50) {
                    throw Exception("Title should contain from 1 to 50 symbols")
                }
                if (request.password.length !in 0..255) {
                    throw Exception("Password should contain from 0 to 255 symbols")
                }

                val roomID = DbManipulator.insertRoom(RoomRow(0, currUser.userID, request.title, request.password))
                DbManipulator.insertUserRoomConnect(currUser.userID, roomID)
                call.respondText(Json.encodeToString(
                    Room_create_response(true, roomID, "")
                ))
            } catch(e :Exception) {
                e.printStackTrace()
                call.respondText(Json.encodeToString(
                    Room_create_response(false, 0, e.message.toString())
                ))
            }
        }

        post("/api/room_change") {
            try {
                val requestJson = call.receiveParameters()["request"] ?: throw Exception("Invalid request")
                val request = Json.decodeFromString<Room_change_request>(requestJson)
                val authToken = request.auth_token
                val currUser = Auth.getUserByToken(authToken) ?: throw Exception("Invalid auth_token")

                val room = DbManipulator.selectRoomById(request.roomID) ?: throw Exception("There are not room with this id")
                if(room.creatorID != currUser.userID) throw Exception("Only the creator of the room can change it")

                if (request.title.length !in 1..50) {
                    throw Exception("Title should contain from 1 to 50 symbols")
                }
                if (request.password.length !in 0..255) {
                    throw Exception("Password should contain from 0 to 255 symbols")
                }
                if (!DbManipulator.isUserInRoom(request.creatorID, room.roomID)) {
                    throw Exception("The user you want to designate as the creator is not in the room ")
                }

                DbManipulator.updateRoom(RoomRow(request.roomID, request.creatorID, request.title, request.password))
                call.respondText(Json.encodeToString(
                    Room_change_response(true, "")
                ))
            } catch(e :Exception) {
                e.printStackTrace()
                call.respondText(Json.encodeToString(
                    Room_change_response(false, e.message.toString())
                ))
            }
        }

        post("/api/room_join") {
            try {
                val requestJson = call.receiveParameters()["request"] ?: throw Exception("Invalid request")
                val request = Json.decodeFromString<Room_join_request>(requestJson)
                val authToken = request.auth_token
                val currUser = Auth.getUserByToken(authToken) ?: throw Exception("Invalid auth_token")

                val room = DbManipulator.selectRoomById(request.roomID) ?: throw Exception("There are not room with this id")
                if(DbManipulator.isUserInRoom(currUser.userID, room.roomID)) throw Exception("You're already in the room")
                if (room.password.isNotEmpty() && room.password != request.password) throw Exception("Wrong room password")

                DbManipulator.insertUserRoomConnect(currUser.userID, room.roomID)
                call.respondText(Json.encodeToString(
                    Room_join_response(true, "")
                ))
            } catch(e :Exception) {
                e.printStackTrace()
                call.respondText(Json.encodeToString(
                    Room_join_response(false, e.message.toString())
                ))
            }
        }

        post("/api/room_leave") {
            try {
                val requestJson = call.receiveParameters()["request"] ?: throw Exception("Invalid request")
                val request = Json.decodeFromString<Room_leave_request>(requestJson)
                val authToken = request.auth_token
                val currUser = Auth.getUserByToken(authToken) ?: throw Exception("Invalid auth_token")

                val room = DbManipulator.selectRoomById(request.roomID) ?: throw Exception("There are not room with this id")

                DbManipulator.removeUserFromRoom(currUser.userID, request.roomID)
                call.respondText(Json.encodeToString(
                    Room_leave_response(true, "")
                ))
            } catch(e :Exception) {
                e.printStackTrace()
                call.respondText(Json.encodeToString(
                    Room_leave_response(false, e.message.toString())
                ))
            }
        }

        post("/api/room_kick") {
            try {
                val requestJson = call.receiveParameters()["request"] ?: throw Exception("Invalid request")
                val request = Json.decodeFromString<Room_kick_request>(requestJson)
                val authToken = request.auth_token
                val currUser = Auth.getUserByToken(authToken) ?: throw Exception("Invalid auth_token")

                val room = DbManipulator.selectRoomById(request.roomID) ?: throw Exception("There are not room with this id")
                if(room.creatorID != currUser.userID) throw Exception("Only the creator of the room can kick participants")

                DbManipulator.removeUserFromRoom(request.userID, request.roomID)
                call.respondText(Json.encodeToString(
                    Room_kick_response(true, "")
                ))
            } catch(e :Exception) {
                e.printStackTrace()
                call.respondText(Json.encodeToString(
                    Room_kick_response(false, e.message.toString())
                ))
            }
        }

        post("/api/room_getusers") {
            try {
                val requestJson = call.receiveParameters()["request"] ?: throw Exception("Invalid request")
                val request = Json.decodeFromString<Room_getusers_request>(requestJson)
                val authToken = request.auth_token
                val currUser = Auth.getUserByToken(authToken) ?: throw Exception("Invalid auth_token")

                val room = DbManipulator.selectRoomById(request.roomID) ?: throw Exception("There are not room with this id")
                if(!DbManipulator.isUserInRoom(currUser.userID, room.roomID)) throw Exception("You are not a member of the room")

                //mapping UserRow to UserClient because UserClient unlike UserRow doesn't contain password field
                val users: List<UserClient> = DbManipulator.selectRoomUsers(room.roomID).map { it.toUserClient() }
                call.respondText(Json.encodeToString(
                    Room_getusers_response(true, users,"")
                ))
            } catch(e :Exception) {
                e.printStackTrace()
                call.respondText(Json.encodeToString(
                    Room_getusers_response(false, listOf<UserClient>(), e.message.toString())
                ))
            }
        }

        post("/api/room_get") {
            try {
                val requestJson = call.receiveParameters()["request"] ?: throw Exception("Invalid request")
                val request = Json.decodeFromString<Room_get_request>(requestJson)
                val authToken = request.auth_token
                val currUser = Auth.getUserByToken(authToken) ?: throw Exception("Invalid auth_token")

                //mapping RoomRow to RoomClient because RoomClient unlike RoomRow doesn't contain password field
                val rooms: List<RoomClient> = DbManipulator.selectUserRooms(currUser.userID).map { it.toRoomClient() }
                call.respondText(Json.encodeToString(
                    Room_get_response(true, rooms,"")
                ))
            } catch(e :Exception) {
                e.printStackTrace()
                call.respondText(Json.encodeToString(
                    Room_get_response(false, listOf<RoomClient>(), e.message.toString())
                ))
            }
        }

        post("/api/room_getpassword") {
            try {
                val requestJson = call.receiveParameters()["request"] ?: throw Exception("Invalid request")
                val request = Json.decodeFromString<Room_getpassword_request>(requestJson)
                val authToken = request.auth_token
                val currUser = Auth.getUserByToken(authToken) ?: throw Exception("Invalid auth_token")

                val room = DbManipulator.selectRoomById(request.roomID) ?: throw Exception("There are not room with this id")
                if(room.creatorID != currUser.userID) throw Exception("Only the creator of the room can request its password")

                call.respondText(Json.encodeToString(
                    Room_getpassword_response(true, room.password, "")
                ))
            } catch(e :Exception) {
                e.printStackTrace()
                call.respondText(Json.encodeToString(
                    Room_getpassword_response(false, "", e.message.toString())
                ))
            }
        }

        post("/api/messages_send") {
            try {
                val requestJson = call.receiveParameters()["request"] ?: throw Exception("Invalid request")
                val request = Json.decodeFromString<Messages_send_request>(requestJson)
                val authToken = request.auth_token
                val currUser = Auth.getUserByToken(authToken) ?: throw Exception("Invalid auth_token")

                if(!DbManipulator.isUserInRoom(currUser.userID, request.roomID)) throw Exception("You are not a member of the room")
                if (request.text.length !in 1..16000) {
                    throw Exception("Message text should contain from 1 to 16000 symbols")
                }

                val messageRow = MessageRow(0, currUser.userID, request.roomID, request.text, (Date()).time / 1000 )
                val messageID =
                    DbManipulator.insertMessage(messageRow)
                //send messages to all websocket sessions related to room participants [every user can have several websocket sessions(several devices)]
                val roomUsers = DbManipulator.selectRoomUsers(request.roomID)
                roomUsers.forEach { roomUser ->
                    Auth.ws_connections.forEach { wsConnection ->
                        if (wsConnection.user.userID == roomUser.userID) {
                            wsConnection.session.send(
                                Json.encodeToString(Ws_messageEvent_response("message", messageRow.toMessageClient()))
                            )
                        }
                    }
                }
                call.respondText(Json.encodeToString(
                    Messages_send_response(true, messageID, "")
                ))
            } catch(e :Exception) {
                e.printStackTrace()
                call.respondText(Json.encodeToString(
                    Messages_send_response(false, 0, e.message.toString())
                ))
            }
        }

        post("/api/messages_get") {
            try {
                val requestJson = call.receiveParameters()["request"] ?: throw Exception("Invalid request")
                val request = Json.decodeFromString<Messages_get_request>(requestJson)
                val authToken = request.auth_token
                val currUser = Auth.getUserByToken(authToken) ?: throw Exception("Invalid auth_token")

                if(!DbManipulator.isUserInRoom(currUser.userID, request.roomID)) throw Exception("You are not a member of the room")

                val messages = DbManipulator.selectRoomMessages(request.roomID, request.laterThanID).map { it.toMessageClient() }
                call.respondText(Json.encodeToString(
                    Messages_get_response(true, messages, "")
                ))
            } catch(e :Exception) {
                e.printStackTrace()
                call.respondText(Json.encodeToString(
                    Messages_get_response(false, listOf<MessageClient>(), e.message.toString())
                ))
            }
        }

        post("/api/user_getById") {
            try {
                val requestJson = call.receiveParameters()["request"] ?: throw Exception("Invalid request")
                val request = Json.decodeFromString<User_getById_request>(requestJson)
                val authToken = request.auth_token
                val currUser = Auth.getUserByToken(authToken) ?: throw Exception("Invalid auth_token")

                val user = DbManipulator.selectUserById(request.userID) ?: throw Exception("User not found")
                call.respondText(Json.encodeToString(
                    User_getById_response(true, user.toUserClient(), "")
                ))
            } catch(e :Exception) {
                e.printStackTrace()
                call.respondText(Json.encodeToString(
                    User_getById_response(false, UserClient(0, "Unknown"), e.message.toString())
                ))
            }
        }
    }
}
