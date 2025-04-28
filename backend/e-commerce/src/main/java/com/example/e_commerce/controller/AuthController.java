// src/main/java/com/example/e_commerce/controller/AuthController.java
package com.example.e_commerce.controller;

import com.example.e_commerce.dto.*;
import com.example.e_commerce.entity.*;
import com.example.e_commerce.repository.*;
import com.example.e_commerce.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins="http://localhost:4200")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final RoleRepository roleRepo;
    private final UserRepository userRepo;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest req) {
        if (userRepo.findByEmail(req.getEmail()).isPresent()) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body("Email already in use");
        }
        Role customerRole = roleRepo.findById(3)
            .orElseThrow(() -> new RuntimeException("Customer role not found"));
        User u = User.builder()
            .firstName(req.getFirstName())
            .lastName(req.getLastName())
            .email(req.getEmail())
            .password(req.getPassword())       
            .mobileNumber(req.getMobileNumber())
            .role(customerRole)
            .build();
        User saved = userService.saveUser(u);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        User u = userRepo.findByEmail(req.getEmail())
            .orElse(null);
        if (u == null || !u.getPassword().equals(req.getPassword())) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Invalid credentials");
        }
        // for now return the user; later return a JWT
        return ResponseEntity.ok(u);
    }

    @GetMapping("/me")
    public UserDTO whoAmI(@AuthenticationPrincipal User user) {
        return new UserDTO(
            user.getUserId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getRole().getRoleId()
        );
    }
}
