package ru.tashchyan.serverResponse

import kotlinx.serialization.Serializable
import ru.tashchyan.entities.UserClient

@Serializable
data class User_getById_response(val success: Boolean, val user: UserClient, val error: String)