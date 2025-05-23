package com.example.e_commerce.controller;

import com.example.e_commerce.dto.AddressDTO;
import com.example.e_commerce.entity.Role;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.repository.RoleRepository;
import com.example.e_commerce.repository.UserRepository;
import com.example.e_commerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired  // Add this annotation to properly inject the dependency
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;
    
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

    @GetMapping("/current")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String email = authentication.getName();
        User user = userService.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> updates,
                                       Authentication authentication) {
        try {
            // Get current user
            String email = authentication.getName();
            User currentUser = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Check if user is updating their own profile or is an admin
            boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isOwnProfile = currentUser.getUserId().equals(id);
            
            if (!isAdmin && !isOwnProfile) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You can only update your own profile"));
            }
            
            User updatedUser = userService.updateUser(id, updates);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

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
    public ResponseEntity<?> resetUserPassword(@PathVariable Long userId, @RequestBody Map<String, String> passwordData) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
            }
            
            // Get the new password from the request body
            String newPassword = passwordData.get("newPassword");
            if (newPassword == null || newPassword.length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("message", "New password must be at least 6 characters"));
            }
            
            // Encrypt and save the new password
            user.setPassword(passwordEncoder.encode(newPassword));
            userService.saveUser(user);
            
            return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error resetting password: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUserWithAllData(userId);
            return ResponseEntity.ok(Map.of("message", "User and all associated data deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/admin-contacts")
    public ResponseEntity<List<Map<String, String>>> getAdminContacts() {
        // Use a targeted database query instead of findAll()
        List<User> adminUsers = userRepository.findByRoleName("ADMIN");
        
        List<Map<String, String>> adminContacts = adminUsers.stream()
            .map(admin -> {
                Map<String, String> contact = new HashMap<>();
                contact.put("firstName", admin.getFirstName());
                contact.put("lastName", admin.getLastName());
                contact.put("email", admin.getEmail());
                return contact;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(adminContacts);
    }
}
