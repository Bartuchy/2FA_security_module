package com.security.module.config.mailing;

import com.security.module.common.ResourceReader;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class EmailFactory {

    @Value("${mail.host}")
    public static String SYSTEM_MAIL;

    public Email createConfirmationEmail(String to, String firstname, String token) throws IOException {
        String content = ResourceReader.readHTMLFromResourcesAsString("templates/mail_template.html");
        String link = "http://localhost:8080/api/v1/auth/confirm-registration?confirmationToken=" + token;

        content = content.replace("##1", firstname);
        content = content.replace("##2", link);

        return Email.builder()
                .to(to)
                .subject("Registration confirmation")
                .content(content)
                .from(SYSTEM_MAIL)
                .build();
    }

    public Email createPasswordResetEmail(String to, String firstname, String token) throws IOException {
        return Email.builder().build();
    }
}
