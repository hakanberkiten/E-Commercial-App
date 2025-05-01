package com.example.e_commerce.controller;

import com.example.e_commerce.dto.NotificationDTO;
import com.example.e_commerce.entity.Notification;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.repository.UserRepository;
import com.example.e_commerce.service.NotificationService;
import lombok.RequiredArgsConstructor;
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
}