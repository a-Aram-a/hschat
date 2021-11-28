package ru.tashchyan.entities

import kotlinx.serialization.Serializable
import ru.tashchyan.DbManipulator

@Serializable
data class UserClient(val userID: Int, val name: String)

@Serializable
data class UserRow(val userID: Int, val name: String, val password: String) {
    fun toUserClient(): UserClient {
        return UserClient(userID, name)
    }
}