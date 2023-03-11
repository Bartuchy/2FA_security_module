package com.security.module.auth;

import com.security.module.auth.dto.AuthenticationRequestDto;
import com.security.module.auth.dto.AuthenticationResponseDto;
import com.security.module.auth.dto.RegisterRequestDto;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("register")
    public ResponseEntity<Void> register(
            @RequestBody RegisterRequestDto request
    ) throws MessagingException, IOException {
        service.register(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("authenticate")
    public ResponseEntity<AuthenticationResponseDto> authenticate(
            @RequestBody AuthenticationRequestDto request
    ) {
        AuthenticationResponseDto response = service.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("confirm-registration")
    public ResponseEntity<Void> confirmRegistration(
            @RequestParam String confirmationToken
    ) {
        service.confirmRegistration(confirmationToken);
        return ResponseEntity.ok().build();
    }
}
