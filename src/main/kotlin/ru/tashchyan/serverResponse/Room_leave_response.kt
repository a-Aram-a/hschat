package ru.tashchyan.serverResponse

import io.ktor.features.*
import kotlinx.serialization.Serializable

@Serializable
data class Room_leave_response(val success: Boolean, val error: String)