package ru.tashchyan.userRequest

import kotlinx.serialization.Serializable

@Serializable
data class Room_kick_request(val auth_token: String, val roomID: Int, val userID: Int)