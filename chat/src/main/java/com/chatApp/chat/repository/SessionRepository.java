package com.chatApp.chat.repository;

import com.chatApp.chat.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Integer> {
    Session findBySessionUuid(String sessionUuid);
}