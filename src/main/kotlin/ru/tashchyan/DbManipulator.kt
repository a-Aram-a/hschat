package ru.tashchyan

import ru.tashchyan.entities.*
import java.sql.DriverManager

//Манипулирует базой данных по JDBC. Названия методов говорят сами за себя.
object DbManipulator {
    private const val dbhost = "176.99.12.6"
    private const val dbuser = "user"
    private const val dbpass = "#jf8E88*"
    private const val dbname = "hyperskillchat"

    fun selectUserIDByNameAndPassword(name: String, password: String): Int? {
        Class.forName("com.mysql.cj.jdbc.Driver")
        val connection = DriverManager.getConnection("jdbc:mysql://$dbhost/$dbname", dbuser, dbpass)
        //getting user from db
        val sql = "SELECT * FROM users WHERE name = ? AND password = ?;"
        val stmt = connection.prepareStatement(sql)
        stmt.setString(1, name)
        stmt.setString(2, password)
        stmt.execute()
        //getting result UserID
        val result = if (stmt.resultSet.next()) {
            val getUserID = stmt.resultSet.getInt(1)
            getUserID
        } else {
            null
        }
        stmt.close()
        connection.close()
        return result
    }
    fun selectUserIDByName(name: String): Int? {
        Class.forName("com.mysql.cj.jdbc.Driver")
        val connection = DriverManager.getConnection("jdbc:mysql://$dbhost/$dbname", dbuser, dbpass)
        //getting user from db
        val sql = "SELECT * FROM users WHERE name = ?;"
        val stmt = connection.prepareStatement(sql)
        stmt.setString(1, name)
        stmt.execute()
        //getting result User
        val result = if (stmt.resultSet.next()) {
            val getUserID = stmt.resultSet.getInt(1)
            getUserID
        } else {
            null
        }
        stmt.close()
        connection.close()
        return result
    }
    fun selectUserById(userID: Int): UserRow? {
        Class.forName("com.mysql.cj.jdbc.Driver")
        val connection = DriverManager.getConnection("jdbc:mysql://$dbhost/$dbname", dbuser, dbpass)
        //getting user from db
        val sql = "SELECT * FROM users WHERE userID = ?;"
        val stmt = connection.prepareStatement(sql)
        stmt.setInt(1, userID)
        stmt.execute()
        //getting result User
        val result = if (stmt.resultSet.next()) {
            val getUserID = stmt.resultSet.getInt(1)
            val getName = stmt.resultSet.getString(2)
            val getPassword = stmt.resultSet.getString(3)
            UserRow(userID, getName, getPassword)
        } else {
            null
        }
        stmt.close()
        connection.close()
        return result
    }
    fun insertUser(userRow: UserRow): Int {
        Class.forName("com.mysql.cj.jdbc.Driver")
        val connection = DriverManager.getConnection("jdbc:mysql://$dbhost/$dbname", dbuser, dbpass)
        val sql = "INSERT INTO users (name, password) VALUES (?, ?);"
        val stmt = connection.prepareStatement(sql)
        stmt.setString(1, userRow.name)
        stmt.setString(2, userRow.password)
        stmt.execute()
        stmt.close()
        val sql2 = "SELECT LAST_INSERT_ID();"
        val stmt2 = connection.prepareStatement(sql2)
        stmt2.execute()
        stmt2.resultSet.next()
        val userID = stmt2.resultSet.getInt(1)
        stmt2.close()
        connection.close()
        return userID
    }

