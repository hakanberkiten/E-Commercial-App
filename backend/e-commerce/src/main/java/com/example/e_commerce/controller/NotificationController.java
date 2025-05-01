package com.example.e_commerce.controller;

import com.example.e_commerce.dto.NotificationDTO;
import com.example.e_commerce.entity.Notification;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.repository.UserRepository;
import com.example.e_commerce.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    private Long getCurrentUserId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByEmail(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getUserId();
    }

    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setSeen(notification.isSeen()); // Changed from 'isRead' to 'isSeen'
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setLink(notification.getLink());
        return dto;
    }

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getAllNotifications(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        List<Notification> notifications = notificationService.getNotificationsForUser(userId);
        List<NotificationDTO> dtoList = notifications.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<NotificationDTO>> getRecentNotifications(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        List<Notification> notifications = notificationService.getRecentNotificationsForUser(userId);
        List<NotificationDTO> dtoList = notifications.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/unseen-count") // Changed from 'unread-count'
    public ResponseEntity<Map<String, Integer>> getUnseenCount(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        int count = notificationService.countUnseenNotifications(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/{id}/seen") // Changed from 'read' to 'seen'
    public ResponseEntity<?> markAsSeen(@PathVariable Long id) {
        notificationService.markAsSeen(id);
        return ResponseEntity.ok(Map.of("message", "Notification marked as seen"));
    }

    @PutMapping("/mark-all-seen") // Changed from 'mark-all-read'
    public ResponseEntity<?> markAllAsSeen(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        notificationService.markAllAsSeen(userId);
        return ResponseEntity.ok(Map.of("message", "All notifications marked as seen"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(Map.of("message", "Notification deleted"));
    }

    @PostMapping("/seller-request")
    public ResponseEntity<?> createSellerRequest(@RequestBody Map<String, Long> request, Authentication authentication) {
        Long requesterId = request.get("userId");
        String requesterEmail = authentication.getName();
        
        // Verify the requester is the authenticated user
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!requester.getEmail().equals(requesterEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Unauthorized request"));
        }
        
        // Check requester's role
        String roleName = requester.getRole().getRoleName();
        if (roleName.equals("ADMIN") || roleName.equals("ROLE_ADMIN")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Admins cannot request seller access"));
        }
        
        if (roleName.equals("SELLER") || roleName.equals("ROLE_SELLER")) {
            return ResponseEntity.badRequest().body(Map.of("message", "User is already a seller"));
        }
        
        // Find all admin users
        List<User> adminUsers = userRepository.findByRoleName("ADMIN");
        
        // Create notifications for all admins
        for (User admin : adminUsers) {
            String message = String.format("User %s %s (%s) has requested seller access", 
                    requester.getFirstName(), requester.getLastName(), requester.getEmail());
            
            // Include requester ID as JSON in link for approval/denial actions
            String link = "/seller-request/" + requesterId;
            
            notificationService.createNotification(admin.getUserId(), message, "SELLER_REQUEST", link);
        }
        
        return ResponseEntity.ok(Map.of("message", "Seller request submitted successfully"));
    }
}