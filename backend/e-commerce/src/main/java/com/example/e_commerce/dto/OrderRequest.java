// OrderRequest.java
package com.example.e_commerce.dto;

import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderRequest {
    private Long userId;
    private Long paymentId;           // opsiyonel
    private List<OrderItemRequest> items;
}
