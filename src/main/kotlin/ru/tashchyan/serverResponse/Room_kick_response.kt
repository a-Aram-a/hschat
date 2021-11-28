package ru.tashchyan.serverResponse

import kotlinx.serialization.Serializable

@Serializable
data class Room_kick_response(val success: Boolean, val error: String)