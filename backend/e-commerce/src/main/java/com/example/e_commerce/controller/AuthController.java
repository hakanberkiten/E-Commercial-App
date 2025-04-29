package com.example.e_commerce.controller;

import com.example.e_commerce.dto.SignupRequest;
import com.example.e_commerce.dto.LoginRequest;
import com.example.e_commerce.entity.Role;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.repository.RoleRepository;
import com.example.e_commerce.repository.UserRepository;
import com.example.e_commerce.security.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @PostMapping("/signup")
public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest req) {
    try {
        // Email kontrolü
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            return ResponseEntity
                .badRequest()
                .body("Error: Email is already in use!");
        }

        // Yeni kullanıcı oluşturma
        User user = new User();
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setMobileNumber(req.getMobileNumber());
        
        // Customer rolünü ayarla (roleId = 3)
        Role customerRole = roleRepository.findById(3)
            .orElseThrow(() -> new RuntimeException("Error: Role CUSTOMER not found."));
        user.setRole(customerRole);
        
        userRepository.save(user);
        
        // Başarılı bir şekilde yanıt dön
        return ResponseEntity.ok().body(Map.of("message", "User registered successfully!"));
    } catch (Exception e) {
        // Tüm hataları loglama
        e.printStackTrace();
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", e.getMessage()));
    }
}

@PostMapping("/login")
public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
    try {
        User user = userRepository.findByEmail(req.getEmail())
            .orElse(null);
            
        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid credentials"));
        }
        
        boolean passwordMatches;
        
        // Check if password is BCrypt encoded
        if (user.getPassword().startsWith("$2a$") || user.getPassword().startsWith("$2b$") || user.getPassword().startsWith("$2y$")) {
            // Password is BCrypt encoded, use matcher
            passwordMatches = passwordEncoder.matches(req.getPassword(), user.getPassword());
        } else {
            // For non-BCrypt passwords, do direct comparison (temporary!)
            passwordMatches = req.getPassword().equals(user.getPassword());
            
            // Optional: Update to BCrypt if plain text password matches
            if (passwordMatches) {
                user.setPassword(passwordEncoder.encode(req.getPassword()));
                userRepository.save(user);
            }
        }
        
        if (!passwordMatches) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid credentials"));
        }
        
        // JWT token üret
        String token = jwtUtils.generateToken(user.getEmail());
        
        // Rest of your code remains the same...
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("userId", user.getUserId());
        userResponse.put("firstName", user.getFirstName());
        userResponse.put("lastName", user.getLastName());
        userResponse.put("email", user.getEmail());
        userResponse.put("mobileNumber", user.getMobileNumber());
        
        Map<String, Object> roleMap = new HashMap<>();
        roleMap.put("roleId", user.getRole().getRoleId());
        roleMap.put("roleName", user.getRole().getRoleName());
        userResponse.put("role", roleMap);
        
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", userResponse);
        
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("message", "Error: " + e.getMessage()));
    }
}
}