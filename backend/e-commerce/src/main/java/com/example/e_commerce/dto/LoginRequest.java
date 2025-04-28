// src/main/java/com/example/e_commerce/dto/LoginRequest.java
package com.example.e_commerce.dto;

import lombok.*;
import jakarta.validation.constraints.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoginRequest {
    @Email @NotBlank private String email;
    @NotBlank private String password;
}
