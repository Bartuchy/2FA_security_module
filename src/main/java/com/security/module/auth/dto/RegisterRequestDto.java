package com.security.module.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDto {
    private String firstname;
    private String lastname;
    private String email;
    private String password;
}
