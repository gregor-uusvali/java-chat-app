package com.chatApp.chat.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String sender;
    private String content;
    private LocalDateTime timestamp;

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTimestamp(LocalDateTime now) {
        this.timestamp = now;
    }

    public String getContent() {
        return content;
    }
    public String getSender() {
        return sender;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
