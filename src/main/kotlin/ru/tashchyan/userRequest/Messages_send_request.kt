package ru.tashchyan.userRequest

import kotlinx.serialization.Serializable

@Serializable
data class Messages_send_request(val auth_token: String, val roomID: Int, val text: String)