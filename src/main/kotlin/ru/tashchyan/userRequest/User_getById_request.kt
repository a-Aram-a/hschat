package ru.tashchyan.userRequest

import kotlinx.serialization.Serializable

@Serializable
data class User_getById_request(val auth_token: String, val userID: Int)