    fun selectRoomById(roomID: Int): RoomRow? {
        Class.forName("com.mysql.cj.jdbc.Driver")
        val connection = DriverManager.getConnection("jdbc:mysql://$dbhost/$dbname", dbuser, dbpass)

        val sql = "SELECT * FROM rooms WHERE roomID = ?;"
        val stmt = connection.prepareStatement(sql)
        stmt.setInt(1, roomID)
        stmt.execute()

        val result: RoomRow? = if (stmt.resultSet.next()) {
            val getRoomID = stmt.resultSet.getInt(1)
            val getCreatorID = stmt.resultSet.getInt(2)
            val getTitle = stmt.resultSet.getString(3)
            val getPassword = stmt.resultSet.getString(4)
            RoomRow(getRoomID, getCreatorID, getTitle, getPassword)
        } else {
            null
        }
        stmt.close()
        connection.close()
        return result
    }
    fun insertRoom(roomRow: RoomRow): Int {
        Class.forName("com.mysql.cj.jdbc.Driver")
        val connection = DriverManager.getConnection("jdbc:mysql://$dbhost/$dbname", dbuser, dbpass)
        val sql = "INSERT INTO rooms (creatorID, title, password) VALUES (?, ?, ?);"
        val stmt = connection.prepareStatement(sql)
        stmt.setInt(1, roomRow.creatorID)
        stmt.setString(2, roomRow.title)
        stmt.setString(3, roomRow.password)
        stmt.execute()
        stmt.close()
        val sql2 = "SELECT LAST_INSERT_ID();"
        val stmt2 = connection.prepareStatement(sql2)
        stmt2.execute()
        stmt2.resultSet.next()
        val roomID = stmt2.resultSet.getInt(1)
        stmt2.close()
        connection.close()
        return roomID
    }
    fun insertUserRoomConnect(userID: Int, roomID: Int) {
        Class.forName("com.mysql.cj.jdbc.Driver")
        val connection = DriverManager.getConnection("jdbc:mysql://$dbhost/$dbname", dbuser, dbpass)
        val sql = "INSERT INTO users_rooms (userID, roomID) VALUES (?, ?);"
        val stmt = connection.prepareStatement(sql)
        stmt.setInt(1, userID)
        stmt.setInt(2, roomID)
        stmt.execute()
        stmt.close()
        connection.close()
    }
    fun updateRoom(roomRow: RoomRow) {
        Class.forName("com.mysql.cj.jdbc.Driver")
        val connection = DriverManager.getConnection("jdbc:mysql://$dbhost/$dbname", dbuser, dbpass)
        val sql = "UPDATE rooms SET creatorID = ?, title = ?, password = ? WHERE roomID = ?;"
        val stmt = connection.prepareStatement(sql)
        stmt.setInt(1, roomRow.creatorID)
        stmt.setString(2, roomRow.title)
        stmt.setString(3, roomRow.password)
        stmt.setInt(4, roomRow.roomID)
        stmt.execute()
        stmt.close()
        connection.close()
    }
    fun removeUserFromRoom(userID: Int, roomID: Int) {
        Class.forName("com.mysql.cj.jdbc.Driver")
        val connection = DriverManager.getConnection("jdbc:mysql://$dbhost/$dbname", dbuser, dbpass)
        val sql = "DELETE FROM users_rooms WHERE userID = ? AND roomID = ?;"
        val stmt = connection.prepareStatement(sql)
        stmt.setInt(1, userID)
        stmt.setInt(2, roomID)
        stmt.execute()
        stmt.close()
        connection.close()
    }

