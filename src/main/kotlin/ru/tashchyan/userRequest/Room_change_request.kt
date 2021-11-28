package ru.tashchyan.userRequest

import kotlinx.serialization.Serializable

@Serializable
data class Room_change_request(val auth_token: String, val roomID: Int, val creatorID: Int, val title: String, val password: String)