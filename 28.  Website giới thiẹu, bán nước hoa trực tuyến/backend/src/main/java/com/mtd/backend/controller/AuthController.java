package com.mtd.backend.controller;

import com.mtd.backend.dto.AuthDtos;
import com.mtd.backend.entity.User;
import com.mtd.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthDtos.RegisterRequest req) {
        User u = authService.register(req);
        return ResponseEntity.ok().body("OK");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDtos.LoginResponse> login(@Valid @RequestBody AuthDtos.LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(name = "Authorization", required = false) String bearer) {
        var u = authService.getUserFromToken(bearer);
        if (u == null) return ResponseEntity.status(401).body("Unauthorized");
        return ResponseEntity.ok(new Object(){ public Long id = u.getId(); public String email = u.getEmail(); public String fullName = u.getFullName(); public String role = u.getRole(); });
    }
}
