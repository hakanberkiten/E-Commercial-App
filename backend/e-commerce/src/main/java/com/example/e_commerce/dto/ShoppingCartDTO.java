package com.example.e_commerce.dto;


import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class ShoppingCartDTO {
    private Integer id;

    @NotNull
    private Integer userId;
}
