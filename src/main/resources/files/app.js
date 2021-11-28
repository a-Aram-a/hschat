var isAuthed = false
var isOnAuthPage = false
var isInHomePage = false
var isInRoom = false
var needUpdateMessages = false
var inRoomCreatorID = 0
var inRoomID = 0
var inRoomTitle = ""
var inRoomAdmin = false

if(localStorage.getItem("authed") === "true" ) {
    isAuthed = true
}

setInterval(function(){
    if(!isAuthed && !isOnAuthPage) {
        showAuthForm()
        isOnAuthPage = true
    }
    if(isAuthed && !isInHomePage) {
        loadRooms()
        isInHomePage = true
    }
    if(isInRoom) {
        updateMessages()
        updateRoomUsers()
    }
}, 500)

function loadRooms() {
    $.ajax({
        url: '/api/room_get',
        method: 'post',
        cache: false,
        data: {request: JSON.stringify(
                {
                    "auth_token": localStorage.getItem("auth_token"),
                })},
        success: function(data){
            data = JSON.parse(data)
            if(data.success) {
                showHomePage(data.rooms)
            } else {
                alert(data.error)
                if(data.error == "Invalid auth_token") {
                    localStorage.clear()
                    window.location.reload();
                }
            }
        }
    });
}

function showAuthForm() {
    var authDiv = document.createElement("div")

    var authDivTitle = document.createElement("h2")
    authDivTitle.innerHTML = "Auth"
    authDiv.appendChild(authDivTitle)

    var loginForm = document.createElement("form")
    var nameInput_login = document.createElement("input")
    nameInput_login.type = "text"
    nameInput_login.placeholder = "name"
    var passwordInput_login = document.createElement("input")
    passwordInput_login.type = "password"
    passwordInput_login.placeholder = "password"
    var sendButton_login = document.createElement("input")
    sendButton_login.type = "submit"
    sendButton_login.value = "Login"
    loginForm.appendChild(nameInput_login)
    loginForm.appendChild(passwordInput_login)
    loginForm.appendChild(sendButton_login)
    authDiv.appendChild(loginForm)

    authDiv.appendChild(document.createElement("br"))

    var registerForm = document.createElement("form")
    var nameInput_register = document.createElement("input")
    nameInput_register.type = "text"
    nameInput_register.placeholder = "name"
    var passwordInput_register = document.createElement("input")
    passwordInput_register.type = "password"
    passwordInput_register.placeholder = "password"
    var passwordInput2_register = document.createElement("input")
    passwordInput2_register.type = "password"
    passwordInput2_register.placeholder = "repeat password"
    var sendButton_register = document.createElement("input")
    sendButton_register.type = "submit"
    sendButton_register.value = "Register"
    registerForm.appendChild(nameInput_register)
    registerForm.appendChild(passwordInput_register)
    registerForm.appendChild(passwordInput2_register)
    registerForm.appendChild(sendButton_register)
    authDiv.appendChild(registerForm)

    document.getElementById("container").textContent = ""
    document.getElementById("container").appendChild(authDiv)

    loginForm.onsubmit = function(e) {
        $.ajax({
            url: '/api/auth_login',
            method: 'post',
            cache: false,
            data: {request: JSON.stringify(
                {
                    "name": nameInput_login.value,
                    "password": passwordInput_login.value
                })},
            success: function(data){
                data = JSON.parse(data)
                if(data.success) {
                    isAuthed = true
                    isOnAuthPage = false
                    localStorage.setItem('auth_token', data.auth_token)
                    localStorage.setItem('userID', data.user.userID)
                    localStorage.setItem('name', data.user.name)
                    localStorage.setItem('authed', "true")
                } else {
                    alert(data.error)
                    if(data.error == "Invalid auth_token") {
                        localStorage.clear()
                        window.location.reload();
                    }
                }
            }
        });
        return false
    }

    registerForm.onsubmit = function(e) {
        if(passwordInput_register.value !== passwordInput2_register.value) {
            alert("Passwords don't match")
            return false
        }
        $.ajax({
            url: '/api/auth_register',
            method: 'post',
            cache: false,
            dataType: 'html',
            data: {request: JSON.stringify(
                    {
                        "name": nameInput_register.value,
                        "password": passwordInput_register.value
                    })},
            success: function(data){
                data = JSON.parse(data)
                if(data.success) {
                    nameInput_login.value = nameInput_register.value
                    passwordInput_login.value = passwordInput_register.value
                    loginForm.onsubmit()
                } else {
                    alert(data.error)
                    if(data.error == "Invalid auth_token") {
                        localStorage.clear()
                        window.location.reload();
                    }
                }
            }
        });
        return false
    }
}

