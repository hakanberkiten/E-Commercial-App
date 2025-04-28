// Payment.java
package com.example.e_commerce.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    private String paymentMethod;
}
