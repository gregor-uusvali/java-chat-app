package com.chatApp.chat.repository;

import com.chatApp.chat.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
    // You can define custom query methods if needed
}