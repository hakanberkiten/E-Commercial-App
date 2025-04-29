package com.example.e_commerce.controller;

import com.example.e_commerce.entity.User;
import com.example.e_commerce.repository.UserRepository;
import com.example.e_commerce.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserRepository userRepo;
    private final JwtUtils jwtUtils;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        User user = userRepo.findByEmail(req.getEmail())
            .orElse(null);
            
        if (user == null || !user.getPassword().equals(req.getPassword())) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Invalid credentials");
        }
        
        // JWT token üret
        String token = jwtUtils.generateToken(user.getEmail());
        
        // Token ve user bilgilerini içeren yanıt oluştur
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", user);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        User user = userRepo.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            
        return ResponseEntity.ok(user);
    }
}

class LoginRequest {
    private String email;
    private String password;
    
    // Getter/Setter'lar
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}