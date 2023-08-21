package com.chatApp.chat.chat;

import com.chatApp.chat.entity.User;
import com.chatApp.chat.service.UserSerivce;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
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
    @GetMapping("/getUser/{id}")
    public User getUser(@PathVariable int id){
        return service.getUser(id);
    }


}
