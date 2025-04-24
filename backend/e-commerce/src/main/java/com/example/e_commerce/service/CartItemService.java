package com.example.e_commerce.service;

import com.example.e_commerce.dto.CartItemDTO;
import java.util.List;

public interface CartItemService {
    CartItemDTO create(CartItemDTO dto);
    CartItemDTO getById(Integer id);
    List<CartItemDTO> getAll();
    List<CartItemDTO> getByCartId(Integer cartId);
    CartItemDTO update(Integer id, CartItemDTO dto);
    void delete(Integer id);
}
