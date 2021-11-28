package ru.tashchyan.userRequest

import kotlinx.serialization.Serializable

@Serializable
data class Ws_subscribeEvents_request(val auth_token: String)