function showHomePage(rooms) {
    var homeDiv = document.createElement("div")
    var joinRoomForm = document.createElement("form")
    var createRoomForm = document.createElement("form")
    var roomsDiv = document.createElement("div")

    var homeDivTitle = document.createElement("h2")
    homeDivTitle.innerHTML = "Hello, " + localStorage.getItem("name")
    homeDiv.appendChild(homeDivTitle)

    var roomIDInput_join = document.createElement("input")
    roomIDInput_join.type = "text"
    roomIDInput_join.placeholder = "roomID to join"
    var roomPasswordInput_join = document.createElement("input")
    roomPasswordInput_join.type = "password"
    roomPasswordInput_join.placeholder = "room's password"
    var roomButton_join = document.createElement("input")
    roomButton_join.type = "submit"
    roomButton_join.value = "Join room"
    joinRoomForm.appendChild(roomIDInput_join)
    joinRoomForm.appendChild(roomPasswordInput_join)
    joinRoomForm.appendChild(roomButton_join)
    homeDiv.appendChild(joinRoomForm)

    homeDiv.appendChild(document.createElement("br"))

    var roomTitleInput_create = document.createElement("input")
    roomTitleInput_create.type = "text"
    roomTitleInput_create.placeholder = "create title of room"
    var roomPasswordInput_create = document.createElement("input")
    roomPasswordInput_create.type = "password"
    roomPasswordInput_create.placeholder = "create room's password"
    var roomButton_create = document.createElement("input")
    roomButton_create.type = "submit"
    roomButton_create.value = "Create room"
    createRoomForm.appendChild(roomTitleInput_create)
    createRoomForm.appendChild(roomPasswordInput_create)
    createRoomForm.appendChild(roomButton_create)
    homeDiv.appendChild(createRoomForm)

    roomButton_join.onclick = function() {
        $.ajax({
            url: '/api/room_join',
            method: 'post',
            cache: false,
            data: {request: JSON.stringify(
                    {
                        "auth_token": localStorage.getItem("auth_token"),
                        "roomID": roomIDInput_join.value,
                        "password": roomPasswordInput_join.value
                    })},
            success: function(data){
                data = JSON.parse(data)
                if(data.success) {
                    loadRooms()
                } else {
                    alert(data.error)
                    if(data.error == "Invalid auth_token") {
                        localStorage.clear()
                        window.location.reload();
                    }
                }
            }
        });
        return false
    }
    roomButton_create.onclick = function() {
        $.ajax({
            url: '/api/room_create',
            method: 'post',
            cache: false,
            data: {request: JSON.stringify(
                    {
                        "auth_token": localStorage.getItem("auth_token"),
                        "title": roomTitleInput_create.value,
                        "password": roomPasswordInput_create.value
                    })},
            success: function(data){
                data = JSON.parse(data)
                if(data.success) {
                    loadRooms()
                } else {
                    alert(data.error)
                    if(data.error == "Invalid auth_token") {
                        localStorage.clear()
                        window.location.reload();
                    }
                }
            }
        });
        return false
    }

    var roomsDivTitle = document.createElement("h2")
    roomsDivTitle.innerHTML = "Rooms: "
    roomsDiv.appendChild(roomsDivTitle)
    if(rooms)
        for (var i = 0; i < rooms.length; i++) {
            var roomElement = document.createElement("button")
            roomElement.innerHTML = "Go to '" + rooms[i].title + "' (roomID:" + rooms[i].roomID + ")"
            roomElement.creatorID = rooms[i].creatorID
            roomElement.title = rooms[i].title
            roomElement.roomID = rooms[i].roomID
            roomElement.onclick = function() {
                loadRoom(this.roomID, this.creatorID, this.title)
            }
            var exitButton = document.createElement("input")
            exitButton.type = "button"
            exitButton.value = "(X)leave this room"
            exitButton.roomID = rooms[i].roomID
            exitButton.onclick = function() {
                leaveRoom(this.roomID)
            }
            roomsDiv.appendChild(roomElement)
            roomsDiv.appendChild(exitButton)
            roomsDiv.appendChild(document.createElement("br"))
        }
    homeDiv.appendChild(roomsDiv)

    document.getElementById("container").textContent = ""
    document.getElementById("container").appendChild(homeDiv)
}

