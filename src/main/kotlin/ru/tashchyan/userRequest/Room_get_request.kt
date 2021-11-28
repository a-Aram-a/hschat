package ru.tashchyan.userRequest

import kotlinx.serialization.Serializable

@Serializable
data class Room_get_request(val auth_token: String)