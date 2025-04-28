// OrderItemRequest.java
package com.example.e_commerce.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItemRequest {
    private Long productId;
    private Integer quantity;
}
