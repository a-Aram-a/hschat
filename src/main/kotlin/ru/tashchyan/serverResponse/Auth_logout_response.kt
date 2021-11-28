package ru.tashchyan.serverResponse
import kotlinx.serialization.Serializable

@Serializable
data class Auth_logout_response(val success: Boolean, val error: String)