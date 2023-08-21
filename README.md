# Springboot Chat Application

Welcome to the Chat Application documentation! This application is built using Java Spring Boot and provides a real-time chat platform. Users can log in, join chat rooms, and exchange messages in real time.

## Getting Started

### Prerequisites
Java JDK (11+)
Gradle (or use wrapper)
Database (MySql)
### Installation

Clone the repository

Copy code:
`git clone https://github.com/your-username/chat-application.git`  

Navigate to the project directory

Copy code:
`cd chat-application`  

Build and run the application

Copy code:
`./gradlew bootRun`  

The application will start on http://localhost:8088 by default.

## Usage

Login
Open your web browser and navigate to http://localhost:8088.
Enter your username and password.
Click the "Login" button.
After logging in, you'll be redirected to the chat interface.
Choose a chat room or start a new chat.
Enter your message in the input field and press Enter or click the "Send" button.
Messages will appear in the chat window in real time.
API Endpoints
/login (POST): Log in a user. Requires a JSON payload with username and password.
Security
This application uses Spring Security for user authentication. Passwords are hashed and compared using BCrypt.
