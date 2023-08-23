package com.chatApp.chat.entity;

import java.time.LocalDateTime;

public class Session {
    private int id;
    private User user;
    private LocalDateTime lifetime;
    private LocalDateTime lastSeen;
    public void setUser(User user) {
        this.user = user;
    }
    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }
}