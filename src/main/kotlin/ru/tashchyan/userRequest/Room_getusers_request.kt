package ru.tashchyan.userRequest

import kotlinx.serialization.Serializable

@Serializable
data class Room_getusers_request(val auth_token: String, val roomID: Int)