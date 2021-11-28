package ru.tashchyan.serverResponse

import kotlinx.serialization.Serializable
import ru.tashchyan.entities.MessageClient

@Serializable
data class Messages_get_response(val success: Boolean, val messages: List<MessageClient>, val error: String)