    fun selectRoomUsers(roomID: Int): List<UserRow> {
        //result for return
        val result = mutableListOf<UserRow>()
        Class.forName("com.mysql.cj.jdbc.Driver")
        val connection = DriverManager.getConnection("jdbc:mysql://$dbhost/$dbname", dbuser, dbpass)

        val sql = """   SELECT users.userID, users.name, users.password
                        FROM users
                        JOIN users_rooms ON users.userID = users_rooms.userID
                        WHERE users_rooms.roomID = ?""".trimMargin()
        val stmt = connection.prepareStatement(sql)
        stmt.setInt(1, roomID)
        stmt.execute()

        while (stmt.resultSet.next()) {
            val getUserID = stmt.resultSet.getInt(1)
            val getName = stmt.resultSet.getString(2)
            val getPassword = stmt.resultSet.getString(3)
            result.add(UserRow(getUserID, getName, getPassword))
        }
        stmt.close()
        connection.close()
        return result.toList()
    }
    fun selectUserRooms(userID: Int): List<RoomRow> {
        //result for return
        val result = mutableListOf<RoomRow>()
        Class.forName("com.mysql.cj.jdbc.Driver")
        val connection = DriverManager.getConnection("jdbc:mysql://$dbhost/$dbname", dbuser, dbpass)

        val sql = """   SELECT rooms.roomID, rooms.creatorID, rooms.title, rooms.password
                        FROM rooms
                        JOIN users_rooms ON rooms.roomID = users_rooms.roomID
                        WHERE users_rooms.userID = ?""".trimMargin()
        val stmt = connection.prepareStatement(sql)
        stmt.setInt(1, userID)
        stmt.execute()

        while (stmt.resultSet.next()) {
            val getRoomID = stmt.resultSet.getInt(1)
            val getCreatorID = stmt.resultSet.getInt(2)
            val getTitle = stmt.resultSet.getString(3)
            val getPassword = stmt.resultSet.getString(4)
            result.add(RoomRow(getRoomID, getCreatorID, getTitle, getPassword))
        }
        stmt.close()
        connection.close()
        return result.toList()
    }
    fun isUserInRoom(userID: Int, roomID: Int): Boolean {
        Class.forName("com.mysql.cj.jdbc.Driver")
        val connection = DriverManager.getConnection("jdbc:mysql://$dbhost/$dbname", dbuser, dbpass)

        val sql = "SELECT COUNT(*) FROM users_rooms WHERE userID = ? AND roomID = ?;"
        val stmt = connection.prepareStatement(sql)
        stmt.setInt(1, userID)
        stmt.setInt(2, roomID)
        stmt.execute()

        stmt.resultSet.next()
        val getCount= stmt.resultSet.getInt(1)
        stmt.close()
        connection.close()
        return getCount != 0
    }

    fun insertMessage(messageRow: MessageRow): Int {
        Class.forName("com.mysql.cj.jdbc.Driver")
        val connection = DriverManager.getConnection("jdbc:mysql://$dbhost/$dbname", dbuser, dbpass)
        val sql = "INSERT INTO messages (senderID, roomID, text, date) VALUES (?, ?, ?, ?);"
        val stmt = connection.prepareStatement(sql)
        stmt.setInt(1, messageRow.senderID)
        stmt.setInt(2, messageRow.roomID)
        stmt.setString(3, messageRow.text)
        stmt.setLong(4, messageRow.date)
        stmt.execute()
        stmt.close()
        val sql2 = "SELECT LAST_INSERT_ID();"
        val stmt2 = connection.prepareStatement(sql2)
        stmt2.execute()
        stmt2.resultSet.next()
        val messageID = stmt2.resultSet.getInt(1)
        stmt2.close()
        connection.close()
        return messageID
    }
    fun selectRoomMessages(roomID: Int, laterThanID: Int = 0): List<MessageRow> {
        val result = mutableListOf<MessageRow>()
        Class.forName("com.mysql.cj.jdbc.Driver")
        val connection = DriverManager.getConnection("jdbc:mysql://$dbhost/$dbname", dbuser, dbpass)

        val sql = "SELECT * FROM messages WHERE roomID = ? AND messageID > ?;"
        val stmt = connection.prepareStatement(sql)
        stmt.setInt(1, roomID)
        stmt.setInt(2, laterThanID)
        stmt.execute()

        while (stmt.resultSet.next()) {
            val getMessageID = stmt.resultSet.getInt(1)
            val getSenderID = stmt.resultSet.getInt(2)
            val getRoomID = stmt.resultSet.getInt(3)
            val getText = stmt.resultSet.getString(4)
            val getDate = stmt.resultSet.getLong(5)
            result.add(MessageRow(getMessageID, getSenderID, getRoomID, getText, getDate))
        }
        stmt.close()
        connection.close()
        return result.toList()
    }
}