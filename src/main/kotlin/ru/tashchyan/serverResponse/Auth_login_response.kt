package ru.tashchyan.serverResponse
import kotlinx.serialization.Serializable
import ru.tashchyan.entities.UserClient

@Serializable
data class Auth_login_response(val success: Boolean, val auth_token: String, val user: UserClient, val error: String)