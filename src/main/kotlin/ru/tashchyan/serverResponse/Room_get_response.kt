package ru.tashchyan.serverResponse

import kotlinx.serialization.Serializable
import ru.tashchyan.entities.RoomClient

@Serializable
data class Room_get_response(val success: Boolean, val rooms: List<RoomClient>, val error: String)