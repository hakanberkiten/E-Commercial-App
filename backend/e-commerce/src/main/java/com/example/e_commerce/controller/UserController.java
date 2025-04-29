package com.example.e_commerce.controller;

import com.example.e_commerce.dto.AddressDTO;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/save")
    public User saveUser(@RequestBody User user) {
        return userService.saveUser(user);
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        try {
            User updatedUser = userService.updateUser(id, updates);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Mevcut UserController sınıfına ekleyin

@GetMapping("/{userId}/addresses")
public ResponseEntity<?> getUserAddresses(@PathVariable Long userId) {
    try {
        List<AddressDTO> addresses = userService.getUserAddresses(userId);
        return ResponseEntity.ok(addresses);
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
}

@PutMapping("/{userId}/addresses/default")
public ResponseEntity<?> setDefaultAddress(@PathVariable Long userId, @RequestBody Map<String, String> payload) {
    try {
        String addressId = payload.get("addressId");
        if (addressId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Address ID is required"));
        }
        
        userService.setDefaultAddress(userId, Long.valueOf(addressId));
        return ResponseEntity.ok(Map.of("message", "Default address updated successfully"));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
}


@PutMapping("/change-password")
public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwordData) {
    try {
        Long userId = Long.valueOf(passwordData.get("userId"));
        String currentPassword = passwordData.get("currentPassword");
        String newPassword = passwordData.get("newPassword");
        
        User user = userService.getUserById(userId);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
        }
        
        // Mevcut şifreyi kontrol et
        boolean passwordMatches;
        if (user.getPassword().startsWith("$2a$") || user.getPassword().startsWith("$2b$") || user.getPassword().startsWith("$2y$")) {
            passwordMatches = passwordEncoder.matches(currentPassword, user.getPassword());
        } else {
            passwordMatches = currentPassword.equals(user.getPassword());
        }
        
        if (!passwordMatches) {
            return ResponseEntity.badRequest().body(Map.of("message", "Current password is incorrect"));
        }
        
        // Şifreyi güncelle
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.saveUser(user);
        
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
}

}
