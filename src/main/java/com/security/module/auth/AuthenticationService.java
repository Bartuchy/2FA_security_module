package com.security.module.auth;

import com.security.module.auth.dto.AuthenticationRequestDto;
import com.security.module.auth.dto.AuthenticationResponseDto;
import com.security.module.auth.dto.RegisterRequestDto;
import com.security.module.config.mailing.Email;
import com.security.module.config.mailing.EmailFactory;
import com.security.module.config.mailing.EmailSender;
import com.security.module.config.mailing.token.ConfirmationToken;
import com.security.module.config.mailing.token.ConfirmationTokenFactory;
import com.security.module.config.mailing.token.ConfirmationTokenService;
import com.security.module.config.security.jwt.JwtService;
import com.security.module.user.User;
import com.security.module.user.UserFactory;
import com.security.module.user.UserRepository;
import com.security.module.user.UserService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailSender emailSender;
    private final EmailFactory emailFactory;
    private final ConfirmationTokenService confirmationTokenService;
    private final ConfirmationTokenFactory confirmationTokenFactory;
    private final UserFactory userFactory;
    private final UserService userService;


    public void register(RegisterRequestDto request) throws IOException, MessagingException {
        User user = userFactory.createUser(request);
        userRepository.save(user);

        ConfirmationToken confirmationToken = confirmationTokenFactory.createConfirmationToken(user);
        confirmationTokenService.saveConfirmationToken(confirmationToken);

        Email email = emailFactory.createConfirmationEmail(request.getEmail(), user.getFirstname(), confirmationToken.getToken());
        emailSender.sendMail(email);
    }

    public AuthenticationResponseDto authenticate(AuthenticationRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        String jwtToken = jwtService.generateToken(user);
        return AuthenticationResponseDto.builder()
                .token(jwtToken)
                .build();
    }

    public void confirmRegistration(String token) {
        ConfirmationToken confirmationToken = confirmationTokenService
                .getToken(token)
                .orElseThrow(() -> new IllegalStateException("token not found"));

        if (confirmationToken.getConfirmedAt() != null) {
            throw new IllegalStateException("email already confirmed");
        }

        if (confirmationToken.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalStateException("token expired");
        }

        confirmationTokenService.setConfirmedAt(token);
        userService.enableUser(confirmationToken.getUser().getEmail());
        userService.unlockAccount(confirmationToken.getUser().getEmail());

        log.info("User {} confirmed registration", confirmationToken.getUser().getEmail());
    }
}