function leaveRoom(roomID) {
    $.ajax({
        url: '/api/room_leave',
        method: 'post',
        cache: false,
        data: {request: JSON.stringify(
                {
                    "auth_token": localStorage.getItem("auth_token"),
                    "roomID": roomID,
                })},
        success: function(data){
            data = JSON.parse(data)
            if(data.success) {
                loadRooms()
            } else {
                alert(data.error)
                if(data.error == "Invalid auth_token") {
                    localStorage.clear()
                    window.location.reload();
                }
            }
        }
    });
}

function loadRoom(roomID, creatorID, roomTitle) {
    isInRoom = true
    inRoomID = roomID
    inRoomTitle = roomTitle
    inRoomCreatorID = creatorID
    inRoomAdmin = (creatorID == localStorage.getItem("userID"))

    loadWsConnection()

    var roomDiv = document.createElement("div")

    var roomHeader = document.createElement("div")
    roomHeader.id = "room_header"
    var roomHeaderTitle = document.createElement("h2")
    roomHeader.appendChild(roomHeaderTitle)
    var roomUsersDiv = document.createElement("div")
    roomUsersDiv.id = "roomusersdiv"
    roomHeader.appendChild(roomUsersDiv)

    if(inRoomAdmin) {
        var adminPanel = document.createElement("div")
        var adminPanelHeader = document.createElement("h2")
        adminPanelHeader.innerHTML = "admin panel"
        adminPanel.appendChild(adminPanelHeader)

        var roomChangeForm = document.createElement("from")
        var roomChange_creatorID = document.createElement("input")
        roomChange_creatorID.type = "text"
        roomChange_creatorID.placeholder = "new room's creatorID"
        var roomChange_title = document.createElement("input")
        roomChange_title.type = "text"
        roomChange_title.placeholder = "new room's title"
        var roomChange_password = document.createElement("input")
        roomChange_password.type = "password"
        roomChange_password.placeholder = "new room's password"
        var roomChange_button = document.createElement("input")
        roomChange_button.type = "submit"
        roomChange_button.value = "change room"
        roomChangeForm.appendChild(roomChange_creatorID)
        roomChangeForm.appendChild(roomChange_title)
        roomChangeForm.appendChild(roomChange_password)
        roomChangeForm.appendChild(roomChange_button)
        adminPanel.appendChild(roomChangeForm)
        roomChange_button.onclick = function() {
            changeRoom(roomChange_creatorID.value, roomChange_title.value, roomChange_password.value)
            return false
        }


        adminPanel.appendChild(document.createElement("br"))

        var kickForm = document.createElement("from")
        var kickID_input = document.createElement("input")
        kickID_input.type = "text"
        kickID_input.placeholder = "userID to kick"
        var kick_button = document.createElement("input")
        kick_button.type = "submit"
        kick_button.value = "kick user"
        kickForm.appendChild(kickID_input)
        kickForm.appendChild(kick_button)
        adminPanel.appendChild(kickForm)
        kick_button.onclick = function() {
            kickUser(kickID_input.value)
            return false
        }
        roomHeader.appendChild(adminPanel)
    }

    var goBack = document.createElement("a")
    goBack.href = "/"
    goBack.innerHTML = "Go back"
    roomHeader.appendChild(goBack)
    roomDiv.appendChild(roomHeader)

    var messagesDiv = document.createElement("div")
    messagesDiv.id = "messages_div"
    roomHeaderTitle.innerHTML = "Room: " + roomTitle
    roomDiv.appendChild(messagesDiv)

    var sendMessageForm = document.createElement("form")
    var sendMessageInput = document.createElement("input")
    sendMessageInput.type = "text"
    sendMessageInput.placeholder = "your message"
    var sendMessageButton = document.createElement("input")
    sendMessageButton.type = "submit"
    sendMessageButton.value = "send"
    sendMessageForm.appendChild(sendMessageInput)
    sendMessageForm.appendChild(sendMessageButton)
    roomDiv.appendChild(sendMessageForm)

    sendMessageForm.onsubmit = function() {
        $.ajax({
            url: '/api/messages_send',
            method: 'post',
            cache: false,
            data: {request: JSON.stringify(
                    {
                        "auth_token": localStorage.getItem("auth_token"),
                        "roomID": inRoomID,
                        "text": sendMessageInput.value
                    })},
            success: function(data){
                data = JSON.parse(data)
                if(data.success) {

                } else {
                    alert(data.error)
                    if(data.error == "Invalid auth_token") {
                        localStorage.clear()
                        window.location.reload();
                    }
                }
            }
        });
        sendMessageInput.value = ""
        return false
    }

    document.getElementById("container").textContent = ""
    document.getElementById("container").appendChild(roomDiv)
    loadRoomUsers(roomID)
    loadRoomMessages(roomID)
}
function changeRoom(creatorID, title, password) {
    $.ajax({
        url: '/api/room_change',
        method: 'post',
        cache: false,
        data: {request: JSON.stringify(
                {
                    "auth_token": localStorage.getItem("auth_token"),
                    "roomID": inRoomID,
                    "creatorID": creatorID,
                    "title": title,
                    "password": password
                })},
        success: function(data) {
            data = JSON.parse(data)
            if(data.success) {
                alert("Room was updated!")
                window.location.reload();
            } else {
                alert(data.error)
                if(data.error == "Invalid auth_token") {
                    localStorage.clear()
                    window.location.reload();
                }
            }
        }
    });
}
function kickUser(userID) {
    $.ajax({
        url: '/api/room_kick',
        method: 'post',
        cache: false,
        data: {request: JSON.stringify(
                {
                    "auth_token": localStorage.getItem("auth_token"),
                    "roomID": inRoomID,
                    "userID": userID
                })},
        success: function(data) {
            data = JSON.parse(data)
            if(data.success) {
                alert("User was kicked!")
                window.location.reload();
            } else {
                alert(data.error)
                if(data.error == "Invalid auth_token") {
                    localStorage.clear()
                    window.location.reload();
                }
            }
        }
    });
}


