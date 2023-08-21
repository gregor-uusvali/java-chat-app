'use strict';

const loginPage = document.querySelector('#login-page');
const registerPage = document.getElementById("register-page")
const chatPage = document.querySelector('#chat-page');
const loginForm = document.querySelector('#loginForm');
const registerForm = document.querySelector('#registerForm');
const messageForm = document.querySelector('#messageForm');
const messageInput = document.querySelector('#message');
const messageArea = document.querySelector('#messageArea');
const connectingElement = document.querySelector('.connecting');
const linkToRegisterBtn = document.getElementById("link-to-register")
const linkToLoginPageBtn = document.getElementById('link-to-login')

let stompClient = null;
let username = null;


let colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

function connect(event) {
    username = document.querySelector('#login-name').value.trim();

    if(username) {
        loginPage.classList.add('hidden');
        chatPage.classList.remove('hidden');

        let socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, onConnected, onError);
    }
    event.preventDefault();
}

function register(event) {
    event.preventDefault();
    const new_username = document.getElementById('register-name').value;
    const email = document.getElementById('register-email').value;
    const password = document.getElementById('register-password').value;
    const password_confirm = document.getElementById('register-password-confirm').value;

    if (password === password_confirm) {
        const new_user = {
            username: new_username,
            email: email,
            password: password
        };

        fetch('/saveUser', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(new_user)
        })
            .then(response => {
                if (response.ok) {
                    return response.json();
                } else if (response.status === 409) {
                    return response.text(); // Return the error message
                } else {
                    throw new Error('User registration error: ' + response.statusText);
                }
            })
            .then(data => {
                if (typeof data === 'object') {
                    console.log('User saved: ', data);
                } else {
                    console.error('Error saving user', data);
                }
            })
            .catch(error => {
                console.error('Error saving user', error);
            });
    }
}

function onConnected() {
    // Subscribe to the Public Topic
    stompClient.subscribe('/topic/public', onMessageReceived);

    // Tell your username to the server
    stompClient.send("/app/chat.addUser",
        {},
        JSON.stringify({sender: username, type: 'JOIN'})
    )

    connectingElement.classList.add('hidden');
}


function onError(error) {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}


function sendMessage(event) {
    let messageContent = messageInput.value.trim();
    if(messageContent && stompClient) {
        let chatMessage = {
            sender: username,
            content: messageInput.value,
            type: 'CHAT'
        };
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
    event.preventDefault();
}


function onMessageReceived(payload) {
    let message = JSON.parse(payload.body);

    let messageElement = document.createElement('li');

    if(message.type === 'JOIN') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' joined!';
    } else if (message.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' left!';
    } else {
        messageElement.classList.add('chat-message');

        let avatarElement = document.createElement('i');
        let avatarText = document.createTextNode(message.sender[0]);
        avatarElement.appendChild(avatarText);
        avatarElement.style['background-color'] = getAvatarColor(message.sender);

        messageElement.appendChild(avatarElement);

        let usernameElement = document.createElement('span');
        let usernameText = document.createTextNode(message.sender);
        usernameElement.appendChild(usernameText);
        messageElement.appendChild(usernameElement);
    }

    let textElement = document.createElement('p');
    let messageText = document.createTextNode(message.content);
    textElement.appendChild(messageText);

    messageElement.appendChild(textElement);

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}


function getAvatarColor(messageSender) {
    let hash = 0;
    for (let i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }
    let index = Math.abs(hash % colors.length);
    return colors[index];
}
const showRegisterPage = (e) => {
    registerPage.classList.remove('hidden')
    loginPage.classList.add('hidden')
    e.preventDefault()
}

const showLoginPage = (e) => {
    registerPage.classList.add('hidden')
    loginPage.classList.remove('hidden')
    e.preventDefault()
}


linkToRegisterBtn.addEventListener('click', showRegisterPage, true)
linkToLoginPageBtn.addEventListener('click', showLoginPage, true)
loginForm.addEventListener('submit', connect, true)
registerForm.addEventListener('submit', register, true)
messageForm.addEventListener('submit', sendMessage, true)