// src/main/java/com/example/e_commerce/dto/SignupRequest.java
package com.example.e_commerce.dto;

import lombok.*;
import jakarta.validation.constraints.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SignupRequest {
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    @Email @NotBlank private String email;
    @NotBlank @Size(min=6) private String password;
    @NotBlank private String mobileNumber;
}
