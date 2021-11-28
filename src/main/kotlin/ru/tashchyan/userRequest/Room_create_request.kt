package ru.tashchyan.userRequest

import kotlinx.serialization.Serializable

@Serializable
data class Room_create_request(val auth_token: String, val title: String, val password: String)