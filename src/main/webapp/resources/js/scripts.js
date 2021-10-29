/**
 * Created by Gennady Trubach on 03.03.2015.
 */
'use strict';

var chatState = {
    chatUrl: "chat",
    currentUser: {
        id: null,
        name: null
    },
    messageList: [],
    isEditing: false,
    isAvailable: false
};

function run() {
    var appContainer = document.getElementsByClassName('wrapper')[0];
    appContainer.addEventListener('click', delegateEvent);
    appContainer.addEventListener('keydown', delegateEvent);
    var currentUser = restoreCurrentUser();
    setCurrentUser(currentUser);
    restoreMessages();
}

function setCurrentUser(user) {
    if (user != null) {
        var userName = document.getElementById('sign-name');
        userName.value = user.name;
        onSignInClick();
    }
}

function delegateEvent(evtObj) {
    if (evtObj.type === 'click') {
        if (evtObj.target.classList.contains('sign-button')) {
            onSignClick(evtObj.target);
        }
        if (evtObj.target.classList.contains('send-button')) {
            onMessageSend();
        }
        if (evtObj.target.classList.contains('tools-button')) {
            onMessageEdit(evtObj.target);
        }
        if(evtObj.target.classList.contains('box-scroll')) {
            scrollDown();
        }
    }
    if (evtObj.type === 'keydown' && evtObj.ctrlKey && evtObj.keyCode == 13) {
        if (evtObj.target.classList.contains('send-message')) {
            onMessageSend();
        }
        if (evtObj.target.classList.contains('message-edit-text')) {
            onMessageEdit(evtObj.target);
        }
    }
}

function onSignClick(button) {
    if (button.id == 'sign-in') {
        onSignInClick();
    }
    if (button.id == 'sign-edit') {
        onSignEditClick();
    }
    if (button.id == 'sign-confirm') {
        onSignConfirmClick();
    }
    if (button.id == 'sign-out') {
        onSignOutClick();
    }
}

function sendActivator(activate) {
    var sendMessage = document.getElementsByClassName('send-message')[0];
    var messageText = sendMessage.firstElementChild;
    messageText.disabled = !activate;
    if (activate == true) {
        messageText.value = '';
    }
    else {
        messageText.value = 'You should to sign in!';
    }
    sendMessage.lastElementChild.disabled = !activate;
}

function inputChecker(text) {
    if (text == '' || text.trim() == '') {
        alert("Check your input!");
        return false;
    }
    return true;
}

function createSignStructure(type) {
    var sign = document.getElementsByClassName('sign')[0];
    var htmlAsText;
    if (type == 'read') {
        htmlAsText = '<xmp id="user-name">' + chatState.currentUser.name + '</xmp>'
        + '<button id="sign-out" class="chat-button sign-button">Sign Out</button>'
        + '<button id="sign-edit" class="chat-button sign-button">Edit</button>';
    }
    if (type == 'modify') {
        htmlAsText = '<input id="sign-name" type="text" maxlength="25">'
        + '<button id="sign-out" class="chat-button sign-button">Sign Out</button>'
        + '<button id="sign-confirm" class="chat-button sign-button">Confirm</button>';
    }
    if (type == 'out') {
        htmlAsText = '<input id="sign-name" type="text" maxlength="25">'
        + '<button id="sign-in" class="chat-button sign-button">Sign in</button>';
    }
    sign.innerHTML = htmlAsText;
}

function onSignInClick() {
    var name = document.getElementById('sign-name');
    if (inputChecker(name.value) == true) {
        $.ajax({
            method: "POST",
            url: "user",
            data: JSON.stringify({
                name: name.value
            }),
            contentType: "application/json; charset=UTF-8",
            success: function () {
                getUser(name.value);
            },
            error: function () {
                serverAvailable(false);
                restoreMessages();
            }
        });
    }
    else {
        name.focus();
    }
}

function onSignEditClick() {
    createSignStructure('modify');
    var name = document.getElementById('sign-name');
    name.value = chatState.currentUser.name;
    name.focus();
    sendActivator(false);
}

function makeSignConfirm() {
    storeCurrentUser(chatState.currentUser);
    createSignStructure('read');
    sendActivator(true);
    createOrUpdateMessages(chatState.messageList);
}

function onSignConfirmClick() {
    var name = document.getElementById('sign-name');
    if (inputChecker(name.value) == true) {
        if (chatState.currentUser.name === name.value) {
            getUser(name.value);
            return;
        }
        $.ajax({
            method: "PUT",
            url: "user",
            data: JSON.stringify({
                id: chatState.currentUser.id,
                name: name.value
            }),
            contentType: "application/json; charset=UTF-8",
            success: function () {
                getUser(name.value);
            },
            error: function (error) {
                if (error.status == 401) {
                    alert("Incorrect name!");
                    name.focus();
                }
                else {
                    serverAvailable(false);
                    restoreMessages();
                }
            }
        });
    }
    else {
        name.focus();
    }
}

