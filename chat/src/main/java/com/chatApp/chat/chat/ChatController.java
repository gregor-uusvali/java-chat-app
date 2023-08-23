package com.chatApp.chat.chat;

import com.chatApp.chat.entity.LoginRequest;
import com.chatApp.chat.entity.Session;
import com.chatApp.chat.entity.User;
import com.chatApp.chat.repository.UserRepository;
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
import java.util.UUID;

import static com.chatApp.chat.service.UserService.sessions;

@RestController
public class ChatController {
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage){
        return chatMessage;
    }
    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor){
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        return chatMessage;
    }
    @Autowired
    private UserService service;
    @PostMapping("/saveUser")
    public ResponseEntity<?> saveUser(@RequestBody User user) {
        try {
            User savedUser = service.saveUser(user);
            return ResponseEntity.ok(savedUser);
        } catch (IllegalArgumentException e) {
            String errorMessage = e.getMessage(); // Assuming IllegalArgumentException contains information about conflict (username or email)
            System.out.println(errorMessage);
            if (errorMessage.contains("Username")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already taken.");
            } else if (errorMessage.contains("Email")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already taken.");
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict occurred.");
            }
        }
    }

    @Autowired
    private UserRepository userRepository; // Inject your UserRepository here

    @Autowired
    private PasswordEncoder passwordEncoder; // Inject your PasswordEncoder here (e.g., BCryptPasswordEncoder)
    @PostMapping("/login")
    public ResponseEntity<?> logUserIn(@RequestBody LoginRequest loginRequest, @CookieValue(name = "session_token", required = false) String sessionTokenCookie, HttpServletResponse response) throws IOException {
        // Retrieve user from the database based on the provided username
        User user = userRepository.findByUsername(loginRequest.getUsername());

        if (user != null) {
            // Compare hashed password in the database with provided password
            if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                // Passwords match, generate authentication token and return it
                String authToken = generateAuthToken(user.getId(), user.getUsername());
                // Log the session token cookie value
                // Check if the session token cookie is present
                if (StringUtils.isEmpty(sessionTokenCookie)) {
                    // Create a new session token and store it
                    createCookie(response, user);
                    System.out.println("Created session cookie for user: " + user.getUsername());
                }

                System.out.println(sessions);

                return ResponseEntity.status(HttpStatus.OK).body(authToken);
            }
        }

        // Invalid credentials
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }
    private String generateAuthToken(int userId, String username) {
        // Logic to generate an authentication token (e.g., JWT)
        // Return the generated token
        return "GeneratedAuthToken"; // Replace with actual token generation logic
    }

    @GetMapping("/getUser/{id}")
    public User getUser(@PathVariable int id){
        return service.getUser(id);
    }

    public void createCookie(HttpServletResponse response, User user) throws IOException {
        UUID sessionToken = UUID.randomUUID();

        Session session = new Session();
        session.setUser(user);
        session.setLastSeen(LocalDateTime.now());

        sessions.put(sessionToken.toString(), session);

        Cookie sessionCookie = new Cookie("session_token", sessionToken.toString());
        sessionCookie.setPath("/");
        sessionCookie.setMaxAge(2 * 60 * 60); // 2 hours in seconds
        response.addCookie(sessionCookie);
    }
}