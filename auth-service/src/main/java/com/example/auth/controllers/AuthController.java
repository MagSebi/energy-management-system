package com.example.auth.controllers;

import com.example.auth.dto.*;
import com.example.auth.entities.User;
import com.example.auth.security.JwtUtil;
import com.example.auth.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @Value("${internal.token:8e7bb9c7-3f2e-4a9e-9c0f-1d2a4b5c6d7e}")
    private String internalToken;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        AuthResponse res = authService.register(req);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest req) {
        AuthResponse res = authService.authenticate(req);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).body("Invalid token");
        }
        UUID userId = jwtUtil.getUserId(token);
        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);
        return ResponseEntity.ok(new AuthResponse(token, userId, username, role));
    }

    @DeleteMapping("/admin/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id,
                                           @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).build();
        }
        UUID requesterId = jwtUtil.getUserId(token);
        String requesterRole = jwtUtil.getRole(token);

        var targetOpt = authService.findUser(id);
        if (targetOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User target = targetOpt.get();

        boolean targetIsAdmin = target.getRole().name().equalsIgnoreCase("ADMIN");
        boolean requesterIsAdmin = "ADMIN".equalsIgnoreCase(requesterRole);

        // Block: ADMIN cannot delete another ADMIN
        if (requesterIsAdmin && targetIsAdmin && !requesterId.equals(id)) {
            return ResponseEntity.status(403).build();
        }
        // Block: ADMIN self-delete also forbidden now
        if (requesterIsAdmin && targetIsAdmin && requesterId.equals(id)) {
            return ResponseEntity.status(403).build();
        }

        authService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/internal/users/{id}")
    public ResponseEntity<Void> internalDeleteUser(@PathVariable UUID id,
                                                   @RequestHeader(value = "X-Internal-Token", required = false) String token) {
        if (token == null || !token.equals(internalToken)) {
            return ResponseEntity.status(403).build();
        }
        authService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/admin/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable UUID id,
                                        @Valid @RequestBody AdminUpdateUserRequest req,
                                        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).body("Invalid token");
        }
        UUID requesterId = jwtUtil.getUserId(token);
        String requesterRole = jwtUtil.getRole(token);
        if (!"ADMIN".equalsIgnoreCase(requesterRole)) {
            return ResponseEntity.status(403).body("Only ADMIN can update users");
        }
        var targetOpt = authService.findUser(id);
        if (targetOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }
        User target = targetOpt.get();
        boolean targetIsAdmin = target.getRole().name().equalsIgnoreCase("ADMIN");
        boolean requesterIsAdmin = "ADMIN".equalsIgnoreCase(requesterRole);
        // Block ADMIN updating other ADMIN (any change)
        if (requesterIsAdmin && targetIsAdmin && !requesterId.equals(id)) {
            return ResponseEntity.status(403).body("ADMIN cannot update another ADMIN");
        }
        // Block self role change from ADMIN to something else
        if (requesterIsAdmin && requesterId.equals(id) && req.getRole() != null && !req.getRole().equalsIgnoreCase("ADMIN")) {
            return ResponseEntity.status(403).body("ADMIN cannot change own role");
        }
        try {
            UserDto updated = authService.updateUserAndProfile(id, req, authHeader);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Update failed: " + ex.getMessage());
        }
    }
}