var roomUsers = []
var roomAnotherUsers = []
var roomMesages = []
function loadRoomUsers(roomID) {
    $.ajax({
        url: '/api/room_getusers',
        method: 'post',
        cache: false,
        data: {request: JSON.stringify(
                {
                    "auth_token": localStorage.getItem("auth_token"),
                    "roomID": roomID
                })},
        success: function(data){
            data = JSON.parse(data)
            if(data.success) {
                roomUsers = roomUsers.concat(data.users)
            } else {
                alert(data.error)
                if(data.error == "Invalid auth_token") {
                    localStorage.clear()
                    window.location.reload();
                }
            }
        }
    });
}
function loadRoomMessages(roomID) {
    $.ajax({
        url: '/api/messages_get',
        method: 'post',
        cache: false,
        data: {request: JSON.stringify(
                {
                    "auth_token": localStorage.getItem("auth_token"),
                    "roomID": roomID,
                })},
        success: function(data){
            data = JSON.parse(data)
            if(data.success) {
                roomMesages = roomMesages.concat(data.messages)
            } else {
                alert(data.error)
                if(data.error == "Invalid auth_token") {
                    localStorage.clear()
                    window.location.reload();
                }
            }
        }
    });
}
function loadWsConnection() {
    var socket = new WebSocket("ws://127.0.0.1:8080/api/events?auth_token="+localStorage.getItem("auth_token"));
    socket.onopen = function() {
        alert("Ws cоединение установлено.");
    };
    socket.onclose = function(event) {
        if (event.wasClean) {
            alert('Соединение закрыто чисто');
        } else {
            alert('Обрыв соединения'); // например, "убит" процесс сервера
        }
        alert('Код: ' + event.code + ' причина: ' + event.reason);
    };
    socket.onmessage = function(event) {
        var data = JSON.parse(event.data)
        roomMesages.push(data.message)
    };
    socket.onerror = function(error) {
        alert("Ошибка " + error.message);
    };
}

