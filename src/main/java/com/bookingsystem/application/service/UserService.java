package com.bookingsystem.application.service;
import com.bookingsystem.application.dto.*;
import com.bookingsystem.application.enums.Role;
import com.bookingsystem.model.User;
import com.bookingsystem.repo.UserRepository;
import com.bookingsystem.shared.JwtUtil;
import com.bookingsystem.shared.exception.EmailAlreadyExistException;
import com.bookingsystem.shared.exception.UserNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email)) {
            throw new EmailAlreadyExistException("Email already exists");
        }
        if (userRepository.existsByUsername(request.username)) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.username);
        user.setEmail(request.email);
        user.setPassword(passwordEncoder.encode(request.password));
        user.setFirstName(request.firstName);
        user.setLastName(request.lastName);
        user.setPhoneNumber(request.phoneNumber);
        user.setCountry(request.country);
        user.setRole(Role.USER);

        // Generate mock token
        String token1 = UUID.randomUUID().toString();
        user.setVerificationToken(token1);

        userRepository.save(user);

        // Mock email sender
        sendVerificationEmail(user.getEmail(), token1);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        AuthResponse response = new AuthResponse();
        response.token = token;
        response.username = user.getUsername();
        response.email = user.getEmail();
        response.role = user.getRole();
        return response;
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        AuthResponse response = new AuthResponse();
        response.token = token;
        response.username = user.getUsername();
        response.email = user.getEmail();
        response.role = user.getRole();
        return response;
    }

    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setPassword(passwordEncoder.encode(request.newPassword));
        userRepository.save(user);
    }

    // Mock email
    private boolean SendVerifyEmail(String email) {
        System.out.println("Sending verification email to " + email);
        return true;
    }

    public UserProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        UserProfileResponse response = new UserProfileResponse();
        response.username = user.getUsername();
        response.email = user.getEmail();
        response.firstName = user.getFirstName();
        response.lastName = user.getLastName();
        response.phoneNumber = user.getPhoneNumber();
        response.country = user.getCountry();
        response.role = user.getRole();
        response.status = user.getStatus();
        return response;
    }

    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        user.setEmailVerified(true);
        user.setVerificationToken(null); // clear token
        userRepository.save(user);
    }

    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password does not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // ---------------- MOCK EMAIL SENDER ---------------- //
    private void sendVerificationEmail(String email, String token) {
        // Mock: In real app, send email with verification link
        System.out.println("📧 Mock email sent to " + email + " with token: " + token);
    }
}

