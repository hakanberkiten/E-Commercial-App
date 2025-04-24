package com.example.e_commerce.service;

import com.example.e_commerce.dto.ShoppingCartDTO;
import java.util.Optional;

public interface ShoppingCartService {
    ShoppingCartDTO create(ShoppingCartDTO dto);
    ShoppingCartDTO getById(Integer id);
    Optional<ShoppingCartDTO> getByUserId(Integer userId);
    void delete(Integer id);
}
