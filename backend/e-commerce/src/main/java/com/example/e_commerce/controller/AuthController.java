package com.example.e_commerce.controller;


import com.example.e_commerce.dto.*;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.repository.UserRepository;
import com.example.e_commerce.config.JwtUtils;
import com.example.e_commerce.service.UserService;
import com.example.e_commerce.service.impl.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserRepository userRepo;
    private final UserService userService;
    private final BCryptPasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginReq) {
        Authentication auth = authManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginReq.getUsername(), loginReq.getPassword()));
        String jwt = jwtUtils.generateJwtToken(auth);
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return ResponseEntity.ok(new JwtResponse(
            jwt, "Bearer", userDetails.getId(),
            userDetails.getUsername(), userDetails.getEmail(),
            userDetails.getRole()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpReq) {
        if (userRepo.existsByUsername(signUpReq.getUsername())) {
            return ResponseEntity
                .badRequest()
                .body(new MessageResponse("Error: Username is already taken!"));
        }
        if (userRepo.existsByEmail(signUpReq.getEmail())) {
            return ResponseEntity
                .badRequest()
                .body(new MessageResponse("Error: Email is already in use!"));
        }
        User user = new User();
        user.setUsername(signUpReq.getUsername());
        user.setEmail(signUpReq.getEmail());
        user.setPassword(encoder.encode(signUpReq.getPassword()));
        user.setUserRole("ROLE_USER");
        User saved = userService.create(user);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(new MessageResponse("User registered successfully!"));
    }
}
