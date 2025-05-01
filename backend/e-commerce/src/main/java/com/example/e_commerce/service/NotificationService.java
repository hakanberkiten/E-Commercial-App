package com.example.e_commerce.service;

import com.example.e_commerce.entity.Notification;

import java.util.List;

public interface NotificationService {
    Notification createNotification(Notification notification);
    Notification createNotification(Long userId, String message, String type, String link);
    List<Notification> getNotificationsForUser(Long userId);
    List<Notification> getRecentNotificationsForUser(Long userId);
    int countUnseenNotifications(Long userId);
    void markAsSeen(Long notificationId);
    void markAllAsSeen(Long userId);
    void deleteNotification(Long notificationId);
}