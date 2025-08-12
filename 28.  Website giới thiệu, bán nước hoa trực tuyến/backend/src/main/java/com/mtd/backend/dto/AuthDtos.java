package com.mtd.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthDtos {
    public static class RegisterRequest {
        @Email @NotBlank public String email;
        @NotBlank public String password;
        @NotBlank public String fullName;
    }
    public static class LoginRequest {
        @Email @NotBlank public String email;
        @NotBlank public String password;
    }
    public static class LoginResponse {
        public String token;
        public Long userId;
        public String email;
        public String fullName;
        public String role;
    }
}
