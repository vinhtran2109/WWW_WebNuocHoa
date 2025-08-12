package com.mtd.backend.service;

import com.mtd.backend.dto.AuthDtos;
import com.mtd.backend.entity.User;
import com.mtd.backend.repository.UserRepository;
import com.mtd.backend.utils.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public User register(AuthDtos.RegisterRequest req) {
        userRepository.findByEmail(req.email).ifPresent(u -> { throw new RuntimeException("Email đã tồn tại"); });
        User u = new User();
        u.setEmail(req.email);
        u.setFullName(req.fullName);
        u.setPasswordHash(encoder.encode(req.password));
        return userRepository.save(u);
    }

    public AuthDtos.LoginResponse login(AuthDtos.LoginRequest req) {
        User u = userRepository.findByEmail(req.email).orElseThrow(() -> new RuntimeException("Sai thông tin đăng nhập"));
        if (!encoder.matches(req.password, u.getPasswordHash())) throw new RuntimeException("Sai thông tin đăng nhập");
        String token = jwtUtil.generateToken(String.valueOf(u.getId()), Map.of(
                "email", u.getEmail(),
                "fullName", u.getFullName(),
                "role", u.getRole()
        ));
        AuthDtos.LoginResponse resp = new AuthDtos.LoginResponse();
        resp.token = token; resp.userId = u.getId(); resp.email = u.getEmail(); resp.fullName = u.getFullName(); resp.role = u.getRole();
        return resp;
    }

    public User getUserFromToken(String bearer) {
        if (bearer == null || !bearer.startsWith("Bearer ")) return null;
        String token = bearer.substring(7);
        var claims = jwtUtil.parseToken(token);
        Long userId = Long.parseLong((String) claims.get("sub"));
        return userRepository.findById(userId).orElse(null);
    }
}
