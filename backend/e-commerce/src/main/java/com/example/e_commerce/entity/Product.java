package com.example.e_commerce.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "product")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductID")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CategoryID")
    private Category category;

    @Column(name = "ProductName", length = 50, nullable = false)
    private String name;

    @Column(name = "ProductDescription", columnDefinition = "TEXT")
    private String description;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    private Byte gender;               // tinyint

    @Column(precision = 10, scale = 2)
    private BigDecimal discount;

    @Column(columnDefinition = "TEXT")
    private String productImage;
}
