package com.example.e_commerce.dto;


import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * CartItem için API katmanındaki veri modeli
 */
@Data
public class CartItemDTO {
    private Integer id;

    @NotNull
    private Integer cartId;

    @NotNull
    private Integer productItemId;

    @Min(1)
    private Integer quantity;
}
