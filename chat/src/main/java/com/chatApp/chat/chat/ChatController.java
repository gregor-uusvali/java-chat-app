package com.chatApp.chat.chat;

import com.chatApp.chat.entity.LoginRequest;
import com.chatApp.chat.entity.Session;
import com.chatApp.chat.entity.User;
import com.chatApp.chat.repository.UserRepository;
import com.chatApp.chat.service.SessionService;
import com.chatApp.chat.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static com.chatApp.chat.service.UserService.sessions;

@RestController
public class ChatController {
    @Autowired
    private UserService userService;
    @Autowired
    private SessionService sessionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        return chatMessage;
    }

    @PostMapping("/saveUser")
    public ResponseEntity<String> saveUser(@RequestBody User user) {
        try {
            User savedUser = userService.saveUser(user);
            return ResponseEntity.ok("User saved: " + savedUser.getUsername());
        } catch (IllegalArgumentException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("Username")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already taken.");
            } else if (errorMessage.contains("Email")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already taken.");
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict occurred.");
            }
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> logUserIn(@RequestBody LoginRequest loginRequest,
                                            @CookieValue(name = "session_token", required = false) String sessionTokenCookie,
                                            HttpServletResponse response) throws IOException {
        User user = userRepository.findByUsername(loginRequest.getUsername());

        if (user != null && passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            String authToken = generateAuthToken(user.getId(), user.getUsername());

            if (StringUtils.isEmpty(sessionTokenCookie)) {
                // Generate a new session UUID
                String sessionToken = UUID.randomUUID().toString();
                createCookie(response, user, sessionToken);
                System.out.println("Created session cookie for user: " + user.getUsername());
                sessionService.saveSession(user, sessionToken); // Save the session with the generated UUID
            } else {
                sessionService.saveSession(user, sessionTokenCookie); // Save the session with the existing UUID
            }

            System.out.println("Sessions after adding user session: " + getSessionInfo(sessions)); // Print the sessions map

            return ResponseEntity.status(HttpStatus.OK).body(authToken);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }

    @GetMapping("/getUser/{id}")
    public User getUser(@PathVariable int id) {
        return userService.getUser(id);
    }

    private String generateAuthToken(int userId, String username) {
        return "GeneratedAuthToken"; // Replace with actual token generation logic
    }

    private void createCookie(HttpServletResponse response, User user, String sessionToken) throws IOException {


        Session session = new Session();
        session.setUser(user);
        session.setLastSeen(LocalDateTime.now());

        sessions.put(sessionToken.toString(), session);

        Cookie sessionCookie = new Cookie("session_token", sessionToken.toString());
        sessionCookie.setPath("/");
        sessionCookie.setMaxAge(2 * 60 * 60); // 2 hours in seconds
        response.addCookie(sessionCookie);
    }
    private String getSessionInfo(Map<String, Session> sessions) {
        StringBuilder info = new StringBuilder("{");
        for (Map.Entry<String, Session> entry : sessions.entrySet()) {
            Session session = entry.getValue();
            User user = session.getUser();
            info.append(entry.getKey()).append(": ")
                    .append("username=").append(user.getUsername())
                    .append(", lastSeen=").append(session.getLastSeen())
                    .append(" | ");
        }
        if (!sessions.isEmpty()) {
            info.setLength(info.length() - 3); // Remove the last " | "
        }
        info.append("}");
        return info.toString();
    }
    @GetMapping("/check-session")
    public ResponseEntity<String> homePage(@CookieValue(name = "session_token", required = false) String sessionTokenCookie,
                                           HttpServletResponse response) {
        if (sessionTokenCookie != null) {
            // Retrieve session information based on sessionTokenCookie
            Session session = sessionService.getSessionBySessionUuid(sessionTokenCookie);

            if (session != null) {
                // Update the last_seen timestamp of the session
                session.setLastSeen(LocalDateTime.now());
                sessionService.updateSessionLastSeen(session);

                String username = session.getUser().getUsername();

                // Respond with a welcome message including the username
                return ResponseEntity.ok(username);

            }
        }

        // Redirect the user to the login page or handle accordingly
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please log in");
    }
}