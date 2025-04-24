package com.example.e_commerce.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Product için API katmanındaki veri modeli
 */
@Data
public class ProductDTO {
    private Integer id;

    @NotBlank
    @Size(max = 50)
    private String name;

    @Size(max = 1000)
    private String description;

    @NotNull
    private BigDecimal price;

    @NotNull
    private Integer categoryId;

    private Byte gender;

    private BigDecimal discount;

    private String productImage;
}
