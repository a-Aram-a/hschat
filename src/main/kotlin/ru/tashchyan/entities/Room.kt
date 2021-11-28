package ru.tashchyan.entities
import kotlinx.serialization.Serializable
import ru.tashchyan.DbManipulator

@Serializable
data class RoomClient(val roomID: Int, val creatorID: Int, val title: String)

@Serializable
data class RoomRow(val roomID: Int, val creatorID: Int, val title: String, val password: String) {
    fun toRoomClient(): RoomClient {
        return RoomClient(roomID, creatorID, title)
    }
}