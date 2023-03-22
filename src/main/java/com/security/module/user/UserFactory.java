package com.security.module.user;

import com.security.module.auth.dto.RegisterRequestDto;
import com.security.module.role.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static com.security.module.config.security.SecurityUtil.USER;

@Component
@RequiredArgsConstructor
public class UserFactory {
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public User createUser(RegisterRequestDto request) {
        return User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(roleService.getRoleByName(USER))
                .build();
    }
}
