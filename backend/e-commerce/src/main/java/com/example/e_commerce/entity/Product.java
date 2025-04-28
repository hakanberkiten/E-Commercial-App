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

    @Column(nullable = false)
    private String description;

    private String image;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer quantityInStock;

    // Category bağlantısı
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // Seller bağlantısı (user tablosundan)
    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;
}
