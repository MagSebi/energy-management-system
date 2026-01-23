package com.example.auth.services;

import com.example.auth.dto.AuthRequest;
import com.example.auth.dto.AuthResponse;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.dto.AdminUpdateUserRequest;
import com.example.auth.dto.UserDto;
import com.example.auth.entities.User;
import com.example.auth.repositories.UserRepository;
import com.example.auth.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SyncPublisher syncPublisher;

    @Value("${user.service.base-url}")
    private String userServiceBaseUrl;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       SyncPublisher syncPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.syncPublisher = syncPublisher;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("username already exists");
        }
        User user = new User(req.getUsername(), passwordEncoder.encode(req.getPassword()), req.getRole());
        user = userRepository.save(user);

        // Publish event with Person data so user-service can create the Person
        syncPublisher.publishUserCreated(
                user.getId(),
                user.getUsername(),
                req.getName(),
                req.getAddress(),
                req.getAge()
        );

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getRole().name());
    }

    public AuthResponse authenticate(AuthRequest req) {
        Optional<User> opt = userRepository.findByUsername(req.getUsername());
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("invalid credentials");
        }
        User user = opt.get();
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("invalid credentials");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getRole().name());
    }

    public Optional<User> findUser(UUID userId) {
        return userRepository.findById(userId);
    }

    public void deleteUser(UUID userId) {
        var userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        userRepository.deleteById(userId);
        
        // Publish deletion event so user-service can delete the Person
        syncPublisher.publishUserDeleted(userId);
    }

    @Transactional
    public UserDto updateUserAndProfile(UUID id, AdminUpdateUserRequest req, String authHeader) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        if (req.getUsername() != null && !req.getUsername().isBlank()) {
            if (!user.getUsername().equals(req.getUsername()) && userRepository.existsByUsername(req.getUsername())) {
                throw new IllegalArgumentException("username already exists");
            }
            user.setUsername(req.getUsername());
        }
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        if (req.getRole() != null && !req.getRole().isBlank()) {
            try {
                var newRole = com.example.auth.entities.UserRole.valueOf(req.getRole().toUpperCase());
                user.setRole(newRole);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role: " + req.getRole());
            }
        }
        user = userRepository.save(user);

        // Profile updates are handled by user-service via RabbitMQ sync events
        // No REST call needed; if profile fields are provided, user-service will pick them up from events

        return UserDto.from(user);
    }
}
