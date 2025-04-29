// Review.java
package com.example.e_commerce.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(name = "review")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
@Column(name = "created_at")
private Date createdAt;
 // Yeni eklenen değerlendirme puanı kolonu (1-5 arası)
 @Column(name = "review_point", nullable = false)
 private Integer reviewPoint;

@PrePersist
protected void onCreate() {
    createdAt = new Date();
}
    @Column(length = 10000)
    private String content;
}
