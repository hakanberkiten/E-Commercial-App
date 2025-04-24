package com.example.e_commerce.dto;


import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Ödeme kayıtları için DTO
 */
@Data
public class PaymentDTO {
    private Integer id;

    @NotNull
    private Integer orderId;

    @NotBlank
    @Size(max = 50)
    private String provider;

    @NotBlank
    @Size(max = 100)
    private String providerTransactionId;

    @NotNull
    private BigDecimal amount;

    @NotBlank
    @Size(max = 30)
    private String status;

    private LocalDateTime paidAt;
}
