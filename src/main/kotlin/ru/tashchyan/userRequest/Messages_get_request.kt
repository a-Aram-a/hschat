package ru.tashchyan.userRequest

import kotlinx.serialization.Serializable

@Serializable
data class Messages_get_request(val auth_token: String, val roomID: Int, val laterThanID: Int = 0)