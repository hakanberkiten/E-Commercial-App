// OrderRequest.java
package com.example.e_commerce.dto;

import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderRequest {
    private Long userId;
    private Long paymentId;           // opsiyonel
    private String paymentMethodId;  // Stripe payment method ID
    private List<OrderItemRequest> items;
}
