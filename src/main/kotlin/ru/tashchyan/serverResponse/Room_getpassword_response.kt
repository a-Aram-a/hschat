package ru.tashchyan.serverResponse

import kotlinx.serialization.Serializable

@Serializable
data class Room_getpassword_response(val success: Boolean, val password: String, val error: String)