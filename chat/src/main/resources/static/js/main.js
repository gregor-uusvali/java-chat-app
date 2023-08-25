'use strict';

const loginPage = document.querySelector('#login-page');
const registerPage = document.getElementById("register-page")
const chatPage = document.querySelector('#chat-page');
const logOutBtn = document.getElementById('logoutBtn')
const logOutForm = document.getElementById('logoutForm')
const loginForm = document.querySelector('#loginForm');
const registerForm = document.querySelector('#registerForm');
const messageForm = document.querySelector('#messageForm');
const messageInput = document.querySelector('#message');
const messageArea = document.querySelector('#messageArea');
const connectingElement = document.querySelector('.connecting');
const linkToRegisterBtn = document.getElementById("link-to-register")
const linkToLoginPageBtn = document.getElementById('link-to-login')
const logErrorMsg = document.getElementById("login-error-msg")
const regErrorMsg = document.getElementById("reg-error-msg")

let stompClient = null;
let username = null;


let colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

document.addEventListener('DOMContentLoaded', () => {
    // Fetch request to check user's session status
    fetch('/check-session')
        .then(response => {
            if (response.ok) {
                // User has an active session, perform necessary actions
                return response.json(); // Return the promise from response.text()
            } else {
                // User does not have a valid session, display login page
                loginPage.classList.remove('hidden');
                chatPage.classList.add('hidden');
                throw new Error('Invalid session');
            }
        })
        .then(data => {
            // Now 'username' holds the resolved value of response.text()
            loadMsgFromDb(data)
            username = data.username
            loginPage.classList.add('hidden');
            chatPage.classList.remove('hidden');
            logOutBtn.classList.remove('hidden')
            let socket = new SockJS('/ws');
            stompClient = Stomp.over(socket);

            stompClient.connect({}, onConnected, onError);
        })
        .catch(error => {
            console.error('Error checking session status:', error);
        });
});

function connect(event) {
    event.preventDefault();
    let invalidCredentials = false;
    username = document.querySelector('#login-name').value.trim();
    const password = document.querySelector('#login-password').value;
    const user = {
        username: username,
        password: password
    }

    fetch('login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(user)
    })
        .then(response => {
            if (response.ok) {
                // loginPage.classList.add('hidden');
                // chatPage.classList.remove('hidden');
                // logOutBtn.classList.remove('hidden')
                let socket = new SockJS('/ws');
                stompClient = Stomp.over(socket);

                stompClient.connect({}, onConnected, onError); // You need to define these functions
                return response.json();
            } else if (response.status == 401) {
                invalidCredentials = true
                logErrorMsg.innerText = "Invalid credentials"
                setTimeout(() => {
                    logErrorMsg.innerText = ""
                }, 4000)
                // throw new Error('User login error: ' + response.statusText);
            }
        })
        .then(data => {
            if(!invalidCredentials){
                loadMsgFromDb(data)// Check the received data
                const chatMessages = data.chatMessages; // Access chatMessages from data
                console.log(chatMessages);
                loginPage.classList.add('hidden');
                chatPage.classList.remove('hidden');
                logOutBtn.classList.remove('hidden')

            }
        })
        .catch(error => {
            console.error('Error logging user in:', error);
        });
}

function register(event) {
    event.preventDefault();
    const new_username = document.getElementById('register-name').value;
    const email = document.getElementById('register-email').value;
    const password = document.getElementById('register-password').value;
    const password_confirm = document.getElementById('register-password-confirm').value;

    if(new_username === "" || email === "" || password === "" || password_confirm === ""){
        regErrorMsg.innerText = "All fields must be filled!"
        setTimeout(() => {
            regErrorMsg.innerText = ""
        }, 3000)
    } else {
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
                        regErrorMsg.innerText = data
                        setTimeout(() => {
                            regErrorMsg.innerText = ""
                        }, 4000)
                        console.error('Error saving user', data);
                    }
                })
                .catch(error => {
                    console.error('Error saving user', error);
                });
        } else {
            regErrorMsg.innerText = "Passwords must match!"
            setTimeout(() => {
                regErrorMsg.innerText = ""
            }, 4000)
        }

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

function loadMsgFromDb(payloadList){
    // console.log(payloadList.chatMessages)
    payloadList.chatMessages.forEach(payload => {
        let messageElement = document.createElement('li');

        messageElement.classList.add('chat-message');

        let avatarElement = document.createElement('i');
        let avatarText = document.createTextNode(payload.sender[0]);
        avatarElement.appendChild(avatarText);
        avatarElement.style['background-color'] = getAvatarColor(payload.sender);

        messageElement.appendChild(avatarElement);

        let usernameElement = document.createElement('span');
        let usernameText = document.createTextNode(payload.sender);
        usernameElement.appendChild(usernameText);
        messageElement.appendChild(usernameElement);

        let textElement = document.createElement('p');
        let messageText = document.createTextNode(payload.content);
        textElement.appendChild(messageText);

        messageElement.appendChild(textElement);

        messageArea.appendChild(messageElement);

    })
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
    logErrorMsg.innerText = ""
    regErrorMsg.innerText = ""
    e.preventDefault()
}

const showLoginPage = (e) => {
    registerPage.classList.add('hidden')
    loginPage.classList.remove('hidden')
    regErrorMsg.innerText = ""
    logErrorMsg.innerText = ""
    e.preventDefault()
}

const logOut = (e) => {
    e.preventDefault();

    // Disconnect the WebSocket connection
    stompClient.disconnect(() => {
        console.log('WebSocket disconnected');
    });

    // Send a logout request to the server
    fetch('/logout', {
        method: 'GET'
    })
        .then(response => {
            console.log(response);
            if (response.ok) {
                // Reset UI and user data
                chatPage.classList.add('hidden');
                loginPage.classList.remove('hidden');
                logOutBtn.classList.add('hidden');
                username = null;
            } else {
                console.error('Logout failed');
            }
        })
        .catch(error => {
            console.error('Error during logout:', error);
        });

    // Notify the server that the user is leaving
    stompClient.send("/app/chat.addUser", {}, JSON.stringify({ sender: username, type: 'LEAVE' }));
};


logOutBtn.addEventListener('click', logOut, true)
linkToRegisterBtn.addEventListener('click', showRegisterPage, true)
linkToLoginPageBtn.addEventListener('click', showLoginPage, true)
loginForm.addEventListener('submit', connect, true)
registerForm.addEventListener('submit', register, true)
messageForm.addEventListener('submit', sendMessage, true)