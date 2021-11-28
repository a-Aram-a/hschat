package ru.tashchyan.serverResponse

import kotlinx.serialization.Serializable
import ru.tashchyan.entities.MessageClient

@Serializable
data class Ws_messageEvent_response(val event: String, val message: MessageClient)