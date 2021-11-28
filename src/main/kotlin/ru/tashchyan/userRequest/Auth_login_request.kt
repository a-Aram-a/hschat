package ru.tashchyan.userRequest
import kotlinx.serialization.Serializable

@Serializable
data class Auth_login_request(val name: String, val password: String)