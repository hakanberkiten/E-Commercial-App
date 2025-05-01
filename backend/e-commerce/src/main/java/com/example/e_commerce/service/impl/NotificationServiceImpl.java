package com.example.e_commerce.service.impl;

import com.example.e_commerce.entity.Notification;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.repository.NotificationRepository;
import com.example.e_commerce.repository.UserRepository;
import com.example.e_commerce.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    public Notification createNotification(Notification notification) {
        return notificationRepository.save(notification);
    }
    
    @Override
    public Notification createNotification(Long userId, String message, String type, String link) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setType(type);
        notification.setSeen(false); // Changed from 'setRead' to 'setSeen'
        notification.setLink(link);
        
        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<Notification> getRecentNotificationsForUser(Long userId) {
        return notificationRepository.findTop5ByUserUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public int countUnseenNotifications(Long userId) { // Changed from 'countUnreadNotifications'
        return notificationRepository.countByUserUserIdAndSeen(userId, false);
    }

    @Override
    @Transactional
    public void markAsSeen(Long notificationId) { // Changed from 'markAsRead'
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setSeen(true); // Changed from 'setRead' to 'setSeen'
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsSeen(Long userId) { // Changed from 'markAllAsRead'
        List<Notification> notifications = notificationRepository.findByUserUserIdOrderByCreatedAtDesc(userId);
        for (Notification notification : notifications) {
            notification.setSeen(true); // Changed from 'setRead' to 'setSeen'
        }
        notificationRepository.saveAll(notifications);
    }

    @Override
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
}