package ru.tashchyan.serverResponse

import kotlinx.serialization.Serializable
import ru.tashchyan.entities.UserClient

@Serializable
data class Room_getusers_response(val success: Boolean, val users: List<UserClient>, val error: String)