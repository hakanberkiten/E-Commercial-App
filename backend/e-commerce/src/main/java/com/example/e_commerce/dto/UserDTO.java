// src/main/java/com/example/e_commerce/dto/UserDto.java
package com.example.e_commerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDTO {
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private Integer roleId;
}
