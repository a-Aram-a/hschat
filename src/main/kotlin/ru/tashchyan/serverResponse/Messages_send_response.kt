package ru.tashchyan.serverResponse

import io.ktor.features.*
import kotlinx.serialization.Serializable

@Serializable
data class Messages_send_response(val success: Boolean, val messageID: Int, val error: String)