// AuthController.java
package com.example.e_commerce.controller;

import com.example.e_commerce.entity.User;
import com.example.e_commerce.repository.RoleRepository;
import com.example.e_commerce.repository.UserRepository;
import com.example.e_commerce.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authMgr;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder pwdEnc;
    private final JwtUtils jwtUtils;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User u) {
        u.setPassword(pwdEnc.encode(u.getPassword()));
        u.setRole(roleRepo.findById(3).orElseThrow()); // default CUSTOMER
        User saved = userRepo.save(u);
        return ResponseEntity.ok(Map.of("userId", saved.getUserId()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String,String> cred) {
        var auth = new UsernamePasswordAuthenticationToken(
            cred.get("email"), cred.get("password"));
        authMgr.authenticate(auth);
        String token = jwtUtils.generateToken(cred.get("email"));
        return ResponseEntity.ok(Map.of("token", token));
    }
}
