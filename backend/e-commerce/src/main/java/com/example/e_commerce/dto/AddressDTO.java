package com.example.e_commerce.dto;


import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * Address için API katmanındaki veri modeli
 */
@Data
public class AddressDTO {
    private Integer id;

    @NotNull
    private Integer userId;

    @NotBlank
    private String addressLine;

    @NotBlank
    private String city;

    @NotBlank
    private String state;

    @NotBlank
    private String postalCode;

    @NotBlank
    private String country;

    @NotBlank
    private String addressType;
}