function onSignOutClick() {
    createSignStructure('out');
    chatState.currentUser.name = null;
    localStorage.removeItem("Current user");
    createOrUpdateMessages(chatState.messageList);
    sendActivator(false);
}

function getUser(name) {
    $.ajax({
        url: "user",
        data: {
            name: name
        },
        success: function (data) {
            serverAvailable(true);
            chatState.currentUser.id = data.user.id;
            chatState.currentUser.name = data.user.name;
            makeSignConfirm();
        },
        error: function () {
            serverAvailable(false);
            restoreMessages();
        },
        cache: false,
        dataType: "json"
    });
}

function onMessageSend() {
    var messageText = document.getElementById('message-text');
    if (inputChecker(messageText.value) == true) {
        $.ajax({
            method: "POST",
            url: chatState.chatUrl,
            data: JSON.stringify({
                senderName: chatState.currentUser.name,
                messageText: messageText.value.trim().replace(new RegExp("\n", 'g'), "\\n")
            }),
            contentType: "application/json; charset=UTF-8",
            error: function () {
                serverAvailable(false);
                restoreMessages();
            }
        });
        messageText.value = '';
    }
    else {
        messageText.focus();
    }
}

function scrollDown() {
    var chatBox = document.getElementsByClassName('chat-box')[0];
    chatBox.scrollTop = chatBox.scrollHeight;
}

function isScrollInBottom() {
    var chatBox = document.getElementsByClassName('chat-box')[0];
    return $(chatBox)[0].scrollHeight - $(chatBox).scrollTop() == $(chatBox).outerHeight()
}

function addMessage(message) {
    var isInBottom = isScrollInBottom();
    var item = createMessage(message);
    var chatBox = document.getElementsByClassName('chat-box')[0];
    chatState.messageList.push(message);
    chatBox.appendChild(item);
    if (isInBottom && !chatState.isEditing) {
        scrollDown();
    }
}

function createMessage(message) {
    var item = document.createElement('div');
    var sendDate = '(' + message.sendDate + ')';
    item.innerHTML = '<div class="message sender-name">' + sendDate + ' ' + message.senderName + '</div>'
    + '<xmp class="message message-item">' + message.messageText + '</xmp>';
    item.setAttribute('class', 'message');
    item.setAttribute('id', message.id);
    updateMessage(item, message);
    return item;
}

function updateMessage(divMessage, message) {
    divMessage.children[0].innerHTML =  '(' + message.sendDate + ') ' + message.senderName;
    if (message.isDeleted == 'true') {
        setDelete(divMessage, message);
        return;
    }
    if(message.modifyDate != 'not modified') {
        setModify(divMessage, message);
    }
    if (chatState.currentUser.name != undefined && message.senderName.toLowerCase() === chatState.currentUser.name.toLowerCase()) {
        addTool(divMessage);
    }
    else {
        removeTool(divMessage);
    }
}

function setDelete(divMessage, message) {
    divMessage.innerHTML = '<div class="message sender-name">(' + message.sendDate + ') '
    + message.senderName + '</div>' + '<p class="modify">deleted</p>';
}

function setModify(divMessage, message) {
    var isInBottom = isScrollInBottom();
    var modify = divMessage.getElementsByClassName('modify')[0];
    var messageItem = divMessage.getElementsByClassName('message message-item')[0];
    messageItem.innerHTML = message.messageText;
    if (modify === undefined) {
        modify = document.createElement('p');
        modify.setAttribute('class', 'modify');
        modify.setAttribute('id', 'modify-edit');
        divMessage.appendChild(modify);
    }
    modify.innerHTML = 'Message was modified on ' + message.modifyDate;
    if (isInBottom && !chatState.isEditing) {
        scrollDown();
    }
}

function addTool(divMessage) {
    var tools = divMessage.getElementsByClassName('tools')[0];
    if (tools === undefined) {
        var positionAfter = divMessage.getElementsByClassName('message-item')[0];
        var item = document.createElement('div');
        item.innerHTML = '<button id="message-delete" class="message tools-button">delete</button>'
        + '<button id="message-edit" class="message tools-button">edit</button>';
        item.setAttribute('class', 'message tools');
        divMessage.insertBefore(item, positionAfter);
    }
}

function removeTool(divMessage) {
    var tools = divMessage.getElementsByClassName('tools')[0];
    if (tools != undefined) {
        divMessage.removeChild(tools);
    }
}

function onMessageEdit(item) {
    if (item.id == 'tools-confirm') {
        onMessageConfirmClick(item.parentElement);
    }
    if(item.classList.contains('message-edit-text')) {
        var editTool = item.parentElement.getElementsByClassName('tools')[0];
        onMessageConfirmClick(editTool);
    }
    if (item.id == 'message-edit') {
        onMessageEditClick(item.parentElement);
    }
    if (item.id == 'message-delete') {
        onMessageDelete(item.parentElement.parentElement);
    }
}

