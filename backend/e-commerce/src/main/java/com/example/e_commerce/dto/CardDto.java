package com.example.e_commerce.dto;

import lombok.Data;

@Data
public class CardDto {
    private String cardNumber;
    private String expirationMonth;
    private String expirationYear;
    private String cvc;
}