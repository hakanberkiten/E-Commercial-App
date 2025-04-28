// CartService.java
package com.example.e_commerce.service;

import com.example.e_commerce.entity.Cart;
import com.example.e_commerce.entity.CartItem;

import java.util.List;

public interface CartService {
    Cart getOrCreateCart(Long userId);
    CartItem addItem(Long userId, Long productId, Integer quantity);
    List<CartItem> listItems(Long userId);
    void removeItem(Long userId, Long itemId);
    CartItem updateItem(Long userId, Long itemId, Integer quantity);
  }
