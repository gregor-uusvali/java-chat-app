package com.chatApp.chat.service;

import com.chatApp.chat.entity.User;
import com.chatApp.chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserSerivce {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder; // Inject the password encoder
    public boolean doesUsernameExist(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean doesEmailExist(String email) {
        return userRepository.existsByEmail(email);
    }
    public User saveUser(User user) {
        if (doesUsernameExist(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists.");
        }

        if (doesEmailExist(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists.");
        }

        String hashedPassword = passwordEncoder.encode(user.getPassword()); // Hash the password
        user.setPassword(hashedPassword); // Set the hashed password in the user object

        return userRepository.save(user);
    }

    public User getUser(int id){
        return userRepository.findById(id).orElse(null);
    }
}
