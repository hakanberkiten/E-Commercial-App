package com.example.e_commerce.dto;


import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class ProductItemDTO {
    private Integer id;

    @NotNull
    private Integer productId;

    private Integer colorId;

    private Integer sizeId;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal discountedPrice;

    @NotNull
    @Min(0)
    private Integer quantityInStock;
}
