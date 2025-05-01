package com.example.e_commerce.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentRequest {
    private Long userId;
    private String stripeCustomerId;
    private String paymentMethodId;
    private BigDecimal amount;
    private String currency = "USD";
    private String description;
}