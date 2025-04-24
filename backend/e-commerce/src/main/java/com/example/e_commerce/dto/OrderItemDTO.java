package com.example.e_commerce.dto;


import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * Sipariş kalemi için DTO
 */
@Data
public class OrderItemDTO {
    private Integer id;

    @NotNull
    private Integer orderId;

    @NotNull
    private Integer productItemId;

    @NotNull
    @Min(1)
    private Integer quantity;
}
