package com.chatApp.chat;

import com.chatApp.chat.entity.User;
import com.chatApp.chat.service.SessionService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SessionServiceTest {

    @Autowired
    private SessionService sessionService;

    @Test
    public void testSaveSession() {
        // Create a dummy User instance
        User user = new User();
        user.setId(1); // Set user ID

        String sessionUuid = "some-random-uuid";

        // Call the saveSession method from the service
        sessionService.saveSession(user, sessionUuid);

        // Add assertions to verify if the session was saved correctly
        // For example, you can check the database to ensure the session was inserted.
    }
}