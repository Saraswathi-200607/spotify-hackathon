package com.hackathon.spotify.service;

import com.hackathon.spotify.model.*;
import com.hackathon.spotify.repository.UserRepository;
import com.hackathon.spotify.security.JwtService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository users;
    private final JwtService jwt;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository users, JwtService jwt) {
        this.users = users;
        this.jwt = jwt;
    }

    public Map<String, Object> register(String email, String password) {
        if (users.existsByEmail(email)) throw new IllegalArgumentException("Email already registered");

        User u = new User();
        u.setUserId("USR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        u.setEmail(email);
        u.setPassword(encoder.encode(password));
        u.setPlan(Plan.FREE);
        users.save(u);

        return login(email, password);
    }

    public Map<String, Object> login(String email, String password) {
        User u = users.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!encoder.matches(password, u.getPassword()))
            throw new IllegalArgumentException("Invalid email or password");

        return Map.of(
                "token", jwt.generate(u.getUserId(), u.getPlan().name()),
                "userId", u.getUserId(),
                "email", u.getEmail(),
                "plan", u.getPlan().name()
        );
    }
}
