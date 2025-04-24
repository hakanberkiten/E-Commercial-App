package com.example.e_commerce.dto;


import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class CategoryDTO {
    private Integer id;

    @NotBlank
    @Size(max = 100)
    private String name;
}

