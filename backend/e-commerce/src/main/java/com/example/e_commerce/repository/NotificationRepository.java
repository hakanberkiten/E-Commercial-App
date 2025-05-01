package com.example.e_commerce.repository;

import com.example.e_commerce.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findTop5ByUserUserIdAndSeenOrderByCreatedAtDesc(Long userId, boolean seen);
    List<Notification> findTop5ByUserUserIdOrderByCreatedAtDesc(Long userId);
    int countByUserUserIdAndSeen(Long userId, boolean seen);
}