// CartItemService.java
package com.example.e_commerce.service;

import com.example.e_commerce.entity.CartItem;

import java.util.List;

public interface CartItemService {
    CartItem saveCartItem(CartItem item);
    List<CartItem> getAllCartItems();
    CartItem getCartItemById(Long id);
    void deleteCartItem(Long id);
}
