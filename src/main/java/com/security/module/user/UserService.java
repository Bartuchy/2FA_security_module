package com.security.module.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public void enableUser(String email) {
        userRepository.enableUser(email);
    }

    public void unlockAccount(String email) {
        userRepository.unlockAccount(email);
    }
}
