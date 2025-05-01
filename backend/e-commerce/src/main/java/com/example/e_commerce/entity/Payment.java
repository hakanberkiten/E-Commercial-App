// Payment.java
package com.example.e_commerce.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "payment")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    private String paymentMethod; // e.g., "card", "bank_transfer"
    
    // Stripe related fields
    private String stripeCustomerId; // Stripe's customer ID
    private String stripePaymentIntentId; // Stripe's payment intent ID
    private String cardLastFour; // Last 4 digits of the card
    private String cardBrand; // e.g., "Visa", "Mastercard"
    private String cardExpirationMonth;
    private String cardExpirationYear;
    
    private BigDecimal amount;
    private String currency;
    private String status; // "succeeded", "pending", "failed"
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // Link to the user who made the payment
}