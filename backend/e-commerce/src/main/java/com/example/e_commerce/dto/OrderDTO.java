package com.example.e_commerce.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Order için API katmanındaki veri modeli
 */
@Data
public class OrderDTO {
    private Integer id;

    @NotNull
    private Integer userId;

    private LocalDateTime orderDate;

    @NotNull
    private Integer addressId;

    @NotBlank
    private String paymentMethod;

    @NotBlank
    private String status;

    @NotNull
    private BigDecimal totalAmount;

    /** İç içe dönecekse, OrderItemDTO listesi */
    private List<OrderItemDTO> items;
}
