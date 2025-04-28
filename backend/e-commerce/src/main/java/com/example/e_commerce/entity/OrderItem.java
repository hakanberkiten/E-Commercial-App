// OrderItem.java
package com.example.e_commerce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "order_item")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemId;

    private BigDecimal orderProductPrice;
    private BigDecimal orderedProductPrice;
    private Integer quantityInOrder;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference           
    private Orders order;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
