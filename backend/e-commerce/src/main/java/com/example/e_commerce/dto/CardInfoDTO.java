package com.example.e_commerce.dto;


import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * Kart bilgileri için DTO
 */
@Data
public class CardInfoDTO {
    private Integer id;

    @NotNull
    private Integer userId;

    @NotBlank
    @Size(max = 50)
    private String cardHolderName;

    @NotBlank
    @Size(max = 50)
    private String cardType;

    @NotBlank
    @Size(min = 13, max = 19)
    private String cardNumber;

    @NotNull
    @Min(1) @Max(12)
    private Integer expirationMonth;

    @NotNull
    @Min(2000) @Max(2100)
    private Integer expirationYear;

    @NotNull
    @Min(100) @Max(9999)
    private Integer cvv;

    public void setExpirationYear(Short expirationYear2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setExpirationYear'");
    }

    public void setExpirationMonth(Byte expirationMonth2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setExpirationMonth'");
    }
}
