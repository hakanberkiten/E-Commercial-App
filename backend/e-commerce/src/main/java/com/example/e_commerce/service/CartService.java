// CartService.java
package com.example.e_commerce.service;

import com.example.e_commerce.entity.Cart;

import java.util.List;

public interface CartService {
    Cart saveCart(Cart cart);
    List<Cart> getAllCarts();
    Cart getCartById(Long id);
    void deleteCart(Long id);
}
