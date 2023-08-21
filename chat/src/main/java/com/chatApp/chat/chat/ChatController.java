package com.chatApp.chat.chat;

import com.chatApp.chat.entity.LoginRequest;
import com.chatApp.chat.entity.User;
import com.chatApp.chat.repository.UserRepository;
import com.chatApp.chat.service.UserSerivce;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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
    private UserSerivce service;
    @PostMapping("/saveUser")
    public ResponseEntity<?> saveUser(@RequestBody User user) {
        try {
            User savedUser = service.saveUser(user);
            return ResponseEntity.ok(savedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username or email already taken.");
        }
    }

    @Autowired
    private UserRepository userRepository; // Inject your UserRepository here

    @Autowired
    private PasswordEncoder passwordEncoder; // Inject your PasswordEncoder here (e.g., BCryptPasswordEncoder)
    @PostMapping("/login")
    public ResponseEntity<?> logUserIn(@RequestBody LoginRequest loginRequest) {
        // Retrieve user from the database based on the provided username
        User user = userRepository.findByUsername(loginRequest.getUsername());

        if (user != null) {
            // Compare hashed password in the database with provided password
            if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                // Passwords match, generate authentication token and return it
                String authToken = generateAuthToken(user.getId(), user.getUsername());
                return ResponseEntity.status(HttpStatus.OK).body(authToken);
            }
        }

        // Invalid credentials
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }
    private String generateAuthToken(int userId, String username) {
        // Logic to generate an authentication token (e.g., JWT)
        // Return the generated token
        return "GeneratedAuthToken"; // Replace with actual token generation logiccc
    }

    @GetMapping("/getUser/{id}")
    public User getUser(@PathVariable int id){
        return service.getUser(id);
    }


}
