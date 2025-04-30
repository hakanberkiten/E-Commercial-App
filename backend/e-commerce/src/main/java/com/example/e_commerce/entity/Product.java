package com.example.e_commerce.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    private String image;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer quantityInStock;
    // Category bağlantısı

    @Column(name = "product_rate", nullable = false, columnDefinition = "DECIMAL(3,2) DEFAULT 0.0")
    @Builder.Default
    private Double productRate = 0.0;
    
    // Yeni eklenen toplam değerlendirme sayısı kolonu
    @Column(name = "review_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer reviewCount = 0;
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // Seller bağlantısı (user tablosundan)
    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;
}
