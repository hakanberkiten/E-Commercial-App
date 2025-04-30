package com.example.e_commerce.controller;

import com.example.e_commerce.dto.AddressDTO;
import com.example.e_commerce.entity.Role;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.repository.RoleRepository;
import com.example.e_commerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired  // Add this annotation to properly inject the dependency
    private RoleRepository roleRepository;
    
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

@PutMapping("/{userId}/role")
public ResponseEntity<?> updateUserRole(@PathVariable Long userId, @RequestBody Map<String, Integer> roleData) {
    try {
        Integer roleId = roleData.get("roleId");
        if (roleId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Role ID is required"));
        }
        
        User user = userService.getUserById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
        }
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));
        
        user.setRole(role);
        userService.saveUser(user);
        
        return ResponseEntity.ok(Map.of("message", "User role updated successfully"));
    } catch (Exception e) {
        // Log the exception for server-side debugging
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Error updating user role: " + e.getMessage()));
    }
}

@PatchMapping("/{userId}/status")
public ResponseEntity<?> updateUserStatus(@PathVariable Long userId, @RequestBody Map<String, Boolean> statusData) {
    try {
        Boolean active = statusData.get("active");
        if (active == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Active status is required"));
        }
        
        User user = userService.getUserById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
        }
        
        // Add active field to your User entity if it doesn't exist
        user.setActive(active);
        userService.saveUser(user);
        
        return ResponseEntity.ok(Map.of("message", "User status updated successfully"));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Error updating user status: " + e.getMessage()));
    }
}

@PostMapping("/{userId}/reset-password")
public ResponseEntity<?> resetUserPassword(@PathVariable Long userId) {
    try {
        User user = userService.getUserById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
        }
        
        // Generate a random temporary password
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        user.setPassword(passwordEncoder.encode(tempPassword));
        userService.saveUser(user);
        
        // In a real application, you would send an email with the temporary password
        // For now, we'll just return it in the response
        return ResponseEntity.ok(Map.of(
            "message", "Password reset successfully",
            "temporaryPassword", tempPassword
        ));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Error resetting password: " + e.getMessage()));
    }
}
}
