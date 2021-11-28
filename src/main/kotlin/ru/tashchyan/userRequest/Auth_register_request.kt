package ru.tashchyan.userRequest

import kotlinx.serialization.Serializable

@Serializable
data class Auth_register_request(val name: String, val password: String)