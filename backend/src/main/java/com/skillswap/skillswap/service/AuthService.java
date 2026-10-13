package com.skillswap.skillswap.service;

import com.skillswap.skillswap.dto.AuthResponse;
import com.skillswap.skillswap.dto.LoginRequest;
import com.skillswap.skillswap.dto.RegisterRequest;
import com.skillswap.skillswap.entity.User;
import com.skillswap.skillswap.entity.UserRole;
import com.skillswap.skillswap.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(RegisterRequest request) {

        if (request == null) {
            throw new RuntimeException("Registration request is required");
        }

        if (request.getName() == null
                || request.getName().trim().isEmpty()) {
            throw new RuntimeException("Name is required");
        }

        if (request.getEmail() == null
                || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }

        if (request.getPassword() == null
                || request.getPassword().length() < 6) {
            throw new RuntimeException(
                    "Password must contain at least 6 characters"
            );
        }

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException(
                    "Email is already registered"
            );
        }

        User user = new User();

        user.setName(request.getName().trim());
        user.setEmail(email);

        // Never save the plain-text password.
        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        user.setCollege(request.getCollege());
        user.setDepartment(request.getDepartment());
        user.setYear(request.getYear());
        user.setBio(request.getBio());
        user.setPreferences(request.getPreferences());
        user.setRole(UserRole.USER);

        User savedUser = userRepository.save(user);

        return new AuthResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                "Registration successful"
        );
    }

    public AuthResponse login(LoginRequest request) {

        if (request == null) {
            throw new RuntimeException("Login request is required");
        }

        if (request.getEmail() == null
                || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }

        if (request.getPassword() == null
                || request.getPassword().isEmpty()) {
            throw new RuntimeException("Password is required");
        }

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Invalid email or password"
                        )
                );

        boolean passwordMatches =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPassword()
                );

        if (!passwordMatches) {
            throw new RuntimeException(
                    "Invalid email or password"
            );
        }

        if (user.getRole() == null) {
            user.setRole(UserRole.USER);
            userRepository.save(user);
        }

        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                "Login successful"
        );
    }
}