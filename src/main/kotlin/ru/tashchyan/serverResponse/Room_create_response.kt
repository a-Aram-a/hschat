package ru.tashchyan.serverResponse

import kotlinx.serialization.Serializable

@Serializable
data class Room_create_response(val success: Boolean, val roomID: Int, val error: String)