function updateRoomUsers() {
    if(!document.getElementById("roomusersdiv")) return
    document.getElementById("roomusersdiv").textContent = ""
    var roomUsersElement = document.createElement("h3")
    roomUsersElement.innerHTML = "Participants: "
    for(var i = 0; i < roomUsers.length; i++) {
        if(inRoomCreatorID != roomUsers[i].userID)
            roomUsersElement.innerHTML += roomUsers[i].name + "#id:"+roomUsers[i].userID + "|"
        else
            roomUsersElement.innerHTML += "<i>" + roomUsers[i].name + "#id:"+ roomUsers[i].userID + "(creator)</i>|"
    }
    document.getElementById("roomusersdiv").appendChild(roomUsersElement)
}
function updateMessages() {
    if(!document.getElementById("messages_div")) return
    document.getElementById("messages_div").textContent = ""
    for(var i = 0; i< roomMesages.length; i++) {
        var messageElement = document.createElement("div")
        messageElement.innerHTML = "<b>" + getNameById(roomMesages[i].senderID) + "</b>: " + roomMesages[i].text + " <i>(" + new Date(roomMesages[i].date * 1000) +")</i>"
        document.getElementById("messages_div").appendChild(messageElement)
    }
}
function getNameById(userID) {
    for(var i = 0; i < roomUsers.length; i++) {
        if(roomUsers[i].userID === userID)
            return roomUsers[i].name
        else {
            getUserById(userID)
        }
    }
    for(var i = 0; i < roomAnotherUsers.length; i++) {
        if(roomAnotherUsers[i].userID === userID)
            return roomAnotherUsers[i].name + "(not in the room)"
    }
    return "User#id:" + userID
}

function getUserById(userID) {
    for(var i = 0; i < roomUsers.length; i++) {
        if(roomUsers[i].userID === userID) {
            return
        }
    }
    for(var i = 0; i < roomAnotherUsers.length; i++) {
        if(roomAnotherUsers[i].userID === userID)
            return
    }
    $.ajax({
        url: '/api/user_getById ',
        method: 'post',
        cache: false,
        data: {request: JSON.stringify(
                {
                    "auth_token": localStorage.getItem("auth_token"),
                    "userID": userID,
                })},
        success: function(data){
            data = JSON.parse(data)
            if(data.success) {
                roomAnotherUsers.push(data.user)
            } else {
                alert(data.error)
                if(data.error == "Invalid auth_token") {
                    localStorage.clear()
                    window.location.reload();
                }
            }
        }
    });
}