function makeToEdit(divMessage, type) {
    var message;
    var item;
    var text;
    if (type == 'edit') {
        sendActivator(false);
        chatState.isEditing = true;
        message = divMessage.getElementsByClassName('message-item')[0];
        item = document.createElement('textarea');
        item.setAttribute('class', 'message message-edit-text');
        item.value = message.innerHTML.trim().replace(new RegExp("\\n", 'g'), "\n");
    }
    if (type == 'read') {
        sendActivator(true);
        chatState.isEditing = false;
        message = divMessage.getElementsByClassName('message-edit-text')[0];
        if (inputChecker(message.value) == false) {
            return "";
        }
        item = document.createElement('xmp');
        item.setAttribute('class', 'message message-item');
        text = message.value.trim().replace(new RegExp("\n", 'g'), "\\n");
        item.innerHTML = text;
    }
    divMessage.replaceChild(item, message);
    item.focus();
    return text;
}

function toolsButtonsChange(type) {
    var button = document.createElement('button');
    button.setAttribute('class', 'message tools-button');
    if (type == 'edit') {
        button.setAttribute('id', 'message-edit');
        button.innerHTML = 'edit';
    }
    if (type == 'confirm') {
        button.setAttribute('id', 'tools-confirm');
        button.innerHTML = 'OK';
    }
    return button;
}

function onMessageEditClick(tools) {
    var divMessage = tools.parentElement;
    makeToEdit(divMessage, 'edit');
    tools.removeChild(tools.lastChild);
    tools.appendChild(toolsButtonsChange('confirm'));
}

function onMessageConfirmClick(tools) {
    var divMessage = tools.parentElement;
    var text = makeToEdit(divMessage, 'read');
    if (text != "") {
        tools.removeChild(tools.lastChild);
        tools.appendChild(toolsButtonsChange('edit'));
        var id = divMessage.attributes['id'].value;
        $.ajax({
            method: "PUT",
            url: chatState.chatUrl,
            data: JSON.stringify({
                id: id,
                messageText: text
            }),
            contentType: "application/json; charset=UTF-8",
            error: function () {
                serverAvailable(false);
                restoreMessages();
            }
        });
    }
}

function onMessageDelete(divMessage) {
    var id = divMessage.attributes['id'].value;
    $.ajax({
        method: "DELETE",
        url: chatState.chatUrl,
        data: JSON.stringify({
            id: id
        }),
        contentType: "application/json; charset=UTF-8",
        error: function () {
            serverAvailable(false);
            restoreMessages();
        }
    });
    sendActivator(true);
}

function storeCurrentUser(user) {
    localStorage.removeItem("Current user");
    localStorage.setItem("Current user", JSON.stringify(user));
}

function restoreCurrentUser() {
    var currentUser = localStorage.getItem("Current user");
    return currentUser && JSON.parse(currentUser);
}

function restoreMessages() {
    $.ajax({
        url: "restore",
        success: function (data) {
            getHistory(data);
            serverAvailable(true);
        },
        error: function () {
            serverAvailable(false);
            restoreMessages();
        },
        cache: false,
        dataType: "json"
    });
}

(function poll() {
    $.ajax({
        url: chatState.chatUrl,
        success: function (data) {
            getHistory(data);
            serverAvailable(true);
        },
        error: function (error) {
            if (error.statusText != "timeout") {
                serverAvailable(false);
                restoreMessages();
            }
        },
        cache: false,
        dataType: "json",
        complete: poll,
        timeout: 30000
    });
})();

function getHistory(response) {
    createOrUpdateMessages(response.messages);
}

function createOrUpdateMessages(messages) {
    var chatBox = document.getElementsByClassName('chat-box')[0];
    for (var i = 0; i < messages.length; i++) {
        var index = findMessageIndexById(messages[i].id);
        if (index > -1) {
            chatState.messageList[index] = messages[i];
            updateMessage(chatBox.children[index], messages[i]);
        }
        else {
            addMessage(messages[i]);
        }
    }
}

function findMessageIndexById(id) {
    for (var i = 0; i < chatState.messageList.length; i++) {
        if (chatState.messageList[i].id == id) {
            return i;
        }
    }
    return -1;
}

function serverAvailable(newCondition) {
    if (chatState.isAvailable != newCondition) {
        availableSwitcher(newCondition)
    }
}

function availableSwitcher(newCondition) {
    var indicator = document.getElementsByClassName('indication-circle')[0];
    if (newCondition) {
        indicator.style.background = "#92D36E";
    }
    else {
        indicator.style.background = "#E61610";
    }
    chatState.isAvailable = newCondition;
}
