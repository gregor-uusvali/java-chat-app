package com.chatApp.chat.service;

import com.chatApp.chat.entity.Session;
import com.chatApp.chat.entity.User;
import com.chatApp.chat.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;

    @Autowired
    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }
    public Session getSessionBySessionUuid(String sessionUuid) {
        return sessionRepository.findBySessionUuid(sessionUuid);
    }

    public void updateSessionLastSeen(Session session) {
        session.setLastSeen(LocalDateTime.now());
        sessionRepository.save(session);
    }

    public void saveSession(Session session) {
        sessionRepository.save(session);
    }

    public void saveSession(User user, String sessionUuid) {
        Session sessionEntity = new Session();
        sessionEntity.setUser(user);
        sessionEntity.setSessionUuid(sessionUuid); // Set the session UUID
        sessionEntity.setCreatedAt(LocalDateTime.now()); // Set the creation timestamp
        sessionEntity.setLastSeen(LocalDateTime.now());

        sessionRepository.save(sessionEntity);
    }
}
