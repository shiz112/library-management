package com.example.library.web;

import com.example.library.domain.Role;
import com.example.library.domain.UserAccount;
import com.example.library.repository.UserRepository;
import com.example.library.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

record RegisterRequest(String username, String email, String password, String role) {}
record LoginRequest(String username, String password) {}
record AuthResponse(String token) {}

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        if (userRepository.findByUsername(req.username()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "username already exists"));
        }
        UserAccount ua = new UserAccount();
        ua.setUsername(req.username());
        ua.setEmail(req.email());
        ua.setPasswordHash(passwordEncoder.encode(req.password()));
        Role r = ("LIBRARIAN".equalsIgnoreCase(req.role())) ? Role.LIBRARIAN : Role.STUDENT;
        ua.setRoles(Set.of(r));
        userRepository.save(ua);
        String token = jwtService.generateToken(ua.getUsername(), Map.of("roles", ua.getRoles()));
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        // generate token with a consistent `roles` claim so frontend decoding is stable
        UserAccount ua = userRepository.findByUsername(req.username()).orElseThrow();
        String token = jwtService.generateToken(req.username(), Map.of("roles", ua.getRoles()));
        return ResponseEntity.ok(new AuthResponse(token));
    }
}


