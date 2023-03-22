package com.security.module.config.mailing.token;

import com.security.module.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ConfirmationTokenFactory {

    public ConfirmationToken createConfirmationToken(User user) {
        String token = UUID.randomUUID().toString();

        return ConfirmationToken.builder()
                .token(token)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(900))
                .user(user)
                .build();
    }
}
