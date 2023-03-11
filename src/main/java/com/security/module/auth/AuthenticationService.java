package com.security.module.auth;

import com.security.module.auth.dto.AuthenticationRequestDto;
import com.security.module.auth.dto.AuthenticationResponseDto;
import com.security.module.auth.dto.RegisterRequestDto;
import com.security.module.config.mailing.Email;
import com.security.module.config.mailing.EmailSender;
import com.security.module.config.mailing.token.ConfirmationToken;
import com.security.module.config.mailing.token.ConfirmationTokenService;
import com.security.module.config.security.jwt.JwtService;
import com.security.module.role.RoleService;
import com.security.module.user.User;
import com.security.module.user.UserRepository;
import com.security.module.user.UserService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.security.module.config.security.SecurityUtil.USER;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final RoleService roleService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailSender emailSender;
    private final ConfirmationTokenService confirmationTokenService;
    private final UserService userService;


    public void register(RegisterRequestDto request) throws IOException, MessagingException {
        User user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(roleService.getRoleByName(USER))
                .build();

        userRepository.save(user);

        ClassPathResource resource = new ClassPathResource("templates/mail_template.html");
        InputStream inputStream = resource.getInputStream();
        byte[] bytes = StreamUtils.copyToByteArray(inputStream);
        String content = new String(bytes, StandardCharsets.UTF_8);

        String token = UUID.randomUUID().toString();
        String link = "http://localhost:8080/api/v1/auth/confirm-registration?confirmationToken=" + token;

        content = content.replace("##1", request.getFirstname());
        content = content.replace("##2", link);

        ConfirmationToken confirmationToken = ConfirmationToken.builder()
                .token(token)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();

        confirmationTokenService.saveConfirmationToken(confirmationToken);

        Email email = Email.builder()
                .to(request.getEmail())
                .subject("Test Subject")
                .body(content)
                .from("TwojaStara")
                .build();

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

        LocalDateTime expiredAt = confirmationToken.getExpiresAt();

        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("token expired");
        }

        confirmationTokenService.setConfirmedAt(token);
        userService.enableUser(
                confirmationToken.getUser().getEmail());
        userService.unlockAccount(confirmationToken.getUser().getEmail());
    }
}
