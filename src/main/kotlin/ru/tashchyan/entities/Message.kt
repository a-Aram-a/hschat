package ru.tashchyan.entities
import kotlinx.serialization.Serializable

@Serializable
data class MessageClient(val messageID: Int, val senderID: Int, val roomID: Int, val text: String, val date: Long)

@Serializable
data class MessageRow(val messageID: Int, val senderID: Int, val roomID: Int, val text: String, val date: Long) {
    fun toMessageClient(): MessageClient {
        return MessageClient(messageID, senderID, roomID, text, date)
    }
}