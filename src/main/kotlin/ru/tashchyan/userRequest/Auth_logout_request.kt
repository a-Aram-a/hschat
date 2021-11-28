package ru.tashchyan.userRequest
import kotlinx.serialization.Serializable

@Serializable
data class Auth_logout_request(val auth_token: String)